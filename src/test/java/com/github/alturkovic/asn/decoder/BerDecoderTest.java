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

package com.github.alturkovic.asn.decoder;

import com.github.alturkovic.asn.model.Address;
import com.github.alturkovic.asn.model.EventA;
import com.github.alturkovic.asn.model.EventB;
import com.github.alturkovic.asn.model.EventListWrapper;
import com.github.alturkovic.asn.model.EventWrapper;
import com.github.alturkovic.asn.model.MultipleAddressWrapper;
import com.github.alturkovic.asn.model.Person;
import com.github.alturkovic.asn.util.HexUtils;
import java.util.HashSet;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BerDecoderTest {

  private final AsnDecoder<byte[]> decoder = new BerDecoder();

  @Test
  public void shouldDecodePersonExample() {
    final byte[] ber = HexUtils.decode("F03C0101FF020118311004063859980690030406385998069002A11FA20D040546697273740201018201FFA20E04065365636F6E64020102820100830128");
    final Person decoded = decoder.decode(Person.class, ber);

    assertThat(decoded).isEqualTo(Person.builder()
        .male(true)
        .age(24)
        .shoeSize((short) 40)
        .adult(true)
        .phones(new HashSet<>(asList("385998069002", "385998069003")))
        .addresses(asList(new Address("First", 1, true), new Address("Second", 2, false)))
        .build());
  }

  @Test
  public void shouldDecodeMultipleAddressStingsAndDiscardTheExtraOne() {
    final byte[] encoded = HexUtils.decode("302aa40c0201010404616472318201ffa40c020102040461647232820100a40c020103040461647233820100");
    final MultipleAddressWrapper decoded = decoder.decode(MultipleAddressWrapper.class, encoded);
    assertThat(decoded.getAddressOne()).isEqualTo(new Address("adr1", 1, true));
    assertThat(decoded.getAddressTwo()).isEqualTo(new Address("adr2", 2, false));
  }

  @Test
  public void shouldDecodePolymorphicType() {
    final byte[] encodedA = HexUtils.decode("300D810161A208A106810101820102");
    final var wrapperA = decoder.decode(EventWrapper.class, encodedA);
    assertThat(wrapperA.getId()).isEqualTo("a");
    assertThat(wrapperA.getEvent()).isInstanceOf(EventA.class);
    assertThat(wrapperA.getEvent().getValue()).isEqualTo(1);
    assertThat(((EventA) wrapperA.getEvent()).getNumber()).isEqualTo(2);

    final byte[] encodedB = HexUtils.decode("300D810162A208A2068101FF820102");
    final var wrapperB = decoder.decode(EventWrapper.class, encodedB);
    assertThat(wrapperB.getId()).isEqualTo("b");
    assertThat(wrapperB.getEvent()).isInstanceOf(EventB.class);
    assertThat(wrapperB.getEvent().getValue()).isEqualTo(2);
    assertThat(((EventB) wrapperB.getEvent()).isEnabled()).isTrue();
  }

  @Test
  public void shouldDecodePolymorphicCollection() {
    final byte[] encoded = HexUtils.decode("301D810101A218A106810101820102A106810103820104A2068101FF820102");
    final var wrapper = decoder.decode(EventListWrapper.class, encoded);
    assertThat(wrapper.getId()).isEqualTo(1);
    assertThat(wrapper.getEvents()).containsExactlyInAnyOrder(
        new EventA(1, 2),
        new EventA(3, 4),
        new EventB(true, 2)
    );
  }
}