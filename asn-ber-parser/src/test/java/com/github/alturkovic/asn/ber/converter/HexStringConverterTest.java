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

import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.ber.params.HexParam;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class HexStringConverterTest {

    private final AsnConverter<byte[], String> converter = new HexStringConverter();

    // Decoding

    @Test
    @Parameters({
            "00, 00",
            "ff, ff",
            "12f9, 12F9",
            "1049, 1049",
            "ff7f80, FF7f80",
            "aaf8dc2a, aaf8dc2a"
    })
    @TestCaseName("[{index}] decode: ({0})")
    public void shouldDecode(@HexParam final byte[] given, final String expected) throws Exception {
        assertThat(converter.decode(given)).isEqualToIgnoringCase(expected);
    }

    @Test
    public void shouldDecodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.decode(null)).isNull();
    }

    // Encoding

    @Test
    @Parameters({
            "00, 00",
            "00, 00",
            "12f9, 12f9",
            "1049, 1049",
            "ff7f80, ff7f80",
            "aaf8dc2a, aaf8dc2a",
    })
    @TestCaseName("[{index}] encode: ({0})")
    public void shouldEncode(final String given, @HexParam final byte[] expected) throws Exception {
        assertThat(converter.encode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldEncodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.encode(null)).isNull();
    }
}