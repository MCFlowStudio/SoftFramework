package com.softhub.softframework.config.convert;

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

    public static String formatTime(int seconds) {
        int months = seconds / 2592000;
        int weeks = (seconds % 2592000) / 604800;
        int days = (seconds % 604800) / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        StringBuilder timeString = new StringBuilder();

        if (months > 0) {
            timeString.append(months).append("달 ");
        }
        if (weeks > 0) {
            timeString.append(weeks).append("주 ");
        }
        if (days > 0) {
            timeString.append(days).append("일 ");
        }
        if (hours > 0) {
            timeString.append(hours).append("시간 ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append("분 ");
        }
        if (secs > 0 || timeString.length() == 0) {
            timeString.append(secs).append("초");
        }

        return timeString.toString().trim();
    }


}
