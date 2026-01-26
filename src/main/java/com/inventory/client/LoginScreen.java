package com.inventory.client;

import com.inventory.common.User;
import com.inventory.server.DatabaseHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginScreen {
    private DatabaseHandler dbHandler;
    private Stage primaryStage;
    private Runnable onLoginSuccess;

    public LoginScreen(DatabaseHandler dbHandler, Stage primaryStage, Runnable onLoginSuccess) {
        this.dbHandler = dbHandler;
        this.primaryStage = primaryStage;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Scene createLoginScene() {
        // Main container with dark background matching the app theme
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Login card container
        VBox loginCard = new VBox(25);
        loginCard.setMaxWidth(500);
        loginCard.setMaxHeight(650);
        loginCard.setPadding(new Insets(50, 60, 50, 60));
        loginCard.setAlignment(Pos.TOP_CENTER);

        // Dark card with subtle border
        loginCard.setStyle(
                "-fx-background-color: #2a2a2a;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #3a3a3a;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");

        // Title with orange accent
        Label titleLabel = new Label("Inventory Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web("#ff9800")); // Orange accent
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(255, 152, 0, 0.3), 5, 0, 0, 2);");

        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.rgb(180, 180, 180));

        // Username field
        VBox usernameBox = createStyledInputBox("Username", false);
        TextField usernameField = (TextField) ((HBox) usernameBox.getChildren().get(1)).getChildren().get(0);

        // Password field
        VBox passwordBox = createStyledInputBox("Password", true);
        PasswordField passwordField = (PasswordField) ((HBox) passwordBox.getChildren().get(1)).getChildren().get(0);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.rgb(255, 100, 100));
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        errorLabel.setVisible(false);

        // Login button with cyan color matching the app
        Button loginButton = createStyledButton("Sign In", "#00bcd4");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText(), errorLabel));

        // Register link
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER);
        Label registerPrompt = new Label("Don't have an account?");
        registerPrompt.setTextFill(Color.rgb(180, 180, 180));
        registerPrompt.setFont(Font.font("Segoe UI", 13));

        Hyperlink registerLink = new Hyperlink("Sign Up");
        registerLink.setTextFill(Color.web("#ff9800")); // Orange accent
        registerLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        registerLink.setUnderline(true);
        registerLink.setOnAction(e -> showRegisterScreen());
        registerLink.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        registerLink.setOnMouseEntered(e -> registerLink.setTextFill(Color.web("#ffb74d")));
        registerLink.setOnMouseExited(e -> registerLink.setTextFill(Color.web("#ff9800")));

        registerBox.getChildren().addAll(registerPrompt, registerLink);

        // Add all elements to card
        loginCard.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                usernameBox,
                passwordBox,
                errorLabel,
                loginButton,
                registerBox);

        root.getChildren().add(loginCard);

        // Allow Enter key to submit
        Scene scene = new Scene(root, 1000, 700);
        scene.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                loginButton.fire();
            }
        });

        return scene;
    }

    private VBox createStyledInputBox(String labelText, boolean isPassword) {
        VBox box = new VBox(8);

        Label label = new Label(labelText);
        label.setTextFill(Color.rgb(180, 180, 180));
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        HBox inputContainer = new HBox();
        inputContainer.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #3a3a3a;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 12 15;");

        TextInputControl inputField;
        if (isPassword) {
            inputField = new PasswordField();
        } else {
            inputField = new TextField();
        }

        inputField.setPromptText("Enter " + labelText.toLowerCase());
        inputField.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e0e0e0;" +
                        "-fx-prompt-text-fill: #666666;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';");
        inputField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputContainer.getChildren().add(inputField);
        box.getChildren().addAll(label, inputContainer);

        return box;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(50);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);

        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 5);");

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: derive(" + color + ", 20%);" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 15, 0, 0, 7);" +
                            "-fx-scale-x: 1.02;" +
                            "-fx-scale-y: 1.02;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 5);");
        });

        return button;
    }

    private void handleLogin(String username, String password, Label errorLabel) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showError(errorLabel, "Please fill in all fields");
            return;
        }

        try {
            User user = dbHandler.authenticateUser(username, password);
            if (user != null) {
                // Login successful
                onLoginSuccess.run();
            } else {
                showError(errorLabel, "Invalid username or password");
            }
        } catch (SQLException e) {
            showError(errorLabel, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showRegisterScreen() {
        RegisterScreen registerScreen = new RegisterScreen(dbHandler, primaryStage, onLoginSuccess);
        primaryStage.setScene(registerScreen.createRegisterScene());
    }
}
