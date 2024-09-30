package com.softhub.softframework.inventory;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SimpleInventoryTitle {

    private static HashMap<UUID, InventoryPlayer> inventoryPlayers = new HashMap<>();

    private static ProtocolManager protocolManager;
    private JavaPlugin plugin;

    public SimpleInventoryTitle(JavaPlugin plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
    }

    public void registerPacketListeners() {
        protocolManager.addPacketListener(getOpenWindowPacketListener());
        protocolManager.addPacketListener(getCloseWindowPacketListener());
    }

    private PacketListener getOpenWindowPacketListener() {
        return new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final UUID uuid = event.getPlayer().getUniqueId();

                final int windowId = event.getPacket().getIntegers().read(0);
                final Object containerType = event.getPacket().getStructures().readSafely(0);

                InventoryPlayer player = new InventoryPlayer(windowId, containerType);
                inventoryPlayers.put(uuid, player);
            }
        };
    }

    private PacketListener getCloseWindowPacketListener() {
        return new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Client.CLOSE_WINDOW) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                final UUID uuid = event.getPlayer().getUniqueId();

                inventoryPlayers.remove(uuid);
            }
        };
    }

    // -- API --

    public static void sendInventoryTitle(Player player, TextComponent title) {

        List<BaseComponent> components = new ArrayList<>();
        components.add(title);
        List<BaseComponent> list = Arrays.asList(title);
        final InventoryType type = player.getOpenInventory().getType();
        if (type == InventoryType.CRAFTING || type == InventoryType.CREATIVE)
            return;

        InventoryPlayer inventoryPlayer = inventoryPlayers.getOrDefault(player.getUniqueId(), null);

        if (inventoryPlayer == null)
            return;

        final int windowId = inventoryPlayer.getWindowId();
        if (windowId == 0)
            return;

        final Object windowType = inventoryPlayer.getContainerType();
        final String titleJson = ComponentSerializer.toString(list);

        sendOpenScreenPacket(player, windowId, windowType, titleJson);
        player.updateInventory();
    }



    // -- Utility --

    private static void sendOpenScreenPacket(Player player, int windowId, Object windowType, String titleJson) {
        final WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromJson(titleJson);

        PacketContainer openScreen = new PacketContainer(PacketType.Play.Server.OPEN_WINDOW);
        openScreen.getIntegers().write(0, windowId);
        openScreen.getStructures().write(0, (InternalStructure) windowType);
        openScreen.getChatComponents().write(0, wrappedChatComponent);

        protocolManager.sendServerPacket(player, openScreen);
    }

    class InventoryPlayer {
        private int windowId;
        private Object containerType;

        public InventoryPlayer(int windowId, Object containerType) {
            this.windowId = windowId;
            this.containerType = containerType;
        }

        public int getWindowId() {
            return windowId;
        }

        public Object getContainerType() {
            return containerType;
        }

    }

}
