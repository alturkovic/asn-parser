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

import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.exception.AsnDecodeException;
import lombok.Data;
import org.apache.commons.lang.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Data
public class BerData {
    private final byte[] tag;
    private final byte[] length;
    private final byte[] value;

    public BerData(final byte[] tag, final byte[] length, final byte[] value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
    }

    public boolean isValuePresent() {
        return !ArrayUtils.isEmpty(value);
    }

    public byte[] toTlv() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(tag);
            out.write(length);
            out.write(value);
        } catch (final IOException e) {
            throw new AsnDecodeException(String.format("Cannot convert %s to byte[]", this), e);
        }

        return out.toByteArray();
    }

    public String toString() {
        return String.format("BerData[tag=%s, length=%s, value=%s]",
                HexUtils.encode(tag),
                HexUtils.encode(length),
                HexUtils.encode(value)
        );
    }
}
