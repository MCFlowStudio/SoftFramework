package com.softhub.softframework.task;

import com.softhub.softframework.BukkitFrameworkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleTask {

    private static ConcurrentMap<String, BukkitTask> tasks = new ConcurrentHashMap<>();

    public static BukkitTask asyncRepeating(String taskId, Runnable runnable, long delay, long period) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitFrameworkPlugin.getInstance(), runnable, delay, period);
        tasks.put(taskId, task);
        return task;
    }

    public static BukkitTask syncRepeating(String taskId, Runnable runnable, long delay, long period) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(BukkitFrameworkPlugin.getInstance(), runnable, delay, period);
        tasks.put(taskId, task);
        return task;
    }

    public static void cancelTask(String taskId) {
        BukkitTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    public static void cancelAllTasks() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }

}
