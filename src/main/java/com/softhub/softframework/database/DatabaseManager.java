package com.softhub.softframework.database;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DatabaseManager {
    CompletableFuture<Void> deleteTable(String table);
    CompletableFuture<Void> truncateTable(String table);
    CompletableFuture<Integer> countRows(String table);
    CompletableFuture<Boolean> tableExists(String table);
    CompletableFuture<Void> set(String selected, Object[] values, String column, String logicGate, String conditionValue, String table);
    CompletableFuture<Void> set(String selected, Object object, String column, String logicGate, String data, String table);
    CompletableFuture<Void> set(String selected, Object object, String[] whereArguments, String table);
    CompletableFuture<Void> setSync(String selected, Object[] values, String column, String logicGate, String conditionValue, String table);
    CompletableFuture<Void> setSync(String selected, Object object, String column, String logicGate, String data, String table);
    CompletableFuture<Void> setSync(String selected, Object object, String[] whereArguments, String table);
    CompletableFuture<Void> deleteData(String column, String logicGate, String data, String table);
    CompletableFuture<Void> insertData(String columns, String values, String table);
    CompletableFuture<Void> upsert(String selected, Object object, String column, String data, String table);
    CompletableFuture<Boolean> exists(String column, String data, String table);
    CompletableFuture<Void> createTable(String table, String columns);

    CompletableFuture<List<String>> getStringList(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<String> getString(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<List<Integer>> getIntList(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<Integer> getInt(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<List<Double>> getDoubleList(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<Double> getDouble(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<List<byte[]>> getByteArrayList(String selected, String table, String column, String logicGate, String data);
    CompletableFuture<byte[]> getByteArray(String selected, String table, String column, String logicGate, String data);

    <T> CompletableFuture<List<T>> getMultipleColumnsList(String selectedColumns, String table, String column, String logicGate, String data, ResultSetExtractor<T> extractor);

}

