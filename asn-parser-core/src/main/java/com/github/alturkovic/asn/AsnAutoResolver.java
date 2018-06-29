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

import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.tag.Tag;

/**
 * Resolves encoding-specific universal details.
 */
public interface AsnAutoResolver {
    /**
     * Resolves the universal converter for the given class.
     *
     * @param c class
     * @return default converter
     */
    Class<? extends AsnConverter<?, ?>> getUniversalConverterClass(Class<?> c);

    /**
     * Resolves the universal tag for the given class.
     *
     * @param c class
     * @param constructed indicates if the tag is structured or primitive
     * @return universal tag
     */
    Tag getUniversalTag(Class<?> c, boolean constructed);
}
