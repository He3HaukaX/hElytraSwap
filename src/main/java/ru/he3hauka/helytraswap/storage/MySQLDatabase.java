package ru.he3hauka.helytraswap.storage;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLDatabase implements Database {
    private static final Logger LOGGER = Logger.getLogger(MySQLDatabase.class.getName());
    private final Connection connection;

    public MySQLDatabase(String host, int port, String database, String username, String password) throws SQLException {
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            createTable();
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL драйвер не найден", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS elytra_swap (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "toggle BOOLEAN NOT NULL DEFAULT FALSE)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public CompletableFuture<Boolean> getToggleStatus(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT toggle FROM elytra_swap WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
            String sql = "INSERT INTO elytra_swap (uuid, toggle) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE toggle = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setBoolean(2, status);
                stmt.setBoolean(3, status);
                stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Ошибка при установке статуса", e);
            }
        });
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при закрытии соединения", e);
        }
    }
}