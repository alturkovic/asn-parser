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

package com.github.alturkovic.asn.util;

import com.github.alturkovic.asn.exception.AsnParseException;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.Type;
import com.github.alturkovic.asn.tag.UniversalTags;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BerUtils {
  public static final Tag UNIVERSAL_TAG = new Tag(UniversalTags.SEQUENCE, Type.UNIVERSAL, true);

  public static Tag parseTag(final byte[] b) {
    if (b == null || b.length == 0) {
      throw new AsnParseException("Null");
    }

    final var type = (b[0] & BerBitMask.CLASS_BITS) >> 6;
    final var isConstructed = (b[0] & BerBitMask.CONSTRUCTED_BIT) == BerBitMask.CONSTRUCTED_BIT;
    var value = 0;

    if (b.length == 1) {
      if ((b[0] & BerBitMask.TAG_VALUE_BITS) == BerBitMask.TAG_VALUE_BITS) {
        throw new AsnParseException(String.format("If bits 5 to 1 are set tag must not be only one byte long: %02X", b[0]));
      }

      value = b[0] & BerBitMask.TAG_VALUE_BITS;
    } else {
      if ((b[0] & BerBitMask.TAG_VALUE_BITS) != BerBitMask.TAG_VALUE_BITS) {
        throw new AsnParseException(String.format("For multibyte tags bits 5 to 1 of the first byte must be all set to 1: %s[%02X]", HexUtils.encode(b), b[0]));
      }

      if ((b[b.length - 1] & BerBitMask.MOST_SIGNIFICANT_BIT) != 0) {
        throw new AsnParseException(String.format("For multibyte tag bit 8 of the final byte must be 0: %s[%02X]", HexUtils.encode(b), b[b.length - 1]));
      }

      for (int i = 1; i < b.length; i++) {
        if ((i < (b.length - 1)) && (b[i] & BerBitMask.MOST_SIGNIFICANT_BIT) != BerBitMask.MOST_SIGNIFICANT_BIT) {
          throw new AsnParseException(String.format("For multibyte tag bit 8 of the internal bytes must be 1: %s[%02X]", HexUtils.encode(b), b[i]));
        }

        value = value << 7;
        value = value | (b[i] & BerBitMask.NON_LEADING_BITS);
      }
    }

    return new Tag(value, Type.fromCode(type), isConstructed);
  }

  public static int parseLength(final byte[] b) {
    final var length = b.length;

    if (length == 1) {
      if ((b[0] & BerBitMask.MOST_SIGNIFICANT_BIT) == BerBitMask.MOST_SIGNIFICANT_BIT) {
        throw new AsnParseException(String.format("When length is 1 byte, first bit should not be 1: %02X", b[0]));
      }

      return b[0];
    }

    // first byte describes how many bytes are used, and first bit is supposed to be 1
    if ((b[0] & BerBitMask.MOST_SIGNIFICANT_BIT) != BerBitMask.MOST_SIGNIFICANT_BIT) {
      throw new AsnParseException(String.format("When length is more than 1 byte, first bit should be 1: %s", HexUtils.encode(b)));
    }

    if ((b[0] & BerBitMask.NON_LEADING_BITS) != (b.length - 1)) {
      throw new AsnParseException(String.format("Length is not as described in the first byte: %s != %d", HexUtils.encode(b), (b.length - 1)));
    }

    // value is the hex representation of the following bytes
    return Integer.valueOf(HexUtils.encode(Arrays.copyOfRange(b, 1, b.length)), 16);
  }

  public static byte[] encodeLength(final int length) {
    if (length < 128) {
      return new byte[]{(byte) length};
    }

    var hex = Integer.toHexString(length);

    // good enough...
    if (hex.length() % 2 != 0) {
      hex = "0" + hex;
    }

    final byte[] raw;
    try {
      raw = HexUtils.decode(hex);
    } catch (final RuntimeException e) {
      throw new AsnParseException(e);
    }

    // result should be the actual HEX representation of the length with the first byte
    // being the length of the HEX, first byte is used to tell how many bytes to read
    final var result = new byte[raw.length + 1];
    result[0] = (byte) (raw.length | BerBitMask.MOST_SIGNIFICANT_BIT);
    System.arraycopy(raw, 0, result, 1, raw.length);
    return result;
  }

  public static byte[] convert(final Tag tag) {
    final var result = new ByteArrayOutputStream();
    final var descriptionByte = (byte) (tag.getType().getCode() << 6 | ((tag.isConstructed() ? 1 : 0) << 5));

    if (tag.getValue() < 31) {
      result.write(descriptionByte | tag.getValue());
    } else {
      result.write(descriptionByte | BerBitMask.TAG_VALUE_BITS);

      final var valueByteStream = new ByteArrayOutputStream();
      var tempValue = tag.getValue();
      valueByteStream.write(tempValue & BerBitMask.NON_LEADING_BITS);

      while ((tempValue >>= 7) > 0) {
        valueByteStream.write((tempValue & BerBitMask.NON_LEADING_BITS) | BerBitMask.MOST_SIGNIFICANT_BIT);
      }

      final var valueBytes = valueByteStream.toByteArray();
      ArrayUtils.reverse(valueBytes);

      try {
        result.write(valueBytes);
      } catch (final IOException e) {
        throw new UncheckedIOException(String.format("Cannot convert %s to byte[]", tag), e);
      }
    }

    return result.toByteArray();
  }
}
