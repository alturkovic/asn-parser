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
public class LongConverterTest {

  private final AsnConverter<byte[], Long> converter = new LongConverter();

  // Decoding

  @Test
  @Parameters({
      "ac1f748eac1f6491, -6043984018785999727",
      "C25ADD3210, -264763526640",
      "EA51A500B0, -93119512400",
      "A0000000, -1610612736",
      "F00000, -1048576",
      "F1B89C, -935780",
      "F555, -2731",
      "80, -128",
      "B1, -79",
      "FF, -1",
      "FFFFFFFF, -1",
      "00, 0",
      "65, 101",
      "7F, 127",
      "0080, 128",
      "0100, 256",
      "6555, 25941",
      "56B89C, 5683356",
      "007BC4F2, 8111346",
      "0011111111, 286331153",
      "6D23CCA5, 1831062693",
      "006FF2352B, 1878144299",
      "007FFFFFFF, 2147483647"
  })
  @TestCaseName("[{index}] decode: ({0})")
  public void shouldDecode(@HexParam final byte[] given, final long expected) throws Exception {
    assertThat(converter.decode(given)).isEqualTo(expected);
  }

  @Test
  public void shouldDecodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.decode(null)).isNull();
  }

  // Encoding

  @Test
  @Parameters({
      "-6043984018785999727, ac1f748eac1f6491",
      "-264763526640, C25ADD3210",
      "-93119512400, EA51A500B0",
      "-1610612736, A0000000",
      "-1048576, F00000",
      "-935780, F1B89C",
      "-2731, F555",
      "-128, 80",
      "-79, B1",
      "-1, FF",
      "0, 00",
      "101, 65",
      "127, 7F",
      "128, 0080",
      "256, 0100",
      "25941, 6555",
      "5683356, 56B89C",
      "8111346, 7BC4F2",
      "286331153, 11111111",
      "1831062693, 6D23CCA5",
      "1878144299, 6FF2352B",
      "2147483647, 7FFFFFFF"
  })
  @TestCaseName("[{index}] encode: ({0})")
  public void shouldEncode(final long given, @HexParam final byte[] expected) throws Exception {
    assertThat(converter.encode(given)).isEqualTo(expected);
  }

  @Test
  public void shouldEncodeNullBecauseInputIsNull() throws Exception {
    assertThat(converter.encode(null)).isNull();
  }
}