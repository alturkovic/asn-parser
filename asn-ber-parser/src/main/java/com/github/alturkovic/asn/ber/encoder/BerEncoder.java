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

package com.github.alturkovic.asn.ber.encoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.ber.tag.BerTag;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.encoder.AbstractAsnTlvEncoder;
import com.github.alturkovic.asn.encoder.AsnTlvStructureBuilder;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.google.common.cache.Cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class BerEncoder extends AbstractAsnTlvEncoder<byte[]> {

    public BerEncoder(final TagFactory tagFactory,
                      final AsnAutoResolver autoResolver,
                      final FieldAccessor fieldAccessor,
                      final Cache<Class<?>, AsnClassDescription> classDescriptionCache,
                      final Cache<Class<? extends AsnConverter<byte[], ?>>, AsnConverter<byte[], ?>> converterCache) {
        super(tagFactory, autoResolver, fieldAccessor, classDescriptionCache, converterCache);
    }

    @Override
    protected byte[] encodePrimitive(final Tag tag, final byte[] value) {
        try {
            final ByteArrayOutputStream result = new ByteArrayOutputStream();

            result.write(BerUtils.convert((BerTag) tag));
            result.write(BerUtils.encodeLength(value.length));
            result.write(value);

            return result.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(String.format("Cannot encode tag %s and value '%s' to a primitive", tag, HexUtils.encode(value)), e);
        }
    }

    @Override
    protected AsnTlvStructureBuilder<byte[]> createNewBuilder(final Tag tag) {
        return new BerStructureBuilder((BerTag) tag);
    }
}
