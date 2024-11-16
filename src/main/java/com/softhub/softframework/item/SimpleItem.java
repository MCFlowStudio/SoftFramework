package com.softhub.softframework.item;

import com.softhub.softframework.BukkitFrameworkPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimpleItem extends ItemStack implements Serializable {

    private static final long serialVersionUID = 1L;

    public SimpleItem(Material material) {
        super(material);
    }

    public SimpleItem(ItemStack stack) {
        super(stack);
    }

    public SimpleItem() {
        super(Material.AIR);
    }

    public void setName(String name) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            this.setItemMeta(meta);
        }
    }

    public String getName() {
        ItemMeta meta = this.getItemMeta();
        return meta != null ? meta.getDisplayName() : null;
    }

    public void setLore(List<String> lore) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            this.setItemMeta(meta);
        }
    }

    public List<String> getLore() {
        ItemMeta meta = this.getItemMeta();
        return meta != null ? meta.getLore() : null;
    }

    public void addLore(String line) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.setLore(lore);
            this.setItemMeta(meta);
        }

    }

    public void addLore(Component line) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            List<Component> loreComponents = meta.lore();

            loreComponents.add(line);

            List<Component> lores = new ArrayList<>();
            for (Component component : loreComponents) {
                lores.add(component.decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lores);
            this.setItemMeta(meta);
        }
    }

    public void removeLore(int index) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() > index) {
                lore.remove(index);
                meta.setLore(lore);
                this.setItemMeta(meta);
            }
        }
    }

    public void clearLore() {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            meta.setLore(new ArrayList<>());
            this.setItemMeta(meta);
        }
    }

    public void setCustomModelData(Integer data) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(data);
            this.setItemMeta(meta);
        }
    }

    public Integer getCustomModelData() {
        ItemMeta meta = this.getItemMeta();
        return meta != null ? meta.getCustomModelData() : null;
    }

    public <T, Z> void setPersistentData(String key, PersistentDataType<T, Z> type, Z value) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(BukkitFrameworkPlugin.getInstance(), key);
            container.set(namespacedKey, type, value);
            this.setItemMeta(meta);
        }
    }

    public <T, Z> Z getPersistentData(String key, PersistentDataType<T, Z> type) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(BukkitFrameworkPlugin.getInstance(), key);
            return container.get(namespacedKey, type);
        }
        return null;
    }

    public void removePersistentData(String key) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(BukkitFrameworkPlugin.getInstance(), key);
            container.remove(namespacedKey);
            this.setItemMeta(meta);
        }
    }

    public <T, Z> boolean hasPersistentData(String key, PersistentDataType<T, Z> type) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(BukkitFrameworkPlugin.getInstance(), key);
            return container.has(namespacedKey, type);
        }
        return false;
    }

    public ItemStack build() {
        return this;
    }
}
