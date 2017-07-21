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

import com.github.alturkovic.asn.ber.util.BerBitMask;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Slf4j
public class BerLengthReader extends AbstractInputStreamReader {

    public byte[] read(final InputStream inputStream) {
        final int firstByte = readByte(inputStream);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(firstByte);

        // if first byte has MSB set to 1
        // then bits 7-1 describe number of octets that represent length
        if (((byte) firstByte & BerBitMask.MOST_SIGNIFICANT_BIT) == BerBitMask.MOST_SIGNIFICANT_BIT) {
            log.debug("Reading multiple bytes...");

            final int lengthOctetsRequired = (byte) firstByte & BerBitMask.NON_LEADING_BITS;
            final byte[] lengthOctets = readBytes(inputStream, lengthOctetsRequired);

            try {
                out.write(lengthOctets);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        final byte[] result = out.toByteArray();
        log.debug("Read {} byte(s)", result.length);
        return result;
    }
}
