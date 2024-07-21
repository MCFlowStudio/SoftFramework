package com.softhub.softframework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // 메소드에 적용
public @interface CommandExecutor {
    String label();
    String description() default "No description provided";
    String permission() default "";
    boolean isOp() default false;
    boolean hideSuggestion() default false;
}