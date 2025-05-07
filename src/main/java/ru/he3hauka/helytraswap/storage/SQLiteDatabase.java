package ru.he3hauka.helytraswap.storage;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteDatabase implements Database {
    private static final Logger LOGGER = Logger.getLogger(SQLiteDatabase.class.getName());
    private final String connectionString;

    public SQLiteDatabase() throws SQLException {
        new File("plugins/hElytraSwap").mkdirs();
        this.connectionString = "jdbc:sqlite:plugins/hElytraSwap/data.db";
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS elytra_swap (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "toggle BOOLEAN NOT NULL DEFAULT FALSE)";
            stmt.execute(sql);
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> getToggleStatus(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT toggle FROM elytra_swap WHERE uuid = ?")) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getBoolean("toggle");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Ошибка при получении статуса", e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setToggleStatus(UUID uuid, boolean status) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR REPLACE INTO elytra_swap (uuid, toggle) VALUES (?, ?)")) {

                stmt.setString(1, uuid.toString());
                stmt.setBoolean(2, status);
                stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Ошибка при установке статуса", e);
            }
        });
    }

    @Override
    public void close() {
    }
}