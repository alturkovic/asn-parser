package com.github.alturkovic.asn.ber.tlv;

import java.io.InputStream;

public interface TlvDataReader {
    BerData readNext(InputStream inputStream);
}
