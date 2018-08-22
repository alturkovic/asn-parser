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

package com.github.alturkovic.asn.ber;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.UniversalTags;
import com.github.alturkovic.asn.ber.converter.*;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.converter.AutoConverter;
import com.github.alturkovic.asn.exception.AsnConfigurationException;
import com.github.alturkovic.asn.tag.Tag;
import java.util.Set;
import lombok.AllArgsConstructor;

public class BerAutoResolver implements AsnAutoResolver {

  @Override
  public Class<? extends AsnConverter<?, ?>> getUniversalConverterClass(final Class<?> c) {
    if (c == null) {
      throw new AsnConfigurationException("Cannot get a converter for null class");
    }

    if (c == String.class) {
      // String has multiple tags that can represent it, just hardcode this one
      return Utf8StringConverter.class;
    }

    final Class<?> clazz = supportedWrapperToPrimitive(c);
    if (clazz.isPrimitive() || clazz == byte[].class) {
      final Mappings[] mappings = Mappings.values();
      for (final Mappings mapping : mappings) {
        if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
          return mapping.converterClass;
        }
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

    Integer value = null;

    final Class<?> clazz = supportedWrapperToPrimitive(c);
    if (clazz.isPrimitive() || clazz == byte[].class) {
      final Mappings[] mappings = Mappings.values();
      for (final Mappings mapping : mappings) {
        if (mapping.clazz != null && mapping.clazz.equals(clazz)) {
          value = mapping.value;
          break;
        }
      }
    }

    if (value == null) {
      value = c.isAssignableFrom(Set.class) ? Mappings.SET.value : Mappings.SEQUENCE.value;
    }

    return new BerTag(value, Type.UNIVERSAL, constructed);
  }

  private Class<?> supportedWrapperToPrimitive(final Class<?> clazz) {
    if (clazz == Integer.class) {
      return int.class;
    }

    if (clazz == Long.class) {
      return long.class;
    }

    if (clazz == Boolean.class) {
      return boolean.class;
    }

    if (clazz == Short.class) {
      return short.class;
    }

    return clazz;
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
