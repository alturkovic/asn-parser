package com.github.alturkovic.asn.ber.model;

import com.github.alturkovic.asn.annotation.AsnPrimitive;
import com.github.alturkovic.asn.annotation.AsnTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressString {

    @AsnPrimitive
    private int type;

    @AsnPrimitive
    private String value;

    @AsnPrimitive(@AsnTag(2))
    private boolean enabled;
}
