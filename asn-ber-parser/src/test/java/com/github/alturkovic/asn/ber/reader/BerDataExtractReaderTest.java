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

import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.UniversalTags;
import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.tlv.BerDataExtractReader;
import com.github.alturkovic.asn.ber.tlv.BerDataReader;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

public class BerDataExtractReaderTest {

    @Test
    public void shouldExtract() {
        final BerDataExtractReader berDataExtractReader = new BerDataExtractReader(Arrays.asList(
            new BerTag(1, Type.CONTEXT, true),
            new BerTag(UniversalTags.INTEGER, Type.UNIVERSAL, false)
        ));
        final BerData rawData = berDataExtractReader.readNext(new ByteArrayInputStream(HexUtils.decode("A103020103")));

        assertThat(rawData.toTlv()).isEqualTo(HexUtils.decode("020103"));
    }
}