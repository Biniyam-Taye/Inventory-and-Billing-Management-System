package com.inventory.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SocketServer {
    private static final int PORT = 9090;
    private final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Socket Server started on port " + PORT);
                while (true) {
                    new Handler(serverSocket.accept()).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    private class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println("Received: " + input);
                    // Echo back or handle commands if needed
                }
            } catch (IOException e) {
                // System.out.println("Client disconnected");
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
