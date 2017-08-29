package com.github.alturkovic.asn.ber.params;

import junitparams.converters.ConversionFailedException;
import junitparams.converters.Converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParamConverter implements Converter<DateParam, Date> {

    private DateFormat format;

    @Override
    public void initialize(final DateParam annotation) {
        format = new SimpleDateFormat(annotation.format());
    }

    @Override
    public Date convert(final Object param) throws ConversionFailedException {
        try {
            return format.parse(param.toString());
        } catch (ParseException e) {
            throw new ConversionFailedException(String.format("failed: %s", e));
        }
    }
}