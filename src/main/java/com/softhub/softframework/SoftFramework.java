package com.softhub.softframework;

import com.softhub.softframework.config.MysqlConfig;
import com.softhub.softframework.database.DatabaseManager;

public class SoftFramework {

    public static boolean isMysql() {
        return MysqlConfig.isEnabled();
    }
    public static DatabaseManager getDatabaseManager() {
        return BukkitInitializer.getDatabaseManager();
    }

}
