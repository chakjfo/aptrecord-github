package com.example.aptrecord.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import com.example.aptrecord.Database.DBConnection; // Assuming you have this package for DB connection
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Signup {

    public TextField roomNumberField;
    @FXML
    private TextField emailField;

    @FXML
    private TextField contactNumberField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Text statusMessage;

    // Database connection instance
    private final Connection connection = DBConnection.getConnection(); // Assumes you have a method to get the connection

    public Signup() throws SQLException {
    }

    @FXML
    private void initialize() {
        signupButton.setOnAction(event -> handleSignup());
        loginLink.setOnAction(event -> redirectToLogin());
    }

    private void handleSignup() {
        String email = emailField.getText().trim();
        String contactnum = contactNumberField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = passwordField.getText().trim();
        String roomNumber = roomNumberField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || contactnum.isEmpty() || roomNumber.isEmpty()) {
            statusMessage.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusMessage.setText("Passwords do not match.");
            return;
        }

        try {
            if (registerUser(email, firstName, lastName, password, contactnum, roomNumber)) {
                statusMessage.setText("Registration successful! You can now log in.");
            } else {
                statusMessage.setText("Registration failed. Email may already be in use.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusMessage.setText("Error occurred: " + e.getMessage());
        }
    }

    private boolean registerUser(String email, String firstName, String lastName, String password, String contactNumber, String roomNumber) throws SQLException {
        String query = "INSERT INTO users (email, password) VALUES (?, ?);";
        String queryUserAccount = "INSERT INTO user_account (first_name, last_name, email, apt_date_acquired, contact_number, room_number) VALUES (?, ?, ?, CURRENT_DATE, ?, ?);";

        try (PreparedStatement userStmt = connection.prepareStatement(query);
             PreparedStatement accountStmt = connection.prepareStatement(queryUserAccount)) {

            // Insert into Users table
            userStmt.setString(1, email);
            userStmt.setString(2, password);
            int rowsAffectedUsers = userStmt.executeUpdate();

            // Insert into User_Account table
            accountStmt.setString(1, firstName);
            accountStmt.setString(2, lastName);
            accountStmt.setString(3, email);
            accountStmt.setString(4, contactNumber);
            accountStmt.setString(5, roomNumber);
            int rowsAffectedAccount = accountStmt.executeUpdate();

            // Check if both inserts were successful
            return rowsAffectedUsers > 0 && rowsAffectedAccount > 0;
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/LoginPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusMessage.setText("Error loading the Login page. Please try again.");
        }
    }

    public void loginLink(ActionEvent actionEvent) {
        redirectToLogin();
    }

    public void signUp(ActionEvent actionEvent) {
        handleSignup();
    }
}