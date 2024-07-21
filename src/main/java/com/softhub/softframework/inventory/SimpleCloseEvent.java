package com.softhub.softframework.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SimpleCloseEvent {
    private final Player player;
    private final SimpleInventory simpleInventory;
    private final Inventory inventory;

    public SimpleCloseEvent(Player player, SimpleInventory simpleInventory, Inventory inventory) {
        this.player = player;
        this.simpleInventory = simpleInventory;
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public SimpleInventory getSimpleInventory() {
        return simpleInventory;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
