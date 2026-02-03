package paint.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Tools extends ToolBar{
    private Button new_b;
    private Button open;
    private Button save;
    private Stage stage;

    public Tools(Stage stage) {
        super();
        this.stage = stage;

        new_b = newButtonWithImage("new.png");

        open = newButtonWithImage("open.png");
        open.setOnAction(this::handleOpen);

        save = newButtonWithImage("save.png");
        this.getItems().addAll(new_b, open, save, new Separator());
    }

    private Button newButtonWithImage(String source) {
        Image icon = new Image(source);
        ImageView iconView = new ImageView(icon);
        iconView.setFitHeight(20);
        iconView.setFitWidth(20);

        Button button = new Button();
        button.setGraphic(iconView);
        button.setPadding(Insets.EMPTY);
        button.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        return button;
    }

    private void handleOpen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            System.out.println("Selected: " + imagePath);
        }
    }
}
