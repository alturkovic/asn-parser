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

package com.github.alturkovic.asn.ber.utils;

import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.exception.AsnParseException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerUtilsLengthParseTest {

    @Test
    @Parameters({
            "83000002, 2",
            "820254, 596",
            "820119, 281",
            "820147, 327",
            "8201b3, 435",
            "820080, 128",
            "8180, 128",
            "7f, 127",
            "3c, 60",
            "08, 8"
    })
    @TestCaseName("[{index}] parse: ({0})")
    public void shouldParseLength(@HexParam final byte[] given, final int length) throws Exception {
        assertThat(BerUtils.parseLength(given)).isEqualTo(length);
    }

    @Parameters("80")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseLengthIsOneByteAndFirstBitIsOne(@HexParam final byte[] data) throws Exception {
        BerUtils.parseLength(data);
    }

    @Parameters("5080")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseFirstBitIsNotOne(@HexParam final byte[] data) throws Exception {
        BerUtils.parseLength(data);
    }

    @Parameters("831A46")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseByteArrayLengthIsNotAsDescribedInFirstByte(@HexParam final byte[] data) throws Exception {
        BerUtils.parseLength(data);
    }
}