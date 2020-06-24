/*
 * MIT License
 *
 * Copyright (c) 2020 Alen Turkovic
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
 *
 */

package com.github.alturkovic.asn.encoder;

import com.github.alturkovic.asn.model.Address;
import com.github.alturkovic.asn.model.Person;
import com.github.alturkovic.asn.util.HexUtils;
import java.util.HashSet;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BerEncoderTest {

  private final AsnEncoder<byte[]> encoder = new BerEncoder();

  @Test
  public void shouldEncodePersonExample() {
    final byte[] encodedHex = encoder.encode(Person.builder()
        .male(true)
        .age(24)
        .shoeSize((short) 40)
        .adult(true)
        .phones(new HashSet<>(asList("385998069002", "385998069003")))
        .addresses(asList(new Address("First", 1, true), new Address("Second", 2, false)))
        .build());

    final String expected = "F03C0101FF020118311004063859980690030406385998069002A11FA20D040546697273740201018201FFA20E04065365636F6E64020102820100830128";
    assertThat(HexUtils.decode(expected)).isEqualTo(encodedHex);
  }
}