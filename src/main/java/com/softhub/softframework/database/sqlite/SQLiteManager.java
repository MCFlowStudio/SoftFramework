package com.softhub.softframework.database.sqlite;

import com.softhub.softframework.database.DatabaseManager;
import com.softhub.softframework.task.SimpleAsync;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class SQLiteManager implements DatabaseManager {

    @FunctionalInterface
    interface PreparedStatementSetter {
        void setValues(PreparedStatement preparedStatement) throws SQLException;
    }

    private CompletableFuture<Void> executeUpdate(String sql, PreparedStatementSetter setter) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                setter.setValues(stmt);
                stmt.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> deleteTable(String table) {
        return executeUpdate("DROP TABLE IF EXISTS " + table, stmt -> {});
    }

    @Override
    public CompletableFuture<Void> truncateTable(String table) {
        // SQLite does not support TRUNCATE TABLE, so use DELETE FROM instead
        return executeUpdate("DELETE FROM " + table, stmt -> {});
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
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> set(String selected, Object object, String column, String logicGate, String data, String table) {
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + column + " " + logicGate + " ?";
        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
            stmt.setString(2, data);
        });
    }

    @Override
    public CompletableFuture<Void> set(String selected, Object object, String[] whereArguments, String table) {
        String whereClause = String.join(" AND ", whereArguments);
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + whereClause;
        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
        });
    }

    @Override
    public CompletableFuture<Void> deleteData(String column, String logicGate, String data, String table) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " " + logicGate + " ?";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
        });
    }

    @Override
    public CompletableFuture<Void> insertData(String columns, String values, String table) {
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
        return executeUpdate(sql, stmt -> {});
    }

    @Override
    public CompletableFuture<Void> upsert(String selected, Object object, String column, String data, String table) {
        // SQLite uses the REPLACE INTO syntax for upsert
        String sql = "REPLACE INTO " + table + " (" + column + ", " + selected + ") VALUES (?, ?)";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
            stmt.setObject(2, object);
        });
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
        return executeUpdate(sql, stmt -> {});
    }
}
