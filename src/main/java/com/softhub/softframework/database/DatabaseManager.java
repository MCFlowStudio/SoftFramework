package com.softhub.softframework.database;

import java.util.concurrent.CompletableFuture;

public interface DatabaseManager {
    CompletableFuture<Void> deleteTable(String table);
    CompletableFuture<Void> truncateTable(String table);
    CompletableFuture<Integer> countRows(String table);
    CompletableFuture<Boolean> tableExists(String table);
    CompletableFuture<Void> set(String selected, Object object, String column, String logicGate, String data, String table);
    CompletableFuture<Void> set(String selected, Object object, String[] whereArguments, String table);
    CompletableFuture<Void> deleteData(String column, String logicGate, String data, String table);
    CompletableFuture<Void> insertData(String columns, String values, String table);
    CompletableFuture<Void> upsert(String selected, Object object, String column, String data, String table);
    CompletableFuture<Boolean> exists(String column, String data, String table);
    CompletableFuture<Void> createTable(String table, String columns);
}

