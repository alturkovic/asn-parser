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

package com.github.alturkovic.asn.ber.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;

/**
 * Keeps count of instances of the same element.
 *
 * @param <T> element type
 */
@AllArgsConstructor
public class MultiSet<T> {
  private final Map<T, AtomicInteger> counter;

  public MultiSet() {
    this(new HashMap<>());
  }

  /**
   * Returns the count of elements that were added for the given key.
   *
   * @param key key
   * @return count
   */
  public int count(final T key) {
    final AtomicInteger count = counter.get(key);
    return count == null ? 0 : count.get();
  }

  /**
   * Increments the counter for the given key.
   *
   * @param key key
   */
  public void add(final T key) {
    counter.computeIfAbsent(key, aKey -> new AtomicInteger()).incrementAndGet();
  }
}
