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

import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Data
@AllArgsConstructor
public class BerDataExtractReader implements TlvDataReader {
    private final List<BerTag> tags;
    private final BerTagReader tagReader;
    private final BerLengthReader lengthReader;
    private final BerValueReader valueReader;

    public BerDataExtractReader(final List<BerTag> tags) {
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

        int depth = 0;
        InputStream stream = inputStream;
        final int tagSize = tags.size();
        do {
            tag = tagReader.read(stream);
            length = lengthReader.read(stream);
            value = valueReader.read(stream, BerUtils.parseLength(length));

            final Tag parsedTag = BerUtils.parseTag(tag);

            if (parsedTag.equals(tags.get(depth))) {
                depth++;
                stream = new ByteArrayInputStream(value);
            }
        } while (depth < tagSize);

        return new BerData(tag, length, value);
    }
}
