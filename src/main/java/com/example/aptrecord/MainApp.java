package com.example.aptrecord;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/aptrecord/LoginPage.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Apartment Management - Login");
            primaryStage.setScene(new Scene(root, 1000, 600)); // Width and height set based on your FXML size
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
