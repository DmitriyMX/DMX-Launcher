package ru.dmitriymx.mclauncher.gui2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiApp extends Application {
    private static final String LAUNCHER_VERSION = "v0.8.0 b1";

    public void launchApp(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResourceAsStream(
                "/ru/dmitriymx/mclauncher/gui2/main_frame.fxml"));

        primaryStage.setTitle("DmitriyMX Minecraft Server [" + LAUNCHER_VERSION + "]");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
