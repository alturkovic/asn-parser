package com.github.alturkovic.asn.ber.converter;

import com.github.alturkovic.asn.ber.params.DateParam;
import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.converter.AsnConverter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class DateConverterTest {

    private final AsnConverter<byte[], Date> converter = new DateConverter();

    // Decoding

    @Test
    @Parameters({
            "015e2e95be8b, 29.08.2017. 17:21:59.179",
            "0159571c757f, 31.12.2016. 23:59:59.999"
    })
    @TestCaseName("[{index}] decode: ({0})")
    public void shouldDecode(@HexParam final byte[] given, @DateParam final Date expected) throws Exception {
       assertThat(converter.decode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldDecodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.decode(null)).isNull();
    }

    // Encoding

    @Test
    @Parameters({
            "29.08.2017. 17:21:59.179, 015e2e95be8b",
            "31.12.2016. 23:59:59.999, 0159571c757f"
    })
    @TestCaseName("[{index}] encode: ({0})")
    public void shouldEncode(@DateParam final Date given, @HexParam final byte[] expected) throws Exception {
        assertThat(converter.encode(given)).isEqualTo(expected);
    }

    @Test
    public void shouldEncodeNullBecauseInputIsNull() throws Exception {
        assertThat(converter.encode(null)).isNull();
    }
}