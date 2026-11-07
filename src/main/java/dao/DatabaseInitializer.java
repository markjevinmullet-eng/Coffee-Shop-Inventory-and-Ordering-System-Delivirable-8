package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
        """;

        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                product_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                price REAL NOT NULL CHECK(price >= 0),
                stock_quantity INTEGER NOT NULL CHECK(stock_quantity >= 0)
            );
        """;

        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS orders (
                order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                cashier_id INTEGER,
                customer_id INTEGER,
                order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                total_amount REAL NOT NULL CHECK(total_amount >= 0),
                FOREIGN KEY (cashier_id) REFERENCES users(user_id) ON DELETE SET NULL,
                FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE SET NULL
            );
        """;

        String createOrderDetailsTable = """
            CREATE TABLE IF NOT EXISTS order_details (
                order_detail_id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                subtotal REAL NOT NULL CHECK(subtotal >= 0),
                FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
            );
        """;

        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = DatabaseConnection.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createProductsTable);
            stmt.execute(createOrdersTable);
            stmt.execute(createOrderDetailsTable);
            stmt.execute(createActivityLogsTable);

            // Default admin account
            stmt.execute("INSERT OR IGNORE INTO users (username, password, roles, status) VALUES ('admin', 'admin123', 'MANAGER', 'APPROVED');");

            // Accurate default sample products matching menu pricing
            stmt.execute("INSERT OR IGNORE INTO products (product_id, name, category, price, stock_quantity) VALUES (1, 'Caramel Macchiato', 'Coffee', 15.00, 50);");
            stmt.execute("INSERT OR IGNORE INTO products (product_id, name, category, price, stock_quantity) VALUES (2, 'Matcha Latte', 'Tea', 16.00, 40);");

        } catch (SQLException e) {
            System.err.println("Initialization Error: " + e.getMessage());
        }
    }
}