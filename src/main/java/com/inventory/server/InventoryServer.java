package com.inventory.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InventoryServer {
    public static void main(String[] args) {
        try {
            // 1. Initialize Database
            DatabaseHandler dbHandler = new DatabaseHandler();

            // 2. Start Socket Server
            SocketServer socketServer = new SocketServer();
            socketServer.start();

            // 3. Start RMI Service
            InventoryServiceImpl service = new InventoryServiceImpl(dbHandler, socketServer);

            // Create RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("InventoryService", service);

            System.out.println("Inventory Server Ready.");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
