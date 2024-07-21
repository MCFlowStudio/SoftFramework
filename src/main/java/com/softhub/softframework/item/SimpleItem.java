package com.softhub.softframework.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SimpleItem extends ItemStack {

    public SimpleItem(Material material) {
        super(material);
    }

    public SimpleItem(ItemStack stack) {
        super(stack);
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

    public ItemStack build() {
        return this;
    }
}
