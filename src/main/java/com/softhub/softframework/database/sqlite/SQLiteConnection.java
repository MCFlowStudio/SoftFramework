package com.softhub.softframework.database.sqlite;

import com.softhub.softframework.BukkitInitializer;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteConnection {
    private static SQLiteDataSource dataSource;

    public static void initialize() {
        if (dataSource == null) {
            dataSource = new SQLiteDataSource();
            File dataFolder = BukkitInitializer.getInstance().getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "database.db");
            dataSource.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
    }
}
