package com.inventory.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InventoryService extends Remote {
    // Product Management
    void addProduct(Product product) throws RemoteException;

    void updateProduct(Product product) throws RemoteException;

    void deleteProduct(int productId) throws RemoteException;

    List<Product> getAllProducts() throws RemoteException;

    Product getProduct(int productId) throws RemoteException;

    // Billing / Transaction
    boolean processSale(int productId, int quantity) throws RemoteException;

    List<SaleRecord> getSalesReport() throws RemoteException;
}
