package com.softhub.softframework;

import com.softhub.softframework.database.DatabaseManager;

public class SoftFramework {

    public static DatabaseManager getDatabaseManager() {
        return BukkitInitializer.getDatabaseManager();
    }

}
