package com.contactsmanager.visualization;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Basic JavaFX Application");

        BorderPane root = createBasicUI();
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BorderPane createBasicUI() {
        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Basic JavaFX Application");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label welcomeLabel = new Label("Welcome to the basic JavaFX application!");
        welcomeLabel.setStyle("-fx-font-size: 16px;");

        content.getChildren().addAll(titleLabel, welcomeLabel);
        root.setCenter(content);
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
