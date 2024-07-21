package com.softhub.softframework.task;

import com.softhub.softframework.BukkitInitializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

public class SimpleAsync {

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitInitializer.getInstance(), runnable);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitInitializer.getInstance(), runnable);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitInitializer.getInstance(), runnable);
    }

    public static BukkitTask asyncLater(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitInitializer.getInstance(), runnable, ticks);
    }

    public static BukkitTask syncLater(Runnable runnable, long ticks) {
        return Bukkit.getScheduler().runTaskLater(BukkitInitializer.getInstance(), runnable, ticks);
    }

}
