package com.contactsmanager.visualization;

public class VisualizationLauncher {

    public static void launchApp() {
        new Thread(() -> {
            try {
                javafx.application.Application.launch(App.class);
            } catch (Exception e) {
                System.err.println("Error launching CSV visualization: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
