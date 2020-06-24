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

package com.github.alturkovic.asn.reader;

import com.github.alturkovic.asn.params.HexParam;
import com.github.alturkovic.asn.tlv.BerData;
import com.github.alturkovic.asn.tlv.BerDataReader;
import java.io.ByteArrayInputStream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerDataReaderTest {

  @Test
  @Parameters({
      "800101b182, 80, 01, 01", // b182 is from the next TLV
      "810322cbdc, 81, 03, 22cbdc",
      "810322cbdcF02c, 81, 03, 22cbdc", // F02c is from the next TLV
      "9F83330895F4c21abbc48daa, 9F8333, 08, 95F4c21abbc48daa",
      "5f250c8002abcd8103def01202010c, 5f25, 0c, 8002abcd8103def01202010c"
  })
  @TestCaseName("[{index}] read: ({0})")
  public void shouldRead(@HexParam final byte[] given, @HexParam final byte[] tag, @HexParam final byte[] length, @HexParam final byte[] value) {
    final BerData rawData = new BerDataReader().readNext(new ByteArrayInputStream(given));

    assertThat(rawData.getTag()).isEqualTo(tag);
    assertThat(rawData.getLength()).isEqualTo(length);
    assertThat(rawData.getValue()).isEqualTo(value);
  }
}