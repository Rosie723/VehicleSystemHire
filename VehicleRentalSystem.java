package com.example.vehiclehiresystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.*;
import javafx.scene.text.Font;
import javafx.scene.paint.*;
import javafx.scene.effect.DropShadow;

import java.util.*;
import java.sql.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class VehicleRentalSystem extends Application {

    // DB credentials
    private final String DB_URL = "jdbc:mysql://localhost:3306/VehicleHire";
    private final String DB_USER = "root";
    private final String DB_PASS = "123456789";

    private Stage primaryStage;
    private Scene loginScene, dashboardScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: DarkGrey;");
    }
    // Unified Login Screen for all user types
    private void showLoginScreen() {
        Label title = new Label("Vehicle Rental System");
        title.setFont(Font.font("Arial", 30));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow());

        // User type selection
        ComboBox<String> userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("Admin", "Employee", "Customer");
        userTypeCombo.setPromptText("Select User Type");
        userTypeCombo.setStyle("-fx-background-color: white;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #2b5876; -fx-text-fill: white;");

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setTextFill(Color.WHITE);

        Label message = new Label();

        // Login button action
        loginBtn.setOnAction(e -> {
            String userType = userTypeCombo.getValue();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (userType == null || username.isEmpty() || password.isEmpty()) {
                message.setText("Please fill all fields");
                message.setTextFill(Color.RED);
                return;
            }

            authenticateUser(userType.toLowerCase(), username, password, message);
        });

        // Registration link action
        registerLink.setOnAction(e -> showRegistrationForm());

        VBox loginLayout = new VBox(15, title, userTypeCombo, usernameField, passwordField,
                loginBtn, registerLink, message);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(30));

        StackPane root = new StackPane(loginLayout);
        root.setStyle("-fx-background-color: linear-gradient(to right, #4e4376, #2b5876);");

        loginScene = new Scene(root, 600, 400);
        primaryStage.setTitle("Login - Vehicle Rental System");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // Unified authentication method
    private void authenticateUser (String userType, String username, String password, Label message) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query;
            PreparedStatement stmt;

            // Determine which query to use based on user type
            if (userType.equalsIgnoreCase("customer")) {
                query = "SELECT role FROM users WHERE username=? AND password=? AND role='Customer'";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
            } else {
                query = "SELECT role FROM users WHERE username=? AND password=? AND (role='Admin' OR role='Employee')";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
            }

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Check if a result was returned
            if (rs.next()) {
                String role = rs.getString("role");
                redirectToDashboard(userType, username, role);
            } else {
                message.setText("Invalid username or password");
                message.setTextFill(Color.RED);
            }
        } catch (SQLException e) {
            message.setText("Database error");
            e.printStackTrace();
        }
    }
    // Route to appropriate dashboard based on user type
    private void redirectToDashboard(String userType, String username, String role) {
        switch (userType) {
            case "admin":
            case "employee":
                showDashboard(username, role);
                break;
            case "customer":
                showCustomerDashboard(username);
                break;
        }
    }

    // Registration form for customers
    private void showRegistrationForm() {
        Stage registrationStage = new Stage();
        registrationStage.setTitle("Customer Registration");

        Label titleLabel = new Label("Register as a Customer");
        titleLabel.setFont(Font.font("Arial", 24));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Information");

        TextField licenseField = new TextField();
        licenseField.setPromptText("Driving License Number");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        Button registerBtn = new Button("Register");
        Label message = new Label();

        registerBtn.setOnAction(e -> {
            // Validate inputs
            if (nameField.getText().isEmpty() || contactField.getText().isEmpty() ||
                    licenseField.getText().isEmpty() || usernameField.getText().isEmpty() ||
                    passwordField.getText().isEmpty()) {
                message.setText("All fields are required");
                message.setTextFill(Color.RED);
                return;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                message.setText("Passwords do not match");
                message.setTextFill(Color.RED);
                return;
            }

            // Register customer
            registerCustomer(
                    nameField.getText(),
                    contactField.getText(),
                    licenseField.getText(),
                    usernameField.getText(),
                    passwordField.getText(),
                    message
            );
        });

        VBox registrationLayout = new VBox(10, titleLabel, nameField, contactField,
                licenseField, usernameField, passwordField, confirmPasswordField,
                registerBtn, message);
        registrationLayout.setAlignment(Pos.CENTER);
        registrationLayout.setPadding(new Insets(20));

        Scene registrationScene = new Scene(registrationLayout, 400, 500);
        registrationStage.setScene(registrationScene);
        registrationStage.show();
    }

    private void registerCustomer(String name, String contact, String license,
                                  String username, String password, Label message) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // First check if username exists in users table
            String checkQuery = "SELECT username FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                message.setText("Username already exists");
                message.setTextFill(Color.RED);
                return;
            }

            // Insert into users table (with role 'Customer')
            String insertQuery = "INSERT INTO users (username, password, role, name, contact_info, driving_license_number) " +
                    "VALUES (?, ?, 'Customer', ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // Should be hashed in production
            insertStmt.setString(3, name);
            insertStmt.setString(4, contact);
            insertStmt.setString(5, license);

            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                message.setText("Registration successful! You can now login");
                message.setTextFill(Color.GREEN);
            }
        } catch (SQLException ex) {
            message.setText("Registration failed: " + ex.getMessage());
            message.setTextFill(Color.RED);
            ex.printStackTrace();
        }
    }

    private void showCustomerDashboard(String username) {
        TabPane tabPane = new TabPane();

        Tab carsTab = new Tab("Cars");
        Tab trucksTab = new Tab("Trucks");
        Tab vansTab = new Tab("Vans");
        Tab bikesTab = new Tab("Bikes");
        Tab paymentsTab = new Tab("Payments"); // New Payments Tab

        // Load vehicle data for each tab
        carsTab.setContent(loadVehicleList("Cars"));
        trucksTab.setContent(loadVehicleList("Trucks"));
        vansTab.setContent(loadVehicleList("Vans"));
        bikesTab.setContent(loadVehicleList("Bikes"));
        paymentsTab.setContent(showPaymentsTab(username)); // Set content for payments tab


        tabPane.getTabs().addAll(carsTab, trucksTab, vansTab, bikesTab,   paymentsTab);
// Create logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> showLoginScreen());

        // Create a container for the button at bottom right
        HBox buttonContainer = new HBox(logoutBtn);
        buttonContainer.setAlignment(Pos.BOTTOM_RIGHT);
        buttonContainer.setPadding(new Insets(10));

        // Create the main layout with tab pane and button
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(tabPane);
        mainLayout.setBottom(buttonContainer);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, Pink, DarkGrey);");


        VBox layout = new VBox(tabPane);
        Scene customerScene = new Scene(layout, 800, 500);
        layout.setStyle("-fx-background-color: linear-gradient(to right, Pink , DarkGrey);");
        primaryStage.setTitle("Customer Dashboard - Vehicle Rental System");
        primaryStage.setScene(customerScene);
    }

    // Load vehicle list based on type
    private VBox loadVehicleList(String type) {
        VBox vehicleListLayout = new VBox(10);
        Label titleLabel = new Label(type + " Available for Booking");
        titleLabel.setFont(Font.font("Arial", 24));

        // Fetch vehicles based on type
        List<Vehicle> vehicles = fetchVehiclesByType(type);

        // Check if the vehicles list is null or empty
        if (vehicles == null || vehicles.isEmpty()) {
            vehicleListLayout.getChildren().add(new Label("No vehicles available for this type."));
        } else {
            for (Vehicle vehicle : vehicles) {
                Button bookButton = new Button("Book " + vehicle.getBrand() + " " + vehicle.getModel());
                bookButton.setOnAction(e -> showBookingForm(vehicle));
                vehicleListLayout.getChildren().add(bookButton);
            }
        }

        vehicleListLayout.getChildren().add(titleLabel);
        return vehicleListLayout;
    }
    private VBox showPaymentsTab(String username) {
        VBox paymentsLayout = new VBox(10);
        paymentsLayout.setPadding(new Insets(20));

        Label titleLabel = new Label("Payments");
        titleLabel.setFont(Font.font("Arial", 24));

        // Booking ID and Amount fields
        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("Booking ID");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        Button payBtn = new Button("Make Payment");
        TextArea paymentMessageArea = new TextArea();
        paymentMessageArea.setEditable(false);

        // Make back button visible and functional
        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #6a11cb; -fx-text-fill: white;");
        backBtn.setOnAction(e -> primaryStage.setScene(dashboardScene));

        payBtn.setOnAction(e -> {
            String bookingId = bookingIdField.getText();
            String amountText = amountField.getText();

            if (bookingId.isEmpty() || amountText.isEmpty()) {
                paymentMessageArea.appendText("Error: Both fields must be filled.\n");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                processPayment(bookingId, amount, paymentMessageArea, username); // Pass username here
            } catch (NumberFormatException ex) {
                paymentMessageArea.appendText("Error: Amount must be a valid number.\n");
            }
        });

        // Add all components to the layout
        paymentsLayout.getChildren().addAll(
                titleLabel,
                bookingIdField,
                amountField,
                payBtn,
                paymentMessageArea,
                backBtn  // Make sure back button is added
        );

        paymentsLayout.getChildren().addAll(titleLabel, bookingIdField, amountField, payBtn, paymentMessageArea);
        paymentsLayout.setStyle("-fx-background-color: linear-gradient(to right, Pink , DarkGrey);");
        return paymentsLayout;
    }
    private void processPayment(String bookingId, double amount, TextArea paymentMessageArea, String username) {
        // Create a dialog for payment method selection
        Dialog<PaymentMethod> paymentMethodDialog = new Dialog<>();
        paymentMethodDialog.setTitle("Select Payment Method");
        paymentMethodDialog.setHeaderText("Choose your payment method");

        // Set the button types
        ButtonType cashButton = new ButtonType("Cash", ButtonBar.ButtonData.OK_DONE);
        ButtonType cardButton = new ButtonType("Credit Card", ButtonBar.ButtonData.OK_DONE);
        ButtonType onlineButton = new ButtonType("Online", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        paymentMethodDialog.getDialogPane().getButtonTypes().addAll(cashButton, cardButton, onlineButton, cancelButton);

        // Convert result to PaymentMethod when a button is clicked
        paymentMethodDialog.setResultConverter(dialogButton -> {
            if (dialogButton == cashButton) {
                return PaymentMethod.CASH;
            } else if (dialogButton == cardButton) {
                return PaymentMethod.CREDIT_CARD;
            } else if (dialogButton == onlineButton) {
                return PaymentMethod.ONLINE;
            }
            return null;
        });

        // Show the dialog and wait for user selection
        Optional<PaymentMethod> result = paymentMethodDialog.showAndWait();

        result.ifPresent(paymentMethod -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                // First, get customer and vehicle details for the invoice
                String customerName = "";
                String vehicleDetails = "";

                // Get customer name
                String customerQuery = "SELECT name FROM users WHERE username = ?";
                PreparedStatement customerStmt = conn.prepareStatement(customerQuery);
                customerStmt.setString(1, username);
                ResultSet customerRs = customerStmt.executeQuery();
                if (customerRs.next()) {
                    customerName = customerRs.getString("name");
                }

                // Get vehicle details
                String vehicleQuery = "SELECT v.brand, v.model FROM vehicles v JOIN bookings b ON v.vehicle_id = b.vehicle_id WHERE b.booking_id = ?";
                PreparedStatement vehicleStmt = conn.prepareStatement(vehicleQuery);
                vehicleStmt.setString(1, bookingId);
                ResultSet vehicleRs = vehicleStmt.executeQuery();
                if (vehicleRs.next()) {
                    vehicleDetails = vehicleRs.getString("brand") + " " + vehicleRs.getString("model");
                }

                // Generate invoice ID
                String invoiceId = "INV-" + System.currentTimeMillis();

                // Insert payment into database
                String paymentQuery = "INSERT INTO payments (invoice_id, booking_id, amount, payment_method) VALUES (?, ?, ?, ?)";
                PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, invoiceId);
                paymentStmt.setString(2, bookingId);
                paymentStmt.setDouble(3, amount);
                paymentStmt.setString(4, paymentMethod.toString());
                paymentStmt.executeUpdate();

                // Create and show invoice
                Invoice invoice = new Invoice(invoiceId, bookingId, customerName, vehicleDetails,
                        amount, paymentMethod, new Date());
                showInvoice(invoice);

                paymentMessageArea.appendText("Payment processed successfully!\n");
                paymentMessageArea.appendText("Invoice generated: " + invoiceId + "\n");
            } catch (SQLException ex) {
                paymentMessageArea.appendText("Error processing payment: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });
    }

    private void showInvoice(Invoice invoice) {
        Stage invoiceStage = new Stage();
        invoiceStage.setTitle("Invoice - " + invoice.getInvoiceId());

        TextArea invoiceTextArea = new TextArea(invoice.generateInvoiceText());
        invoiceTextArea.setEditable(false);
        invoiceTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        Button printButton = new Button("Print Invoice");
        printButton.setOnAction(e -> printInvoice(invoice));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> invoiceStage.close());

        HBox buttonBox = new HBox(10, printButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, invoiceTextArea, buttonBox);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 400);
        invoiceStage.setScene(scene);
        invoiceStage.show();
    }

    private void printInvoice(Invoice invoice) {
        try {
            // Create a temporary file
            File tempFile = File.createTempFile("invoice_" + invoice.getInvoiceId(), ".txt");
            try (PrintWriter writer = new PrintWriter(tempFile)) {
                writer.println(invoice.generateInvoiceText());
            }

            // Try to print directly
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.PRINT)) {
                    desktop.print(tempFile);
                    return;
                }
                // Fallback to open if print isn't supported
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(tempFile);
                    return;
                }
            }

            // Final fallback - show dialog
            showInvoiceDialog(invoice);

        } catch (IOException ex) {
            ex.printStackTrace();
            showInvoiceDialog(invoice);
        }
    }

    private void showInvoiceDialog(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice");
        alert.setHeaderText("Invoice " + invoice.getInvoiceId());
        alert.setContentText(invoice.generateInvoiceText());
        alert.showAndWait();
    }

    // Dashboard
    private void showDashboard(String username, String role) {
        Label welcome = new Label("Welcome, " + username + " (" + role + ")");
        welcome.setFont(Font.font("Arial", 20));
        welcome.setTextFill(Color.WHITE);

        Button vehiclesBtn = new Button("Manage Vehicles");
        Button customersBtn = new Button("Manage Customers");
        Button bookingsBtn = new Button("Booking System");
        Button paymentsBtn = new Button("Payments & Billing");
        Button reportsBtn = new Button("View Reports");
        Button logoutBtn = new Button("Logout");

        vehiclesBtn.setOnAction(e -> showInfo("Vehicle Management"));
        customersBtn.setOnAction(e -> showInfo("Customer Management"));
        bookingsBtn.setOnAction(e -> showInfo("Booking System"));
        paymentsBtn.setOnAction(e -> showInfo("Payment & Billing"));
        reportsBtn.setOnAction(e -> showReports());
        logoutBtn.setOnAction(e -> showLoginScreen());

        VBox menu = new VBox(10, welcome, vehiclesBtn, customersBtn, bookingsBtn, paymentsBtn, reportsBtn, logoutBtn);
        menu.setAlignment(Pos.CENTER_LEFT);
        menu.setPadding(new Insets(20));

        menu.getChildren().forEach(btn -> {
            if (btn instanceof Button) {
                ((Button) btn).setStyle("-fx-background-color: #6a11cb; -fx-text-fill: white;");
                ((Button) btn).setMinWidth(200);
            }
        });

        Label footer = new Label("© 2025 Vehicle Rental System | All Rights Reserved");
        footer.setTextFill(Color.WHITE);
        footer.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setLeft(menu);
        layout.setBottom(footer);
        layout.setStyle("-fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);");

        vehiclesBtn.setOnAction(e -> showVehicleManagement());
        customersBtn.setOnAction(e -> showCustomerManagement());
        bookingsBtn.setOnAction(e -> showBookingSystem());
        paymentsBtn.setOnAction(e -> showPayments());
        reportsBtn.setOnAction(e -> showReports());

        dashboardScene = new Scene(layout, 800, 500);
        primaryStage.setTitle("Dashboard - Vehicle Rental System");
        primaryStage.setScene(dashboardScene);
    }

    private void showVehicleManagement() {
        Stage vehicleStage = new Stage();
        vehicleStage.setTitle("Vehicle Management");

        Label titleLabel = new Label("Manage Vehicles");
        titleLabel.setFont(Font.font("Arial", 24));

        CheckBox availabilityCheckBox = new CheckBox("Available");

        TextField idField = new TextField();
        idField.setPromptText("Vehicle ID");
        TextField brandField = new TextField();
        brandField.setPromptText("Brand");
        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField priceField = new TextField();
        priceField.setPromptText("Rental Price");

        Button addBtn = new Button("Add Vehicle");
        Button updateBtn = new Button("Update Vehicle");
        Button deleteBtn = new Button("Delete Vehicle");
        Button clearBtn = new Button("Clear Fields");
        Button returnBtn = new Button("Return to Dashboard");

        TextArea vehicleListArea = new TextArea();
        vehicleListArea.setEditable(false);
        vehicleListArea.setPromptText("Vehicle List");

        // Clear fields action
        clearBtn.setOnAction(e -> {
            idField.clear();
            brandField.clear();
            modelField.clear();
            categoryField.clear();
            priceField.clear();
        });

        // Add action
        addBtn.setOnAction(e -> {
            String id = idField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();
            String category = categoryField.getText();
            String price = priceField.getText();
            boolean availability = availabilityCheckBox.isSelected();

            // Validate inputs
            if (id.isEmpty() || brand.isEmpty() || model.isEmpty() || category.isEmpty() || price.isEmpty()) {
                vehicleListArea.appendText("Error: All fields must be filled.\n");
                return;
            }

            // Insert vehicle into database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "INSERT INTO vehicles (vehicle_id, brand, model, category, rental_price, availability) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, id);
                stmt.setString(2, brand);
                stmt.setString(3, model);
                stmt.setString(4, category);
                stmt.setString(5, price);
                stmt.setBoolean(6, availability);
                stmt.executeUpdate();
                vehicleListArea.appendText("Added: " + brand + " " + model + " (Available: " + availability + ")\n");
            } catch (SQLException ex) {
                vehicleListArea.appendText("Error adding vehicle: " + ex.getMessage() + "\n");
            }
        });

        // Update action
        updateBtn.setOnAction(e -> {
            String id = idField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();
            String category = categoryField.getText();
            String price = priceField.getText();
            boolean availability = availabilityCheckBox.isSelected();

            // Check if the ID field is empty
            if (id.isEmpty()) {
                vehicleListArea.appendText("Error: Vehicle ID cannot be empty.\n");
                return;
            }

            // Update vehicle in database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "UPDATE vehicles SET brand = ?, model = ?, category = ?, rental_price = ?, availability = ? WHERE vehicle_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, brand);
                stmt.setString(2, model);
                stmt.setString(3, category);
                stmt.setString(4, price);
                stmt.setBoolean(5, availability);
                stmt.setString(6, id);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    vehicleListArea.appendText("Updated: " + brand + " " + model + "\n");
                } else {
                    vehicleListArea.appendText("Error: No vehicle found with ID " + id + ".\n");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                vehicleListArea.appendText("Error updating vehicle: " + ex.getMessage() + "\n");
            }
        });

        // Delete action
        deleteBtn.setOnAction(e -> {
            String id = idField.getText();
            if (id.isEmpty()) {
                vehicleListArea.appendText("Error: Vehicle ID cannot be empty.\n");
                return;
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "DELETE FROM vehicles WHERE vehicle_id=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, id);
                stmt.executeUpdate();
                vehicleListArea.appendText("Deleted vehicle with ID: " + id + "\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        returnBtn.setOnAction(e -> {
            vehicleStage.close();
            showDashboard("admin", "Admin");
        });

        VBox vehicleLayout = new VBox(10, titleLabel, idField, brandField, modelField, categoryField, priceField,
                availabilityCheckBox, addBtn, updateBtn, deleteBtn, clearBtn, returnBtn, vehicleListArea);
        vehicleLayout.setAlignment(Pos.CENTER);
        vehicleLayout.setPadding(new Insets(20));
        vehicleLayout.setStyle("-fx-background-color: linear-gradient(to right, Tomato, blue);");

        Scene vehicleScene = new Scene(vehicleLayout, 600, 400);
        vehicleStage.setScene(vehicleScene);
        vehicleStage.show();
    }

    private void showBookingForm(Vehicle vehicle) {
        Stage bookingStage = new Stage();
        bookingStage.setTitle("Booking Vehicle");

        Label titleLabel = new Label("Booking " + vehicle.getBrand() + " " + vehicle.getModel());
        titleLabel.setFont(Font.font("Arial", 24));

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();
        Button confirmBookingBtn = new Button("Confirm Booking");
        TextArea bookingMessage = new TextArea();
        bookingMessage.setEditable(false);
        Button backBtn = new Button("Back");

        confirmBookingBtn.setOnAction(e -> {
            String start = startDate.getValue().toString();
            String end = endDate.getValue().toString();

            // Insert booking into the database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "INSERT INTO bookings (vehicle_id, start_date, end_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, vehicle.getId());
                stmt.setString(2, start);
                stmt.setString(3, end);
                stmt.executeUpdate();
                bookingMessage.appendText("Booking confirmed for " + vehicle.getBrand() + " from " + start + " to " + end + "\n");
            } catch (SQLException ex) {
                bookingMessage.appendText("Error during booking: " + ex.getMessage() + "\n");
            }
        });
        backBtn.setOnAction(e -> {
            bookingStage.close();
            showCustomerDashboard("customer");
        });

        VBox bookingLayout = new VBox(10, titleLabel, startDate, endDate, confirmBookingBtn, bookingMessage);
        bookingLayout.setAlignment(Pos.CENTER);
        bookingLayout.setPadding(new Insets(20));
        bookingLayout.setStyle("-fx-background-color: linear-gradient(to right, Green, lightBlue);");

        Scene bookingScene = new Scene(bookingLayout, 400, 300);
        bookingStage.setScene(bookingScene);
        bookingStage.show();
    }

    private void showCustomerManagement() {
        Stage customerStage = new Stage();
        customerStage.setTitle("Customer Management");

        Label titleLabel = new Label("Manage Customers");
        titleLabel.setFont(Font.font("Arial", 24));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Information");
        TextField licenseField = new TextField();
        licenseField.setPromptText("Driving License Number");

        Button addBtn = new Button("Add Customer");
        Button updateBtn = new Button("Update Customer");
        Button deleteBtn = new Button("Delete Customer");
        Button clearBtn = new Button("Clear Fields");
        Button returnBtn = new Button("Return to Dashboard");

        TextArea customerListArea = new TextArea();
        customerListArea.setEditable(false);
        customerListArea.setPromptText("Customer List");

        clearBtn.setOnAction(e -> {
            nameField.clear();
            contactField.clear();
            licenseField.clear();
        });

        addBtn.setOnAction(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();
            String license = licenseField.getText();

            if (name.isEmpty() || contact.isEmpty() || license.isEmpty()) {
                customerListArea.appendText("Error: All fields must be filled.\n");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "INSERT INTO customers (name, contact_info, driving_license_number) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, contact);
                stmt.setString(3, license);
                stmt.executeUpdate();
                customerListArea.appendText("Added: " + name + "\n");
            } catch (SQLException ex) {
                customerListArea.appendText("Error adding customer: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        updateBtn.setOnAction(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();
            String license = licenseField.getText();

            if (name.isEmpty() || contact.isEmpty() || license.isEmpty()) {
                customerListArea.appendText("Error: All fields must be filled to update a customer.\n");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "UPDATE customers SET name=?, contact_info=? WHERE driving_license_number=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, contact);
                stmt.setString(3, license);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    customerListArea.appendText("Updated customer with license: " + license + "\n");
                } else {
                    customerListArea.appendText("No customer found with license: " + license + "\n");
                }
            } catch (SQLException ex) {
                customerListArea.appendText("Error updating customer: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        deleteBtn.setOnAction(e -> {
            String license = licenseField.getText();

            if (license.isEmpty()) {
                customerListArea.appendText("Error: Driving License Number must be provided for deletion.\n");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "DELETE FROM customers WHERE driving_license_number=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, license);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    customerListArea.appendText("Deleted customer with license: " + license + "\n");
                } else {
                    customerListArea.appendText("No customer found with license: " + license + "\n");
                }
            } catch (SQLException ex) {
                customerListArea.appendText("Error deleting customer: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        });

        returnBtn.setOnAction(e -> {
            customerStage.close();
            showDashboard("admin", "Admin");
        });

        VBox customerLayout = new VBox(10, titleLabel, nameField, contactField, licenseField,
                addBtn, updateBtn, deleteBtn, clearBtn, returnBtn, customerListArea);
        customerLayout.setAlignment(Pos.CENTER);
        customerLayout.setPadding(new Insets(20));

        Scene customerScene = new Scene(customerLayout, 600, 400);
        customerStage.setScene(customerScene);
        customerStage.show();
    }

    private void showBookingSystem() {
        Stage bookingStage = new Stage();
        bookingStage.setTitle("Booking System");

        Label titleLabel = new Label("Book a Vehicle");
        titleLabel.setFont(Font.font("Arial", 24));

        ComboBox<String> vehicleComboBox = new ComboBox<>();
        loadAvailableVehicles(vehicleComboBox);

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();

        Button bookBtn = new Button("Book Vehicle");
        TextArea bookingListArea = new TextArea();
        bookingListArea.setEditable(false);
        Button returnBtn = new Button("Return to Dashboard");

        bookBtn.setOnAction(e -> {
            String vehicleId = vehicleComboBox.getValue();
            String start = startDate.getValue().toString();
            String end = endDate.getValue().toString();

            if (vehicleId == null || start == null || end == null) {
                bookingListArea.appendText("Error: Please select all required fields.\n");
                return;
            }

            // Insert booking into database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String query = "INSERT INTO bookings (vehicle_id, start_date, end_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, vehicleId);
                stmt.setString(2, start);
                stmt.setString(3, end);
                stmt.executeUpdate();
                bookingListArea.appendText("Booked: " + vehicleId + " from " + start + " to " + end + "\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        returnBtn.setOnAction(e -> {
            bookingStage.close();
            showDashboard("admin", "Admin");
        });

        VBox bookingLayout = new VBox(10, titleLabel, vehicleComboBox, startDate, endDate,
                bookBtn, bookingListArea, returnBtn);
        bookingLayout.setAlignment(Pos.CENTER);
        bookingLayout.setPadding(new Insets(20));
        bookingLayout.setStyle("-fx-background-color: linear-gradient(to right, Green, lightBlue);");

        Scene bookingScene = new Scene(bookingLayout, 600, 400);
        bookingStage.setScene(bookingScene);
        bookingStage.show();
    }

    private VBox loadAvailableVehicles(ComboBox<String> comboBox) {
        VBox vehicleListLayout = new VBox(10);
        String type = new String();
        Label titleLabel = new Label(type + " Available for Booking");
        titleLabel.setFont(Font.font("Arial", 24));

        // Create a map of vehicle types to their sample images
        Map<String, String[]> vehicleImages = new HashMap<>();
        vehicleImages.put("Cars", new String[]{
                "/car1.jpg",
                "/car3.jpeg"
        });
        vehicleImages.put("Trucks", new String[]{
                "/truck1.webp",
                "/truck2.jpg"
        });
        vehicleImages.put("Vans", new String[]{
                "/van1.webp",
                "/van2.jpeg"
        });
        vehicleImages.put("Bikes", new String[]{
                "/bike1.jpeg",
                "/bike2.jpg"
        });
        // Fetch vehicles based on type
        List<Vehicle> vehicles = fetchVehiclesByType(type);

        // Check if the vehicles list is null or empty
        if (vehicles == null || vehicles.isEmpty()) {
            // Show sample vehicles with images if none in database
            String[] images = vehicleImages.get(type);
            if (images != null) {
                for (int i = 0; i < Math.min(2, images.length); i++) {
                    final int currentIndex = i;
                    try {
                        Image image = new Image(getClass().getResourceAsStream(images[i]));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(150);
                        imageView.setFitWidth(200);

                        String vehicleName;
                        if (type.equals("Cars")) {
                            vehicleName = (i == 0) ? "Toyota Camry" : "BMW";
                        } else if (type.equals("Trucks")) {
                            vehicleName = (i == 0) ? "Ford F-150" : "Nizzan Navara";
                        } else if (type.equals("Vans")) {
                            vehicleName = (i == 0) ? "Mercedes Sprinter" : "Ford Transit";
                        } else if (type.equals("Bikes")) {
                            vehicleName = (i == 0) ? "Harley Davidson" : "Yamaha R1";
                        } else {
                            vehicleName = "";
                        }

                        Button bookButton = new Button("Book " + vehicleName);
                        bookButton.setOnAction(e -> {
                            Vehicle sampleVehicle = new Vehicle(
                                    "SAMPLE-" + type + "-" + (currentIndex+1),
                                    vehicleName.split(" ")[0],
                                    vehicleName.split(" ")[1],
                                    type,
                                    100.00 * (currentIndex+1),
                                    true, "/com/example/vehiclehiresystem/" + type.toLowerCase() + (currentIndex+1) + ".jpg"
                            );
                            showBookingForm(sampleVehicle);
                        });

                        VBox vehicleBox = new VBox(10, imageView, bookButton);
                        vehicleBox.setAlignment(Pos.CENTER);
                        vehicleListLayout.getChildren().add(vehicleBox);
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                    }
                }
            } else {
                vehicleListLayout.getChildren().add(new Label("No vehicles available for this type."));
            }
        } else {
            // Show vehicles from database
            for (Vehicle vehicle : vehicles) {
                try {
                    HBox vehicleBox = new HBox(20);
                    vehicleBox.setAlignment(Pos.CENTER_LEFT);

                    // Load vehicle image if path exists
                    if (vehicle.getImagePath() != null && !vehicle.getImagePath().isEmpty()) {
                        Image image = new Image(getClass().getResourceAsStream(vehicle.getImagePath()));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(150);
                        imageView.setFitWidth(200);
                        vehicleBox.getChildren().add(imageView);
                    }

                    VBox infoBox = new VBox(10);
                    infoBox.getChildren().add(new Label("Brand: " + vehicle.getBrand()));
                    infoBox.getChildren().add(new Label("Model: " + vehicle.getModel()));
                    infoBox.getChildren().add(new Label("Price: $" + vehicle.getRentalPrice() + "/day"));

                    Button bookButton = new Button("Book Now");
                    bookButton.setOnAction(e -> showBookingForm(vehicle));
                    infoBox.getChildren().add(bookButton);

                    vehicleBox.getChildren().add(infoBox);
                    vehicleListLayout.getChildren().add(vehicleBox);
                } catch (Exception e) {
                    System.err.println("Error displaying vehicle: " + e.getMessage());
                }
            }
        }

        vehicleListLayout.getChildren().add(0, titleLabel);
        vehicleListLayout.setAlignment(Pos.CENTER);
        vehicleListLayout.setPadding(new Insets(20));
        return vehicleListLayout;
    }
    private void showPayments() {
        Stage paymentStage = new Stage();
        paymentStage.setTitle("Payments & Billing");

        Label titleLabel = new Label("Process Payment");
        titleLabel.setFont(Font.font("Arial", 24));

        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("Booking ID");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        Button payBtn = new Button("Make Payment");
        TextArea paymentListArea = new TextArea();
        paymentListArea.setEditable(false);
        Button returnBtn = new Button("Return to Dashboard");

        payBtn.setOnAction(e -> {
            String bookingId = bookingIdField.getText();
            String amountText = amountField.getText();

            if (bookingId.isEmpty() || amountText.isEmpty()) {
                paymentListArea.appendText("Error: Both fields must be filled.\n");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                // Payment processing logic, update payment status in the database
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String query = "INSERT INTO payments (booking_id, amount) VALUES (?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, bookingId);
                    stmt.setDouble(2, amount);
                    stmt.executeUpdate();
                    paymentListArea.appendText("Payment processed for Booking ID: " + bookingId + " Amount: $" + amount + "\n");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                paymentListArea.appendText("Error: Amount must be a valid number.\n");
            }
        });

        returnBtn.setOnAction(e -> {
            paymentStage.close();
            showDashboard("admin", "Admin");
        });

        VBox paymentLayout = new VBox(10, titleLabel, bookingIdField, amountField,
                payBtn, paymentListArea, returnBtn);
        paymentLayout.setAlignment(Pos.CENTER);
        paymentLayout.setPadding(new Insets(20));

        Scene paymentScene = new Scene(paymentLayout, 600, 400);
        paymentStage.setScene(paymentScene);
        paymentStage.show();
    }

    // Placeholder Info Window
    private void showInfo(String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(title + " feature will be implemented here.");
        alert.showAndWait();
    }

    // Reports with Charts
    private void showReports() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Revenue Report");
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");

        XYChart.Series<String, Number> data = new XYChart.Series<>();
        data.setName("2025");
        data.getData().add(new XYChart.Data<>("Jan", 1200));
        data.getData().add(new XYChart.Data<>("Feb", 1800));
        data.getData().add(new XYChart.Data<>("Mar", 1600));
        chart.getData().add(data);

        VBox layout = new VBox(10, chart);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to right, #4e54c8, #8f94fb);");

        Scene reportScene = new Scene(layout, 800, 500);
        Stage reportStage = new Stage();
        reportStage.setTitle("Reports");
        reportStage.setScene(reportScene);
        reportStage.show();
    }

    // Placeholder method for fetching vehicles by type
    private List<Vehicle> fetchVehiclesByType(String type) {
        List<Vehicle> vehicles = new ArrayList<>();
        String query = "SELECT * FROM vehicles WHERE category = ? AND availability = TRUE"; // Adjust the query as needed

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("vehicle_id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                String category = rs.getString("category");
                double rentalPrice = rs.getDouble("rental_price");
                boolean available = rs.getBoolean("availability");
                String imagePath = rs.getString("image_path"); // Add this column to your DB query

                vehicles.add(new Vehicle(id, brand, model, category, rentalPrice, available, imagePath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vehicles; // Return the list of vehicles
    }
}

// Placeholder Vehicle class
class Vehicle {
    private String id;
    private String brand;
    private String model;
    private String category;
    private double rentalPrice;
    private boolean available;
    private String imagePath; // New field for image path


    public Vehicle(String id, String brand, String model, String category, double rentalPrice, boolean available, String imagePath) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.rentalPrice = rentalPrice;
        this.available = available;
        this.imagePath = imagePath; // Initialize the image path
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getImagePath() {
        return imagePath;
    } // Getter for image path
}
// Add this enum class to define payment methods
enum PaymentMethod {
    CASH, CREDIT_CARD, ONLINE
}

// Add this class to represent an invoice
class Invoice {
    private String invoiceId;
    private String bookingId;
    private String customerName;
    private String vehicleDetails;
    private double amount;
    private PaymentMethod paymentMethod;
    private Date paymentDate;

    public Invoice(String invoiceId, String bookingId, String customerName,
                   String vehicleDetails, double amount,
                   PaymentMethod paymentMethod, Date paymentDate) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.vehicleDetails = vehicleDetails;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }
    // Method to generate printable invoice text
    public String generateInvoiceText() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("       VEHICLE RENTAL INVOICE\n");
        sb.append("================================\n\n");
        sb.append(String.format("Invoice ID: %s\n", invoiceId));
        sb.append(String.format("Booking ID: %s\n", bookingId));
        sb.append(String.format("Customer: %s\n", customerName));
        sb.append(String.format("Vehicle: %s\n", vehicleDetails));
        sb.append(String.format("Amount Paid: $%.2f\n", amount));
        sb.append(String.format("Payment Method: %s\n", paymentMethod.toString()));
        sb.append(String.format("Payment Date: %s\n", paymentDate.toString()));
        sb.append("\n================================\n");
        sb.append("Thank you for your business!\n");
        sb.append("================================\n");
        return sb.toString();
    }
    public String getInvoiceId() {
        return invoiceId;
    }
}
