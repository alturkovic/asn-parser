/*
 * MIT License
 *
 * Copyright (c) 2019 Alen Turkovic
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
import com.github.alturkovic.asn.field.CollectionTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import lombok.Data;

@Data
public class BerEncoder implements AsnEncoder<byte[]> {
  private final TagFactory tagFactory;
  private final AsnAutoResolver autoResolver;
  private final FieldAccessor fieldAccessor;
  private final Map<Class<?>, AsnClassDescription> classDescriptionCache;
  private final Map<Class<? extends AsnConverter<byte[], Object>>, AsnConverter<byte[], Object>> converterCache;

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
      final AsnClassDescription asnClassDescription = classDescriptionCache.computeIfAbsent(clazz, (aClass) -> new AsnClassDescription(tagFactory, autoResolver, aClass));

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
        } else if (taggedField.isCollection()) {
          final CollectionTaggedField collectionTaggedField = (CollectionTaggedField) taggedField;

          final Collection<Object> collection = fieldAccessor.getFieldValue(object, taggedField.getField());

          if (collection == null) {
            continue;
          }

          final BerStructureBuilder collectionBuilder = new BerStructureBuilder((BerTag) collectionTaggedField.getTag());

          if (collectionTaggedField.isStructured()) {
            collection.forEach(e -> collectionBuilder.addValue(encodeStructure(e, collectionTaggedField.getElementTag())));
          } else {
            collection.forEach(e -> {
              //noinspection unchecked
              final AsnConverter<byte[], Object> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) collectionTaggedField.getConverter());
              final byte[] encodedFieldValue = asnConverter.encode(e);
              collectionBuilder.addValue(encodePrimitive(collectionTaggedField.getElementTag(), encodedFieldValue));
            });
          }

          encoded = collectionBuilder.build();
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

  private byte[] encodePrimitive(final Object object, final PrimitiveTaggedField taggedField) {
    try {
      //noinspection unchecked
      final AsnConverter<byte[], Object> asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) taggedField.getConverter());
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
