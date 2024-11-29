package com.example.aptrecord.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Example of DBConnection class
public class DBConnection {
    private static Connection connection;

    // Static method to return the connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Set up your DB connection here (Make sure to use correct credentials)
                String url = "jdbc:mysql://localhost:3306/aptmanagement"; // Database URL
                String user = "root"; // Database username
                String password = ""; // Database password

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Database connected!");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Connection failed!");
                throw new SQLException("Unable to connect to the database.");
            }
        }
        return connection;
    }
}
