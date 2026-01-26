package com.inventory.client;

import com.inventory.common.InventoryService;
import com.inventory.common.Product;
import com.inventory.common.SaleRecord;
import com.inventory.server.DatabaseHandler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientApp extends Application {
    private InventoryService service;
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<SaleRecord> salesList = FXCollections.observableArrayList();
    private TextArea logArea;

    // UI Controls Inventory
    private TableView<Product> table;
    private TextField nameField, priceField, qtyField, catField;

    // UI Controls Billing
    private TextField billQtyField;
    private ComboBox<Product> productComboBox;

    // UI Controls Reports
    private TableView<SaleRecord> salesTable;
    private Label totalSalesLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventory & Billing System");

        // Initialize DatabaseHandler for authentication
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Create and show login screen
        LoginScreen loginScreen = new LoginScreen(dbHandler, primaryStage, () -> {
            // This runs after successful login
            loadMainApplication(primaryStage);
        });

        primaryStage.setScene(loginScreen.createLoginScene());
        primaryStage.show();
    }

    private void loadMainApplication(Stage primaryStage) {
        // Connect to RMI
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (InventoryService) registry.lookup("InventoryService");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Cannot connect to Server: " + e.getMessage()).showAndWait();
            return;
        }

        // Connect Socket
        SocketClient socketClient = new SocketClient(this::log);
        socketClient.start();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createInventoryTab(), createBillingTab(), createReportsTab());

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(logArea);

        Scene scene = new Scene(root, 900, 650);

        // Load CSS
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not find stylesheet: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("Inventory & Billing System - Main Dashboard");

        refreshTable();
    }

    private Tab createInventoryTab() {
        Tab tab = new Tab("Inventory Management");
        tab.setClosable(false);

        table = new TableView<>();
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.getColumns().addAll(idCol, nameCol, priceCol, qtyCol, catCol);
        table.setItems(productList);

        // Form
        nameField = new TextField();
        nameField.setPromptText("Name");
        priceField = new TextField();
        priceField.setPromptText("Price");
        qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        catField = new TextField();
        catField.setPromptText("Category");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addProduct());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateProduct());

        Button deleteButton = new Button("Delete");
        // Style handled by CSS
        deleteButton.setOnAction(e -> deleteProduct());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshTable());

        FlowPane form = new FlowPane(15, 15);
        form.setPadding(new Insets(20));
        form.getChildren().addAll(nameField, priceField, qtyField, catField, addButton, updateButton, deleteButton,
                refreshButton);

        VBox layout = new VBox(15, table, form);
        layout.setPadding(new Insets(20));

        // Listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                qtyField.setText(String.valueOf(newSelection.getQuantity()));
                catField.setText(newSelection.getCategory());
            }
        });

        tab.setContent(layout);
        return tab;
    }

    private Tab createBillingTab() {
        Tab tab = new Tab("Billing");
        tab.setClosable(false);

        productComboBox = new ComboBox<>(productList);
        productComboBox.setPromptText("Select Product");
        productComboBox.setPrefWidth(300);

        billQtyField = new TextField();
        billQtyField.setPromptText("Quantity");
        billQtyField.setMaxWidth(300);

        Button billButton = new Button("Generate Invoice & Sell");
        billButton.setOnAction(e -> processBilling());

        VBox layout = new VBox(10, new Label("Select Product and Enter Quantity:"),
                productComboBox, billQtyField, billButton);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setPadding(new Insets(20));

        tab.setContent(layout);
        return tab;
    }

    private Tab createReportsTab() {
        Tab tab = new Tab("Reports");
        tab.setClosable(false);

        salesTable = new TableView<>();
        TableColumn<SaleRecord, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<SaleRecord, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SaleRecord, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SaleRecord, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<SaleRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        salesTable.getColumns().addAll(idCol, nameCol, qtyCol, priceCol, dateCol);
        salesTable.setItems(salesList);

        totalSalesLabel = new Label("Total Revenue: $0.00");
        totalSalesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button refreshReportsBtn = new Button("Refresh Report");
        refreshReportsBtn.setOnAction(e -> refreshReports());

        VBox layout = new VBox(15, salesTable, totalSalesLabel, refreshReportsBtn);
        layout.setPadding(new Insets(20));

        tab.setContent(layout);
        return tab;
    }

    private void addProduct() {
        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(qtyField.getText());
            String cat = catField.getText();

            Product p = new Product(name, price, qty, cat);
            service.addProduct(p);
            log("Added product: " + name);
            refreshTable();

            nameField.clear();
            priceField.clear();
            qtyField.clear();
            catField.clear();
        } catch (Exception e) {
            log("Error adding product: " + e.getMessage());
        }
    }

    private void updateProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product to update").show();
            return;
        }

        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            int qty = Integer.parseInt(qtyField.getText());
            String cat = catField.getText();

            selected.setName(name);
            selected.setPrice(price);
            selected.setQuantity(qty);
            selected.setCategory(cat);

            service.updateProduct(selected);
            log("Updated product: " + name);
            refreshTable();
        } catch (Exception e) {
            log("Error updating product: " + e.getMessage());
        }
    }

    private void deleteProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product to delete").show();
            return;
        }

        try {
            service.deleteProduct(selected.getId());
            log("Deleted product: " + selected.getName());
            refreshTable();
            nameField.clear();
            priceField.clear();
            qtyField.clear();
            catField.clear();
        } catch (Exception e) {
            log("Error deleting product: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Product> list = service.getAllProducts();
            productList.setAll(list);
        } catch (Exception e) {
            log("Error refreshing table: " + e.getMessage());
        }
    }

    private void refreshReports() {
        try {
            List<SaleRecord> list = service.getSalesReport();
            salesList.setAll(list);

            double total = list.stream().mapToDouble(SaleRecord::getTotalPrice).sum();
            totalSalesLabel.setText("Total Revenue: $" + String.format("%.2f", total));
        } catch (Exception e) {
            log("Error refreshing reports: " + e.getMessage());
        }
    }

    private void processBilling() {
        try {
            Product selected = productComboBox.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a product!").show();
                return;
            }

            int id = selected.getId();
            int qty = Integer.parseInt(billQtyField.getText());

            boolean success = service.processSale(id, qty);
            if (success) {
                // Generate Invoice
                Product p = service.getProduct(id); // Refresh to get latest data
                generateInvoice(p, qty);
                log("Sale processed for ID: " + id);
                refreshTable();
            } else {
                log("Sale Failed! Check stock or ID.");
                new Alert(Alert.AlertType.WARNING, "Sale Failed! Insufficient Stock.").show();
            }
        } catch (Exception e) {
            log("Error billing: " + e.getMessage());
        }
    }

    private void generateInvoice(Product p, int qty) {
        String filename = "Invoice_" + System.currentTimeMillis() + ".txt";
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("INVOICE\n");
            fw.write("Date: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
            fw.write("--------------------------------\n");
            fw.write("Product: " + p.getName() + "\n");
            fw.write("Category: " + p.getCategory() + "\n");
            fw.write("Unit Price: $" + p.getPrice() + "\n");
            fw.write("Quantity: " + qty + "\n");
            fw.write("Total: $" + (p.getPrice() * qty) + "\n");
            fw.write("--------------------------------\n");
            fw.write("Thank you for your business!");

            log("Invoice saved to " + filename);
            new Alert(Alert.AlertType.INFORMATION, "Invoice Generated: " + filename).show();
        } catch (IOException e) {
            log("Error writing invoice: " + e.getMessage());
        }
    }

    private void log(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }
}
