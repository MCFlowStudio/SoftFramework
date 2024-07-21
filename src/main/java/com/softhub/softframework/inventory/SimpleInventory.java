package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleInventory implements InventoryHolder, Listener {

    private final Inventory inventory;
    private SimpleInventoryProvider provider;

    public SimpleInventory(String inventoryName, int size) {
        this.inventory = Bukkit.createInventory(this, size, inventoryName);
    }

    public SimpleInventory(Component inventoryName, int size) {
        this.inventory = Bukkit.createInventory(this, size, inventoryName);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void register(SimpleInventoryProvider provider) {
        setProvider(provider);
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getProvidingPlugin(getClass()));
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

    public void setItem(int slot, SimpleItem item) {
        this.inventory.setItem(slot, item.build());
    }

    public void update() {
        // 아이템 재정렬 및 인벤토리 업데이트 로직 추가
    }
}
