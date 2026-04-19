package nsu.wireframe.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.wireframe.model.AppState;
import nsu.wireframe.ui.MainWindow;

public class MainApp extends Application {
    private static final double INITIAL_WIDTH = 1000;
    private static final double INITIAL_HEIGHT = 750;
    private static final double MIN_WIDTH = 640;
    private static final double MIN_HEIGHT = 480;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        AppState state = AppState.createDefault();
        MainWindow mainWindow = new MainWindow(stage, state);

        stage.setTitle("ICGWireframe");
        stage.setScene(new Scene(mainWindow, INITIAL_WIDTH, INITIAL_HEIGHT));
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();

        mainWindow.refreshScene();
    }
}
