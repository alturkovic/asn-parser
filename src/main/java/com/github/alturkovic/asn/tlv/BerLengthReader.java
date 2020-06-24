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

package com.github.alturkovic.asn.tlv;

import com.github.alturkovic.asn.util.BerBitMask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class BerLengthReader extends AbstractInputStreamReader {

  public byte[] read(final InputStream inputStream) {
    final var firstByte = readByte(inputStream);
    final var out = new ByteArrayOutputStream();
    out.write(firstByte);

    // if first byte has MSB set to 1
    // then bits 7-1 describe number of octets that represent length
    if (((byte) firstByte & BerBitMask.MOST_SIGNIFICANT_BIT) == BerBitMask.MOST_SIGNIFICANT_BIT) {
      final var lengthOctetsRequired = (byte) firstByte & BerBitMask.NON_LEADING_BITS;
      final var lengthOctets = readBytes(inputStream, lengthOctetsRequired);

      try {
        out.write(lengthOctets);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return out.toByteArray();
  }
}
