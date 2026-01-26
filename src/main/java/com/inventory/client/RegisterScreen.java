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

public class RegisterScreen {
    private DatabaseHandler dbHandler;
    private Stage primaryStage;
    private Runnable onLoginSuccess;

    public RegisterScreen(DatabaseHandler dbHandler, Stage primaryStage, Runnable onLoginSuccess) {
        this.dbHandler = dbHandler;
        this.primaryStage = primaryStage;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Scene createRegisterScene() {
        // Main container with dark background matching the app theme
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Register card container
        VBox registerCard = new VBox(20);
        registerCard.setMaxWidth(500);
        registerCard.setMaxHeight(750);
        registerCard.setPadding(new Insets(40, 60, 40, 60));
        registerCard.setAlignment(Pos.TOP_CENTER);

        // Dark card with subtle border
        registerCard.setStyle(
                "-fx-background-color: #2a2a2a;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #3a3a3a;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");

        // Title with orange accent
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web("#ff9800")); // Orange accent
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(255, 152, 0, 0.3), 5, 0, 0, 2);");

        Label subtitleLabel = new Label("Join us today");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.rgb(180, 180, 180));

        // Full Name field
        VBox fullNameBox = createStyledInputBox("Full Name", false);
        TextField fullNameField = (TextField) ((HBox) fullNameBox.getChildren().get(1)).getChildren().get(0);

        // Username field
        VBox usernameBox = createStyledInputBox("Username", false);
        TextField usernameField = (TextField) ((HBox) usernameBox.getChildren().get(1)).getChildren().get(0);

        // Email field
        VBox emailBox = createStyledInputBox("Email", false);
        TextField emailField = (TextField) ((HBox) emailBox.getChildren().get(1)).getChildren().get(0);

        // Password field
        VBox passwordBox = createStyledInputBox("Password", true);
        PasswordField passwordField = (PasswordField) ((HBox) passwordBox.getChildren().get(1)).getChildren().get(0);

        // Confirm Password field
        VBox confirmPasswordBox = createStyledInputBox("Confirm Password", true);
        PasswordField confirmPasswordField = (PasswordField) ((HBox) confirmPasswordBox.getChildren().get(1))
                .getChildren().get(0);

        // Error/Success label
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        messageLabel.setVisible(false);

        // Register button with cyan color matching the app
        Button registerButton = createStyledButton("Create Account", "#00bcd4");
        registerButton.setOnAction(e -> handleRegister(
                fullNameField.getText(),
                usernameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                messageLabel));

        // Login link
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(Pos.CENTER);
        Label loginPrompt = new Label("Already have an account?");
        loginPrompt.setTextFill(Color.rgb(180, 180, 180));
        loginPrompt.setFont(Font.font("Segoe UI", 13));

        Hyperlink loginLink = new Hyperlink("Sign In");
        loginLink.setTextFill(Color.web("#ff9800")); // Orange accent
        loginLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        loginLink.setUnderline(true);
        loginLink.setOnAction(e -> showLoginScreen());
        loginLink.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        loginLink.setOnMouseEntered(e -> loginLink.setTextFill(Color.web("#ffb74d")));
        loginLink.setOnMouseExited(e -> loginLink.setTextFill(Color.web("#ff9800")));

        loginBox.getChildren().addAll(loginPrompt, loginLink);

        // Add all elements to card
        registerCard.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                fullNameBox,
                usernameBox,
                emailBox,
                passwordBox,
                confirmPasswordBox,
                messageLabel,
                registerButton,
                loginBox);

        // Wrap in ScrollPane for smaller screens
        ScrollPane scrollPane = new ScrollPane(registerCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(20));

        root.getChildren().add(scrollPane);

        // Allow Enter key to submit
        Scene scene = new Scene(root, 1000, 700);
        scene.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                registerButton.fire();
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

    private void handleRegister(String fullName, String username, String email,
            String password, String confirmPassword, Label messageLabel) {
        // Validation
        if (fullName.trim().isEmpty() || username.trim().isEmpty() ||
                email.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            showError(messageLabel, "Please fill in all fields");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(messageLabel, "Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError(messageLabel, "Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(messageLabel, "Passwords do not match");
            return;
        }

        try {
            // Check if username or email already exists
            if (dbHandler.usernameExists(username)) {
                showError(messageLabel, "Username already exists");
                return;
            }

            if (dbHandler.emailExists(email)) {
                showError(messageLabel, "Email already registered");
                return;
            }

            // Create new user
            User newUser = new User(username, email, password, fullName);
            boolean success = dbHandler.registerUser(newUser);

            if (success) {
                showSuccess(messageLabel, "Account created successfully! Redirecting...");
                // Wait a moment then show login screen
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::showLoginScreen);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError(messageLabel, "Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            showError(messageLabel, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(Label messageLabel, String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.rgb(255, 100, 100));
        messageLabel.setVisible(true);
    }

    private void showSuccess(Label messageLabel, String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.rgb(100, 255, 150));
        messageLabel.setVisible(true);
    }

    private void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(dbHandler, primaryStage, onLoginSuccess);
        primaryStage.setScene(loginScreen.createLoginScene());
    }
}
