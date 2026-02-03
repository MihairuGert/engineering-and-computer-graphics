package paint;

import javafx.application.Application;
import javafx.stage.Stage;

public class Paint extends Application {
    final private double stageWidth = 1000;
    final private double stageHeight = 750;

    @Override
    public void start(Stage stage) {
        initWindow(stage);
    }

    private void initWindow(Stage stage) {
        stage.setTitle("Пыинт");
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        stage.setResizable(false);
        stage.show();
    }

    public void run() {
        launch();
    }
}
