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

import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.Type;
import com.github.alturkovic.asn.tag.UniversalTags;
import com.github.alturkovic.asn.tlv.BerData;
import com.github.alturkovic.asn.tlv.BerDataExtractReader;
import com.github.alturkovic.asn.util.HexUtils;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BerDataExtractReaderTest {

  @Test
  public void shouldExtract() {
    final BerDataExtractReader berDataExtractReader = new BerDataExtractReader(Arrays.asList(
        new Tag(1, Type.CONTEXT, true),
        new Tag(UniversalTags.INTEGER, Type.UNIVERSAL, false)
    ));
    final BerData rawData = berDataExtractReader.readNext(new ByteArrayInputStream(HexUtils.decode("A103020103")));

    assertThat(rawData.toTlv()).isEqualTo(HexUtils.decode("020103"));
  }
}