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

package com.github.alturkovic.asn.ber.converter;

import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConvertException;

import java.math.BigInteger;

public class IntegerConverter implements AsnConverter<byte[], Integer> {

    @Override
    public Integer decode(final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return new BigInteger(data).intValueExact();
        } catch (final ArithmeticException | NumberFormatException e) {
            throw new AsnConvertException(String.format("Cannot convert %s to integer", HexUtils.encode(data)), e);
        }
    }

    @Override
    public byte[] encode(final Integer data) {
        if (data == null) {
            return null;
        }

        return BigInteger.valueOf(data).toByteArray();
    }
}