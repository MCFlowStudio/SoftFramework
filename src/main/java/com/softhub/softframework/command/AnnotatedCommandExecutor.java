package com.softhub.softframework.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

public class AnnotatedCommandExecutor implements CommandExecutor {
    private Object handler;
    private Method method;
    private Command commandAnnotation;

    public AnnotatedCommandExecutor(Object handler, Method method) {
        this.handler = handler;
        this.method = method;
        this.commandAnnotation = method.getAnnotation(Command.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        try {
            if (commandAnnotation.isOp() && !sender.isOp()) {
                sender.sendMessage("You do not have permission to execute this command.");
                return true;
            }

            if (!commandAnnotation.permission().isEmpty() && !sender.hasPermission(commandAnnotation.permission())) {
                sender.sendMessage("You do not have the required permission: " + commandAnnotation.permission());
                return true;
            }

            return (Boolean) method.invoke(handler, sender, args);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("Error executing command: " + e.getMessage());
            return false;
        }
    }
}
