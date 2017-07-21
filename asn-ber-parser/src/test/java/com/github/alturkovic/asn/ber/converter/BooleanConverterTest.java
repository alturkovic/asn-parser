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

import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConvertException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BooleanConverterTest {

    private final AsnConverter<byte[], Boolean> converter = new BooleanConverter();

    // Decoding

    @Test
    @Parameters("ff")
    public void shouldDecodeTrue(@HexParam final byte[] data) throws Exception {
        final Boolean decode = converter.decode(data);
        assertThat(decode).isTrue();
    }

    @Test
    @Parameters("00")
    public void shouldDecodeFalse(@HexParam final byte[] data) throws Exception {
        final Boolean decode = converter.decode(data);
        assertThat(decode).isFalse();
    }

    @Test
    public void shouldDecodeNullBecauseDataIsNull() throws Exception {
        assertThat(converter.decode(null)).isNull();
    }

    @Parameters("00ff")
    @Test(expected = AsnConvertException.class)
    public void shouldFailBecauseLengthIsNotOne(@HexParam final byte[] data) throws Exception {
        converter.decode(data);
    }

    @Parameters("aa")
    @Test(expected = AsnConvertException.class)
    public void shouldFailBecauseDataIsNotBoolean(@HexParam final byte[] data) throws Exception {
        converter.decode(data);
    }

    // Encoding

    @Test
    public void shouldEncodeTrue() throws Exception {
        final byte[] encoded = converter.encode(true);
        assertThat(HexUtils.encode(encoded)).isEqualToIgnoringCase("FF");
    }

    @Test
    public void shouldEncodeFalse() throws Exception {
        final byte[] encoded = converter.encode(false);
        assertThat(HexUtils.encode(encoded)).isEqualToIgnoringCase("00");
    }

    @Test
    public void shouldEncodeNullBecauseDataIsNull() throws Exception {
        assertThat(converter.encode(null)).isNull();
    }
}