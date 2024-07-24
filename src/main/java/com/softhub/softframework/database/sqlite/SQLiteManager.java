package com.softhub.softframework.database.sqlite;

import com.softhub.softframework.database.DatabaseManager;
import com.softhub.softframework.database.ResultSetExtractor;
import com.softhub.softframework.task.SimpleAsync;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SQLiteManager implements DatabaseManager {

    @FunctionalInterface
    interface PreparedStatementSetter {
        void setValues(PreparedStatement preparedStatement) throws SQLException;
    }

    private CompletableFuture<Integer> executeUpdate(String sql, PreparedStatementSetter setter) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            try (Connection conn = SQLiteConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                setter.setValues(stmt);
                int rowsAffected = stmt.executeUpdate();
                future.complete(rowsAffected);
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private CompletableFuture<Integer> executeUpdateSync(String sql, PreparedStatementSetter setter) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.setValues(stmt);
            int rowsAffected = stmt.executeUpdate();
            future.complete(rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }
        return future;
    }

    private <T> CompletableFuture<List<T>> executeQueryList(String sql, ResultSetExtractor<T> extractor, Object... params) {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            try (Connection connection = SQLiteConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                if (params != null && params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        preparedStatement.setObject(i + 1, params[i]);
                    }
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(extractor.extractData(resultSet));
                    }
                    future.complete(result);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }


    private <T> CompletableFuture<T> executeQuerySingle(String sql, ResultSetExtractor<T> extractor, Object... params) {
        CompletableFuture<T> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            try (Connection connection = SQLiteConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        T result = extractor.extractData(resultSet);
                        future.complete(result);
                    } else {
                        future.complete(null);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> deleteTable(String table) {
        return executeUpdate("DROP TABLE IF EXISTS " + table, stmt -> {}).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> truncateTable(String table) {
        return executeUpdate("DELETE FROM " + table, stmt -> {}).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Integer> countRows(String table) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (Connection conn = SQLiteConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    future.complete(rs.getInt(1));
                } else {
                    future.complete(0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> tableExists(String table) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, table);
                ResultSet rs = stmt.executeQuery();
                future.complete(rs.next());
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> set(String selected, Object[] values, String column, String logicGate, String conditionValue, String table) {
        String[] columnArray = selected.split(", ");
        StringBuilder setClause = new StringBuilder();
        for (String columnName : columnArray) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(columnName).append(" = ?");
        }

        String updateSql = "UPDATE " + table + " SET " + setClause.toString() + " WHERE " + column + " " + logicGate + " ?";
        String insertSql = "INSERT INTO " + table + " (" + column + ", " + selected + ") VALUES (?, " + String.join(", ", Collections.nCopies(values.length, "?")) + ")";

        return executeUpdate(updateSql, stmt -> {
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            stmt.setObject(values.length + 1, conditionValue);
        }).thenCompose(result -> {
            if (result.equals(0)) {
                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, conditionValue);
                    for (int i = 0; i < values.length; i++) {
                        stmt.setObject(i + 2, values[i]);
                    }
                }).thenApply(insertResult -> null);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Void> set(String selected, Object object, String column, String logicGate, String data, String table) {
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + column + " " + logicGate + " ?";

        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
            stmt.setString(2, data);
        }).thenCompose(rowsAffected -> {
            if (rowsAffected == 0) {
                String insertSql = "INSERT INTO " + table + " (" + selected + ", " + column + ") VALUES (?, ?)";
                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, object);
                    stmt.setString(2, data);
                }).thenApply(insertRowsAffected -> null);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> set(String selected, Object object, String[] whereArguments, String table) {
        String whereClause = String.join(" AND ", whereArguments);
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + whereClause;
        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
        }).thenCompose(rowsAffected -> {
            if (rowsAffected == 0) {
                String columns = selected + ", " + String.join(", ", whereArguments);
                String valuesPlaceholders = "?, " + "?,".repeat(whereArguments.length - 1) + "?";
                String insertSql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + valuesPlaceholders + ")";

                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, object);
                    for (int i = 0; i < whereArguments.length; i++) {
                        stmt.setString(i + 2, whereArguments[i]);
                    }
                }).thenApply(insertRowsAffected -> null);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> setSync(String selected, Object[] values, String column, String logicGate, String conditionValue, String table) {
        String[] columnArray = selected.split(", ");
        StringBuilder setClause = new StringBuilder();
        for (String columnName : columnArray) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(columnName).append(" = ?");
        }

        String updateSql = "UPDATE " + table + " SET " + setClause.toString() + " WHERE " + column + " " + logicGate + " ?";
        String insertSql = "INSERT INTO " + table + " (" + column + ", " + selected + ") VALUES (?, " + String.join(", ", Collections.nCopies(values.length, "?")) + ")";

        return executeUpdateSync(updateSql, stmt -> {
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            stmt.setObject(values.length + 1, conditionValue);
        }).thenCompose(result -> {
            if (result.equals(0)) {
                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, conditionValue);
                    for (int i = 0; i < values.length; i++) {
                        stmt.setObject(i + 2, values[i]);
                    }
                }).thenApply(insertResult -> null);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setSync(String selected, Object object, String column, String logicGate, String data, String table) {
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + column + " " + logicGate + " ?";

        return executeUpdateSync(sql, stmt -> {
            stmt.setObject(1, object);
            stmt.setString(2, data);
        }).thenCompose(rowsAffected -> {
            if (rowsAffected == 0) {
                String insertSql = "INSERT INTO " + table + " (" + selected + ", " + column + ") VALUES (?, ?)";
                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, object);
                    stmt.setString(2, data);
                }).thenApply(insertRowsAffected -> null);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> setSync(String selected, Object object, String[] whereArguments, String table) {
        String whereClause = String.join(" AND ", whereArguments);
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + whereClause;
        return executeUpdateSync(sql, stmt -> {
            stmt.setObject(1, object);
        }).thenCompose(rowsAffected -> {
            if (rowsAffected == 0) {
                String columns = selected + ", " + String.join(", ", whereArguments);
                String valuesPlaceholders = "?, " + "?,".repeat(whereArguments.length - 1) + "?";
                String insertSql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + valuesPlaceholders + ")";

                return executeUpdate(insertSql, stmt -> {
                    stmt.setObject(1, object);
                    for (int i = 0; i < whereArguments.length; i++) {
                        stmt.setString(i + 2, whereArguments[i]);
                    }
                }).thenApply(insertRowsAffected -> null);
            }
            return CompletableFuture.completedFuture(null);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteData(String column, String logicGate, String data, String table) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> insertData(String columns, String values, String table) {
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
        return executeUpdate(sql, stmt -> {}).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> upsert(String selected, Object object, String column, String data, String table) {
        String sql = "REPLACE INTO " + table + " (" + column + ", " + selected + ") VALUES (?, ?)";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
            stmt.setObject(2, object);
        }).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Boolean> exists(String column, String data, String table) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SELECT EXISTS(SELECT 1 FROM " + table + " WHERE " + column + " = ?)";
            try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, data);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                future.complete(rs.getInt(1) == 1);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> createTable(String table, String columns) {
        String sql = "CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ")";
        return executeUpdate(sql, stmt -> {}).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<List<String>> getStringList(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQueryList(sql, rs -> rs.getString(selected), data);
    }

    @Override
    public CompletableFuture<String> getString(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQuerySingle(sql, rs -> rs.getString(selected), data);
    }

    @Override
    public CompletableFuture<List<Integer>> getIntList(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQueryList(sql, rs -> rs.getInt(selected), data);
    }

    @Override
    public CompletableFuture<Integer> getInt(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQuerySingle(sql, rs -> rs.getInt(selected), data);
    }

    @Override
    public CompletableFuture<List<Double>> getDoubleList(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQueryList(sql, rs -> rs.getDouble(selected), data);
    }

    @Override
    public CompletableFuture<Double> getDouble(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQuerySingle(sql, rs -> rs.getDouble(selected), data);
    }

    @Override
    public CompletableFuture<List<byte[]>> getByteArrayList(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeQueryList(sql, rs -> rs.getBytes(selected), data);
    }

    @Override
    public CompletableFuture<byte[]> getByteArray(String selected, String table, String column, String logicGate, String data) {
        String sql = "SELECT " + selected + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";

        return executeQuerySingle(sql, rs -> {
            byte[] result = rs.getBytes(selected);
            return result;
        }, data).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public <T> CompletableFuture<List<T>> getMultipleColumnsList(String selectedColumns, String table, String column, String logicGate, String data, ResultSetExtractor<T> extractor) {
        String sql;
        Object[] params;

        if (column == null || column.isEmpty() || logicGate == null || logicGate.isEmpty()) {
            sql = "SELECT " + selectedColumns + " FROM " + table + " WHERE 1";
            params = new Object[] {};
        } else {
            sql = "SELECT " + selectedColumns + " FROM " + table + " WHERE " + column + " " + logicGate + " ?";
            params = new Object[] { data };
        }

        return executeQueryList(sql, extractor, params);
    }

}
