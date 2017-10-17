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

import com.github.alturkovic.asn.ber.util.BerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Data
@Slf4j
@AllArgsConstructor
public class BerDataReader implements TlvDataReader {
    private final BerTagReader tagReader;
    private final BerLengthReader lengthReader;
    private final BerValueReader valueReader;

    public BerDataReader() {
        this.tagReader = new BerTagReader();
        this.lengthReader = new BerLengthReader();
        this.valueReader = new BerValueReader();
    }

    @Override
    public BerData readNext(final InputStream inputStream) {
        final byte[] tag = tagReader.read(inputStream);
        final byte[] length = lengthReader.read(inputStream);
        final byte[] value = valueReader.read(inputStream, BerUtils.parseLength(length));
        return new BerData(tag, length, value);
    }
}
