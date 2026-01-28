package com.inventory.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class InventoryServer {
    public static void main(String[] args) {
        try {
            // Detect and set the server's IP address for RMI
            String serverIP = getServerIP();
            System.setProperty("java.rmi.server.hostname", serverIP);

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

            System.out.println("\n===========================================");
            System.out.println("✓ Inventory Server Ready!");
            System.out.println("===========================================");
            System.out.println("Server IP: " + serverIP);
            System.out.println("RMI Port: 1099");
            System.out.println("Socket Port: 9090");
            System.out.println("===========================================");
            System.out.println("\n>>> CLIENTS SHOULD CONNECT TO: " + serverIP + " <<<\n");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Automatically detect the server's LAN IP address
     * Skips VirtualBox and other virtual adapters
     */
    private static String getServerIP() {
        List<String> availableIPs = new ArrayList<>();
        String selectedIP = "localhost";

        try {
            System.out.println("\n=== Detecting Network Interfaces ===");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                String ifaceName = iface.getDisplayName().toLowerCase();

                // Skip VirtualBox, VMware, and other virtual adapters
                if (ifaceName.contains("virtualbox") ||
                        ifaceName.contains("vmware") ||
                        ifaceName.contains("virtual") ||
                        ifaceName.contains("vethernet")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Get IPv4 address that's not loopback
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        String ip = addr.getHostAddress();

                        // Skip VirtualBox IP range (192.168.56.x)
                        if (ip.startsWith("192.168.56.")) {
                            System.out.println("  [SKIPPED] " + ip + " (VirtualBox adapter)");
                            continue;
                        }

                        availableIPs.add(ip);
                        System.out.println("  [FOUND] " + ip + " on " + iface.getDisplayName());

                        // Prefer 192.168.x.x or 10.x.x.x addresses (common LAN ranges)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            selectedIP = ip;
                        }
                    }
                }
            }

            System.out.println("===================================\n");

            if (!availableIPs.isEmpty() && !selectedIP.equals("localhost")) {
                System.out.println("✓ Selected IP: " + selectedIP);
                return selectedIP;
            } else if (!availableIPs.isEmpty()) {
                selectedIP = availableIPs.get(0);
                System.out.println("✓ Selected IP: " + selectedIP);
                return selectedIP;
            } else {
                System.out.println("⚠ No suitable network interface found. Using localhost.");
                System.out.println("  Make sure you're connected to Wi-Fi or Ethernet!");
            }

        } catch (Exception e) {
            System.err.println("Error detecting IP: " + e.getMessage());
        }

        return selectedIP;
    }
}
