package com.softhub.softframework;

import com.google.gson.Gson;
import com.softhub.softframework.command.CommandRegister;
import com.softhub.softframework.command.defaultcommand.SoftCommand;
import com.softhub.softframework.config.MysqlConfig;
import com.softhub.softframework.config.RedisConfig;
import com.softhub.softframework.inventory.SimpleInventoryListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitInitializer extends JavaPlugin {

    @Getter
    private static BukkitInitializer instance;
    @Getter
    private static Gson gson = new Gson();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        MysqlConfig.init();
        RedisConfig.init();
        getServer().getPluginManager().registerEvents(new SimpleInventoryListener(), this);
        CommandRegister.registerCommands(new SoftCommand());
    }

    @Override
    public void onDisable() {

    }
}
