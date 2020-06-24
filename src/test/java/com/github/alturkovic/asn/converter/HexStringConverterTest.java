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

package com.github.alturkovic.asn.converter;

import com.github.alturkovic.asn.params.HexParam;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class HexStringConverterTest {

  private final AsnConverter<byte[], String> converter = new HexStringConverter();

  // Decoding

  @Test
  @Parameters({
      "00, 00",
      "ff, ff",
      "12f9, 12F9",
      "1049, 1049",
      "ff7f80, FF7f80",
      "aaf8dc2a, aaf8dc2a"
  })
  @TestCaseName("[{index}] decode: ({0})")
  public void shouldDecode(@HexParam final byte[] given, final String expected) throws Exception {
    assertThat(converter.decode(given)).isEqualToIgnoringCase(expected);
  }

  @Test
  public void shouldDecodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.decode(null)).isNull();
  }

  // Encoding

  @Test
  @Parameters({
      "00, 00",
      "00, 00",
      "12f9, 12f9",
      "1049, 1049",
      "ff7f80, ff7f80",
      "aaf8dc2a, aaf8dc2a",
  })
  @TestCaseName("[{index}] encode: ({0})")
  public void shouldEncode(final String given, @HexParam final byte[] expected) throws Exception {
    assertThat(converter.encode(given)).isEqualTo(expected);
  }

  @Test
  public void shouldEncodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.encode(null)).isNull();
  }
}