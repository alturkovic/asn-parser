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

package com.github.alturkovic.asn.ber.util;

import com.github.alturkovic.asn.exception.AsnParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HexUtils {

  private static final char[] DIGITS = "0123456789ABCDEF".toCharArray();

  public static String encode(final byte[] data) {
    final int l = data.length;
    final char[] out = new char[l << 1];
    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
      out[j++] = DIGITS[0x0F & data[i]];
    }
    return new String(out);
  }

  public static byte[] decode(final String hex) {
    final char[] data = hex.toCharArray();

    final int len = data.length;

    if ((len & 0x01) != 0) {
      throw new AsnParseException("Odd number of characters.");
    }

    final byte[] out = new byte[len >> 1];

    // two characters form the hex value.
    for (int i = 0, j = 0; j < len; i++) {
      int f = toDigit(data[j], j) << 4;
      j++;
      f = f | toDigit(data[j], j);
      j++;
      out[i] = (byte) (f & 0xFF);
    }

    return out;
  }

  private static int toDigit(final char ch, final int index) {
    final int digit = Character.digit(ch, 16);
    if (digit == -1) {
      throw new AsnParseException("Illegal hexadecimal character " + ch + " at index " + index);
    }
    return digit;
  }
}
