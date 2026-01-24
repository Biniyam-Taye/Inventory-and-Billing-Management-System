package com.inventory.server;

import com.inventory.common.InventoryService;
import com.inventory.common.Product;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

public class InventoryServiceImpl extends UnicastRemoteObject implements InventoryService {
    private final DatabaseHandler dbHandler;
    private final SocketServer socketServer;

    public InventoryServiceImpl(DatabaseHandler dbHandler, SocketServer socketServer) throws RemoteException {
        super();
        this.dbHandler = dbHandler;
        this.socketServer = socketServer;
    }

    @Override
    public void addProduct(Product product) throws RemoteException {
        try {
            dbHandler.addProduct(product);
            socketServer.broadcast("System: New product added - " + product.getName());
        } catch (SQLException e) {
            throw new RemoteException("Error adding product", e);
        }
    }

    @Override
    public void updateProduct(Product product) throws RemoteException {
        try {
            dbHandler.updateProduct(product);
            socketServer.broadcast("System: Product updated - " + product.getName());
        } catch (SQLException e) {
            throw new RemoteException("Error updating product", e);
        }
    }

    @Override
    public void deleteProduct(int productId) throws RemoteException {
        try {
            dbHandler.deleteProduct(productId);
            socketServer.broadcast("System: Product deleted ID " + productId);
        } catch (SQLException e) {
            throw new RemoteException("Error deleting product", e);
        }
    }

    @Override
    public List<Product> getAllProducts() throws RemoteException {
        try {
            return dbHandler.getAllProducts();
        } catch (SQLException e) {
            throw new RemoteException("Error listing products", e);
        }
    }

    @Override
    public Product getProduct(int productId) throws RemoteException {
        try {
            return dbHandler.getProduct(productId);
        } catch (SQLException e) {
            throw new RemoteException("Error getting product", e);
        }
    }

    @Override
    public boolean processSale(int productId, int quantity) throws RemoteException {
        try {
            boolean success = dbHandler.processSale(productId, quantity);
            if (success) {
                // Check for low stock
                Product p = dbHandler.getProduct(productId);
                if (p != null && p.getQuantity() < 5) {
                    socketServer.broadcast("ALERT: Low stock for " + p.getName() + " (Qty: " + p.getQuantity() + ")");
                }
                socketServer.broadcast("Sale: " + quantity + " units of item " + productId + " sold.");
            }
            return success;
        } catch (SQLException e) {
            throw new RemoteException("Error processing sale", e);
        }
    }

    @Override
    public List<com.inventory.common.SaleRecord> getSalesReport() throws RemoteException {
        try {
            return dbHandler.getSales();
        } catch (SQLException e) {
            throw new RemoteException("Error getting sales report", e);
        }
    }
}
