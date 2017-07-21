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

package com.github.alturkovic.asn.ber.tag;

import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(of = {"value", "type"})
public class BerTag implements Tag {
    private final int value;
    private final Type type;
    private final boolean constructed;

    public BerTag(final int value, final Type type, final boolean constructed) {
        this.value = value;
        this.type = type;
        this.constructed = constructed;
    }

    @Override
    // order by type, then by value
    public int compareTo(final Tag o) {
        if (!(o instanceof BerTag)) {
            // should not happen, if it does, just mix the two, doesn't matter much
            return 0;
        }

        final int typeComparison = type.compareTo(((BerTag) o).type);

        if (typeComparison != 0) {
            return typeComparison;
        }


        return Integer.compare(value, ((BerTag) o).value);
    }
}