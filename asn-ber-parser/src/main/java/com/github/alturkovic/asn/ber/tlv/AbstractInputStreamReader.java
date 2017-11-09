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

import com.github.alturkovic.asn.exception.AsnReadException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractInputStreamReader {

    protected int readByte(final InputStream inputStream) {
        try {
            final int bite = inputStream.read();
            checkClosure(bite);
            return bite;
        } catch (final IOException e) {
            throw new AsnReadException(e);
        }
    }

    protected void checkClosure(final int bite) throws IOException {
        if (bite < 0) {
            throw new IOException("Socket closed during message assembly");
        }
    }

    protected byte[] readBytes(final InputStream inputStream, final int bytesToRead) {
        final byte[] result = new byte[bytesToRead];

        final DataInputStream dis = new DataInputStream(inputStream);
        try {
            dis.readFully(result);
        } catch (final IOException e) {
            throw new AsnReadException(e);
        }

        return result;
    }
}
