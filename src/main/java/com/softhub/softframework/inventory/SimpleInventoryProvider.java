package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface SimpleInventoryProvider {
    void init(Player player);
    void onClick(SimpleClickEvent event);
    void onClose(SimpleCloseEvent event);
}
