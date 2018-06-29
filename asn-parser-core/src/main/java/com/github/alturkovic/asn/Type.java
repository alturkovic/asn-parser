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

package com.github.alturkovic.asn;

/**
 * Tag type.
 */
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