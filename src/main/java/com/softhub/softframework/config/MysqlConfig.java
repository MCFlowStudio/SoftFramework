package com.softhub.softframework.config;

import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.database.mysql.MysqlConnection;
import com.softhub.softframework.database.mysql.MysqlManager;
import com.softhub.softframework.database.sqlite.SQLiteConnection;
import com.softhub.softframework.database.sqlite.SQLiteManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class MysqlConfig {

    @Getter
    private static boolean enabled;
    @Getter
    private static String host;
    @Getter
    private static String port;
    @Getter
    private static String username;
    @Getter
    private static String password;
    @Getter
    private static String database;
    @Getter
    private static Integer poolsize;

    public static void init() {
        FileConfiguration config = BukkitInitializer.getInstance().getConfig();
        enabled = config.getBoolean("database.mysql.enabled")? true : false;
        host = config.getString("database.mysql.host", "localhost");
        port = config.getString("database.mysql.port", "3306");
        username = config.getString("database.mysql.username", "root");
        password = config.getString("database.mysql.password", "12345678");
        database = config.getString("database.mysql.database", "softframework");
        poolsize = config.getInt("database.mysql.poolsize", 10);
        if (enabled) {
            MysqlConnection.initialize(host, port, password, username, database, poolsize);
            BukkitInitializer.setDatabaseManager(new MysqlManager());
        } else {
            SQLiteConnection.initialize();
            BukkitInitializer.setDatabaseManager(new SQLiteManager());
        }
    }

}
