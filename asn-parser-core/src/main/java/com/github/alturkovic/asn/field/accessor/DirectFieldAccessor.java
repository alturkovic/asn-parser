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

package com.github.alturkovic.asn.field.accessor;

import java.lang.reflect.Field;

public class DirectFieldAccessor implements FieldAccessor {

    @Override
    public void setFieldValue(final Object instance, final Field field, final Object value) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            field.set(instance, value);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T getFieldValue(final Object instance, final Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            //noinspection unchecked
            return (T) field.get(instance);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}