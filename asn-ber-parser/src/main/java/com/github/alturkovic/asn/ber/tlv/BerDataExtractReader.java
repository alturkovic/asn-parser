/*
 * Copyright (c)  2017 Alen Turković <alturkovic@gmail.com>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.github.alturkovic.asn.ber.tlv;

import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tlv.TlvDataReader;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Data
@AllArgsConstructor
public class BerDataExtractReader implements TlvDataReader<BerData> {
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
    public BerData readNext(final Class<?> clazz, final InputStream inputStream) {
        byte[] tag;
        byte[] length;
        byte[] value;

        int depth = 0;
        InputStream stream = inputStream;
        final int tagSize = tags.size();
        do {
            tag = tagReader.read(stream);
            length = lengthReader.read(stream);
            value = valueReader.read(inputStream, BerUtils.parseLength(length));

            final Tag parsedTag = BerUtils.parseTag(tag);

            if (parsedTag.equals(tags.get(depth))) {
                depth++;
                stream = new ByteArrayInputStream(value);
            }
        } while (depth < tagSize);

        return new BerData(tag, length, value);
    }
}
