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

public class Menu extends ToolBar{
    private Button new_b;
    private Button open;
    private Button save;

    private Line line;
    private Pencil pencil;

    private Stage stage;

    private DrawPanel drawPanel;

    public Menu(Stage stage, DrawPanel drawPanel) {
        super();
        this.stage = stage;
        this.drawPanel = drawPanel;

        new_b = newButtonWithImage("new.png");

        open = newButtonWithImage("open.png");
        open.setOnAction(this::handleOpen);

        save = newButtonWithImage("save.png");

        line = new Line("line_active.png", "line.png");
        line.button.setOnAction(this::handleLine);

        pencil = new Pencil("pencil_active.png", "pencil.png");
        pencil.button.setOnAction(this::handlePencil);

        getItems().addAll(new_b, open, save, new Separator(), pencil.button, line.button);
    }

    public static ImageView createIcon(String source) {
        ImageView image = new ImageView(source);
        image.setFitHeight(20);
        image.setFitWidth(20);
        return image;
    }

    public static Button newButtonWithImage(String source) {
        ImageView img = createIcon(source);
        Button button = new Button();
        button.setGraphic(img);
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

        if (selectedFile != null && drawPanel != null) {
            try {
                Image image = new Image("file:" + selectedFile.getAbsolutePath());

                drawPanel.loadImage(image);

                System.out.println("Loaded image: " + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    private void handleLine(ActionEvent event) {
        if (!line.isClicked()) {
            line.setActive();
            drawPanel.setCurrentTool(ToolMode.LINE);
            return;
        }
        line.setInactive();
        drawPanel.setCurrentTool(ToolMode.NONE);
    }

    private void handlePencil(ActionEvent event) {
        if (!pencil.isClicked()) {
            pencil.setActive();
            drawPanel.setCurrentTool(ToolMode.PENCIL);
            return;
        }
        pencil.setInactive();
        drawPanel.setCurrentTool(ToolMode.NONE);
    }
}
