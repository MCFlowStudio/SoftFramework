package com.softhub.softframework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 클래스에 적용
public @interface Command {
    String name();
    String description() default "No description provided";
    String usage() default "/<command>";
    String permission() default "";
    String[] aliases() default {};
    boolean isOp() default false;
}
