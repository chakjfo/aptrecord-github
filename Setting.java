package com.example.aptrecord.Controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import com.example.aptrecord.Database.DBConnection;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Setting {

    @FXML
    private Label roomnumber;
    @FXML
    private TextField firstname;
    @FXML
    private TextField lastname;
    @FXML
    private TextField email;
    @FXML
    private TextField roomnum;
    @FXML
    private TextField aptrequired;
    @FXML
    private TextField contactnum;
    @FXML
    private Button savechanges;

    private int userId; // User ID for database operations

    // Original field values for change tracking
    private String originalFirstName;
    private String originalLastName;
    private String originalEmail;
    private String originalRoomNumber;
    private String originalAptDateAcquired;
    private String originalContactNumber;


    public void initializeUserData(int userId, String roomNumber) {
        this.userId = userId;
        roomnumber.setText(roomNumber);
        loadUserData();
    }

    @FXML
    private void setHomebtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/DashboardPage.fxml"));
            Parent root = loader.load();

            Dashboard dashboardController = loader.getController();

            // Debug: Print userId to verify
            System.out.println("User ID in Payment Page: " + userId);

            // Retrieve the first name
            String firstName = getUserFirstName(userId);

            // Debug: Print firstName
            System.out.println("First Name Retrieved: " + firstName);

            // Verify both userId and firstName before setting
            if (firstName != null && !firstName.isEmpty()) {
                dashboardController.setFirstName(firstName, userId);
            }

            // Always initialize user data
            dashboardController.initializeUserData(userId, roomnumber.getText());

            Stage stage = new Stage();
            stage.setTitle("Dashboard");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open Dashboard page.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Method to retrieve user's first name from the database
    private String getUserFirstName(int userId) {
        String firstName = null;
        String query = "SELECT first_name FROM user_account WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    firstName = resultSet.getString("first_name");
                    System.out.println("First Name Found: " + firstName); // Debug print
                } else {
                    System.out.println("No first name found for userId: " + userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Error retrieving first name: " + e.getMessage());
        }

        return firstName;
    }


    private void loadUserData() {
        String query = "SELECT first_name, last_name, email, room_number, apt_date_acquired, contact_number "
                + "FROM user_account WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Populate fields
                    firstname.setText(rs.getString("first_name"));
                    lastname.setText(rs.getString("last_name"));
                    email.setText(rs.getString("email"));
                    roomnum.setText(rs.getString("room_number"));
                    aptrequired.setText(rs.getString("apt_date_acquired"));
                    contactnum.setText(rs.getString("contact_number"));

                    // Store original values
                    originalFirstName = rs.getString("first_name");
                    originalLastName = rs.getString("last_name");
                    originalEmail = rs.getString("email");
                    originalRoomNumber = rs.getString("room_number");
                    originalAptDateAcquired = rs.getString("apt_date_acquired");
                    originalContactNumber = rs.getString("contact_number");
                } else {
                    showErrorAlert("No Data Found", "Unable to retrieve user information.");
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error loading user data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveChanges() {
        String changedFields = getChangedFields();

        if (!changedFields.isEmpty()) {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Changes");
            confirmationAlert.setHeaderText("You have made changes to the following fields:");
            confirmationAlert.setContentText(changedFields);

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    saveChanges();
                }
            });
        } else {
            showInformationAlert("No Changes", "No changes detected to save.");
        }
    }

    private String getChangedFields() {
        StringBuilder changedFields = new StringBuilder();

        if (!firstname.getText().equals(originalFirstName)) changedFields.append("First Name\n");
        if (!lastname.getText().equals(originalLastName)) changedFields.append("Last Name\n");
        if (!email.getText().equals(originalEmail)) changedFields.append("Email\n");
        if (!roomnum.getText().equals(originalRoomNumber)) changedFields.append("Room Number\n");
        if (!aptrequired.getText().equals(originalAptDateAcquired)) changedFields.append("Date Acquired\n");
        if (!contactnum.getText().equals(originalContactNumber)) changedFields.append("Contact Number\n");

        return changedFields.toString();
    }

    private void saveChanges() {
        String updateQuery = "UPDATE user_account SET first_name = ?, last_name = ?, email = ?, "
                + "room_number = ?, apt_date_acquired = ?, contact_number = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            if (!validateInputs()) return;

            stmt.setString(1, firstname.getText().trim());
            stmt.setString(2, lastname.getText().trim());
            stmt.setString(3, email.getText().trim());
            stmt.setString(4, roomnum.getText().trim());
            stmt.setString(5, aptrequired.getText().trim());
            stmt.setString(6, contactnum.getText().trim());
            stmt.setInt(7, userId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showSuccessAlert("Success", "User data updated successfully.");
                loadUserData();
            } else {
                showErrorAlert("Update Failed", "No rows were updated.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error updating user data: " + e.getMessage());
        }
    }


    private boolean validateInputs() {
        if (firstname.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "First Name cannot be empty.");
            return false;
        }
        if (lastname.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Last Name cannot be empty.");
            return false;
        }
        if (email.getText().trim().isEmpty() || !isValidEmail(email.getText().trim())) {
            showErrorAlert("Validation Error", "Please enter a valid email address.");
            return false;
        }
        if (contactnum.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Contact Number cannot be empty.");
            return false;
        }
        return true;
    }


    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // Utility methods for showing alerts
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.show();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showInformationAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
