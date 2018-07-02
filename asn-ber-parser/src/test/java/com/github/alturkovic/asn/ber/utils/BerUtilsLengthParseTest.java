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

package com.github.alturkovic.asn.ber.utils;

import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.exception.AsnParseException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerUtilsLengthParseTest {

  @Test
  @Parameters({
      "83000002, 2",
      "820254, 596",
      "820119, 281",
      "820147, 327",
      "8201b3, 435",
      "820080, 128",
      "8180, 128",
      "7f, 127",
      "3c, 60",
      "08, 8"
  })
  @TestCaseName("[{index}] parse: ({0})")
  public void shouldParseLength(@HexParam final byte[] given, final int length) throws Exception {
    assertThat(BerUtils.parseLength(given)).isEqualTo(length);
  }

  @Parameters("80")
  @Test(expected = AsnParseException.class)
  public void shouldFailBecauseLengthIsOneByteAndFirstBitIsOne(@HexParam final byte[] data) throws Exception {
    BerUtils.parseLength(data);
  }

  @Parameters("5080")
  @Test(expected = AsnParseException.class)
  public void shouldFailBecauseFirstBitIsNotOne(@HexParam final byte[] data) throws Exception {
    BerUtils.parseLength(data);
  }

  @Parameters("831A46")
  @Test(expected = AsnParseException.class)
  public void shouldFailBecauseByteArrayLengthIsNotAsDescribedInFirstByte(@HexParam final byte[] data) throws Exception {
    BerUtils.parseLength(data);
  }
}