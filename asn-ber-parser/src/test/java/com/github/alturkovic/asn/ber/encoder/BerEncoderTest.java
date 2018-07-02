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

package com.github.alturkovic.asn.ber.encoder;

import com.github.alturkovic.asn.ber.model.Address;
import com.github.alturkovic.asn.ber.model.Person;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.encoder.AsnEncoder;
import java.util.HashSet;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BerEncoderTest {

  private final AsnEncoder<byte[]> encoder = new BerEncoderBuilder().build();

  @Test
  public void shouldEncodePersonExample() {
    final byte[] encodedHex = encoder.encode(Person.builder()
        .male(true)
        .age(24)
        .adult(true)
        .phones(new HashSet<>(asList("385998069002", "385998069003")))
        .addresses(asList(new Address("First", 1, true), new Address("Second", 2, false)))
        .build());

    assertThat(HexUtils.decode("F0390101FF020118311085063859980690038506385998069002A11F300D040546697273740201018201FF300E04065365636F6E64020102820100")).isEqualTo(encodedHex);
  }
}