package com.softhub.softframework.database.mysql;

import com.softhub.softframework.task.SimpleAsync;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MysqlManager {

    @FunctionalInterface
    interface PreparedStatementSetter {
        void setValues(PreparedStatement preparedStatement) throws SQLException;
    }

    private static CompletableFuture<Void> executeUpdate(String sql, PreparedStatementSetter setter) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            try (Connection conn = MysqlConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                setter.setValues(stmt);
                stmt.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Void> deleteTable(String table) {
        return executeUpdate("DROP TABLE IF EXISTS " + table, stmt -> {});
    }

    public static CompletableFuture<Void> truncateTable(String table) {
        return executeUpdate("TRUNCATE TABLE " + table, stmt -> {});
    }

    public static CompletableFuture<Integer> countRows(String table) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SELECT COUNT(*) FROM " + table;
            try (Connection conn = MysqlConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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

    public static CompletableFuture<Boolean> tableExists(String table) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SHOW TABLES LIKE ?";
            try (Connection conn = MysqlConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, table);
                ResultSet rs = stmt.executeQuery();
                future.complete(rs.next());
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Void> set(String selected, Object object, String column, String logic_gate, String data, String table) {
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + column + " " + logic_gate + " ?";
        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
            stmt.setString(2, data);
        });
    }

    public static CompletableFuture<Void> set(String selected, Object object, String[] where_arguments, String table) {
        String whereClause = String.join(" AND ", where_arguments);
        String sql = "UPDATE " + table + " SET " + selected + " = ? WHERE " + whereClause;
        return executeUpdate(sql, stmt -> {
            stmt.setObject(1, object);
        });
    }

    public static CompletableFuture<Void> deleteData(String column, String logic_gate, String data, String table) {
        String sql = "DELETE FROM " + table + " WHERE " + column + " " + logic_gate + " ?";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
        });
    }

    public static CompletableFuture<Void> insertData(String columns, String values, String table) {
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
        return executeUpdate(sql, stmt -> {});
    }

    public static CompletableFuture<Void> upsert(String selected, Object object, String column, String data, String table) {
        String sql = "INSERT INTO " + table + " (" + column + ", " + selected + ") VALUES (?, ?) ON DUPLICATE KEY UPDATE " + selected + " = VALUES(" + selected + ")";
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, data);
            stmt.setObject(2, object);
        });
    }

    public static CompletableFuture<Boolean> exists(String column, String data, String table) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        SimpleAsync.async(() -> {
            String sql = "SELECT EXISTS(SELECT 1 FROM " + table + " WHERE " + column + " = ?)";
            try (Connection conn = MysqlConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public static CompletableFuture<Void> createTable(String table, String columns) {
        String sql = "CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ")";
        return executeUpdate(sql, stmt -> {});
    }
}
