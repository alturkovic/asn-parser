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

package com.github.alturkovic.asn.ber.tlv;

import com.github.alturkovic.asn.exception.AsnReadException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractInputStreamReader {

  protected int readByte(final InputStream inputStream) {
    try {
      final int bite = inputStream.read();
      checkClosure(bite);
      return bite;
    } catch (final IOException e) {
      throw new AsnReadException(e);
    }
  }

  protected void checkClosure(final int bite) throws IOException {
    if (bite < 0) {
      throw new IOException("Socket closed during message assembly");
    }
  }

  protected byte[] readBytes(final InputStream inputStream, final int bytesToRead) {
    final byte[] result = new byte[bytesToRead];

    final DataInputStream dis = new DataInputStream(inputStream);
    try {
      dis.readFully(result);
    } catch (final IOException e) {
      throw new AsnReadException(e);
    }

    return result;
  }
}
