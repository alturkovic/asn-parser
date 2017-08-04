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

package com.github.alturkovic.asn.ber.util;

import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.UniversalTags;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.exception.AsnParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BerUtils {
    public static final BerTag UNIVERSAL_TAG = new BerTag(UniversalTags.SEQUENCE, Type.UNIVERSAL, true);

    public static BerTag parseTag(final byte[] b) {
        if (b == null || b.length == 0) {
            throw new AsnParseException("Null");
        }

        final int type = (b[0] & BerBitMask.CLASS_BITS) >> 6;

        final boolean isConstructed = (b[0] & BerBitMask.CONSTRUCTED_BIT) == BerBitMask.CONSTRUCTED_BIT;
        int value = 0;

        if (b.length == 1) {
            log.debug("BerTag is 1 byte long");

            if ((b[0] & BerBitMask.TAG_VALUE_BITS) == BerBitMask.TAG_VALUE_BITS) {
                throw new AsnParseException(String.format("If bits 5 to 1 are set tag must not be only one byte long: %02X", b[0]));
            }

            value = b[0] & BerBitMask.TAG_VALUE_BITS;

        } else {
            log.debug("BerTag has {} bytes", b.length);

            if ((b[0] & BerBitMask.TAG_VALUE_BITS) != BerBitMask.TAG_VALUE_BITS) {
                throw new AsnParseException(String.format("For multibyte tags bits 5 to 1 of the first byte must be all set to 1: %s[%02X]", HexUtils.encode(b), b[0]));
            }

            if ((b[b.length - 1] & BerBitMask.MOST_SIGNIFICANT_BIT) != 0) {
                throw new AsnParseException(String.format("For multibyte tag bit 8 of the final byte must be 0: %s[%02X]", HexUtils.encode(b), b[b.length - 1]));
            }

            for (int i = 1; i < b.length; i++) {
                if ((i < (b.length - 1)) && (b[i] & BerBitMask.MOST_SIGNIFICANT_BIT) != BerBitMask.MOST_SIGNIFICANT_BIT) {
                    throw new AsnParseException(String.format("For multibyte tag bit 8 of the internal bytes must be 1: %s[%02X]", HexUtils.encode(b), b[i]));
                }

                value = value << 7;
                value = value | (b[i] & BerBitMask.NON_LEADING_BITS);
            }
        }

        final BerTag tag = new BerTag(value, Type.fromCode(type), isConstructed);
        log.debug("Read tag: {}", tag);
        return tag;
    }

    public static int parseLength(final byte[] b) {
        final int length = b.length;

        if (length == 1) {
            if ((b[0] & BerBitMask.MOST_SIGNIFICANT_BIT) == BerBitMask.MOST_SIGNIFICANT_BIT) {
                throw new AsnParseException(String.format("When length is 1 byte, first bit should not be 1: %02X", b[0]));
            }

            return b[0];
        }

        // first byte describes how many bytes are used, and first bit is supposed to be 1
        if ((b[0] & BerBitMask.MOST_SIGNIFICANT_BIT) != BerBitMask.MOST_SIGNIFICANT_BIT) {
            throw new AsnParseException(String.format("When length is more than 1 byte, first bit should be 1: %s", HexUtils.encode(b)));
        }

        if ((b[0] & BerBitMask.NON_LEADING_BITS) != (b.length - 1)) {
            throw new AsnParseException(String.format("Length is not as described in the first byte: %s != %d", HexUtils.encode(b), (b.length - 1)));
        }

        // value is the hex representation of the following bytes
        return Integer.valueOf(HexUtils.encode(Arrays.copyOfRange(b, 1, b.length)), 16);
    }

    public static byte[] encodeLength(final int length) {
        if (length < 128) {
            return new byte[]{(byte) length};
        }

        String hex = Integer.toHexString(length);

        // good enough...
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        final byte[] raw;
        try {
            raw = HexUtils.decode(hex);
        } catch (final RuntimeException e) {
            throw new AsnParseException(e);
        }

        return ArrayUtils.add(raw, 0, (byte) (raw.length | BerBitMask.MOST_SIGNIFICANT_BIT));
    }

    public static byte[] convert(final BerTag tag) {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        final byte descriptionByte = (byte) (tag.getType().getCode() << 6 | ((tag.isConstructed() ? 1 : 0) << 5));

        if (tag.getValue() < 31) {
            result.write(descriptionByte | tag.getValue());
        } else {
            result.write(descriptionByte | BerBitMask.TAG_VALUE_BITS);

            final ByteArrayOutputStream valueByteStream = new ByteArrayOutputStream();
            int tempValue = tag.getValue();
            valueByteStream.write(tempValue & BerBitMask.NON_LEADING_BITS);

            while ((tempValue >>= 7) > 0) {
                valueByteStream.write((tempValue & BerBitMask.NON_LEADING_BITS) | BerBitMask.MOST_SIGNIFICANT_BIT);
            }

            final byte[] valueBytes = valueByteStream.toByteArray();
            ArrayUtils.reverse(valueBytes);

            try {
                result.write(valueBytes);
            } catch (final IOException e) {
                throw new UncheckedIOException(String.format("Cannot convert %s to byte[]", tag), e);
            }
        }

        return result.toByteArray();
    }
}
