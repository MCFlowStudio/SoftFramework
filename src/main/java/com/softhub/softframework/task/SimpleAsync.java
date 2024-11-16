package com.softhub.softframework.task;

import com.softhub.softframework.BukkitFrameworkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

public class SimpleAsync {

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitFrameworkPlugin.getInstance(), runnable);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitFrameworkPlugin.getInstance(), runnable);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitFrameworkPlugin.getInstance(), runnable);
    }

    public static BukkitTask asyncLater(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitFrameworkPlugin.getInstance(), runnable, ticks);
    }

    public static BukkitTask syncLater(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLater(BukkitFrameworkPlugin.getInstance(), runnable, ticks);
    }

}
