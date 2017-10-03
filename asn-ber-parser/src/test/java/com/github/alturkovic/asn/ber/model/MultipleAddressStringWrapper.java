package com.github.alturkovic.asn.ber.model;

import com.github.alturkovic.asn.Type;
import com.github.alturkovic.asn.annotation.AsnStructure;
import com.github.alturkovic.asn.annotation.AsnTag;
import lombok.Data;

@Data
@AsnStructure(@AsnTag(value = 16, type = Type.UNIVERSAL))
public class MultipleAddressStringWrapper {

    @AsnStructure(@AsnTag(4))
    private AddressString addressOne;

    @AsnStructure(@AsnTag(4))
    private AddressString addressTwo;
}
