/*
 * Copyright (c)  2017 Alen Turković <alturkovic@gmail.com>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.github.alturkovic.asn.ber.decoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnPostProcessMethod;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.tlv.TlvDataReader;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.decoder.AsnDecoder;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnDecodeException;
import com.github.alturkovic.asn.exception.AsnException;
import com.github.alturkovic.asn.field.ListTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.google.common.cache.Cache;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Data
public class BerDecoder implements AsnDecoder<byte[]>{
    private final TagFactory tagFactory;
    private final AsnAutoResolver autoResolver;
    private final FieldAccessor fieldAccessor;
    private final TlvDataReader tlvDataReader;
    private final Cache<Class<?>, AsnClassDescription> classDescriptionCache;
    private final Cache<Class<? extends AsnConverter<byte[], ?>>, AsnConverter<byte[], ?>> converterCache;

    @Override
    public <X> X decode(final Class<X> clazz, final byte[] data) {
        if (data == null) {
            throw new AsnDecodeException("Cannot decode null data into: " + clazz.getSimpleName());
        }

        final AsnStructure clazzDeclaredAnnotation = clazz.getDeclaredAnnotation(AsnStructure.class);
        if (clazzDeclaredAnnotation == null) {
            throw new AsnDecodeException("Missing class AsnStructure annotation");
        }

        final AsnTag tag = clazzDeclaredAnnotation.value();
        final Tag fieldStructureTag = tagFactory.get(tag, true);
        return decodeStructure(clazz, fieldStructureTag, data);
    }

    private <X> X decodeStructure(final Class<X> clazz, final Tag structureTag, final byte[] data) {
        try {
            final AsnClassDescription asnClassDescription = classDescriptionCache.get(clazz, () -> new AsnClassDescription(tagFactory, autoResolver, clazz));

            final BerData tlvData = tlvDataReader.readNext(new ByteArrayInputStream(data));
            final Tag parsedMainTag = BerUtils.parseTag(tlvData.getTag());

            if (!structureTag.equals(parsedMainTag)) {
                throw new AsnDecodeException(String.format("Defined structure tag %s on %s does not match parser tag %s", structureTag, clazz, parsedMainTag));
            }

            final X instance = clazz.newInstance();
            final InputStream valueStream = new ByteArrayInputStream(tlvData.getValue());

            final Multiset<Tag> tagCounter = HashMultiset.create();
            while (valueStream.available() > 0) {
                //Read element by element
                final BerData fieldTlvData = tlvDataReader.readNext(valueStream);
                if (!fieldTlvData.isValuePresent()) {
                    continue;
                }

                final Tag parsedFieldTag = BerUtils.parseTag(fieldTlvData.getTag());
                final int index = tagCounter.count(parsedFieldTag);
                tagCounter.add(parsedFieldTag);

                final TaggedField taggedField = asnClassDescription.findByTag(parsedFieldTag, index);
                if (taggedField == null) {
                    continue;
                }

                if (taggedField.isPrimitive()) {
                    //noinspection unchecked
                    final AsnConverter<byte[], ?> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], ?>>) ((PrimitiveTaggedField) taggedField).getConverter());

                    try {
                        fieldAccessor.setFieldValue(instance, taggedField.getField(), asnConverter.decode(fieldTlvData.getValue()));
                    } catch (final AsnException e) {
                        throw new AsnDecodeException(String.format("Cannot set value '%s' into field '%s'", HexUtils.encode(fieldTlvData.getValue()), taggedField.getField().getName()), e);
                    }
                } else if (taggedField.isStructure()) {
                    fieldAccessor.setFieldValue(instance, taggedField.getField(), decodeStructure(taggedField.getField().getType(), taggedField.getTag(), fieldTlvData.toTlv()));
                } else if (taggedField.isList()) {
                    final List<Object> list = new ArrayList<>();
                    fieldAccessor.setFieldValue(instance, taggedField.getField(), list);

                    decodeList(list, fieldTlvData.getValue(), (ListTaggedField) taggedField);
                } else {
                    throw new AsnDecodeException("Unknown TaggedField type: " + taggedField);
                }
            }

            final AsnPostProcessMethod asnPostProcessMethod = clazz.getDeclaredAnnotation(AsnPostProcessMethod.class);

            if (asnPostProcessMethod != null) {
                final Method declaredMethod = clazz.getDeclaredMethod(asnPostProcessMethod.value());
                if (!declaredMethod.isAccessible()) {
                    declaredMethod.setAccessible(true);
                }
                declaredMethod.invoke(instance);
            }

            return instance;
        } catch (final Exception e) {
            throw new AsnDecodeException(String.format("Cannot decode '%s' into '%s' class for tag %s", HexUtils.encode(data), clazz.getName(), structureTag), e);
        }
    }

    private void decodeList(final List<Object> list, final byte[] listValueData, final ListTaggedField taggedField) {
        try {
            final InputStream listStream = new ByteArrayInputStream(listValueData);
            while (listStream.available() > 0) {
                final BerData listElementTlv = tlvDataReader.readNext(listStream);
                final Tag parsedListElementTag = BerUtils.parseTag(listElementTlv.getTag());
                if (BerUtils.UNIVERSAL_TAG.equals(parsedListElementTag)) {
                    if (taggedField.isStructured()) {
                        final Object listObject = decodeStructure(taggedField.getType(), BerUtils.UNIVERSAL_TAG, listElementTlv.toTlv());
                        list.add(listObject);
                    } else {
                        //noinspection unchecked
                        final AsnConverter<byte[], ?> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], ?>>) taggedField.getConverter());
                        try {
                            list.add(asnConverter.decode(listElementTlv.toTlv()));
                        } catch (final AsnException e) {
                            throw new AsnDecodeException(String.format("Cannot add primitive value '%s' to list '%s'", HexUtils.encode(listElementTlv.toTlv()), taggedField.getField().getName()), e);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new AsnDecodeException(String.format("Cannot decode list data '%s' into '%s' class", HexUtils.encode(listValueData), taggedField.getType().getName()), e);
        }
    }

    // TODO extract into loader or something and reuse in encoder (include in builders)
    private AsnConverter<byte[], ?> loadAsnConverterFromCache(final Class<? extends AsnConverter<byte[], ?>> asnConverterClass) {
        try {
            return converterCache.get(asnConverterClass, asnConverterClass::newInstance);
        } catch (final ExecutionException e) {
            throw new AsnConfigurationException(String.format("Cannot create a new instance of converter %s", asnConverterClass), e);
        }
    }
}
