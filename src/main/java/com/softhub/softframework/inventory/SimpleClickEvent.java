package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class SimpleClickEvent {
    @Getter
    private final Player player;
    @Getter
    private final int slot;
    @Getter
    private final int rawSlot;
    @Getter
    private final SimpleItem clickItem;
    @Getter
    private final SimpleItem cursorItem;
    @Getter
    private final SimpleInventory simpleInventory;
    @Getter
    private final Inventory inventory;
    @Setter
    private boolean cancelled;
    @Getter
    private final ClickType clickType;  // 클릭 타입 추가

    public SimpleClickEvent(Player player, int slot, int rawSlot, SimpleItem clickItem, SimpleItem cursorItem, SimpleInventory simpleInventory, Inventory inventory, ClickType clickType) {
        this.player = player;
        this.slot = slot;
        this.rawSlot = rawSlot;
        this.clickItem = clickItem;
        this.cursorItem = cursorItem;
        this.simpleInventory = simpleInventory;
        this.inventory = inventory;
        this.clickType = clickType;
    }

    public boolean isTopInventory() {
        InventoryView view = player.getOpenInventory();
        int topInventorySize = view.getTopInventory().getSize();
        return rawSlot < topInventorySize;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isShiftClick() {
        return clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT;
    }

    public boolean isLeftClick() {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }

    public boolean isRightClick() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }
}