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

package com.github.alturkovic.asn;

import com.github.alturkovic.asn.annotation.AsnList;
import com.github.alturkovic.asn.annotation.AsnPrimitive;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.converter.AutoConverter;
import com.github.alturkovic.asn.field.ListTaggedField;
import com.github.alturkovic.asn.field.PrimitiveTaggedField;
import com.github.alturkovic.asn.field.StructureTaggedField;
import com.github.alturkovic.asn.field.TaggedField;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.*;

@Data
public class AsnClassDescription {
    @Getter(AccessLevel.NONE)
    private Multimap<Tag, TaggedField> multimap;

    public AsnClassDescription(final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver, final Class<?> clazz) {
        init(clazz, tagFactory, asnAutoResolver);
    }

    private void init(final Class<?> clazz, final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver) {
        final Multimap<Tag, TaggedField> multimap = MultimapBuilder.treeKeys().arrayListValues().build();

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
            } else if (field.isAnnotationPresent(AsnList.class)) {
                final AsnList listTag = field.getAnnotation(AsnList.class);
                final AsnTag asnTag = listTag.value();

                final Class<? extends AsnConverter<?, ?>> converter = listTag.structured() ? null : getConverter(asnAutoResolver, listTag.asnConverter(), listTag.type());

                tag = getTag(tagFactory, asnAutoResolver, asnTag, field.getType(), true);
                taggedField = new ListTaggedField(fieldPosition, tag, field, listTag.structured(), listTag.type(), converter);
            }

            if (tag != null && taggedField != null) {
                multimap.put(tag, taggedField);
            }

            fieldPosition++;
        }

        this.multimap = multimap;
    }

    // ensures that the order of class defined fields will be kept when encoding
    public List<TaggedField> getClassDeclaredOrderedTaggedFields() {
        final ArrayList<TaggedField> fieldArrayList = new ArrayList<>(multimap.values());
        Collections.sort(fieldArrayList);
        return fieldArrayList;
    }

    private Tag getTag(final TagFactory tagFactory, final AsnAutoResolver asnAutoResolver, final AsnTag asnTag, final Class<?> clazz, final boolean structured) {
        if (asnAutoResolver.shouldTryToResolveUniversal(asnTag)) {
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

    public TaggedField findByTag(final Tag tag, final int index) {
        final List<TaggedField> taggedFields = (List<TaggedField>) multimap.get(tag);
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
}