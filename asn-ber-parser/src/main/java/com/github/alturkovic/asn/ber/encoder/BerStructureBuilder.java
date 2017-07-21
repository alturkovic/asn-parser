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

package com.github.alturkovic.asn.ber.encoder;

import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.encoder.AsnTlvStructureBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class BerStructureBuilder implements AsnTlvStructureBuilder<byte[]> {
    private final ByteArrayOutputStream result = new ByteArrayOutputStream();
    private final List<byte[]> values = new ArrayList<>();
    private int totalLength = 0;

    public BerStructureBuilder(final BerTag tag) {
        try {
            result.write(BerUtils.convert(tag));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void addValue(final byte[] value) {
        totalLength += value.length;
        values.add(value);
    }

    @Override
    public byte[] build() {
        try {
            result.write(BerUtils.encodeLength(totalLength));
            for (final byte[] value : values) {
                result.write(value);
            }
            return result.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
