package nsu.task5.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.task5.ui.RaytracingWindow;

public class RaytracingApp extends Application {
    private static final double INITIAL_WIDTH = 1100;
    private static final double INITIAL_HEIGHT = 780;
    private static final double MIN_WIDTH = 720;
    private static final double MIN_HEIGHT = 520;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        RaytracingWindow window = new RaytracingWindow(stage);
        stage.setTitle("ICGRaytracing");
        stage.setScene(new Scene(window, INITIAL_WIDTH, INITIAL_HEIGHT));
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setOnCloseRequest(event -> window.shutdown());
        stage.show();
        window.refreshView();
    }
}
