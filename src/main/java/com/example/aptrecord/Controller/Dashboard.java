package com.example.aptrecord.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.example.aptrecord.Database.DBConnection;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dashboard {

    public Label electricitypayment;

    public Button settingbtn;
    public Button historybtn;
    public Button paybtn;
    public Label roomnumber;

    @FXML
    private Label totalpayment;

    @FXML
    private Label waterpayment;

    @FXML
    private Label firstname;

    @FXML
    private LineChart<String, Number> waterchart;

    @FXML
    private LineChart<String, Number> electricitychart;


    private int userId;  // store the logged-in user ID

    // Updated to accept both firstName and userId
    public void setFirstName(String firstName, int userId) {
        this.userId = userId; // Set user ID
        firstname.setText(firstName); // Set first name on the label
        loadWaterData(); // Load water data for the user
        loadElectricityData(); // Load electricity data for the user
        loadRoomNumber();
    }

    public void initializeUserData(int userId, String roomNumber) {
        this.userId = userId;
        roomnumber.setText(roomNumber);
        settingbtn.setOnAction(event -> goToSettingPage(event));
        historybtn.setOnAction(event -> setHistorybtn(event));
        paybtn.setOnAction(event -> setPaybtn(event));
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

            // Close the current stage (Dashboard)
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
    private void setHistorybtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/HistoryPage.fxml"));
            Parent root = loader.load();

            Setting settingController = loader.getController();
            settingController.initializeUserData(userId, roomnumber.getText()); // Pass room number

            Stage stage = new Stage();
            stage.setTitle("History");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open History page.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Method to handle navigation to SettingPage.fxml
    private void goToSettingPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/SettingsPage.fxml"));
            Parent root = loader.load();

            Setting settingController = loader.getController();
            settingController.initializeUserData(userId, roomnumber.getText()); // Pass room number
            settingController.initializeUserData(userId, roomnumber.getText()); // Pass room number

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open settings page.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadWaterData() {
        String chartQuery = "SELECT MONTH(month) AS month, SUM(amount) AS total_amount " +
                "FROM water WHERE user_id = ? GROUP BY MONTH(month)";
        String sumQuery = "SELECT SUM(amount) AS total_payment FROM water WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            // Execute chart query (existing functionality)
            try (PreparedStatement chartStmt = conn.prepareStatement(chartQuery)) {
                chartStmt.setInt(1, userId);
                try (ResultSet rs = chartStmt.executeQuery()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Water Usage");

                    while (rs.next()) {
                        int monthNumber = rs.getInt("month");
                        double totalAmount = rs.getDouble("total_amount");
                        String monthName = convertMonthToWord(monthNumber);
                        series.getData().add(new XYChart.Data<>(monthName, totalAmount));
                    }
                    waterchart.getData().add(series);
                }
            }


            try (PreparedStatement sumStmt = conn.prepareStatement(sumQuery)) {
                sumStmt.setInt(1, userId);
                try (ResultSet sumRs = sumStmt.executeQuery()) {
                    if (sumRs.next()) {
                        double totalPayment = sumRs.getDouble("total_payment");
                        waterpayment.setText(String.format("₱%.2f", totalPayment)); // Display total amount in the label
                        updateTotalPayment(getWaterPayment(), totalPayment); // Update total
                    }
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, "Unable to load water data or calculate total payment.");
        }
    }

    private void loadElectricityData() {
        String chartQuery = "SELECT MONTH(month) AS month, SUM(amount) AS total_amount " +
                "FROM electricity WHERE user_id = ? GROUP BY MONTH(month)";
        String sumQuery = "SELECT SUM(amount) AS total_payment FROM electricity WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement chartStmt = conn.prepareStatement(chartQuery)) {
                chartStmt.setInt(1, userId);
                try (ResultSet rs = chartStmt.executeQuery()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Electricity Usage");

                    while (rs.next()) {
                        int monthNumber = rs.getInt("month");
                        double totalAmount = rs.getDouble("total_amount");
                        String monthName = convertMonthToWord(monthNumber);
                        series.getData().add(new XYChart.Data<>(monthName, totalAmount));
                    }
                    electricitychart.getData().add(series);
                }
            }

            try (PreparedStatement sumStmt = conn.prepareStatement(sumQuery)) {
                sumStmt.setInt(1, userId);
                try (ResultSet sumRs = sumStmt.executeQuery()) {
                    if (sumRs.next()) {
                        double totalPayment = sumRs.getDouble("total_payment");
                        electricitypayment.setText(String.format("₱%.2f", totalPayment));
                        updateTotalPayment(getElectricityPayment(), totalPayment); // Update total
                    }
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, "Unable to load electricity data or calculate total payment.");
        }
    }

    private double getWaterPayment() {
        try {
            String text = waterpayment.getText().replace("₱", "").trim();
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double getElectricityPayment() {
        try {
            String text = electricitypayment.getText().replace("₱", "").trim();
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void updateTotalPayment(double waterPayment, double electricityPayment) {
        double total = waterPayment + electricityPayment;
        totalpayment.setText(String.format("₱%.2f", total));
    }

    private String convertMonthToWord(int monthNumber) {
        switch (monthNumber) {
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "Mar";
            case 4: return "Apr";
            case 5: return "May";
            case 6: return "Jun";
            case 7: return "Jul";
            case 8: return "Aug";
            case 9: return "Sept";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
            default: return "";
        }
    }

    private void loadRoomNumber() {
        String query = "SELECT room_number FROM user_account WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String roomNumberValue = rs.getString("room_number");
                    roomnumber.setText(roomNumberValue); // Display room number in the dashboard
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Unable to load room number.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleSQLException(SQLException e, String customMessage) {
        System.out.println(customMessage);
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("An error occurred while fetching data.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}