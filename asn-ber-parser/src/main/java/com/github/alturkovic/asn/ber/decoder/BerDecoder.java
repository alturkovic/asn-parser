/*
 * MIT License
 *
 * Copyright (c) 2018 Alen Turkovic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.alturkovic.asn.ber.decoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnPostProcessMethod;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.ber.collection.MultiSet;
import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.tlv.TlvDataReader;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.decoder.AsnDecoder;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnDecodeException;
import com.github.alturkovic.asn.exception.AsnException;
import com.github.alturkovic.asn.field.CollectionTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class BerDecoder implements AsnDecoder<byte[]> {
    private final TagFactory tagFactory;
    private final AsnAutoResolver autoResolver;
    private final FieldAccessor fieldAccessor;
    private final TlvDataReader tlvDataReader;
    private final Map<Class<?>, AsnClassDescription> classDescriptionCache;
    private final Map<Class<? extends AsnConverter<byte[], Object>>, AsnConverter<byte[], Object>> converterCache;

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
            final AsnClassDescription asnClassDescription = classDescriptionCache.computeIfAbsent(clazz, (aClass) -> new AsnClassDescription(tagFactory, autoResolver, aClass));

            final BerData tlvData = tlvDataReader.readNext(new ByteArrayInputStream(data));
            final Tag parsedMainTag = BerUtils.parseTag(tlvData.getTag());

            if (!structureTag.equals(parsedMainTag)) {
                throw new AsnDecodeException(String.format("Defined structure tag %s on %s does not match parser tag %s", structureTag, clazz, parsedMainTag));
            }

            final X instance = clazz.newInstance();
            final InputStream valueStream = new ByteArrayInputStream(tlvData.getValue());

            final MultiSet<Tag> tagCounter = new MultiSet<>();
            while (valueStream.available() > 0) {
                //Read element by element
                final BerData fieldTlvData = tlvDataReader.readNext(valueStream);

                final Tag parsedFieldTag = BerUtils.parseTag(fieldTlvData.getTag());
                final int index = tagCounter.count(parsedFieldTag);
                tagCounter.add(parsedFieldTag);

                final TaggedField taggedField = asnClassDescription.findByTag(parsedFieldTag, index);
                if (taggedField == null) {
                    continue;
                }

                if (taggedField.isPrimitive()) {
                    //noinspection unchecked
                    final AsnConverter<byte[], Object> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) ((PrimitiveTaggedField) taggedField).getConverter());

                    try {
                        fieldAccessor.setFieldValue(instance, taggedField.getField(), asnConverter.decode(fieldTlvData.getValue()));
                    } catch (final AsnException e) {
                        throw new AsnDecodeException(String.format("Cannot set value '%s' into field '%s'", HexUtils.encode(fieldTlvData.getValue()), taggedField.getField().getName()), e);
                    }
                } else if (taggedField.isStructure()) {
                    fieldAccessor.setFieldValue(instance, taggedField.getField(), decodeStructure(taggedField.getField().getType(), taggedField.getTag(), fieldTlvData.toTlv()));
                } else if (taggedField.isCollection()) {
                    final Class<?> fieldClass = taggedField.getField().getType();

                    final Collection<Object> collection;

                    if (fieldClass.isAssignableFrom(List.class)) {
                        collection = new ArrayList<>();
                    } else if (fieldClass.isAssignableFrom(Set.class)) {
                        collection = new HashSet<>();
                    } else {
                        throw new AsnDecodeException(String.format("Unsupported collection type: '%s'. Only List and Set supported!", fieldClass));
                    }

                    fieldAccessor.setFieldValue(instance, taggedField.getField(), collection);

                    decodeCollection(collection, fieldTlvData.getValue(), (CollectionTaggedField) taggedField);
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

    private void decodeCollection(final Collection<Object> collection, final byte[] elementData, final CollectionTaggedField taggedField) {
        try {
            final InputStream stream = new ByteArrayInputStream(elementData);
            while (stream.available() > 0) {
                final BerData elementBerData = tlvDataReader.readNext(stream);
                final Tag parsedElementTag = BerUtils.parseTag(elementBerData.getTag());
                if (taggedField.getElementTag().equals(parsedElementTag)) {
                    if (taggedField.isStructured()) {
                        final Object element = decodeStructure(taggedField.getType(), BerUtils.UNIVERSAL_TAG, elementBerData.toTlv());
                        collection.add(element);
                    } else {
                        //noinspection unchecked
                        final AsnConverter<byte[], Object> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) taggedField.getConverter());
                        try {
                            collection.add(asnConverter.decode(elementBerData.getValue()));
                        } catch (final AsnException e) {
                            throw new AsnDecodeException(String.format("Cannot add primitive value '%s' to collection '%s'", HexUtils.encode(elementBerData.getValue()), taggedField.getField().getName()), e);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new AsnDecodeException(String.format("Cannot decode collection data '%s' into '%s' class", HexUtils.encode(elementData), taggedField.getType().getName()), e);
        }
    }

    private AsnConverter<byte[], Object> loadAsnConverterFromCache(final Class<? extends AsnConverter<byte[], Object>> asnConverterClass) {
        return converterCache.computeIfAbsent(asnConverterClass, (aClass) -> {
            try {
                return aClass.newInstance();
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new AsnConfigurationException(String.format("Cannot create a new instance of converter %s", aClass), e);
            }
        });
    }
}
