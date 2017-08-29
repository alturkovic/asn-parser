package com.github.alturkovic.asn.ber.params;

import junitparams.converters.Param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Param(converter = DateParamConverter.class)
public @interface DateParam {
    String format() default "dd.MM.yyyy. HH:mm:ss.SSS";
}
