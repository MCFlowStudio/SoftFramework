package com.softhub.softframework.inventory;

import org.bukkit.entity.Player;

public interface SimpleInventoryProvider {
    void init(Player player);
    void onClick(SimpleClickEvent event);
    void onClose(SimpleCloseEvent event);
}
