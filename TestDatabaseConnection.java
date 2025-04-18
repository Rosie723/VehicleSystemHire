package com.example.vehiclehiresystem;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        DatabaseConnection dbConnection = new DatabaseConnection();

        // Test the connection
        if (dbConnection.getConnection() != null) {
            System.out.println("Connection test successful!");
        } else {
            System.out.println("Connection test failed!");
        }

        // Close the connection
        dbConnection.closeConnection();
    }
}