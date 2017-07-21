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

package com.github.alturkovic.asn.ber.decoder;

import com.github.alturkovic.asn.AsnAutoResolver;
import com.github.alturkovic.asn.AsnClassDescription;
import com.github.alturkovic.asn.ber.BerAutoResolver;
import com.github.alturkovic.asn.ber.tag.BerTagFactory;
import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.tlv.BerDataReader;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.field.accessor.DirectFieldAccessor;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.TagFactory;
import com.github.alturkovic.asn.tlv.TlvDataReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class BerDecoderBuilder {
    private static final int DEFAULT_CLASS_DESCRIPTION_SIZE = 64;
    private static final int DEFAULT_CONVERTER_CACHE_SIZE = 64;

    private TagFactory tagFactory = new BerTagFactory();
    private AsnAutoResolver autoResolver = new BerAutoResolver();
    private FieldAccessor fieldAccessor = new DirectFieldAccessor();
    private TlvDataReader<BerData> tlvDataReader = new BerDataReader();
    private Cache<Class<?>, AsnClassDescription> classDescriptionCache;
    private Cache<Class<? extends AsnConverter<byte[], ?>>, AsnConverter<byte[], ?>> converterCache;

    public BerDecoderBuilder() {
        this(DEFAULT_CLASS_DESCRIPTION_SIZE, DEFAULT_CONVERTER_CACHE_SIZE);
    }

    public BerDecoderBuilder(final int classCacheDescriptionSize, final int converterCacheSize) {
        this.classDescriptionCache = CacheBuilder.newBuilder().maximumSize(classCacheDescriptionSize).build();
        this.converterCache = CacheBuilder.newBuilder().maximumSize(converterCacheSize).build();
    }

    public BerDecoderBuilder tagFactory(final TagFactory tagFactory) {
        this.tagFactory = tagFactory;
        return this;
    }

    public BerDecoderBuilder autoResolver(final AsnAutoResolver autoResolver) {
        this.autoResolver = autoResolver;
        return this;
    }

    public BerDecoderBuilder fieldAccessor(final FieldAccessor fieldAccessor) {
        this.fieldAccessor = fieldAccessor;
        return this;
    }

    public BerDecoderBuilder tlvDataReader(final TlvDataReader<BerData> tlvDataReader) {
        this.tlvDataReader = tlvDataReader;
        return this;
    }

    public BerDecoder build() {
        return new BerDecoder(tagFactory, autoResolver, fieldAccessor, tlvDataReader, classDescriptionCache, converterCache);
    }
}
