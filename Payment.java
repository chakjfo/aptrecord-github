package com.example.aptrecord.Controller;

import com.example.aptrecord.Database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Payment {

    @FXML
    private Label roomnumber;
    @FXML
    private DatePicker watermonth;
    @FXML
    private TextField wateramount;
    @FXML
    private TextField waterkwph;
    @FXML
    private DatePicker electricitymonth;
    @FXML
    private TextField electricityamount;
    @FXML
    private TextField electricitykwph;

    private int userId;

    // Method to initialize the page with user data
    public void initializeUserData(int userId, String roomNumber) {
        this.userId = userId;
        roomnumber.setText(roomNumber);
        watermonth.setOnAction(event -> updateWaterDetails());
        electricitymonth.setOnAction(event -> updateElectricityDetails());
    }

    @FXML
    private void setPaybtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/PaymentPage.fxml"));
            Parent root = loader.load();

            Payment paymentController = loader.getController();
            paymentController.initializeUserData(userId, roomnumber.getText());

            Stage stage = new Stage();
            stage.setTitle("Pay Here");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open Payment page.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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

    private void updateWaterDetails() {
        LocalDate selectedMonth = watermonth.getValue();
        if (selectedMonth != null) {
            String month = selectedMonth.getYear() + "-" + String.format("%02d", selectedMonth.getMonthValue()) + "-01";
            String query = "SELECT amount, kwph FROM water WHERE DATE_FORMAT(month, '%Y-%m-%d') = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, month);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    wateramount.setText(String.valueOf(resultSet.getDouble("amount")));
                    waterkwph.setText(String.valueOf(resultSet.getDouble("kwph")));
                } else {
                    wateramount.setText("N/A");
                    waterkwph.setText("N/A");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateElectricityDetails() {
        LocalDate selectedMonth = electricitymonth.getValue();
        if (selectedMonth != null) {
            String month = selectedMonth.getYear() + "-" + String.format("%02d", selectedMonth.getMonthValue()) + "-01";
            String query = "SELECT amount, kwph FROM electricity WHERE DATE_FORMAT(month, '%Y-%m-%d') = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, month);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    electricityamount.setText(String.valueOf(resultSet.getDouble("amount")));
                    electricitykwph.setText(String.valueOf(resultSet.getDouble("kwph")));
                } else {
                    electricityamount.setText("N/A");
                    electricitykwph.setText("N/A");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
