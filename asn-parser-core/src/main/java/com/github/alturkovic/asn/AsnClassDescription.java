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

package com.github.alturkovic.asn;

import com.github.alturkovic.asn.annotation.AsnCollection;
import com.github.alturkovic.asn.annotation.AsnPrimitive;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.converter.AutoConverter;
import com.github.alturkovic.asn.field.CollectionTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.StructureTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The cached representation of a class. All annotated fields of a class will be analyzed and stored for further processing.
 */
public class AsnClassDescription {
  private Map<Tag, List<TaggedField>> multimap;
  private List<TaggedField> classOrderedTaggedFields;

  public AsnClassDescription(final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver, final Class<?> clazz) {
    init(clazz, tagFactory, asnAutoResolver);
  }

  // ensures that the order of class defined fields will be kept when encoding
  public List<TaggedField> getClassDeclaredOrderedTaggedFields() {
    if (classOrderedTaggedFields == null) {
      classOrderedTaggedFields = multimap.values()
          .stream()
          .flatMap(Collection::stream)
          .sorted()
          .collect(Collectors.toList());
    }

    return classOrderedTaggedFields;
  }

  public TaggedField findByTag(final Tag tag, final int index) {
    final List<TaggedField> taggedFields = multimap.get(tag);
    if (taggedFields == null || taggedFields.size() == 0) {
      return null;
    }

    if (index >= taggedFields.size()) {
      // this means that we need less data with this tag than there is available
      // if we need to get the second data, we also need to define the first with proper ordering
      // if we wanted to get the 1st and 3rd, we would also need to define the 2nd to order them correctly

      // example is available in asn-ber-parser BerDecoderTest where we define MultipleAddressStringWrapper with 2 fields with the same tag
      // but 3 are available and the 3rd is discarded
      return null;
    }

    return taggedFields.get(index);
  }

  private void init(final Class<?> clazz, final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver) {
    multimap = new HashMap<>();

    int fieldPosition = 0;
    for (final Field field : clazz.getDeclaredFields()) {
      Tag tag = null;
      TaggedField taggedField = null;

      if (field.isAnnotationPresent(AsnPrimitive.class)) {
        final AsnPrimitive primitiveTag = field.getAnnotation(AsnPrimitive.class);
        final AsnTag asnTag = primitiveTag.value();

        final Class<? extends AsnConverter<?, ?>> converter = getConverter(asnAutoResolver, primitiveTag.asnConverter(), field.getType());

        tag = getTag(tagFactory, asnAutoResolver, asnTag, field.getType(), false);
        taggedField = new PrimitiveTaggedField(fieldPosition, tag, field, converter);
      } else if (field.isAnnotationPresent(AsnStructure.class)) {
        final AsnStructure structureTag = field.getAnnotation(AsnStructure.class);
        final AsnTag asnTag = structureTag.value();

        tag = getTag(tagFactory, asnAutoResolver, asnTag, field.getType(), true);
        taggedField = new StructureTaggedField(fieldPosition, tag, field);
      } else if (field.isAnnotationPresent(AsnCollection.class)) {
        final AsnCollection collectionTag = field.getAnnotation(AsnCollection.class);
        final AsnTag asnTag = collectionTag.value();

        final Class<? extends AsnConverter<?, ?>> converter = collectionTag.structured() ? null : getConverter(asnAutoResolver, collectionTag.asnConverter(), collectionTag.type());

        tag = getTag(tagFactory, asnAutoResolver, asnTag, field.getType(), true);
        final Tag elementTag = getTag(tagFactory, asnAutoResolver, collectionTag.elementTag(), collectionTag.type(), collectionTag.structured());

        taggedField = new CollectionTaggedField(fieldPosition, tag, field, collectionTag.structured(), collectionTag.type(), elementTag, converter);
      }

      if (tag != null && taggedField != null) {
        final List<TaggedField> listForTag = multimap.computeIfAbsent(tag, k -> new ArrayList<>());
        listForTag.add(taggedField);
      }

      fieldPosition++;
    }
  }

  private Tag getTag(final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver, final AsnTag asnTag, final Class<?> clazz, final boolean structured) {
    if (asnTag.value() == -1) {
      return asnAutoResolver.getUniversalTag(clazz, structured);
    }

    return tagFactory.get(asnTag, structured);
  }

  private Class<? extends AsnConverter<?, ?>> getConverter(final AsnAutoResolver asnAutoResolver, final Class<? extends AsnConverter<?, ?>> converter, final Class<?> clazz) {
    if (converter.equals(AutoConverter.class)) {
      return asnAutoResolver.getUniversalConverterClass(clazz);
    }

    return converter;
  }
}