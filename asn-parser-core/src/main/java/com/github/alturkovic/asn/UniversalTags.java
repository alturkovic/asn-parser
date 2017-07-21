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

package com.github.alturkovic.asn;

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
