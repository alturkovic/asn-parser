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

import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.util.BerUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BerDataExtractReader implements TlvDataReader {
  private final List<Tag> tags;
  private final BerTagReader tagReader;
  private final BerLengthReader lengthReader;
  private final BerValueReader valueReader;

  public BerDataExtractReader(final List<Tag> tags) {
    this.tags = tags;
    this.tagReader = new BerTagReader();
    this.lengthReader = new BerLengthReader();
    this.valueReader = new BerValueReader();
  }

  @Override
  public BerData readNext(final InputStream inputStream) {
    byte[] tag;
    byte[] length;
    byte[] value;

    var depth = 0;
    var stream = inputStream;
    do {
      tag = tagReader.read(stream);
      length = lengthReader.read(stream);
      value = valueReader.read(stream, BerUtils.parseLength(length));

      final var parsedTag = BerUtils.parseTag(tag);

      if (parsedTag.equals(tags.get(depth))) {
        depth++;
        stream = new ByteArrayInputStream(value);
      }
    } while (depth < tags.size());

    return new BerData(tag, length, value);
  }
}
