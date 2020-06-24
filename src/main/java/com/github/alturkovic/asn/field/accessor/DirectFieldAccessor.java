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

package com.github.alturkovic.asn.field.accessor;

import com.github.alturkovic.asn.exception.AsnAccessException;
import java.lang.reflect.Field;

public class DirectFieldAccessor implements FieldAccessor {

  @Override
  public void setFieldValue(final Object instance, final Field field, final Object value) {
    if (!field.canAccess(instance)) {
      field.setAccessible(true);
    }

    try {
      field.set(instance, value);
    } catch (final IllegalAccessException e) {
      throw new AsnAccessException(e);
    }
  }

  @Override
  public <T> T getFieldValue(final Object instance, final Field field) {
    if (!field.canAccess(instance)) {
      field.setAccessible(true);
    }

    try {
      //noinspection unchecked
      return (T) field.get(instance);
    } catch (final IllegalAccessException e) {
      throw new AsnAccessException(e);
    }
  }
}