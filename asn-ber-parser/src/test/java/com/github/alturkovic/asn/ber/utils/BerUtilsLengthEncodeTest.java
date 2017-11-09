package com.github.alturkovic.asn.ber.utils;

import com.github.alturkovic.asn.ber.params.HexParam;
import com.github.alturkovic.asn.ber.util.BerUtils;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class BerUtilsLengthEncodeTest {

    @Test
    @Parameters({
            "2, 02",
            "127, 7F",
            "154, 819A",
            "2585978, 8327757A"
    })
    public void shouldEncodeLength(final int length, @HexParam final byte[] expected) throws Exception {
        assertThat(BerUtils.encodeLength(length)).isEqualTo(expected);
    }
}
