package com.softhub.softframework.command;

import com.softhub.softframework.BukkitInitializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegister {

    private static JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BukkitInitializer.class);
    private static final Map<String, PluginCommand> registeredCommands = new HashMap<>();

    public static void registerCommands(Object commandInstance) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Failed to access CommandMap.");
            return;
        }

        Class<?> cls = commandInstance.getClass();

        if (cls.isAnnotationPresent(Command.class)) {
            Command commandAnnotation = cls.getAnnotation(Command.class);

            // 기존 명령어 해제
            unregisterCommand(commandAnnotation.name());
            for (String alias : commandAnnotation.aliases()) {
                unregisterCommand(alias);
            }

            PluginCommand mainCommand = createPluginCommand(commandAnnotation.name(), plugin);
            if (mainCommand != null) {
                mainCommand.setDescription(commandAnnotation.description());
                mainCommand.setUsage(commandAnnotation.usage());
                mainCommand.setPermission(commandAnnotation.permission());
                mainCommand.setAliases(Arrays.asList(commandAnnotation.aliases()));

                List<String> subCommandLabels = new ArrayList<>();
                List<String> opSubCommandLabels = new ArrayList<>();
                final Method[] helpMethod = {null};

                for (Method method : cls.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(CommandHelp.class)) {
                        helpMethod[0] = method;
                    }
                    if (method.isAnnotationPresent(CommandExecutor.class)) {
                        CommandExecutor executorAnnotation = method.getAnnotation(CommandExecutor.class);
                        if (!executorAnnotation.hideSuggestion()) {
                            subCommandLabels.add(executorAnnotation.label());
                            if (executorAnnotation.isOp()) {
                                opSubCommandLabels.add(executorAnnotation.label());
                            }
                        }

                        mainCommand.setExecutor((sender, cmd, label, args) -> {
                            if (!registeredCommands.containsKey(commandAnnotation.name())) {
                                sender.sendMessage("§c명령어가 등록되지 않았습니다.");
                                return true;
                            }

                            if (args.length == 0) {
                                sender.sendMessage("§e명령어 도움말:");
                                for (String subLabel : subCommandLabels) {
                                    for (Method m : cls.getDeclaredMethods()) {
                                        if (m.isAnnotationPresent(CommandExecutor.class) && m.getAnnotation(CommandExecutor.class).label().equals(subLabel)) {
                                            StringBuilder usage = new StringBuilder("§6/" + label + " " + subLabel);

                                            for (Parameter parameter : m.getParameters()) {
                                                if (parameter.isAnnotationPresent(CommandParameter.class)) {
                                                    CommandParameter paramAnnotation = parameter.getAnnotation(CommandParameter.class);
                                                    usage.append(paramAnnotation.required() ? " <" : " [")
                                                            .append(paramAnnotation.name())
                                                            .append(paramAnnotation.required() ? ">" : "]");
                                                }
                                            }
                                            usage.append(" - ").append(m.getAnnotation(CommandExecutor.class).description());
                                            sender.sendMessage(usage.toString());
                                        }
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
                                                Parameter[] parameters = execMethod.getParameters();
                                                Object[] invokeArgs = new Object[parameters.length];
                                                invokeArgs[0] = sender;
                                                int requiredArgs = 0;
                                                for (Parameter parameter : parameters) {
                                                    if (parameter.isAnnotationPresent(CommandParameter.class)) {
                                                        CommandParameter paramAnnotation = parameter.getAnnotation(CommandParameter.class);
                                                        int index = paramAnnotation.index();
                                                        if (paramAnnotation.required()) {
                                                            requiredArgs++;
                                                        }
                                                        if (index < args.length) {
                                                            String paramValue = args[index];
                                                            switch (paramAnnotation.type()) {
                                                                case INTEGER:
                                                                    invokeArgs[index] = Integer.parseInt(paramValue);
                                                                    break;
                                                                case DOUBLE:
                                                                    invokeArgs[index] = Double.parseDouble(paramValue);
                                                                    break;
                                                                case BOOLEAN:
                                                                    invokeArgs[index] = Boolean.parseBoolean(paramValue);
                                                                    break;
                                                                case STRING:
                                                                default:
                                                                    invokeArgs[index] = paramValue;
                                                            }
                                                        } else if (paramAnnotation.required()) {
                                                            sender.sendMessage("§c필수 파라미터가 누락되었습니다: " + paramAnnotation.name());
                                                            return true;
                                                        }
                                                    }
                                                }
                                                if (args.length < requiredArgs) {
                                                    sender.sendMessage("§c필수 파라미터가 누락되었습니다.");
                                                    return true;
                                                }
                                                execMethod.invoke(commandInstance, invokeArgs);
                                                executed = true;
                                            } catch (NumberFormatException e) {
                                                sender.sendMessage("§c잘못된 숫자 형식입니다: " + e.getMessage());
                                                return true;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                sender.sendMessage("Error executing command: " + e.getMessage());
                                                return true;
                                            }
                                        }
                                    }
                                }
                                if (!executed) {
                                    sender.sendMessage("§e명령어 도움말:");
                                    for (String subLabel : subCommandLabels) {
                                        for (Method m : cls.getDeclaredMethods()) {
                                            if (m.isAnnotationPresent(CommandExecutor.class) && m.getAnnotation(CommandExecutor.class).label().equals(subLabel)) {
                                                StringBuilder usage = new StringBuilder("§6/" + label + " " + subLabel);
                                                for (Parameter parameter : m.getParameters()) {
                                                    if (parameter.isAnnotationPresent(CommandParameter.class)) {
                                                        CommandParameter paramAnnotation = parameter.getAnnotation(CommandParameter.class);
                                                        usage.append(paramAnnotation.required() ? " <" : " [")
                                                                .append(paramAnnotation.name())
                                                                .append(paramAnnotation.required() ? ">" : "]");
                                                    }
                                                }
                                                usage.append(" - ").append(m.getAnnotation(CommandExecutor.class).description());
                                                sender.sendMessage(usage.toString());
                                            }
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
                        List<String> labels = sender.isOp() ? subCommandLabels : subCommandLabels.stream().filter(lbl -> !opSubCommandLabels.contains(lbl)).collect(Collectors.toList());
                        return labels.stream()
                                .filter(lbl -> lbl.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    return null;
                });

                commandMap.register(plugin.getName(), mainCommand);
                registeredCommands.put(commandAnnotation.name(), mainCommand);

                for (String alias : commandAnnotation.aliases()) {
                    PluginCommand aliasCommand = createPluginCommand(alias, plugin);
                    if (aliasCommand != null) {
                        aliasCommand.setDescription(commandAnnotation.description());
                        aliasCommand.setUsage(commandAnnotation.usage());
                        aliasCommand.setPermission(commandAnnotation.permission());
                        aliasCommand.setExecutor(mainCommand.getExecutor());
                        aliasCommand.setTabCompleter(mainCommand.getTabCompleter());
                        commandMap.register(plugin.getName(), aliasCommand);
                        registeredCommands.put(alias, aliasCommand);
                    }
                }
            } else {
                System.out.println("Failed to create main command: " + commandAnnotation.name());
            }
        } else {
            System.out.println("Class does not have @Command annotation: " + cls.getName());
        }
    }

    private static void unregisterCommand(String commandName) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            for (org.bukkit.command.Command command : commandMap.getKnownCommands().values()) {
                if (command.getName().equalsIgnoreCase(commandName))
                    command.unregister(commandMap);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregisterCommands(Object commandInstance) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Failed to access CommandMap.");
            return;
        }

        Class<?> cls = commandInstance.getClass();

        if (cls.isAnnotationPresent(Command.class)) {
            Command commandAnnotation = cls.getAnnotation(Command.class);
            String mainCommandName = commandAnnotation.name();

            try {
                Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

                knownCommands.remove(mainCommandName);
                registeredCommands.remove(mainCommandName);

                for (String alias : commandAnnotation.aliases()) {
                    knownCommands.remove(alias);
                    registeredCommands.remove(alias);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
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
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
