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