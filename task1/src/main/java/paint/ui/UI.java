package paint.ui;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UI extends BorderPane {
    private Tools tools;

    public UI(Stage stage) {
        super();
        tools = new Tools(stage);
        this.setTop(tools);
    }
}
