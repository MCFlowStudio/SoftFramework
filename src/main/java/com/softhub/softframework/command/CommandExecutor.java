package com.softhub.softframework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandExecutor {
    String label();
    String description() default "No description provided";
    String permission() default "";
    boolean isOp() default false;
    boolean hideSuggestion() default false;
    boolean consoleAvailable() default true;
}