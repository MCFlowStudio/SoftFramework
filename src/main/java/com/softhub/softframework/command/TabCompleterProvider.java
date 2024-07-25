package com.softhub.softframework.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface TabCompleterProvider {
    List<String> getCompletions(String commandLabel, int argIndex, String[] args, CommandSender sender);
}

