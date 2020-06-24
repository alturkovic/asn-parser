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

import com.github.alturkovic.asn.params.DateParam;
import com.github.alturkovic.asn.params.HexParam;
import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class DateConverterTest {

  private final AsnConverter<byte[], Date> converter = new DateConverter();

  // Decoding

  @Test
  @Parameters({
      "015e2e95be8b, 29.08.2017. 17:21:59.179",
      "0159571c757f, 31.12.2016. 23:59:59.999"
  })
  @TestCaseName("[{index}] decode: ({0})")
  public void shouldDecode(@HexParam final byte[] given, @DateParam final Date expected) throws Exception {
    assertThat(converter.decode(given)).isEqualTo(expected);
  }

  @Test
  public void shouldDecodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.decode(null)).isNull();
  }

  // Encoding

  @Test
  @Parameters({
      "29.08.2017. 17:21:59.179, 015e2e95be8b",
      "31.12.2016. 23:59:59.999, 0159571c757f"
  })
  @TestCaseName("[{index}] encode: ({0})")
  public void shouldEncode(@DateParam final Date given, @HexParam final byte[] expected) throws Exception {
    assertThat(converter.encode(given)).isEqualTo(expected);
  }

  @Test
  public void shouldEncodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.encode(null)).isNull();
  }
}