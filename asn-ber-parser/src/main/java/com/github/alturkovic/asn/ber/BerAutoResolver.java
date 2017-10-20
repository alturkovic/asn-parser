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

package com.github.alturkovic.asn.ber;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.UniversalTags;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.ber.converter.BooleanConverter;
import com.github.alturkovic.asn.ber.converter.IntegerConverter;
import com.github.alturkovic.asn.ber.converter.LongConverter;
import com.github.alturkovic.asn.ber.converter.StringConverter;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.converter.AutoConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.tag.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.ClassUtils;

public class BerAutoResolver implements AsnAutoResolver {

    @Override
    public Class<? extends AsnConverter<?, ?>> getUniversalConverterClass(final Class<?> c) {
        if (c == null) {
            throw new AsnConfigurationException("Cannot get a converter for null class");
        }
        if (c == String.class) {
            // String has multiple tags that can represent it, just hardcode this one
            return StringConverter.class;
        }

        final Class<?> clazz = ClassUtils.primitiveToWrapper(c);

        final Mappings[] mappings = Mappings.values();
        for (final Mappings mapping : mappings) {
            if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
                return mapping.converterClass;
            }
        }

        throw new AsnConfigurationException("Cannot get a converter for: " + clazz.getName());
    }

    @Override
    public Tag getUniversalTag(final Class<?> c, final boolean constructed) {
        if (c == null) {
            return null;
        }

        if (c == String.class) {
            // String has multiple tags that can represent it, just hardcode this one
            return new BerTag(Mappings.OCTET_STRING.value, Type.UNIVERSAL, constructed);
        }

        final Class<?> clazz = ClassUtils.primitiveToWrapper(c);

        Integer value = null;
        final Mappings[] mappings = Mappings.values();
        for (final Mappings mapping : mappings) {
            if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
                value = mapping.value;
                break;
            }
        }

        if (value == null) {
            value = Mappings.SEQUENCE.value;
        }

        return new BerTag(value, Type.UNIVERSAL, constructed);
    }

    @Override
    public boolean shouldTryToResolveUniversal(final AsnTag asnTag) {
        return asnTag.value() == -1;
    }

    @AllArgsConstructor
    private enum Mappings {
        BOOLEAN(UniversalTags.BOOLEAN, Boolean.class, BooleanConverter.class),
        INTEGER(UniversalTags.INTEGER, Integer.class, IntegerConverter.class),
        LONG(UniversalTags.INTEGER, Long.class, LongConverter.class),
        BIT_STRING(UniversalTags.BIT_STRING, null, null), // not configured
        OCTET_STRING(UniversalTags.OCTET_STRING, byte[].class, AutoConverter.class),
        ENUMERATED(UniversalTags.ENUMERATED, null, null), // not configured
        UTF8_STRING(UniversalTags.UTF8_STRING, String.class, StringConverter.class),
        SEQUENCE(UniversalTags.SEQUENCE, null, null), // not configured
        NUMERIC_STRING(UniversalTags.NUMERIC_STRING, String.class, StringConverter.class),
        PRINTABLE_STRING(UniversalTags.PRINTABLE_STRING, String.class, StringConverter.class),
        TELETEX_STRING(UniversalTags.TELETEX_STRING, String.class, StringConverter.class),
        VIDEOTEX_STRING(UniversalTags.VIDEOTEX_STRING, String.class, StringConverter.class),
        IA5_STRING(UniversalTags.IA5_STRING, String.class, StringConverter.class),
        UTC_TIME(UniversalTags.UTC_TIME, null, null), // not configured
        GENERALIZED_TIME(UniversalTags.GENERALIZED_TIME, null, null), // not configured
        GRAPHIC_STRING(UniversalTags.GRAPHIC_STRING, String.class, StringConverter.class),
        VISIBLE_STRING(UniversalTags.VISIBLE_STRING, String.class, StringConverter.class),
        GENERAL_STRING(UniversalTags.GENERAL_STRING, String.class, StringConverter.class),
        UNIVERSAL_STRING(UniversalTags.UNIVERSAL_STRING, String.class, StringConverter.class),
        BITMAP_STRING(UniversalTags.BITMAP_STRING, String.class, StringConverter.class),
        CHARACTER_STRING(UniversalTags.CHARACTER_STRING, String.class, StringConverter.class);

        private final int value;
        private final Class<?> clazz;
        private final Class<? extends AsnConverter<?, ?>> converterClass;
    }
}
