package com.softhub.softframework.database.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.softhub.softframework.BukkitInitializer;
import com.softhub.softframework.database.redis.message.ProtocolMessage;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class RedisManager {

    public static String SENDING_CHANNEL = "softframework:protocol_request";
    public static String RESPONSE_CHANNEL = "softframework:protocol_response";

    private static RedisManager instance;
    private static RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private ConcurrentHashMap<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private static Map<String, RedisPubSubListener<String, String>> listeners = new ConcurrentHashMap<>();
    private RedisPubSubAsyncCommands<String, String> pubSubAsyncCommands;


    private RedisManager(String host, int port, String password) {
        RedisURI.Builder builder = RedisURI.Builder.redis(host).withPort(port);
        if (password != null && !password.isEmpty()) {
            builder.withPassword(password.toCharArray());
        }
        RedisURI redisUri = builder.build();
        redisClient = RedisClient.create(redisUri);
        connect();

        pubSubConnection = redisClient.connectPubSub();
        setupListener();
    }

    public static synchronized void initialize(String host, int port, String password) {
        if (instance == null) {
            instance = new RedisManager(host, port, password);
        }
    }

    public static synchronized RedisManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("RedisManager is not initialized.");
        return instance;
    }

    public static synchronized RedisClient getRedisClient() {
        if (redisClient == null) {
            throw new IllegalStateException("RedisManager is not initialized.");
        }
        return redisClient;
    }

    public RedisCommands<String, String> getSyncCommands() {
        return connection.sync();
    }

    public RedisAsyncCommands<String, String> getAsyncCommands() {
        return connection.async();
    }


    // Redis 로그인
    private void connect() {
        try {
            connection = redisClient.connect();
            System.out.println("Connected to Redis server.");
        } catch (Exception e) {
            System.err.println("Error connecting to Redis server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Redis 로그아웃
    public void shutdown() {
        pubSubConnection.sync().unsubscribe(SENDING_CHANNEL, RESPONSE_CHANNEL);
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        if (pubSubConnection != null)
            pubSubConnection.close();
        if (pubSubAsyncCommands != null)
            pubSubAsyncCommands.shutdown(true);
        instance = null;
    }

    public void addListener(String channel, RedisPubSubListener<String, String> listener) {
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        connection.addListener(listener);
        connection.async().subscribe(channel);
        listeners.put(channel, listener);
    }

    public void removeListener(String channel, RedisPubSubListener<String, String> listener) {
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        if (connection != null) {
            connection.async().unsubscribe(channel);
            connection.removeListener(listener);
        } else {
            System.out.println("Listener not found or already removed");
        }
    }

    public void publishMessage(String channel, ProtocolMessage message) {
        String jsonMessage = BukkitInitializer.getGson().toJson(message);
        RedisAsyncCommands<String, String> asyncCommands = RedisManager.getInstance().getAsyncCommands();
        asyncCommands.publish(channel, jsonMessage).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }
    public void publishMessage(String channel, Object message) {
        String jsonMessage = BukkitInitializer.getGson().toJson(message);
        RedisAsyncCommands<String, String> asyncCommands = RedisManager.getInstance().getAsyncCommands();
        asyncCommands.publish(channel, jsonMessage).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void setupListener() {
        pubSubAsyncCommands = pubSubConnection.async();

        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                if (channel.equals(RESPONSE_CHANNEL)) {
                    try {
                        JsonObject jsonObject = BukkitInitializer.getGson().fromJson(message, JsonObject.class);
                        if (jsonObject != null) {
                            CompletableFuture<String> future = responseFutures.get(jsonObject.get("requestId").getAsString());
                            if (future != null) {
                                future.complete(message);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        System.err.println("JSON parsing error: " + message);
                    }
                }
            }
        });

        pubSubAsyncCommands.subscribe(RESPONSE_CHANNEL);
    }

    public CompletableFuture<String> publishResponse(String requestId, String channel, Object message) {
        CompletableFuture<String> future = new CompletableFuture<>();

        responseFutures.put(requestId, future);

        String jsonMessage = BukkitInitializer.getGson().toJson(message);
        RedisAsyncCommands<String, String> asyncCommands = getAsyncCommands();
        asyncCommands.publish(channel, jsonMessage).whenComplete((result, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                future.completeExceptionally(ex);
            }
        });

        future.whenComplete((result, ex) -> {
            responseFutures.remove(requestId.toString());
        });

        return future;
    }

    public void writeMessage(String key, String value) {
        RedisAsyncCommands<String, String> asyncCommands = RedisManager.getInstance().getAsyncCommands();
        asyncCommands.set(key, value);
    }

    public CompletableFuture<String> getKey(String key) {
        try {
            RedisAsyncCommands<String, String> asyncCommands = RedisManager.getInstance().getAsyncCommands();
            RedisFuture<String> future = asyncCommands.get(key);

            return future.toCompletableFuture();
        } catch (Exception e) {
            System.err.println("Redis에서 키를 읽는 중 오류 발생: " + e.getMessage());
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public CompletableFuture<HashSet<String>> getKeys(String pattern) {
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        RedisFuture<List<String>> futureKeys = asyncCommands.keys(pattern);
        return futureKeys.thenApply(HashSet::new).toCompletableFuture();
    }

    public CompletableFuture<Void> deleteKey(String key) {
        RedisAsyncCommands<String, String> asyncCommands = RedisManager.getInstance().getAsyncCommands();
        asyncCommands.del(key);
        return null;
    }

    public CompletionStage<Object> deletePatternKey(String pattern) {
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        return asyncCommands.keys(pattern).thenCompose(keys -> {
            if (!keys.isEmpty()) {
                return asyncCommands.del(keys.toArray(new String[0])).thenApply(result -> null);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

}
