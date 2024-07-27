package com.softhub.softframework.config.convert;

import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.item.SimpleItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemConponent {

    public static SimpleItem loadItem(FileConfiguration config, String key, Object... args) {
        String path = "items." + key + ".";

        Material type = Material.getMaterial(config.getString(path + "type", "AIR"));
        int amount = config.getInt(path + "amount", 1);
        ItemStack item = new ItemStack(type, amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = config.getString(path + "name", "");
            for (int i = 0; i < args.length; i++) {
                name = name.replace("%" + (i + 1) + "%", args[i] != null ? args[i].toString() : "");
            }
            meta.displayName(BukkitInitializer.getMiniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));

            List<String> loreLines = config.getStringList(path + "lore");
            List<Component> lore = loreLines.stream()
                    .map(line -> {
                        for (int i = 0; i < args.length; i++) {
                            line = line.replace("%" + (i + 1) + "%", args[i] != null ? args[i].toString() : "");
                        }
                        return BukkitInitializer.getMiniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false);
                    })
                    .collect(Collectors.toList());

            if (!lore.isEmpty()) {
                meta.lore(lore);
            }

            if (config.contains(path + "customModelData")) {
                meta.setCustomModelData(config.getInt(path + "customModelData"));
            }

            if (config.contains(path + "enchantments")) {
                config.getConfigurationSection(path + "enchantments").getKeys(false).forEach(enchantKey -> {
                    Enchantment enchantment = Enchantment.getByName(enchantKey);
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, config.getInt(path + "enchantments." + enchantKey), true);
                    }
                });
            }

            item.setItemMeta(meta);
        }
        return new SimpleItem(item);
    }

    public static SimpleItem updateItem(FileConfiguration config, ItemStack originalItem, String key, Object... args) {
        String path = "items." + key + ".";

        Material type = Material.getMaterial(config.getString(path + "type", "AIR"));
        int amount = config.getInt(path + "amount", 1);
        ItemStack item = new ItemStack(type, amount);

        ItemMeta meta = item.getItemMeta();
        ItemMeta originalMeta = originalItem.getItemMeta();
        if (meta != null && originalMeta != null) {
            String name = config.getString(path + "name", "");
            for (int i = 0; i < args.length; i++) {
                name = name.replace("%" + (i + 1) + "%", args[i] != null ? args[i].toString() : "");
            }
            originalMeta.displayName(BukkitInitializer.getMiniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));

            List<String> loreLines = config.getStringList(path + "lore");
            List<Component> lore = loreLines.stream()
                    .map(line -> {
                        for (int i = 0; i < args.length; i++) {
                            line = line.replace("%" + (i + 1) + "%", args[i] != null ? args[i].toString() : "");
                        }
                        return BukkitInitializer.getMiniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false);
                    })
                    .collect(Collectors.toList());

            if (!lore.isEmpty()) {
                originalMeta.lore(lore);
            }

            if (config.contains(path + "customModelData")) {
                originalMeta.setCustomModelData(config.getInt(path + "customModelData"));
            }

            if (config.contains(path + "enchantments")) {
                config.getConfigurationSection(path + "enchantments").getKeys(false).forEach(enchantKey -> {
                    Enchantment enchantment = Enchantment.getByName(enchantKey);
                    if (enchantment != null) {
                        originalMeta.addEnchant(enchantment, config.getInt(path + "enchantments." + enchantKey), true);
                    }
                });
            }

            originalItem.setItemMeta(originalMeta);
        }
        return new SimpleItem(originalItem);
    }

}
