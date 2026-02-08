package paint.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import paint.ui.clickable.Clickable;
import paint.ui.clickable.Line;
import paint.ui.clickable.Pencil;
import paint.ui.windows.NewWindow;
import paint.ui.windows.SettingsWindow;

import java.io.File;

public class Menu extends ToolBar {
    private Clickable new_b;
    private Clickable open;
    private Clickable save;
    private Clickable settingsBtn;
    private Clickable aboutBtn;

    private Line line;
    private Pencil pencil;

    private Stage stage;
    private DrawPanel drawPanel;
    private Settings settings;
    private SettingsWindow settingsWindow;

    private NewWindow newWindow;

    public Menu(Stage stage, DrawPanel drawPanel) {
        super();
        this.stage = stage;
        this.drawPanel = drawPanel;
        this.settings = new Settings();

        new_b = new Clickable("new.png");
        new_b.button.setOnAction(this::handleNew);
        new_b.setTip("Отчищает изображение");

        open = new Clickable("open.png");
        open.button.setOnAction(this::handleOpen);
        open.setTip("Open");

        save = new Clickable("save.png");
        save.button.setOnAction(this::handleSave);
        save.setTip("Save");

        line = new Line("line_active.png", "line.png");
        line.button.setOnAction(this::handleLine);
        line.setTip("Line");

        pencil = new Pencil("pencil_active.png", "pencil.png");
        pencil.button.setOnAction(this::handlePencil);
        pencil.setTip("Pencil");

        settingsBtn = new Clickable("settings.png");
        settingsBtn.button.setOnAction(this::handleSettings);
        settingsBtn.setTip("Settings");

        aboutBtn = new Clickable("about.png");
        aboutBtn.button.setOnAction(this::handleAbout);
        aboutBtn.setTip("About");

        getItems().addAll(aboutBtn.button, settingsBtn.button, new Separator(), new_b.button, open.button, save.button, new Separator(),
                pencil.button, line.button);
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

    void handleNew(ActionEvent event) {
        newWindow.showNew();
    }

    void handleOpen(ActionEvent event) {
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

    void handleSave(ActionEvent event) {

    }

    void handleLine(ActionEvent event) {
        if (!line.isClicked()) {
            line.setActive();
            drawPanel.setCurrentTool(ToolMode.LINE);
            return;
        }
        line.setInactive();
        drawPanel.setCurrentTool(ToolMode.NONE);
    }

    void handlePencil(ActionEvent event) {
        if (!pencil.isClicked()) {
            pencil.setActive();
            drawPanel.setCurrentTool(ToolMode.PENCIL);
            return;
        }
        pencil.setInactive();
        drawPanel.setCurrentTool(ToolMode.NONE);
    }

    void handleSettings(ActionEvent event) {
        settingsWindow.showSettings();
    }

    void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О пыинте");
        alert.setHeaderText("Пыинт v0.1");
        alert.setContentText("Автор: Пятанов М.Ю.\n\n" +
                "Свой \"MS Paint\", сделанный в рамках курса по инженерной графике НГУ ФИТ 3 курс.\n" +
                "© 2026 Все права защищены.");

        alert.showAndWait();
    }

    public Settings getSettings() {
        return settings;
    }

    public void setDrawPanel(DrawPanel drawPanel) {
        this.drawPanel = drawPanel;
        if (drawPanel != null && settingsWindow == null && newWindow == null) {
            settingsWindow = new SettingsWindow(stage, settings, drawPanel);
            newWindow = new NewWindow(stage, drawPanel);
        }
    }
}