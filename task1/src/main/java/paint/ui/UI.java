package paint.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Menu;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UI extends BorderPane {
    private MenuBar menuBar;
    private paint.ui.Menu toolbar;
    private DrawPanel drawPanel;

    private ToggleGroup toolsGroup = new ToggleGroup();
    private RadioMenuItem pencilRadio, lineRadio, fillRadio, stampRadio;

    public UI(Stage stage) {
        super();

        toolbar = new paint.ui.Menu(stage, null);
        drawPanel = new DrawPanel(500, 500, toolbar.getSettings());
        toolbar.setDrawPanel(drawPanel);

        createMenuBar(stage);

        toolbar.activeToolProperty().addListener((obs, oldTool, newTool) -> {
            if (newTool == null) {
                toolsGroup.selectToggle(null);
            } else {
                switch (newTool.getToolMode()) {
                    case PENCIL: toolsGroup.selectToggle(pencilRadio); break;
                    case LINE:   toolsGroup.selectToggle(lineRadio); break;
                    case FILL:   toolsGroup.selectToggle(fillRadio); break;
                    case STAMP:  toolsGroup.selectToggle(stampRadio); break;
                    default:     toolsGroup.selectToggle(null);
                }
            }
        });

        StackPane centerPane = new StackPane(drawPanel);
        centerPane.setStyle("-fx-background-color: lightgray;");

        centerPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            int newWidth = newVal.intValue();
            if (newWidth > drawPanel.getCanvasWidth()) {
                drawPanel.resizeCanvas(newWidth, drawPanel.getCanvasHeight());
            }
        });

        centerPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            int newHeight = newVal.intValue();
            if (newHeight > drawPanel.getCanvasHeight()) {
                drawPanel.resizeCanvas(drawPanel.getCanvasWidth(), newHeight);
            }
        });

        ScrollPane scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(Insets.EMPTY);

        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, toolbar);

        setCenter(scrollPane);
        setTop(topContainer);
    }

    private void createMenuBar(Stage stage) {
        menuBar = new MenuBar();

        Menu fileMenu = new Menu("Файл");
        MenuItem newItem = new MenuItem("Новый");
        newItem.setOnAction(e -> toolbar.handleNew(e));
        MenuItem openItem = new MenuItem("Открыть");
        openItem.setOnAction(e -> toolbar.handleOpen(e));
        MenuItem saveItem = new MenuItem("Сохранить");
        saveItem.setOnAction(e -> toolbar.handleSave(e));
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu toolsMenu = new Menu("Инструменты");

        pencilRadio = new RadioMenuItem("Карандаш");
        pencilRadio.setToggleGroup(toolsGroup);
        pencilRadio.setOnAction(e -> toolbar.activateTool(ToolMode.PENCIL));

        lineRadio = new RadioMenuItem("Линия");
        lineRadio.setToggleGroup(toolsGroup);
        lineRadio.setOnAction(e -> toolbar.activateTool(ToolMode.LINE));

        fillRadio = new RadioMenuItem("Заливка");
        fillRadio.setToggleGroup(toolsGroup);
        fillRadio.setOnAction(e -> toolbar.activateTool(ToolMode.FILL));

        stampRadio = new RadioMenuItem("Штамп");
        stampRadio.setToggleGroup(toolsGroup);
        stampRadio.setOnAction(e -> toolbar.activateTool(ToolMode.STAMP));

        toolsMenu.getItems().addAll(pencilRadio, lineRadio, fillRadio, stampRadio);

        Menu settingsMenu = new Menu("Настройки");
        MenuItem settingsItem = new MenuItem("Открыть");
        settingsItem.setOnAction(e -> toolbar.handleSettings(e));
        settingsMenu.getItems().add(settingsItem);

        Menu helpMenu = new Menu("Помощь");
        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.setOnAction(e -> toolbar.handleAbout(e));
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, settingsMenu, helpMenu);

        toolsGroup.selectToggle(null);
    }

    public static void showErr(String msgHeader, String msgContent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ошибка!");
        alert.setHeaderText(msgHeader);
        alert.setContentText(msgContent);
        alert.showAndWait();
    }
}