package paint;

import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import paint.ui.UI;

public class Paint extends Application {
    final private double stageWidth = 1000;
    final private double stageHeight = 750;
    private UI ui;
    private Stage mainStage;

    @Override
    public void start(Stage stage) {
        ui = new UI(stage);
        initWindow(stage);
    }

    private void initWindow(Stage stage) {
        stage.setTitle("Пыинт");
        stage.setScene(new Scene(ui, stageWidth, stageHeight));
        stage.setResizable(false);
        stage.show();
    }

    public void run() {
        launch();
    }
}
