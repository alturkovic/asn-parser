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

package com.github.alturkovic.asn.ber.converter;

import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.converter.AsnConverter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class Utf8StringConverterTest {

    private final AsnConverter<byte[], String> converter = new Utf8StringConverter();

    // Decoding

    @Test
    @Parameters({
            "746573742e61706e, test.apn",
            "7465737431407273612e636f6d, test1@rsa.com",
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
            "test1@rsa.com, 7465737431407273612e636f6d",
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