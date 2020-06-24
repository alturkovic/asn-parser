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

package com.github.alturkovic.asn.decoder;

import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.annotation.AsnPostProcessMethod;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.exception.AsnDecodeException;
import com.github.alturkovic.asn.exception.AsnException;
import com.github.alturkovic.asn.field.CollectionTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.field.accessor.DirectFieldAccessor;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.support.Counter;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tlv.BerData;
import com.github.alturkovic.asn.tlv.BerDataReader;
import com.github.alturkovic.asn.tlv.TlvDataReader;
import com.github.alturkovic.asn.util.BerUtils;
import com.github.alturkovic.asn.util.ClassUtils;
import com.github.alturkovic.asn.util.HexUtils;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BerDecoder implements AsnDecoder<byte[]> {
  private final FieldAccessor fieldAccessor;
  private final TlvDataReader tlvDataReader;
  private final Map<Class<?>, AsnClassDescription> classDescriptionCache;
  private final Map<Class<? extends AsnConverter<byte[], Object>>, AsnConverter<byte[], Object>> converterCache;

  public BerDecoder() {
    this(new BerDataReader());
  }

  public BerDecoder(final TlvDataReader tlvDataReader) {
    this(tlvDataReader, new DirectFieldAccessor());
  }

  public BerDecoder(final TlvDataReader tlvDataReader, final FieldAccessor fieldAccessor) {
    this(fieldAccessor, tlvDataReader, new HashMap<>(), new HashMap<>());
  }

  @Override
  public <X> X decode(final Class<X> clazz, final byte[] data) {
    if (data == null) {
      throw new AsnDecodeException("Cannot decode null data into: " + clazz.getSimpleName());
    }

    return decodeStructure(clazz, data);
  }

  private <X> X decodeStructure(final Class<X> clazz, final byte[] data) {
    try {
      final var asnClassDescription = loadAsnClassDescription(clazz);
      final var tlvData = tlvDataReader.readNext(new ByteArrayInputStream(data));

      if (clazz.isInterface()) {
        return decodePolymorphic(asnClassDescription, tlvData.getValue());
      }

      final var valueStream = new ByteArrayInputStream(tlvData.getValue());
      final var instance = clazz.getDeclaredConstructor().newInstance();

      final var tagCounter = new Counter<Tag>();
      while (valueStream.available() > 0) {
        //Read element by element
        final var fieldTlvData = tlvDataReader.readNext(valueStream);

        final var taggedField = parseTaggedField(asnClassDescription, tagCounter, fieldTlvData);
        if (taggedField == null) {
          continue;
        }

        if (taggedField.isPrimitive()) {
          decodePrimitiveField(instance, fieldTlvData, taggedField);
        } else if (taggedField.isStructure()) {
          decodeStructureField(instance, fieldTlvData, taggedField);
        } else if (taggedField.isCollection()) {
          decodeCollectionField(instance, fieldTlvData, taggedField);
        } else {
          throw new AsnDecodeException("Unknown TaggedField type: " + taggedField);
        }
      }

      invokePostProcessMethod(clazz, instance);

      return instance;
    } catch (final Exception e) {
      throw new AsnDecodeException(String.format("Cannot decode '%s' into '%s' class", HexUtils.encode(data), clazz.getName()), e);
    }
  }

  @SuppressWarnings("unchecked")
  private <X> X decodePolymorphic(final AsnClassDescription asnClassDescription, final byte[] data) {
    // read the next tlv which actually represents the nested choice implementation data
    final var implementationData = tlvDataReader.readNext(new ByteArrayInputStream(data));
    final var tag = BerUtils.parseTag(implementationData.getTag());
    final var implementation = asnClassDescription.findImplementationByTag(tag);

    if (implementation == null) {
      return null;
    }

    if (ClassUtils.isPrimitiveOrWrapper(implementation)) {
      final var converter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) implementation);
      return (X) converter.decode(implementationData.getValue());
    }

    return (X) decodeStructure(implementation, implementationData.toTlv());
  }

  private void decodeCollection(final Collection<Object> collection, final byte[] elementData, final CollectionTaggedField taggedField) {
    try {
      final var stream = new ByteArrayInputStream(elementData);
      while (stream.available() > 0) {
        final var elementBerData = tlvDataReader.readNext(stream);
        final var parsedElementTag = BerUtils.parseTag(elementBerData.getTag());

        if (taggedField.getType().isInterface()) {
          final var asnClassDescription = loadAsnClassDescription(taggedField.getType());
          collection.add(decodePolymorphic(asnClassDescription, elementBerData.toTlv()));
        } else if (taggedField.getElementTag().equals(parsedElementTag)) {
          if (taggedField.isStructured()) {
            collection.add(decodeStructure(taggedField.getType(), elementBerData.toTlv()));
          } else {
            //noinspection unchecked
            final var asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) taggedField.getConverter());
            collection.add(asnConverter.decode(elementBerData.getValue()));
          }
        }
      }
    } catch (final Exception e) {
      throw new AsnDecodeException(String.format("Cannot decode collection data '%s' into '%s' class", HexUtils.encode(elementData), taggedField.getType().getName()), e);
    }
  }

  private <X> void decodePrimitiveField(final X instance, final BerData fieldTlvData, final TaggedField taggedField) {
    //noinspection unchecked
    final var asnConverter = loadAsnConverterFromCache((Class<? extends AsnConverter<byte[], Object>>) ((PrimitiveTaggedField) taggedField).getConverter());

    try {
      fieldAccessor.setFieldValue(instance, taggedField.getField(), asnConverter.decode(fieldTlvData.getValue()));
    } catch (final AsnException e) {
      throw new AsnDecodeException(String.format("Cannot set value '%s' into field '%s'", HexUtils.encode(fieldTlvData.getValue()), taggedField.getField().getName()), e);
    }
  }

  private <X> void decodeStructureField(final X instance, final BerData fieldTlvData, final TaggedField taggedField) {
    fieldAccessor.setFieldValue(instance, taggedField.getField(), decodeStructure(taggedField.getField().getType(), fieldTlvData.toTlv()));
  }

  private <X> void decodeCollectionField(final X instance, final BerData fieldTlvData, final TaggedField taggedField) {
    final var fieldClass = taggedField.getField().getType();

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
  }

  private TaggedField parseTaggedField(final AsnClassDescription asnClassDescription, final Counter<Tag> tagCounter, final BerData fieldTlvData) {
    final var parsedFieldTag = BerUtils.parseTag(fieldTlvData.getTag());
    final var index = tagCounter.count(parsedFieldTag);
    return asnClassDescription.findByTag(parsedFieldTag, index);
  }

  private AsnClassDescription loadAsnClassDescription(final Class<?> clazz) {
    return classDescriptionCache.computeIfAbsent(clazz, AsnClassDescription::new);
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

  private <X> void invokePostProcessMethod(final Class<X> clazz, final X instance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final var asnPostProcessMethod = clazz.getDeclaredAnnotation(AsnPostProcessMethod.class);

    if (asnPostProcessMethod != null) {
      final var declaredMethod = clazz.getDeclaredMethod(asnPostProcessMethod.value());
      if (!declaredMethod.canAccess(instance)) {
        declaredMethod.setAccessible(true);
      }
      declaredMethod.invoke(instance);
    }
  }
}
