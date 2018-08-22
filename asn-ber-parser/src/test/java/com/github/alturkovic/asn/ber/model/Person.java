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

package com.github.alturkovic.asn.ber.model;

import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.annotation.AsnCollection;
import com.github.alturkovic.asn.annotation.AsnPostProcessMethod;
import com.github.alturkovic.asn.annotation.AsnPrimitive;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import com.github.alturkovic.asn.ber.converter.HexStringConverter;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AsnPostProcessMethod("postDecode")
@AsnStructure(@AsnTag(value = 16, type = Type.PRIVATE))
public class Person {

  @AsnPrimitive
  private boolean male;

  @AsnPrimitive
  private Integer age;

  @AsnCollection(elementTag = @AsnTag(5), structured = false, asnConverter = HexStringConverter.class, type = String.class)
  private Set<String> phones;

  @AsnCollection(value = @AsnTag(1), type = Address.class)
  private List<Address> addresses;

  @AsnPrimitive(@AsnTag(3))
  private short shoeSize;

  private boolean adult;

  public Person(final boolean male, final int age, final Set<String> phones) {
    this.male = male;
    this.age = age;
    this.phones = phones;
  }

  private void postDecode() {
    adult = age >= 18;
  }
}
