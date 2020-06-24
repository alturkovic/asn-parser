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

package com.github.alturkovic.asn.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniversalTags {
  public static final int BOOLEAN = 1;
  public static final int INTEGER = 2;
  public static final int BIT_STRING = 3;
  public static final int OCTET_STRING = 4;
  public static final int ENUMERATED = 10;
  public static final int UTF8_STRING = 12;
  public static final int SEQUENCE = 16;
  public static final int SET = 17;
  public static final int NUMERIC_STRING = 18;
  public static final int PRINTABLE_STRING = 19;
  public static final int TELETEX_STRING = 20;
  public static final int VIDEOTEX_STRING = 21;
  public static final int IA5_STRING = 22;
  public static final int UTC_TIME = 23;
  public static final int GENERALIZED_TIME = 24;
  public static final int GRAPHIC_STRING = 25;
  public static final int VISIBLE_STRING = 26;
  public static final int GENERAL_STRING = 27;
  public static final int UNIVERSAL_STRING = 28;
  public static final int CHARACTER_STRING = 29;
  public static final int BITMAP_STRING = 30;
}
