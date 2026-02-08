package paint.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NewToolbar extends VBox {
    public NewToolbar() {
        super();

        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f4f4f4;");

        initializeComponents();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
    }

    private void initializeComponents() {
        Label sizeLabel = new Label("Размер:");
        getChildren().addAll(sizeLabel);
    }
}
