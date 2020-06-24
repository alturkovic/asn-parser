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
import java.io.InputStream;

public class BerTagReader extends AbstractInputStreamReader {

  public byte[] read(final InputStream inputStream) {
    final var firstByte = readByte(inputStream);
    final var out = new ByteArrayOutputStream();
    out.write(firstByte);

    // if first byte has bits 5-1 set to 1
    // then it is a multibyte value
    if (((byte) firstByte & BerBitMask.TAG_VALUE_BITS) == BerBitMask.TAG_VALUE_BITS) {

      int valueByte;
      do {
        valueByte = readByte(inputStream);
        out.write(valueByte);
      } while (((byte) valueByte & BerBitMask.MOST_SIGNIFICANT_BIT) == BerBitMask.MOST_SIGNIFICANT_BIT);
    }

    return out.toByteArray();
  }
}
