package com.softhub.softframework.command.defaultcommand;

import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.command.Command;
import com.softhub.softframework.command.CommandExecutor;
import com.softhub.softframework.command.CommandParameter;
import com.softhub.softframework.config.MysqlConfig;
import com.softhub.softframework.config.RedisConfig;
import com.softhub.softframework.database.mysql.MysqlConnection;
import com.softhub.softframework.database.mysql.MysqlManager;
import com.softhub.softframework.database.redis.RedisManager;
import com.softhub.softframework.task.SimpleTask;
import org.bukkit.command.CommandSender;

import java.util.UUID;

@Command(name = "soft", aliases = "softframework", description = "프레임워크를 관리하는 명령어입니다.", isOp = true)
public class SoftCommand {

    @CommandExecutor(label = "리로드", description = "콘피그를 다시 불러옵니다.", isOp = true)
    public boolean onReload(CommandSender sender, String[] args) {
        BukkitInitializer.getInstance().reloadConfig();
        if (MysqlConfig.isEnabled()) {
            MysqlConnection.closePool();
            MysqlConfig.init();
        }
        if (RedisConfig.isEnabled()) {
            RedisManager.getInstance().shutdown();
            RedisConfig.init();
        }
        sender.sendMessage("콘피그를 리로드하였습니다.");
        return true;
    }

}

