package com.softhub.softframework.config;

import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.database.redis.RedisManager;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class RedisConfig {

    @Getter
    private static boolean enabled;
    @Getter
    private static String host;
    @Getter
    private static String port;
    @Getter
    private static String password;

    public static void init() {
        FileConfiguration config = BukkitInitializer.getInstance().getConfig();
        enabled = config.getBoolean("database.redis.enabled", false) ? true : false;
        host = config.getString("database.redis.host", "localhost");
        port = config.getString("database.redis.port", "6379");
        password = config.getString("database.redis.password");
        if (enabled) {
            RedisManager.initialize(host, Integer.valueOf(port), password);
            StatefulRedisPubSubConnection<String, String> pubSubConnection = RedisManager.getRedisClient().connectPubSub();
            pubSubConnection.async().subscribe("softframework:protocol_request");
        }
    }

}
