package com.example.aptrecord.Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import com.example.aptrecord.Database.DBConnection;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Text statusMessage;

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerLink.setOnAction(this::registerLink);
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusMessage.setText("Please fill in all fields.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (validateLogin(conn, email, password)) {
                String firstName = fetchFirstName(conn, email);
                int userId = fetchUserId(conn, email);
                statusMessage.setText("Welcome, " + firstName + "!");
                loadDashboard(firstName, userId);
            } else {
                statusMessage.setText("Invalid email or password. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusMessage.setText("Error occurred: " + e.getMessage());
        } finally {
            // Ensure the connection is closed after all operations are completed
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String fetchFirstName(Connection conn, String email) {
        String query = "SELECT first_name FROM user_account WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("first_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "user";  // Default return if not found
    }

    // Fetch the user ID based on the email, using the open connection
    private int fetchUserId(Connection conn, String email) {
        int userId = -1;
        String query = "SELECT id FROM user_account WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("id");  // Return the user ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    // Validate the login credentials by checking the email and password
    private boolean validateLogin(Connection conn, String email, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // If any record is found, the login is valid
            }
        }
    }

    // Load the Dashboard page after successful login
    private void loadDashboard(String firstName, int userId) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/aptrecord/DashboardPage.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML file not found!");
                statusMessage.setText("Error: FXML file not found.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();
            Dashboard dashboardController = fxmlLoader.getController();
            dashboardController.setFirstName(firstName, userId);  // Pass both firstName and userId

            Stage stage = new Stage();
            stage.setTitle("Dashboard");
            stage.setScene(new Scene(root));
            stage.show();

            // Close the login window after opening the dashboard window
            Stage currentStage = (Stage) passwordField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            statusMessage.setText("Failed to load Dashboard: " + e.getMessage());
        }

    }

    @FXML
    private void registerLink(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/SignupPage.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Sign Up");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleForgotPassword() {
        // Add the necessary logic to handle the forgot password functionality
    }
}
