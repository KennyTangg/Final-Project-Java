package com.contactsmanager.contactsmanagerfx;

import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("views/MainDisplay.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 700);
        stage.setTitle("Contacts Manager FX");
        stage.setScene(scene);

        // Controller access
        AppDisplayController controller = fxmlLoader.getController();

        // Disable all settings until user chooses a view
        controller.reloadTableByCurrentMode();

        // Set up contact books for each ADT
        AdjacencyMatrixGraphCB contactsBook1 = new AdjacencyMatrixGraphCB(50, true);
        AdjacencyMatrixGraphCB contactsBook2 = new AdjacencyMatrixGraphCB(50, false);
        AdjacencyListGraphCB contactsBook3 = new AdjacencyListGraphCB(true);
        AdjacencyListGraphCB contactsBook4 = new AdjacencyListGraphCB(false);
        HashMapCB contactsBook5 = new HashMapCB();

        controller.setAdjMatrixGraph(contactsBook1, true);
        controller.setAdjMatrixGraph(contactsBook2, false);
        controller.setAdjListGraph(contactsBook3, true);
        controller.setAdjListGraph(contactsBook4, false);
        controller.setHashMap(contactsBook5);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}