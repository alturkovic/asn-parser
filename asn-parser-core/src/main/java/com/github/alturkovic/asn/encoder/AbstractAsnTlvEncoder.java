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

package com.github.alturkovic.asn.encoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnEncodeException;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutionException;

@AllArgsConstructor
public abstract class AbstractAsnTlvEncoder<V> implements AsnEncoder<V> {

    private final TagFactory tagFactory;
    private final AsnAutoResolver autoResolver;
    private final FieldAccessor fieldAccessor;
    private final Cache<Class<?>, AsnClassDescription> classDescriptionCache;
    private final Cache<Class<? extends AsnConverter<V, ?>>, AsnConverter<V, ?>> converterCache;

    protected abstract V encodePrimitive(Tag tag, V value);
    protected abstract AsnTlvStructureBuilder<V> createNewBuilder(Tag tag);

    @Override
    public V encode(final Object object) {
        final AsnStructure clazzDeclaredAnnotation = object.getClass().getDeclaredAnnotation(AsnStructure.class);

        if (clazzDeclaredAnnotation == null) {
            throw new AsnEncodeException("Missing class AsnStructure annotation");
        }

        final AsnTag tag = clazzDeclaredAnnotation.value();
        final Tag fieldStructureTag = tagFactory.get(tag, true);
        return encodeStructure(object, fieldStructureTag);
    }

    protected V encodeStructure(final Object object, final Tag structureTag) {
        final AsnTlvStructureBuilder<V> asnTlvStructureBuilder;
        try {
            final Class<?> clazz = object.getClass();
            final AsnClassDescription asnClassDescription = classDescriptionCache.get(clazz, () -> new AsnClassDescription(tagFactory, autoResolver, clazz));

            asnTlvStructureBuilder = createNewBuilder(structureTag);

            for (final TaggedField taggedField : asnClassDescription.getSortedFields()) {
                final V encoded;
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

                asnTlvStructureBuilder.addValue(encoded);
            }
        } catch (final Exception e) {
            throw new AsnEncodeException(String.format("Cannot encode '%s'", object), e);
        }

        return asnTlvStructureBuilder.build();
    }

    private V encodePrimitive(final Object object, final PrimitiveTaggedField taggedField) throws Exception {
        try {
            //noinspection unchecked
            final AsnConverter<V, ?> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<V, ?>>) taggedField.getConverter());
            final V encodedFieldValue = asnConverter.encode(fieldAccessor.getFieldValue(object, taggedField.getField()));

            if (encodedFieldValue == null) {
                return null;
            }

            return encodePrimitive(taggedField.getTag(), encodedFieldValue);
        } catch (final Exception e) {
            throw new AsnEncodeException(String.format("Cannot encode '%s' from '%s'", taggedField, object), e);
        }
    }

    private AsnConverter<V, ?> loadAsnConverterFromCache(final Class<? extends AsnConverter<V, ?>> asnConverterClass) {
        final AsnConverter<V, ?> asnConverter;
        try {
            asnConverter = converterCache.get(asnConverterClass, asnConverterClass::newInstance);
        } catch (final ExecutionException e) {
            throw new AsnConfigurationException(String.format("Cannot create a new instance of converter %s", asnConverterClass), e);
        }

        return asnConverter;
    }
}
