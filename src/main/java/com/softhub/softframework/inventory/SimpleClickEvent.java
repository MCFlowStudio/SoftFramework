package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SimpleClickEvent {
    private final Player player;
    private final int slot;
    private final SimpleItem clickItem;
    private final SimpleItem cursorItem;
    private final SimpleInventory simpleInventory;
    private final Inventory inventory;
    private boolean cancelled;

    public SimpleClickEvent(Player player, int slot, SimpleItem clickItem, SimpleItem cursorItem, SimpleInventory simpleInventory, Inventory inventory) {
        this.player = player;
        this.slot = slot;
        this.clickItem = clickItem;
        this.cursorItem = cursorItem;
        this.simpleInventory = simpleInventory;
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public int getSlot() {
        return slot;
    }

    public SimpleItem getClickItem() {
        return clickItem;
    }

    public SimpleItem getCursorItem() {
        return cursorItem;
    }

    public SimpleInventory getSimpleInventory() {
        return simpleInventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
