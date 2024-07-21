package com.softhub.softframework.config.convert;

import com.softhub.softframework.BukkitInitializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MessageComponent {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component formatMessage(FileConfiguration config, String key, Object... args) {
        Object rawMessage = config.get("messages." + key);

        String message = "알 수 없는 메시지";
        if (rawMessage instanceof List) {
            List<String> messages = (List<String>) rawMessage;
            message = String.join("\n", messages);
        } else if (rawMessage instanceof String) {
            message = (String) rawMessage;
        }

        for (int i = 0; i < args.length; i++) {
            String replacement;
            if (args[i] instanceof Integer) {
                replacement = NumberFormat.getNumberInstance(Locale.getDefault()).format(args[i]);
            } else if (args[i] instanceof Double) {
                replacement = new DecimalFormat("#,###.##").format(args[i]);
            } else {
                replacement = args[i].toString();
            }
            message = message.replace("%" + (i + 1) + "%", replacement);
        }

        Component component = miniMessage.deserialize(message);

        String serializedMessage = LegacyComponentSerializer.legacySection().serialize(component);
        return component;
    }

}
