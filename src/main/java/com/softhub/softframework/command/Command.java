package com.softhub.softframework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    String name();
    String description() default "No description provided";
    String usage() default "/<command>";
    String permission() default "";
    String[] aliases() default {};
    boolean isOp() default false;
    boolean consoleAvailable() default true;
    Class<? extends TabCompleterProvider> tabCompleterProvider() default TabCompleterProvider.class;
}
