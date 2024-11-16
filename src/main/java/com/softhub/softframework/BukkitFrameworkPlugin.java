package com.softhub.softframework;

import com.google.gson.Gson;
import com.softhub.softframework.command.CommandRegister;
import com.softhub.softframework.command.defaultcommand.SoftCommand;
import com.softhub.softframework.config.MysqlConfig;
import com.softhub.softframework.config.RedisConfig;
import com.softhub.softframework.database.DatabaseManager;
import com.softhub.softframework.inventory.SimpleInventoryListener;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitFrameworkPlugin extends JavaPlugin {

    @Getter
    private static BukkitFrameworkPlugin instance;
    @Getter
    private static Gson gson = new Gson();
    @Getter
    private static MiniMessage miniMessage = MiniMessage.miniMessage();
    @Getter
    @Setter
    private static DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        MysqlConfig.init();
        RedisConfig.init();
        getServer().getPluginManager().registerEvents(new SimpleInventoryListener(), this);
        CommandRegister.registerCommands(new SoftCommand());

        getLogger().info("SoftFramework가 활성화 되었습니다.");
    }

    @Override
    public void onDisable() {

    }
}
