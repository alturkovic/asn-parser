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
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.exception.AsnConvertException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class IntegerConverterTest {

    private final AsnConverter<byte[], Integer> converter = new IntegerConverter();

    // Decoding

    @Test
    @Parameters({
            "A0000000, -1610612736",
            "F00000, -1048576",
            "80, -128",
            "B1, -79",
            "FF, -1",
            "FFFFFFFF, -1",
            "00, 0",
            "65, 101",
            "7F, 127",
            "0080, 128",
            "0100, 256",
            "6555, 25941",
            "56B89C, 5683356",
            "007BC4F2, 8111346",
            "0011111111, 286331153",
            "6D23CCA5, 1831062693",
            "6FF2352B, 1878144299",
            "006FF2352B, 1878144299",
            "7FFFFFFF, 2147483647",
            "007FFFFFFF, 2147483647"
    })
    @TestCaseName("[{index}] decode: ({0})")
    public void shouldDecode(@HexParam final byte[] given, final int expected) throws Exception {
        assertThat(converter.decode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldDecodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.decode(null)).isNull();
    }

    @Parameters("008fffffff")
    @Test(expected = AsnConvertException.class)
    public void shouldFailBecauseIntegerOverflow(@HexParam final byte[] data) throws Exception {
        converter.decode(data);
    }

    // Encoding

    @Test
    @Parameters({
            "-1610612736, A0000000",
            "-1048576, F00000",
            "-935780, F1B89C",
            "-2731, F555",
            "-128, 80",
            "-79, B1",
            "-1, FF",
            "0, 00",
            "101, 65",
            "127, 7F",
            "128, 0080",
            "256, 0100",
            "25941, 6555",
            "5683356, 56B89C",
            "8111346, 7BC4F2",
            "286331153, 11111111",
            "1831062693, 6D23CCA5",
            "1878144299, 6FF2352B",
            "2147483647, 7FFFFFFF"
    })
    @TestCaseName("[{index}] encode: ({0})")
    public void shouldEncode(final int given, @HexParam final byte[] expected) throws Exception {
        assertThat(converter.encode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldEncodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.encode(null)).isNull();
    }
}