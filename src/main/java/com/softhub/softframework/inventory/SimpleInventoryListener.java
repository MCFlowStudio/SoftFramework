package com.softhub.softframework.inventory;

import com.softhub.softframework.item.SimpleItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class SimpleInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SimpleInventory) {
            SimpleInventory simpleInventory = (SimpleInventory) event.getInventory().getHolder();
            SimpleInventoryProvider provider = simpleInventory.getProvider();

            if (provider != null) {
                ItemStack clickedItem = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();

                if (event.getClick().isKeyboardClick() && event.getHotbarButton() != -1) {
                    Player player = (Player) event.getWhoClicked();
                    ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                    if (hotbarItem != null) {
                        clickedItem = hotbarItem;
                    }
                }

                SimpleClickEvent clickEvent = new SimpleClickEvent(
                        (Player) event.getWhoClicked(),
                        event.getSlot(),
                        clickedItem != null ? new SimpleItem(clickedItem) : new SimpleItem(Material.AIR),
                        cursorItem != null ? new SimpleItem(cursorItem) : new SimpleItem(Material.AIR),
                        simpleInventory,
                        event.getInventory()
                );
                provider.onClick(clickEvent);
                if (clickEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SimpleInventory) {
            SimpleInventory simpleInventory = (SimpleInventory) event.getInventory().getHolder();
            SimpleInventoryProvider provider = simpleInventory.getProvider();

            if (provider != null) {
                SimpleCloseEvent closeEvent = new SimpleCloseEvent(
                        (Player) event.getPlayer(),
                        simpleInventory,
                        event.getInventory()
                );
                provider.onClose(closeEvent);
            }
        }
    }

}
