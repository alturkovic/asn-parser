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

package com.github.alturkovic.asn.ber.utils;

import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.exception.AsnParseException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerUtilsTagParseTest {

    @Test
    @Parameters({
            "0, CONTEXT, false, 80",
            "3, CONTEXT, false, 83",
            "37, APPLICATION, true, 7F25",
            "37, CONTEXT, true, BF25",
            "37, UNIVERSAL, false, 1F25",
            "159, CONTEXT, true, BF811F",
            "159, CONTEXT, false, 9F811F",
            "227, APPLICATION, true, 7f8163",
            "435, CONTEXT, false, 9f8333",
            "16819, CONTEXT, false, 9f818333",
            "2148770, PRIVATE, false, df81839322"
    })
    @TestCaseName("[{index}] encode: ({0}, {1}, {2})")
    public void shouldEncode(final int value, final Type type, final boolean constructed, @HexParam final byte[] expected) throws Exception {
        final BerTag tag = new BerTag(value, type, constructed);
        assertThat(BerUtils.convert(tag)).isEqualTo(expected);
    }

    @SuppressWarnings("unused") // used by shouldParse method @Parameters
    private Object parametersForShouldParse() {
        return new Object[][]{
                {"80", new BerTag(0, Type.CONTEXT, false)},
                {"7F25", new BerTag(37, Type.APPLICATION, true)},
                {"BF25", new BerTag(37, Type.CONTEXT, true)},
                {"9F25", new BerTag(37, Type.CONTEXT, false)},
                {"BF8104", new BerTag(132, Type.CONTEXT, true)},
                {"7f8163", new BerTag(227, Type.APPLICATION, true)},
                {"9f8333", new BerTag(435, Type.CONTEXT, false)},
                {"9f808003", new BerTag(3, Type.CONTEXT, false)},
                {"9f818333", new BerTag(16819, Type.CONTEXT, false)}
        };
    }

    @Test
    @Parameters
    @TestCaseName("[{index}] parse: ({0})")
    public void shouldParse(@HexParam final byte[] given, final BerTag expected) throws Exception {
        final BerTag parsed = BerUtils.parseTag(given);
        assertThat(parsed.getType()).isEqualTo(expected.getType());
        assertThat(parsed.getValue()).isEqualTo(expected.getValue());
        assertThat(parsed.isConstructed()).isEqualTo(expected.isConstructed());
    }

    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseDataIsNull() throws Exception {
        BerUtils.parseTag(null);
    }

    @Parameters("AF25")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseValueBitsOfFirstByteAreNotAllOnes(@HexParam final byte[] data) throws Exception {
        BerUtils.parseTag(data);
    }

    @Parameters("9F")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseTagIsOneByteAndValueBitsAreAllOnes(@HexParam final byte[] data) throws Exception {
        BerUtils.parseTag(data);
    }

    @Parameters("BF8188")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseLastBytesMSBIsNotOne(@HexParam final byte[] data) throws Exception {
        BerUtils.parseTag(data);
    }

    @Parameters("1F011D")
    @Test(expected = AsnParseException.class)
    public void shouldFailBecauseOneOfValueBytesMSBIsNotOne(@HexParam final byte[] data) throws Exception {
        BerUtils.parseTag(data);
    }
}