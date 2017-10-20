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

package com.github.alturkovic.asn.ber.encoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.encoder.AsnEncoder;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnEncodeException;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.google.common.cache.Cache;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

@Data
public class BerEncoder implements AsnEncoder<byte[]> {
    private final TagFactory tagFactory;
    private final AsnAutoResolver autoResolver;
    private final FieldAccessor fieldAccessor;
    private final Cache<Class<?>, AsnClassDescription> classDescriptionCache;
    private final Cache<Class<? extends AsnConverter<byte[], ?>>, AsnConverter<byte[], ?>> converterCache;

    @Override
    public byte[] encode(final Object object) {
        final AsnStructure clazzDeclaredAnnotation = object.getClass().getDeclaredAnnotation(AsnStructure.class);

        if (clazzDeclaredAnnotation == null) {
            throw new AsnEncodeException("Missing class AsnStructure annotation");
        }

        final AsnTag tag = clazzDeclaredAnnotation.value();
        final Tag fieldStructureTag = tagFactory.get(tag, true);
        return encodeStructure(object, fieldStructureTag);
    }

    private byte[] encodeStructure(final Object object, final Tag structureTag) {
        final BerStructureBuilder berStructureBuilder;
        try {
            final Class<?> clazz = object.getClass();
            final AsnClassDescription asnClassDescription = classDescriptionCache.get(clazz, () -> new AsnClassDescription(tagFactory, autoResolver, clazz));

            berStructureBuilder = new BerStructureBuilder((BerTag) structureTag);

            for (final TaggedField taggedField : asnClassDescription.getClassDeclaredOrderedTaggedFields()) {
                final byte[] encoded;
                if (taggedField.isPrimitive()) {
                    encoded = encodePrimitive(object, (PrimitiveTaggedField) taggedField);

                    if (encoded == null) {
                        continue;
                    }
                } else if (taggedField.isStructure()) {
                    final Object fieldValue = fieldAccessor.getFieldValue(object, taggedField.getField());

                    if (fieldValue == null) {
                        continue;
                    }

                    encoded = encodeStructure(fieldValue, taggedField.getTag());
                } else if (taggedField.isList()) {
                    //TODO
                    encoded = null;
                } else {
                    continue;
                }

                berStructureBuilder.addValue(encoded);
            }
        } catch (final Exception e) {
            throw new AsnEncodeException(String.format("Cannot encode '%s'", object), e);
        }

        return berStructureBuilder.build();
    }

    private byte[] encodePrimitive(final Object object, final PrimitiveTaggedField taggedField) throws Exception {
        try {
            //noinspection unchecked
            final AsnConverter<byte[], ?> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], ?>>) taggedField.getConverter());
            final byte[] encodedFieldValue = asnConverter.encode(fieldAccessor.getFieldValue(object, taggedField.getField()));

            if (encodedFieldValue == null) {
                return null;
            }

            return encodePrimitive(taggedField.getTag(), encodedFieldValue);
        } catch (final Exception e) {
            throw new AsnEncodeException(String.format("Cannot encode '%s' from '%s'", taggedField, object), e);
        }
    }

    private byte[] encodePrimitive(final Tag tag, final byte[] value) {
        try {
            final ByteArrayOutputStream result = new ByteArrayOutputStream();

            result.write(BerUtils.convert((BerTag) tag));
            result.write(BerUtils.encodeLength(value.length));
            result.write(value);

            return result.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(String.format("Cannot encode tag %s and value '%s' to a primitive", tag, HexUtils.encode(value)), e);
        }
    }

    private AsnConverter<byte[], ?> loadAsnConverterFromCache(final Class<? extends AsnConverter<byte[], ?>> asnConverterClass) {
        try {
            return converterCache.get(asnConverterClass, asnConverterClass::newInstance);
        } catch (final ExecutionException e) {
            throw new AsnConfigurationException(String.format("Cannot create a new instance of converter %s", asnConverterClass), e);
        }
    }
}
