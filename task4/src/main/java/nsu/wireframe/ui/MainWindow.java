package nsu.wireframe.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nsu.wireframe.io.SceneFileException;
import nsu.wireframe.io.SceneFileService;
import nsu.wireframe.math.ColorService;
import nsu.wireframe.math.GeometryService;
import nsu.wireframe.math.ProjectionService;
import nsu.wireframe.math.SplineService;
import nsu.wireframe.model.AppState;
import nsu.wireframe.render.WireframeRenderer;

import java.io.File;

public class MainWindow extends BorderPane {
    private static final double ROTATION_STEP_PER_PIXEL = 0.5;
    private static final double MIN_ZN = 0.1;

    private final Stage stage;
    private final AppState state;
    private final GeometryService geometryService;
    private final WireframeRenderer wireframeRenderer;
    private final SceneFileService sceneFileService;
    private final Canvas canvas;

    private double lastMouseX;
    private double lastMouseY;

    public MainWindow(Stage stage, AppState state) {
        this.stage = stage;
        this.state = state;
        this.geometryService = new GeometryService(new SplineService());
        this.wireframeRenderer = new WireframeRenderer(new ProjectionService(), new ColorService());
        this.sceneFileService = new SceneFileService();
        this.canvas = new Canvas();

        setTop(new VBox(createMenuBar(), createToolBar()));
        setCenter(createCanvasPane());
        configureMouse();
    }

    public void refreshScene() {
        state.rebuildWireframe(geometryService);
        wireframeRenderer.render(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight(), state);
    }

    private MenuBar createMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New / Reset to Default Scene");
        newItem.setOnAction(this::handleNewScene);
        MenuItem openItem = new MenuItem("Open Scene");
        openItem.setOnAction(this::handleOpenScene);
        MenuItem saveItem = new MenuItem("Save Scene");
        saveItem.setOnAction(this::handleSaveScene);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> Platform.exit());
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu parametersMenu = new Menu("Parameters");
        MenuItem editorItem = new MenuItem("Parameters / Generatrix Editor");
        editorItem.setOnAction(this::handleOpenEditor);
        MenuItem resetRotationItem = new MenuItem("Reset Rotation Angles");
        resetRotationItem.setOnAction(this::handleResetRotations);
        parametersMenu.getItems().addAll(editorItem, resetRotationItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(this::handleAbout);
        helpMenu.getItems().add(aboutItem);

        return new MenuBar(fileMenu, parametersMenu, helpMenu);
    }

    private ToolBar createToolBar() {
        Button newButton = createToolbarButton("New", "Reset to the default scene", this::handleNewScene);
        Button openButton = createToolbarButton("Open", "Open a scene from a file", this::handleOpenScene);
        Button saveButton = createToolbarButton("Save", "Save the current scene", this::handleSaveScene);
        Button editorButton = createToolbarButton("Parameters", "Open the generatrix editor", this::handleOpenEditor);
        Button resetRotationButton = createToolbarButton("Reset Angles", "Reset rotation angles to zero", this::handleResetRotations);
        Button aboutButton = createToolbarButton("About", "Show information about the application", this::handleAbout);
        Button exitButton = createToolbarButton("Exit", "Close the application", event -> Platform.exit());

        return new ToolBar(
                newButton,
                openButton,
                saveButton,
                new Separator(),
                editorButton,
                resetRotationButton,
                new Separator(),
                aboutButton,
                exitButton
        );
    }

    private Button createToolbarButton(String text, String tooltip, javafx.event.EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(handler);
        return button;
    }

    private StackPane createCanvasPane() {
        StackPane pane = new StackPane(canvas);
        pane.setPadding(Insets.EMPTY);
        pane.setMinSize(0, 0);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.setStyle("-fx-background-color: white;");
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> refreshScene());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> refreshScene());
        return pane;
    }

    private void configureMouse() {
        canvas.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }

            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;
            state.getViewParameters().setRotationY(state.getViewParameters().getRotationY() + dx * ROTATION_STEP_PER_PIXEL);
            state.getViewParameters().setRotationX(state.getViewParameters().getRotationX() - dy * ROTATION_STEP_PER_PIXEL);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            refreshScene();
        });

        canvas.setOnScroll(event -> {
            double factor = event.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
            double nextZn = Math.max(MIN_ZN, state.getViewParameters().getZn() * factor);
            state.getViewParameters().setZn(nextZn);
            refreshScene();
        });
    }

    private void handleNewScene(ActionEvent event) {
        state.replaceFrom(AppState.createDefault());
        refreshScene();
    }

    private void handleOpenScene(ActionEvent event) {
        FileChooser fileChooser = createSceneFileChooser("Open Scene");
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            AppState loadedState = sceneFileService.load(file);
            state.replaceFrom(loadedState);
            refreshScene();
        } catch (SceneFileException e) {
            AlertUtils.showError(stage, "Could Not Open Scene", e.getMessage());
        } catch (Exception e) {
            AlertUtils.showError(stage, "Could Not Open Scene", "Unexpected error: " + e.getMessage());
        }
    }

    private void handleSaveScene(ActionEvent event) {
        FileChooser fileChooser = createSceneFileChooser("Save Scene");
        fileChooser.setInitialFileName("scene.icgw");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            sceneFileService.save(ensureSceneExtension(file), state);
        } catch (SceneFileException e) {
            AlertUtils.showError(stage, "Could Not Save Scene", e.getMessage());
        } catch (Exception e) {
            AlertUtils.showError(stage, "Could Not Save Scene", "Unexpected error: " + e.getMessage());
        }
    }

    private void handleOpenEditor(ActionEvent event) {
        SplineEditorDialog dialog = new SplineEditorDialog(stage, state.copy(), appliedState -> {
            state.replaceFrom(appliedState);
            refreshScene();
        });
        dialog.showAndWait();
    }

    private void handleResetRotations(ActionEvent event) {
        state.getViewParameters().resetRotations();
        refreshScene();
    }

    private void handleAbout(ActionEvent event) {
        AlertUtils.showInfo(
                stage,
                "About",
                "ICGWireframe\n\n" +
                        "This program displays a wireframe surface of revolution built from a 2D generatrix.\n\n" +
                        "Main features:\n" +
                        "- edit control points in the generatrix editor;\n" +
                        "- preview the B-spline generatrix;\n" +
                        "- configure K, N, M, and M1 parameters;\n" +
                        "- rotate the model with the mouse;\n" +
                        "- zoom with the mouse wheel using Zn;\n" +
                        "- reset rotation angles;\n" +
                        "- save and load scenes in a simple text format;\n" +
                        "- render the model as a wireframe with depth-based coloring.\n\n" +
                        "Made by Mikhail Pyatanov 2026."
        );
    }

    private FileChooser createSceneFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ICGWireframe scene (*.icgw)", "*.icgw"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        return fileChooser;
    }

    private File ensureSceneExtension(File file) {
        if (file.getName().toLowerCase().endsWith(".icgw")) {
            return file;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return new File(file.getName() + ".icgw");
        }
        return new File(parent, file.getName() + ".icgw");
    }
}
