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

package com.github.alturkovic.asn.ber.converter;

import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConvertException;

public class BooleanConverter implements AsnConverter<byte[], Boolean> {

  @Override
  public Boolean decode(final byte[] data) {
    if (data == null) {
      return null;
    }

    if (data.length != 1) {
      throw new AsnConvertException("Data has multiple bytes: " + HexUtils.encode(data));
    }

    if (data[0] == 0) {
      return false;
    }

    if (data[0] == -1) {
      return true;
    }

    throw new AsnConvertException(String.format("%s doesn't represent boolean", HexUtils.encode(data)));
  }

  @Override
  public byte[] encode(final Boolean data) {
    if (data == null) {
      return null;
    }

    return new byte[]{(byte) (data ? -1 : 0)};
  }
}