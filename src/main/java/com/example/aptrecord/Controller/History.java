package com.example.aptrecord.Controller;

import com.example.aptrecord.Database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class History {

    @FXML
    private Label roomnumber;

    private int userId;

    // Method to initialize the page with user data
    public void initializeUserData(int userId, String roomNumber) {
        this.userId = userId;
        roomnumber.setText(roomNumber);
    }

    @FXML
    private void setHistorybtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/DashboardPage.fxml"));
            Parent root = loader.load();

            Dashboard DashboardController = loader.getController();
            DashboardController.initializeUserData(userId, roomnumber.getText()); // Pass room number

            Stage stage = new Stage();
            stage.setTitle("History");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open History page.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void setPaybtn(ActionEvent actionEvent) {
    }
}
