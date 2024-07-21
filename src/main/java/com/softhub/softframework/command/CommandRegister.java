package com.softhub.softframework.command;

import com.softhub.softframework.BukkitInitializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandRegister {

    private static JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BukkitInitializer.class);

    public static void registerCommands() {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Failed to access CommandMap.");
            return;
        }

        for (Class<?> cls : plugin.getClass().getDeclaredClasses()) {
            if (cls.isAnnotationPresent(Command.class)) {
                Command commandAnnotation = cls.getAnnotation(Command.class);
                PluginCommand mainCommand = createPluginCommand(commandAnnotation.name(), plugin);
                if (mainCommand != null) {
                    mainCommand.setDescription(commandAnnotation.description());
                    mainCommand.setUsage(commandAnnotation.usage());
                    mainCommand.setPermission(commandAnnotation.permission());
                    mainCommand.setAliases(Arrays.asList(commandAnnotation.aliases()));

                    for (Method method : cls.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(CommandExecutor.class)) {
                            CommandExecutor executorAnnotation = method.getAnnotation(CommandExecutor.class);
                            mainCommand.setExecutor((sender, cmd, label, args) -> {
                                if (args.length > 0 && args[0].equalsIgnoreCase(executorAnnotation.label())) {
                                    if (executorAnnotation.isOp() && !sender.isOp()) {
                                        sender.sendMessage("You do not have the necessary permissions.");
                                        return true;
                                    }
                                    if (!executorAnnotation.permission().isEmpty() && !sender.hasPermission(executorAnnotation.permission())) {
                                        sender.sendMessage("You do not have the necessary permissions: " + executorAnnotation.permission());
                                        return true;
                                    }
                                    try {
                                        return (Boolean) method.invoke(cls.newInstance(), sender, args);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sender.sendMessage("Error executing command: " + e.getMessage());
                                        return false;
                                    }
                                }
                                return false;
                            });
                        }
                    }
                    commandMap.register(plugin.getName(), mainCommand);
                }
            }
        }
    }

    public static void registerCommands(Object commandInstance) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Failed to access CommandMap.");
            return;
        }

        Class<?> cls = commandInstance.getClass();

        if (cls.isAnnotationPresent(Command.class)) {
            Command commandAnnotation = cls.getAnnotation(Command.class);
            PluginCommand mainCommand = createPluginCommand(commandAnnotation.name(), plugin);
            if (mainCommand != null) {
                mainCommand.setDescription(commandAnnotation.description());
                mainCommand.setUsage(commandAnnotation.usage());
                mainCommand.setPermission(commandAnnotation.permission());
                mainCommand.setAliases(Arrays.asList(commandAnnotation.aliases()));

                List<String> subCommandLabels = new ArrayList<>();
                final Method[] helpMethod = {null};

                for (Method method : cls.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(CommandHelp.class)) {
                        helpMethod[0] = method;
                    }
                    if (method.isAnnotationPresent(CommandExecutor.class)) {
                        CommandExecutor executorAnnotation = method.getAnnotation(CommandExecutor.class);
                        if (!executorAnnotation.hideSuggestion()) {
                            subCommandLabels.add(executorAnnotation.label());
                        }

                        mainCommand.setExecutor((sender, cmd, label, args) -> {
                            if (args.length == 0) {
                                if (helpMethod[0] != null) {
                                    try {
                                        helpMethod[0].invoke(commandInstance, sender);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sender.sendMessage("Error executing help command: " + e.getMessage());
                                    }
                                } else {
                                    sender.sendMessage("§e명령어 도움말:");
                                    for (String subLabel : subCommandLabels) {
                                        sender.sendMessage("§6/" + label + " " + subLabel);
                                    }
                                }
                                return true;
                            }

                            if (args.length > 0) {
                                boolean executed = false;
                                for (Method execMethod : cls.getDeclaredMethods()) {
                                    if (execMethod.isAnnotationPresent(CommandExecutor.class)) {
                                        CommandExecutor execAnnotation = execMethod.getAnnotation(CommandExecutor.class);
                                        if (args[0].equalsIgnoreCase(execAnnotation.label())) {
                                            if (execAnnotation.isOp() && !sender.isOp()) {
                                                sender.sendMessage("§c권한이 부족합니다.");
                                                return true;
                                            }
                                            if (!execAnnotation.permission().isEmpty() && !sender.hasPermission(execAnnotation.permission())) {
                                                sender.sendMessage("§c권한이 부족합니다: " + execAnnotation.permission());
                                                return true;
                                            }
                                            try {
                                                execMethod.invoke(commandInstance, sender, Arrays.copyOfRange(args, 1, args.length));
                                                executed = true;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                sender.sendMessage("Error executing command: " + e.getMessage());
                                                return false;
                                            }
                                        }
                                    }
                                }
                                if (!executed) {
                                    if (helpMethod[0] != null) {
                                        try {
                                            helpMethod[0].invoke(commandInstance, sender);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            sender.sendMessage("Error executing help command: " + e.getMessage());
                                        }
                                    } else {
                                        sender.sendMessage("§e명령어 도움말:");
                                        for (String subLabel : subCommandLabels) {
                                            sender.sendMessage("§6/" + label + " " + subLabel);
                                        }
                                    }
                                }
                                return true;
                            }
                            return false;
                        });
                    }
                }

                mainCommand.setTabCompleter((sender, command, alias, args) -> {
                    if (args.length == 1) {
                        return subCommandLabels.stream()
                                .filter(lbl -> lbl.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    return null;
                });

                commandMap.register(plugin.getName(), mainCommand);
                for (String alias : commandAnnotation.aliases()) {
                    PluginCommand aliasCommand = createPluginCommand(alias, plugin);
                    if (aliasCommand != null) {
                        aliasCommand.setDescription(commandAnnotation.description());
                        aliasCommand.setUsage(commandAnnotation.usage());
                        aliasCommand.setPermission(commandAnnotation.permission());
                        aliasCommand.setExecutor(mainCommand.getExecutor());
                        aliasCommand.setTabCompleter(mainCommand.getTabCompleter());
                        commandMap.register(plugin.getName(), aliasCommand);
                    }
                }
            } else {
                System.out.println("Failed to create main command: " + commandAnnotation.name());
            }
        } else {
            System.out.println("Class does not have @Command annotation: " + cls.getName());
        }
    }

    private static CommandMap getCommandMap() {
        try {
            final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            return (CommandMap) f.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PluginCommand createPluginCommand(String name, JavaPlugin plugin) {
        try {

            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            PluginCommand pluginCommand = c.newInstance(name, plugin);
            commandMap.register(name, pluginCommand);
            return pluginCommand;

//            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
//            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, JavaPlugin.class);
//            constructor.setAccessible(true);
//            return constructor.newInstance(name, plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
