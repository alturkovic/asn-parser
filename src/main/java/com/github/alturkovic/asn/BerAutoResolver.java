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

package com.github.alturkovic.asn;

import com.github.alturkovic.asn.converter.AsciiStringConverter;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.converter.AutoConverter;
import com.github.alturkovic.asn.converter.BooleanConverter;
import com.github.alturkovic.asn.converter.IntegerConverter;
import com.github.alturkovic.asn.converter.LongConverter;
import com.github.alturkovic.asn.converter.ShortConverter;
import com.github.alturkovic.asn.converter.Utf8StringConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.Type;
import com.github.alturkovic.asn.tag.UniversalTags;
import com.github.alturkovic.asn.util.ClassUtils;
import java.util.Collection;
import lombok.AllArgsConstructor;

public class BerAutoResolver {

  public static Class<? extends AsnConverter<?, ?>> getUniversalConverterClass(final Class<?> c) {
    if (c == null) {
      throw new AsnConfigurationException("Cannot get a converter for null class");
    }

    if (c == String.class) {
      // String has multiple tags that can represent it, just hardcode this one
      return Utf8StringConverter.class;
    }

    final var clazz = ClassUtils.supportedWrapperToPrimitive(c);
    if (clazz.isPrimitive() || clazz == byte[].class) {
      for (final var mapping : Mappings.values()) {
        if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
          return mapping.converterClass;
        }
      }
    }

    throw new AsnConfigurationException("Cannot get a converter for: " + clazz.getName());
  }

  public static Tag getUniversalTag(final Class<?> c, final boolean constructed) {
    if (c == null) {
      return null;
    }

    if (c == String.class) {
      // String has multiple tags that can represent it, just hardcode this one
      return new Tag(Mappings.OCTET_STRING.value, Type.UNIVERSAL, constructed);
    }

    Integer value = null;

    final var clazz = ClassUtils.supportedWrapperToPrimitive(c);
    if (clazz.isPrimitive() || clazz == byte[].class) {
      for (final var mapping : Mappings.values()) {
        if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
          value = mapping.value;
          break;
        }
      }
    }

    if (value == null) {
      value = Collection.class.isAssignableFrom(c) ? Mappings.SET.value : Mappings.SEQUENCE.value;
    }

    return new Tag(value, Type.UNIVERSAL, constructed);
  }

  @AllArgsConstructor
  private enum Mappings {
    BOOLEAN(UniversalTags.BOOLEAN, boolean.class, BooleanConverter.class),
    INTEGER(UniversalTags.INTEGER, int.class, IntegerConverter.class),
    SHORT(UniversalTags.INTEGER, short.class, ShortConverter.class),
    LONG(UniversalTags.INTEGER, long.class, LongConverter.class),
    BIT_STRING(UniversalTags.BIT_STRING, null, null), // not configured
    OCTET_STRING(UniversalTags.OCTET_STRING, byte[].class, AutoConverter.class),
    ENUMERATED(UniversalTags.ENUMERATED, null, null), // not configured
    UTF8_STRING(UniversalTags.UTF8_STRING, String.class, Utf8StringConverter.class),
    SEQUENCE(UniversalTags.SEQUENCE, null, null), // not configured
    SET(UniversalTags.SET, null, null), // not configured
    NUMERIC_STRING(UniversalTags.NUMERIC_STRING, String.class, Utf8StringConverter.class),
    PRINTABLE_STRING(UniversalTags.PRINTABLE_STRING, String.class, Utf8StringConverter.class),
    TELETEX_STRING(UniversalTags.TELETEX_STRING, String.class, Utf8StringConverter.class),
    VIDEOTEX_STRING(UniversalTags.VIDEOTEX_STRING, String.class, Utf8StringConverter.class),
    IA5_STRING(UniversalTags.IA5_STRING, String.class, AsciiStringConverter.class),
    UTC_TIME(UniversalTags.UTC_TIME, null, null), // not configured
    GENERALIZED_TIME(UniversalTags.GENERALIZED_TIME, null, null), // not configured
    GRAPHIC_STRING(UniversalTags.GRAPHIC_STRING, String.class, Utf8StringConverter.class),
    VISIBLE_STRING(UniversalTags.VISIBLE_STRING, String.class, AsciiStringConverter.class),
    GENERAL_STRING(UniversalTags.GENERAL_STRING, String.class, Utf8StringConverter.class),
    UNIVERSAL_STRING(UniversalTags.UNIVERSAL_STRING, String.class, Utf8StringConverter.class),
    BITMAP_STRING(UniversalTags.BITMAP_STRING, String.class, Utf8StringConverter.class),
    CHARACTER_STRING(UniversalTags.CHARACTER_STRING, String.class, Utf8StringConverter.class);

    private final int value;
    private final Class<?> clazz;
    private final Class<? extends AsnConverter<?, ?>> converterClass;
  }
}
