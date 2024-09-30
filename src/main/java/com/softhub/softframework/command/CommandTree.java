package com.softhub.softframework.command;

import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.config.convert.MessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.softhub.softframework.command.CommandRegister.registeredCommands;

public class CommandTree {

    private final Object commandInstance;
    private final Class<?> cls;
    private final Command commandAnnotation;
    private final Plugin plugin;
    private final List<String> subCommandLabels = new ArrayList<>();
    private final List<String> opSubCommandLabels = new ArrayList<>();
    private final Method[] helpMethod = {null};
    private final TabCompleterProvider tabCompleterProvider;

    public CommandTree(Object commandInstance, Plugin plugin) {
        this.commandInstance = commandInstance;
        this.cls = commandInstance.getClass();
        this.plugin = plugin;
        this.commandAnnotation = cls.getAnnotation(Command.class);

        if (TabCompleterProvider.class.isAssignableFrom(cls)) {
            this.tabCompleterProvider = (TabCompleterProvider) commandInstance;
        } else {
            this.tabCompleterProvider = null;
        }

        init();
    }

    private void init() {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandHelp.class)) {
                helpMethod[0] = method;
            }
            if (method.isAnnotationPresent(CommandExecutor.class)) {
                CommandExecutor executorAnnotation = method.getAnnotation(CommandExecutor.class);
                subCommandLabels.add(executorAnnotation.label());
                if (executorAnnotation.isOp()) {
                    opSubCommandLabels.add(executorAnnotation.label());
                }
            }
        }
    }

    public void setupMainCommand(PluginCommand mainCommand) {
        mainCommand.setDescription(commandAnnotation.description());
        mainCommand.setUsage(commandAnnotation.usage());
        mainCommand.setPermission(commandAnnotation.permission());
        mainCommand.setAliases(Arrays.asList(commandAnnotation.aliases()));
        mainCommand.setExecutor(this::executeCommand);
        mainCommand.setTabCompleter(this::getTabCompletions);
    }

    private boolean executeCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!registeredCommands.containsKey(commandAnnotation.name())) {
            sender.sendMessage("§c명령어가 등록되지 않았습니다.");
            return true;
        }

        if (args.length == 0 && helpMethod[0] != null) {
            try {
                if (helpMethod[0].getParameterTypes()[0].equals(Player.class)) {
                    if (sender instanceof Player) {
                        helpMethod[0].invoke(commandInstance, (Player) sender);
                    } else {
                        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_console"));
                    }
                } else {
                    helpMethod[0].invoke(commandInstance, sender);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("Error executing help command: " + e.getMessage());
            }
            return true;
        }

        if (args.length == 0) {
            sendCommandHelp(sender, label);
            return true;
        }

        if (args.length > 0) {
            return handleSubCommandExecution(sender, label, args);
        }

        return false;
    }

    private void sendCommandHelp(CommandSender sender, String label) {
        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "command_help"));
        Set<String> displayedCommands = new HashSet<>();

        for (Method m : cls.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CommandExecutor.class)) {
                CommandExecutor execAnnotation = m.getAnnotation(CommandExecutor.class);
                String subLabel = execAnnotation.label();

                if (execAnnotation.isOp() && !sender.isOp()) {
                    continue;
                }

                if (!execAnnotation.permission().isEmpty() && !sender.hasPermission(execAnnotation.permission())) {
                    continue;
                }

                if (!displayedCommands.add(subLabel) || (!sender.isOp() && execAnnotation.hideSuggestion()))
                    continue;

                StringBuilder usage = new StringBuilder("/" + label + " " + subLabel);
                boolean hasParameters = false;

                for (Parameter parameter : m.getParameters()) {
                    if (parameter.isAnnotationPresent(CommandParameter.class)) {
                        hasParameters = true;
                        CommandParameter paramAnnotation = parameter.getAnnotation(CommandParameter.class);
                        usage.append(paramAnnotation.required() ? " <" : " [")
                                .append(paramAnnotation.name())
                                .append(paramAnnotation.required() ? ">" : "]");
                    }
                }

                usage.append(" <gray>").append(execAnnotation.description());
                Component message = MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "command_arg_help", usage.toString());

                if (hasParameters) {
                    message = message.clickEvent(ClickEvent.suggestCommand("/" + label + " " + subLabel));
                } else {
                    message = message.clickEvent(ClickEvent.runCommand("/" + label + " " + subLabel));
                }

                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(message);
                } else {
                    sender.sendMessage(message);
                }
            }
        }
    }

    private boolean handleSubCommandExecution(CommandSender sender, String label, String[] args) {
        boolean executed = false;
        for (Method execMethod : cls.getDeclaredMethods()) {
            if (execMethod.isAnnotationPresent(CommandExecutor.class)) {
                CommandExecutor execAnnotation = execMethod.getAnnotation(CommandExecutor.class);
                if (args[0].equalsIgnoreCase(execAnnotation.label())) {
                    if (execAnnotation.isOp() && !sender.isOp()) {
                        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_permission"));
                        return true;
                    }
                    if (!execAnnotation.permission().isEmpty() && !sender.hasPermission(execAnnotation.permission())) {
                        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_permission"));
                        return true;
                    }
                    if (!execAnnotation.consoleAvailable() && !(sender instanceof Player)) {
                        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_console"));
                        return true;
                    }
                    try {
                        Parameter[] parameters = execMethod.getParameters();
                        Object[] invokeArgs = new Object[parameters.length];

                        if (parameters[0].equals(Player.class) && sender instanceof Player) {
                            invokeArgs[0] = sender;
                        } else if (!execAnnotation.consoleAvailable() && !(sender instanceof Player)) {
                            sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_console"));
                            return true;
                        } else {
                            invokeArgs[0] = sender;
                        }

                        int requiredArgs = 1;
                        for (int i = 1; i < parameters.length; i++) {
                            Parameter parameter = parameters[i];
                            if (parameter.isAnnotationPresent(CommandParameter.class)) {
                                CommandParameter paramAnnotation = parameter.getAnnotation(CommandParameter.class);
                                int index = paramAnnotation.index();
                                if (paramAnnotation.required()) {
                                    requiredArgs++;
                                }
                                if (index < args.length) {
                                    String paramValue = args[index];
                                    invokeArgs[i] = convertParameterType(paramValue, parameter.getType());
                                } else if (paramAnnotation.required()) {
                                    sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_args", paramAnnotation.name()));
                                    return true;
                                }
                            } else {
                                invokeArgs[i] = getDefault(parameter.getType());
                            }
                        }
                        if (args.length < requiredArgs) {
                            sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_arg"));
                            return true;
                        }
                        execMethod.invoke(commandInstance, invokeArgs);
                        executed = true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageComponent.formatMessage(BukkitInitializer.getInstance().getConfig(), "no_format"));
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
            sendCommandHelp(sender, label);
        }
        return true;
    }



    private Object convertParameterType(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == String.class) {
            return value;
        }
        return null;
    }

    private Object getDefault(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0;
        }
        return null;
    }

    private List<String> getTabCompletions(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length > 0) {
            int argIndex = args.length - 1;
            List<String> completions = new ArrayList<>();

            if (args.length == 1) {
                List<String> labels = subCommandLabels.stream()
                        .filter(lbl -> {
                            for (Method method : cls.getDeclaredMethods()) {
                                if (method.isAnnotationPresent(CommandExecutor.class)) {
                                    CommandExecutor execAnnotation = method.getAnnotation(CommandExecutor.class);
                                    if (execAnnotation.label().equalsIgnoreCase(lbl)) {
                                        if (execAnnotation.isOp() && !sender.isOp()) {
                                            return false;
                                        }
                                        if (!execAnnotation.permission().isEmpty() && !sender.hasPermission(execAnnotation.permission())) {
                                            return false;
                                        }
                                        if (!sender.isOp() && execAnnotation.hideSuggestion())
                                            return false;
                                    }
                                }
                            }
                            return true;
                        })
                        .collect(Collectors.toList());

                return labels.stream()
                        .filter(lbl -> lbl.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (tabCompleterProvider != null) {
                completions.addAll(tabCompleterProvider.getCompletions(args[0], argIndex, args, sender));
            } else {
                for (Method method : cls.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(CommandExecutor.class)) {
                        CommandExecutor execAnnotation = method.getAnnotation(CommandExecutor.class);
                        if (execAnnotation.label().equalsIgnoreCase(args[0])) {
                            if (execAnnotation.isOp() && !sender.isOp()) {
                                continue;
                            }

                            if (!execAnnotation.permission().isEmpty() && !sender.hasPermission(execAnnotation.permission())) {
                                continue;
                            }

                            Parameter[] parameters = method.getParameters();
                            if (argIndex < parameters.length) {
                                return Bukkit.getOnlinePlayers().stream()
                                        .map(Player::getName)
                                        .collect(Collectors.toList());
                            }
                        }
                    }
                }
            }

            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[argIndex].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
