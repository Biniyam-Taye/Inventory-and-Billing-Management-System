package com.inventory.server;

import com.inventory.common.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "inventory_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public DatabaseHandler() {
        initDB();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL + DB_NAME, USER, PASS);
    }

    private void initDB() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            String createProductTable = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL," +
                    "category VARCHAR(50))";
            stmt.executeUpdate(createProductTable);

            String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT," +
                    "quantity INT," +
                    "total_price DOUBLE," +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "product_name VARCHAR(100))";
            stmt.executeUpdate(createSalesTable);

            // Migration for existing table
            try {
                stmt.executeUpdate("ALTER TABLE sales ADD COLUMN product_name VARCHAR(100)");
            } catch (SQLException e) {
                // Column likely exists, ignore
            }

            System.out.println("Database and tables initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setDouble(2, p.getPrice());
            pstmt.setInt(3, p.getQuantity());
            pstmt.setString(4, p.getCategory());
            pstmt.executeUpdate();
        }
    }

    public synchronized void updateProduct(Product p) throws SQLException {
        String sql = "UPDATE products SET name=?, price=?, quantity=?, category=? WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setDouble(2, p.getPrice());
            pstmt.setInt(3, p.getQuantity());
            pstmt.setString(4, p.getCategory());
            pstmt.setInt(5, p.getId());
            pstmt.executeUpdate();
        }
    }

    public synchronized void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public synchronized List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category")));
            }
        }
        return list;
    }

    public synchronized Product getProduct(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("category"));
                }
            }
        }
        return null;
    }

    // Transactional sale
    public synchronized boolean processSale(int productId, int quantity) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // check stock
            String stockSql = "SELECT quantity, price, name FROM products WHERE id=?";
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            stockStmt.setInt(1, productId);
            ResultSet rs = stockStmt.executeQuery();

            if (rs.next()) {
                int currentStock = rs.getInt("quantity");
                double price = rs.getDouble("price");
                String productName = rs.getString("name");

                if (currentStock >= quantity) {
                    // Update Stock
                    String updateSql = "UPDATE products SET quantity=? WHERE id=?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, currentStock - quantity);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();

                    // Record Sale
                    String saleSql = "INSERT INTO sales (product_id, quantity, total_price, product_name) VALUES (?, ?, ?, ?)";
                    PreparedStatement saleStmt = conn.prepareStatement(saleSql);
                    saleStmt.setInt(1, productId);
                    saleStmt.setInt(2, quantity);
                    saleStmt.setDouble(3, price * quantity);
                    saleStmt.setString(4, productName);
                    saleStmt.executeUpdate();

                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null)
                conn.setAutoCommit(true);
            if (conn != null)
                conn.close();
        }
    }

    public synchronized List<com.inventory.common.SaleRecord> getSales() throws SQLException {
        List<com.inventory.common.SaleRecord> list = new ArrayList<>();
        // Join with products table to get the name (fallback)
        String sql = "SELECT s.id, s.product_id, s.product_name, p.name as current_name, s.quantity, s.total_price, s.sale_date FROM sales s LEFT JOIN products p ON s.product_id = p.id ORDER BY s.sale_date DESC";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("product_name");
                if (name == null) {
                    name = rs.getString("current_name");
                }
                list.add(new com.inventory.common.SaleRecord(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        name != null ? name : "Unknown",
                        rs.getInt("quantity"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("sale_date")));
            }
        }
        return list;
    }
}
