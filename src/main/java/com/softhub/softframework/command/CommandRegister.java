package com.softhub.softframework.command;

import com.softhub.softframework.BukkitInitializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommandRegister {

    private static JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BukkitInitializer.class);
    public static final Map<String, PluginCommand> registeredCommands = new HashMap<>();

    public static void registerCommands(Object commandInstance) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Failed to access CommandMap.");
            return;
        }

        Class<?> cls = commandInstance.getClass();

        if (cls.isAnnotationPresent(Command.class)) {
            Command commandAnnotation = cls.getAnnotation(Command.class);

            unregisterCommand(commandAnnotation.name());
            for (String alias : commandAnnotation.aliases()) {
                unregisterCommand(alias);
            }

            PluginCommand mainCommand = createPluginCommand(commandAnnotation.name(), plugin);
            if (mainCommand != null) {
                CommandTree commandTree = new CommandTree(commandInstance, plugin);
                commandTree.setupMainCommand(mainCommand);

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
        Class<?> cls = commandInstance.getClass();

        if (cls.isAnnotationPresent(Command.class)) {
            Command commandAnnotation = cls.getAnnotation(Command.class);

            unregisterCommand(commandAnnotation.name());
            for (String alias : commandAnnotation.aliases()) {
                unregisterCommand(alias);
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
            commandMap.register(plugin.getName(), pluginCommand);
            return pluginCommand;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
