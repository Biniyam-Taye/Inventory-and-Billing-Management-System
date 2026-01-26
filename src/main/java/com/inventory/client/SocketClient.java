package com.inventory.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;

public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private final Consumer<String> onMessageReceived;
    private Socket socket;
    private volatile boolean running = true;

    public SocketClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line;
                while (running && (line = in.readLine()) != null) {
                    final String msg = line;
                    Platform.runLater(() -> onMessageReceived.accept(msg));
                }
            } catch (IOException e) {
                if (running) {
                    Platform.runLater(() -> onMessageReceived.accept("Connection Error: " + e.getMessage()));
                }
            } finally {
                close();
            }
        }).start();
    }

    public void close() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
