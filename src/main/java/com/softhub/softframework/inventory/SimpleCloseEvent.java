package com.softhub.softframework.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class SimpleCloseEvent {
    private final InventoryCloseEvent event;
    private final Player player;
    private final SimpleInventory simpleInventory;
    private final Inventory inventory;

    public SimpleCloseEvent(InventoryCloseEvent event, Player player, SimpleInventory simpleInventory, Inventory inventory) {
        this.event = event;
        this.player = player;
        this.simpleInventory = simpleInventory;
        this.inventory = inventory;
    }

    public InventoryCloseEvent getEvent() {
        return event;
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
