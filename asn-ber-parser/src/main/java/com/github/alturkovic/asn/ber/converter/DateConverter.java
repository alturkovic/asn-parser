package com.github.alturkovic.asn.ber.converter;

import com.github.alturkovic.asn.converter.AsnConverter;

import java.util.Date;

public class DateConverter implements AsnConverter<byte[], Date> {

    private final LongConverter longConverter = new LongConverter();

    @Override
    public Date decode(final byte[] data) {
        if (data == null) {
            return null;
        }

        return new Date(longConverter.decode(data));
    }

    @Override
    public byte[] encode(final Date data) {
        if (data == null) {
            return null;
        }

        return longConverter.encode(data.getTime());
    }
}
