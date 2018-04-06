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

import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.exception.AsnDecodeException;
import lombok.Data;

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
        return value != null && value.length > 0;
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
