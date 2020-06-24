/*
 * MIT License
 *
 * Copyright (c) 2020 Alen Turkovic
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
 *
 */

package com.github.alturkovic.asn.encoder;

import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnEncodeException;
import com.github.alturkovic.asn.field.CollectionTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.accessor.DirectFieldAccessor;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.util.BerUtils;
import com.github.alturkovic.asn.util.HexUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BerEncoder implements AsnEncoder<byte[]> {
  private final FieldAccessor fieldAccessor;
  private final Map<Class<?>, AsnClassDescription> classDescriptionCache;
  private final Map<Class<? extends AsnConverter<byte[], Object>>, AsnConverter<byte[], Object>> converterCache;

  public BerEncoder() {
    this(new DirectFieldAccessor());
  }

  public BerEncoder(final FieldAccessor fieldAccessor) {
    this(fieldAccessor, new HashMap<>(), new HashMap<>());
  }

  @Override
  public byte[] encode(final Object object) {
    final var clazzDeclaredAnnotation = object.getClass().getDeclaredAnnotation(AsnStructure.class);

    if (clazzDeclaredAnnotation == null) {
      throw new AsnEncodeException("Missing class AsnStructure annotation");
    }

    final var tag = clazzDeclaredAnnotation.value();
    final var fieldStructureTag = new Tag(tag.value(), tag.type(), true);
    return encodeStructure(object, fieldStructureTag);
  }

  private byte[] encodeStructure(final Object object, final Tag structureTag) {
    final BerStructureBuilder berStructureBuilder;
    try {
      final var clazz = object.getClass();
      final var asnClassDescription = classDescriptionCache.computeIfAbsent(clazz, AsnClassDescription::new);

      berStructureBuilder = new BerStructureBuilder(structureTag);

      for (final var taggedField : asnClassDescription.getClassDeclaredOrderedTaggedFields()) {
        final byte[] encoded;
        if (taggedField.isPrimitive()) {
          encoded = encodePrimitive(object, (PrimitiveTaggedField) taggedField);

          if (encoded == null) {
            continue;
          }
        } else if (taggedField.isStructure()) {
          final var fieldValue = fieldAccessor.getFieldValue(object, taggedField.getField());

          if (fieldValue == null) {
            continue;
          }

          encoded = encodeStructure(fieldValue, taggedField.getTag());
        } else if (taggedField.isCollection()) {
          final var collectionTaggedField = (CollectionTaggedField) taggedField;

          final Collection<Object> collection = fieldAccessor.getFieldValue(object, taggedField.getField());

          if (collection == null) {
            continue;
          }

          final var collectionBuilder = new BerStructureBuilder(collectionTaggedField.getTag());

          if (collectionTaggedField.isStructured()) {
            collection.forEach(e -> collectionBuilder.addValue(encodeStructure(e, collectionTaggedField.getElementTag())));
          } else {
            collection.forEach(e -> {
              //noinspection unchecked
              final var asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) collectionTaggedField.getConverter());
              collectionBuilder.addValue(encodePrimitive(collectionTaggedField.getElementTag(), asnConverter.encode(e)));
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
      final var asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) taggedField.getConverter());
      final var encodedFieldValue = asnConverter.encode(fieldAccessor.getFieldValue(object, taggedField.getField()));

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
      final var result = new ByteArrayOutputStream();

      result.write(BerUtils.convert(tag));
      result.write(BerUtils.encodeLength(value.length));
      result.write(value);

      return result.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(String.format("Cannot encode tag %s and value '%s' to a primitive", tag, HexUtils.encode(value)), e);
    }
  }

  private AsnConverter<byte[], Object> loadAsnConverterFromCache(final Class<? extends AsnConverter<byte[], Object>> asnConverterClass) {
    return converterCache.computeIfAbsent(asnConverterClass, c -> {
      try {
        return c.getDeclaredConstructor().newInstance();
      } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        throw new AsnConfigurationException(String.format("Cannot create a new instance of converter %s", c), e);
      }
    });
  }
}
