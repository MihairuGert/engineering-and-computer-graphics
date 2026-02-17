package paint.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UI extends BorderPane {
    private MenuBar menuBar;
    private paint.ui.Menu toolbar;
    private DrawPanel drawPanel;

    public UI(Stage stage) {
        super();

        toolbar = new paint.ui.Menu(stage, null);
        drawPanel = new DrawPanel(500, 500, toolbar.getSettings());
        toolbar.setDrawPanel(drawPanel);

        createMenuBar(stage);

        StackPane centerPane = new StackPane(drawPanel);
        centerPane.setStyle("-fx-background-color: lightgray;");

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
        MenuItem pencilItem = new MenuItem("Карандаш");
        pencilItem.setOnAction(e -> toolbar.handleToolAction(e));

        MenuItem lineItem = new MenuItem("Линия");
        lineItem.setOnAction(e -> toolbar.handleToolAction(e));

        MenuItem fillItem = new MenuItem("Заливка");
        fillItem.setOnAction(e -> toolbar.handleToolAction(e));

        MenuItem stampItem = new MenuItem("Штамп");
        stampItem.setOnAction(e -> toolbar.handleToolAction(e));

        toolsMenu.getItems().addAll(pencilItem, lineItem, fillItem, stampItem);

        Menu settingsMenu = new Menu("Настройки");
        MenuItem settingsItem = new MenuItem("Открыть");
        settingsItem.setOnAction(e -> toolbar.handleSettings(e));

        settingsMenu.getItems().add(settingsItem);

        Menu helpMenu = new Menu("Помощь");
        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.setOnAction(e -> toolbar.handleAbout(e));

        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, settingsMenu, helpMenu);
    }

    public static void showErr(String msgHeader, String msgContent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ошибка!");
        alert.setHeaderText(msgHeader);
        alert.setContentText(msgContent);

        alert.showAndWait();
    }
}