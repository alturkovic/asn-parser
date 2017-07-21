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

public enum Type {
    UNIVERSAL(0, 'U'), APPLICATION(1, 'A'), CONTEXT(2, 'C'), PRIVATE(3, 'P');

    private final int code;
    private final char character;

    Type(final int code, final char character) {
        this.code = code;
        this.character = character;
    }

    public static Type fromCode(final int code) {
        for (final Type type : Type.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown Type code: " + code);
    }

    public static Type fromCharacter(final char character) {
        for (final Type type : Type.values()) {
            if (type.getCharacter() == character) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown Type character: " + character);
    }

    public int getCode() {
        return code;
    }

    public char getCharacter() {
        return character;
    }
}