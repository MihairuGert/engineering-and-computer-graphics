package paint.ui;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class UI extends BorderPane {
    private Menu menu;
    private DrawPanel drawPanel;

    public UI(Stage stage) {
        super();
        drawPanel = new DrawPanel(1000, 1000);
        menu = new Menu(stage, drawPanel);

        StackPane centerPane = new StackPane(drawPanel);
        centerPane.setStyle("-fx-background-color: lightgray;");

        ScrollPane scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(Insets.EMPTY);

        setCenter(scrollPane);
        setTop(menu);
    }
}
