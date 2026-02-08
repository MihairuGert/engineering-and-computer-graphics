package paint.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
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
        drawPanel = new DrawPanel(1000, 1000, toolbar.getSettings());
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

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> {
            drawPanel.newImage();
        });

        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> toolbar.handleOpen(e));

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> {

        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu toolsMenu = new Menu("Tools");
        MenuItem pencilItem = new MenuItem("Pencil");
        pencilItem.setOnAction(e -> toolbar.handlePencil(e));

        MenuItem lineItem = new MenuItem("Line");
        lineItem.setOnAction(e -> toolbar.handleLine(e));

        toolsMenu.getItems().addAll(pencilItem, lineItem);

        Menu settingsMenu = new Menu("Settings");
        MenuItem settingsItem = new MenuItem("Open Settings");
        settingsItem.setOnAction(e -> toolbar.handleSettings(e));

        settingsMenu.getItems().add(settingsItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> toolbar.handleAbout(e));

        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, settingsMenu, helpMenu);
    }
}