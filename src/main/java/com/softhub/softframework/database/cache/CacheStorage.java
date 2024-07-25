package com.softhub.softframework.database.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheStorage {

    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    private static String generateKey(String service, String key) {
        return service + ":" + key;
    }

    public static Map<String, Object> getAll(String service) {
        Map<String, Object> serviceData = new ConcurrentHashMap<>();
        String prefix = service + ":";
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String key = entry.getKey().substring(prefix.length());
                serviceData.put(key, entry.getValue());
            }
        }
        return serviceData;
    }


    public static void set(String service, String key, Object value) {
        cache.put(generateKey(service, key), value);
    }

    public static void setString(String service, String key, String value) {
        cache.put(generateKey(service, key), value);
    }

    public static void setInt(String service, String key, Integer value) {
        cache.put(generateKey(service, key), value);
    }

    public static void setDouble(String service, String key, Double value) {
        cache.put(generateKey(service, key), value);
    }

    public static void setList(String service, String key, List<?> value) {
        cache.put(generateKey(service, key), value);
    }

    public static Object get(String service, String key) {
        return cache.get(generateKey(service, key));
    }

    public static Object get(String service, String key, Object defaultValue) {
        return cache.getOrDefault(generateKey(service, key), defaultValue);
    }

    public static String getString(String service, String key) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public static String getString(String service, String key, String defaultValue) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public static Integer getInt(String service, String key) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    public static Integer getInt(String service, String key, Integer defaultValue) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    public static Double getDouble(String service, String key) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof Double) {
            return (Double) value;
        }
        return null;
    }

    public static Double getDouble(String service, String key, Double defaultValue) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof Double) {
            return (Double) value;
        }
        return defaultValue;
    }

    public static List<?> getList(String service, String key) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof List) {
            return (List<?>) value;
        }
        return null;
    }

    public static List<?> getList(String service, String key, List<?> defaultValue) {
        Object value = cache.get(generateKey(service, key));
        if (value instanceof List) {
            return (List<?>) value;
        }
        return defaultValue;
    }

    public static boolean containsKey(String service, String key) {
        return cache.containsKey(generateKey(service, key));
    }

    public static void remove(String service, String key) {
        cache.remove(generateKey(service, key));
    }
}
