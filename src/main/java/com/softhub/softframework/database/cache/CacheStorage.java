package com.softhub.softframework.database.cache;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheStorage {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private String generateKey(JavaPlugin instance, String key) {
        return instance.getName() + ":" + key;
    }

    public void set(JavaPlugin instance, String key, Object value) {
        cache.put(generateKey(instance, key), value);
    }

    public void setString(JavaPlugin instance, String key, String value) {
        cache.put(generateKey(instance, key), value);
    }

    public void setInt(JavaPlugin instance, String key, Integer value) {
        cache.put(generateKey(instance, key), value);
    }

    public void setDouble(JavaPlugin instance, String key, Double value) {
        cache.put(generateKey(instance, key), value);
    }

    public void setList(JavaPlugin instance, String key, List<?> value) {
        cache.put(generateKey(instance, key), value);
    }

    public Object get(JavaPlugin instance, String key) {
        return cache.get(generateKey(instance, key));
    }

    public String getString(JavaPlugin instance, String key) {
        Object value = cache.get(generateKey(instance, key));
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public Integer getInt(JavaPlugin instance, String key) {
        Object value = cache.get(generateKey(instance, key));
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    public Double getDouble(JavaPlugin instance, String key) {
        Object value = cache.get(generateKey(instance, key));
        if (value instanceof Double) {
            return (Double) value;
        }
        return null;
    }

    public List<?> getList(JavaPlugin instance, String key) {
        Object value = cache.get(generateKey(instance, key));
        if (value instanceof List) {
            return (List<?>) value;
        }
        return null;
    }

    public boolean containsKey(JavaPlugin instance, String key) {
        return cache.containsKey(generateKey(instance, key));
    }

    public void remove(JavaPlugin instance, String key) {
        cache.remove(generateKey(instance, key));
    }
}
