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
import com.github.alturkovic.asn.ber.tlv.BerData;
import com.github.alturkovic.asn.ber.util.BerUtils;
import com.github.alturkovic.asn.ber.util.HexUtils;
import com.github.alturkovic.asn.converter.AsnConverter;
import com.github.alturkovic.asn.decoder.AbstractAsnTlvDecoder;
import com.github.alturkovic.asn.field.accessor.FieldAccessor;
import com.github.alturkovic.asn.tag.Tag;
import com.github.alturkovic.asn.tag.TagFactory;
import com.github.alturkovic.asn.tlv.TlvDataReader;
import com.google.common.cache.Cache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class BerDecoder extends AbstractAsnTlvDecoder<byte[], byte[], byte[], BerData> {
    public BerDecoder(final TagFactory tagFactory,
                      final AsnAutoResolver autoResolver,
                      final FieldAccessor fieldAccessor,
                      final TlvDataReader<BerData> tlvDataReader,
                      final Cache<Class<?>, AsnClassDescription> classDescriptionCache,
                      final Cache<Class<? extends AsnConverter<byte[], ?>>, AsnConverter<byte[], ?>> converterCache) {
        super(tagFactory, autoResolver, fieldAccessor, tlvDataReader, classDescriptionCache, converterCache);
    }

    @Override
    protected Tag parseTag(final BerData tlvData) {
        return BerUtils.parseTag(tlvData.getTag());
    }

    @Override
    protected InputStream getValueInputStream(final byte[] value) {
        return new ByteArrayInputStream(value);
    }

    @Override
    protected Tag getUniversalTag() {
        return BerUtils.UNIVERSAL_TAG;
    }

    @Override
    protected String getValueAsString(final byte[] value) {
        return HexUtils.encode(value);
    }
}
