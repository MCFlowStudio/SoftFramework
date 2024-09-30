package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleInventory implements InventoryHolder, Listener {

    private Inventory inventory;
    private SimpleInventoryProvider provider;

    public SimpleInventory(String inventoryName, int size) {
        this.inventory = Bukkit.createInventory(this, size, inventoryName);
    }

    public SimpleInventory(Component inventoryName, int size) {
        this.inventory = Bukkit.createInventory(this, size, inventoryName);
    }

    public SimpleInventory(Component inventoryName, InventoryType type) {
        this.inventory = Bukkit.createInventory(this, type, inventoryName);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory sourceInventory) {
        if (sourceInventory == null || this.inventory == null) {
            return;
        }

        int size = Math.min(sourceInventory.getSize(), this.inventory.getSize());

        for (int i = 0; i < size; i++) {
            ItemStack item = sourceInventory.getItem(i);
            if (item != null) {
                this.inventory.setItem(i, item);
            } else {
                this.inventory.setItem(i, null);
            }
        }
    }

    public void register(SimpleInventoryProvider provider) {
        setProvider(provider);
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getProvidingPlugin(getClass()));
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    public void setProvider(SimpleInventoryProvider provider) {
        this.provider = provider;
    }

    public SimpleInventoryProvider getProvider() {
        return provider;
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    public void setItem(int slot, ItemStack item) {
        this.inventory.setItem(slot, item);
    }

    public void setItem(ItemStack item, int... slots) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

}
