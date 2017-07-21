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
public class StringConverterTest {

    private final AsnConverter<byte[], String> converter = new StringConverter();

    // Decoding

    @Test
    @Parameters({
            "746573742e61706e, test.apn",
            "48412f22235444414b2223727438, HA/\"#TDAK\"#rt8",
            "253d28262f2923282f2634234a2824263b5f3a40, %=(&/)#(/&4#J($&;_:@",
            "67333938666737293a5f6566613a3b284624472f244647284648, g398fg7):_efa:;(F$G/$FG(FH"
    })
    @TestCaseName("[{index}] decode: ({0})")
    public void shouldDecode(@HexParam final byte[] given, final String expected) throws Exception {
        assertThat(converter.decode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldDecodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.decode(null)).isNull();
    }

    // Encoding

    @Test
    @Parameters({
            "Shell, 5368656c6c",
            "John Doe, 4a6f686e20446f65",
            "test.apn, 746573742e61706e",
            "Hello world, 48656c6c6f20776f726c64",
            "HA/\"#TDAK\"#rt8, 48412f22235444414b2223727438",
            "%=(&/)#(/&4#J($&;_:@, 253d28262f2923282f2634234a2824263b5f3a40",
            "g398fg7):_efa:;(F$G/$FG(FH, 67333938666737293a5f6566613a3b284624472f244647284648"
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