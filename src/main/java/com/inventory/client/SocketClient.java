package com.inventory.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;

public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private final Consumer<String> onMessageReceived;

    public SocketClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void start() {
        new Thread(() -> {
            try (Socket socket = new Socket(HOST, PORT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    Platform.runLater(() -> onMessageReceived.accept(msg));
                }
            } catch (IOException e) {
                Platform.runLater(() -> onMessageReceived.accept("Connection Error: " + e.getMessage()));
            }
        }).start();
    }
}
