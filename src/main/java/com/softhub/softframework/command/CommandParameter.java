package com.softhub.softframework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CommandParameter {
    String name();
    String description() default "No description provided";
    ParamType type();
    boolean required() default true;
    int index();

    enum ParamType {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        STRING_ARRAY
    }
}

