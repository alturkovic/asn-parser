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

package com.github.alturkovic.asn.ber.reader;

import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.tlv.BerDataReader;
import com.github.alturkovic.asn.ber.params.HexParam;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerDataReaderTest {

    @Test
    @Parameters({
            "800101b182, 80, 01, 01", // b182 is from the next TLV
            "810322cbdc, 81, 03, 22cbdc",
            "810322cbdcF02c, 81, 03, 22cbdc", // F02c is from the next TLV
            "9F83330895F4c21abbc48daa, 9F8333, 08, 95F4c21abbc48daa",
            "5f250c8002abcd8103def01202010c, 5f25, 0c, 8002abcd8103def01202010c"
    })
    @TestCaseName("[{index}] read: ({0})")
    public void shouldRead(@HexParam final byte[] given, @HexParam final byte[] tag, @HexParam final byte[] length, @HexParam final byte[] value) throws Exception {
        final BerData rawData = new BerDataReader().readNext(null, new ByteArrayInputStream(given)); // class not used with this implementation

        assertThat(rawData.getTag()).isEqualTo(tag);
        assertThat(rawData.getLength()).isEqualTo(length);
        assertThat(rawData.getValue()).isEqualTo(value);
    }
}