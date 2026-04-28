package nsu.task5.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nsu.task5.io.RayFileException;
import nsu.task5.io.RenderFileData;
import nsu.task5.io.RenderFileService;
import nsu.task5.io.SceneFileService;
import nsu.task5.model.RayCamera;
import nsu.task5.model.SceneModel;
import nsu.task5.render.ImagePostProcessor;
import nsu.task5.render.RayWireframeRenderer;
import nsu.task5.render.RenderResult;
import nsu.task5.render.RenderService;
import nsu.task5.render.RenderSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class RaytracingWindow extends BorderPane {
    private static final double ROTATION_STEP_PER_PIXEL = 0.008;

    private final Stage stage;
    private final Canvas canvas = new Canvas();
    private final ImageView imageView = new ImageView();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressLabel = new Label();
    private final RayWireframeRenderer wireframeRenderer = new RayWireframeRenderer();
    private final SceneFileService sceneFileService = new SceneFileService();
    private final RenderFileService renderFileService = new RenderFileService();
    private final RenderService renderService = new RenderService();
    private final ImagePostProcessor postProcessor = new ImagePostProcessor();

    private ToggleButton selectViewButton;
    private ToggleButton renderButton;
    private Button openButton;
    private Button loadRenderButton;
    private Button saveRenderButton;
    private Button initButton;
    private Button settingsButton;
    private Button saveImageButton;

    private SceneModel scene = SceneModel.empty();
    private RenderSettings settings = RenderSettings.defaults();
    private RayCamera camera = RayCamera.defaultCamera();
    private WritableImage renderedImage;
    private File currentSceneFile;
    private Task<RenderResult> renderTask;
    private double lastMouseX;
    private double lastMouseY;

    public RaytracingWindow(Stage stage) {
        this.stage = stage;

        setTop(new VBox(createMenuBar(), createToolBar()));
        setCenter(createViewPane());
        setBottom(createProgressPane());
        configureInput();
        configureImageView();
        showSelectView();
    }

    public void shutdown() {
        cancelRender();
    }

    public void refreshView() {
        if (isRendering()) {
            return;
        }
        if (selectViewButton.isSelected()) {
            renderWireframe();
        } else {
            showRenderedImage();
        }
    }

    private MenuBar createMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(this::handleOpenScene);
        MenuItem loadRenderItem = new MenuItem("Load render settings");
        loadRenderItem.setOnAction(this::handleLoadRenderSettings);
        MenuItem saveRenderItem = new MenuItem("Save render settings");
        saveRenderItem.setOnAction(this::handleSaveRenderSettings);
        MenuItem saveImageItem = new MenuItem("Save image");
        saveImageItem.setOnAction(this::handleSaveImage);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> Platform.exit());
        fileMenu.getItems().addAll(
                openItem,
                loadRenderItem,
                saveRenderItem,
                new SeparatorMenuItem(),
                saveImageItem,
                new SeparatorMenuItem(),
                exitItem
        );

        Menu renderMenu = new Menu("Render");
        MenuItem initItem = new MenuItem("Init");
        initItem.setOnAction(this::handleInitCamera);
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(this::handleSettings);
        MenuItem startRenderItem = new MenuItem("Render");
        startRenderItem.setOnAction(event -> startRender());
        renderMenu.getItems().addAll(initItem, settingsItem, startRenderItem);

        return new MenuBar(fileMenu, renderMenu);
    }

    private ToolBar createToolBar() {
        openButton = createToolbarButton("Open", "Open .scene file", this::handleOpenScene);
        loadRenderButton = createToolbarButton("Load render settings", "Open .render file", this::handleLoadRenderSettings);
        saveRenderButton = createToolbarButton("Save render settings", "Save current render settings", this::handleSaveRenderSettings);
        initButton = createToolbarButton("Init", "Reset camera for current scene", this::handleInitCamera);
        settingsButton = createToolbarButton("Settings", "Edit render settings", this::handleSettings);
        saveImageButton = createToolbarButton("Save image", "Save current view as PNG", this::handleSaveImage);

        ToggleGroup modeGroup = new ToggleGroup();
        selectViewButton = new ToggleButton("Select view");
        selectViewButton.setTooltip(new Tooltip("Show wireframe scene view"));
        selectViewButton.setToggleGroup(modeGroup);
        selectViewButton.setOnAction(event -> showSelectView());
        renderButton = new ToggleButton("Render");
        renderButton.setTooltip(new Tooltip("Render image in background"));
        renderButton.setToggleGroup(modeGroup);
        renderButton.setOnAction(event -> startRender());
        selectViewButton.setSelected(true);

        return new ToolBar(
                openButton,
                loadRenderButton,
                saveRenderButton,
                new Separator(),
                initButton,
                settingsButton,
                new Separator(),
                selectViewButton,
                renderButton,
                new Separator(),
                saveImageButton
        );
    }

    private Button createToolbarButton(String text, String tooltip, javafx.event.EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(handler);
        return button;
    }

    private StackPane createViewPane() {
        StackPane pane = new StackPane(canvas, imageView);
        pane.setPadding(Insets.EMPTY);
        pane.setMinSize(0, 0);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.setStyle("-fx-background-color: white;");
        pane.setFocusTraversable(true);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> handleViewportResize());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> handleViewportResize());
        pane.setOnMouseClicked(event -> pane.requestFocus());
        return pane;
    }

    private HBox createProgressPane() {
        HBox pane = new HBox(10, progressBar, progressLabel);
        pane.setPadding(new Insets(6, 10, 6, 10));
        progressBar.setPrefWidth(240);
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        return pane;
    }

    private void configureImageView() {
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(canvas.widthProperty());
        imageView.fitHeightProperty().bind(canvas.heightProperty());
        imageView.setVisible(false);
    }

    private void configureInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnMousePressed(event -> {
            if (!canChangeCamera() || event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            canvas.requestFocus();
        });

        canvas.setOnMouseDragged(event -> {
            if (!canChangeCamera() || !event.isPrimaryButtonDown()) {
                return;
            }

            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;
            camera.rotateAroundView(-dx * ROTATION_STEP_PER_PIXEL, -dy * ROTATION_STEP_PER_PIXEL);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            renderWireframe();
        });

        canvas.setOnScroll(event -> {
            if (!canChangeCamera()) {
                return;
            }

            if (event.isControlDown()) {
                double distance = nsu.task5.math.VectorMath.distance(camera.eye(), camera.view());
                double step = distance * 0.08 * (event.getDeltaY() > 0 ? 1 : -1);
                camera.moveEyeAlongViewDirection(step);
            } else {
                double factor = event.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
                camera.moveZn(factor);
            }
            renderWireframe();
        });

        canvas.setOnKeyPressed(event -> {
            if (!canChangeCamera()) {
                return;
            }

            double distance = nsu.task5.math.VectorMath.distance(camera.eye(), camera.view());
            double step = Math.max(0.05, distance * 0.04);
            if (event.getCode() == KeyCode.LEFT) {
                camera.pan(-step, 0);
            } else if (event.getCode() == KeyCode.RIGHT) {
                camera.pan(step, 0);
            } else if (event.getCode() == KeyCode.UP) {
                camera.pan(0, step);
            } else if (event.getCode() == KeyCode.DOWN) {
                camera.pan(0, -step);
            } else {
                return;
            }
            renderWireframe();
        });
    }

    private boolean canChangeCamera() {
        return selectViewButton.isSelected() && !isRendering();
    }

    private void handleViewportResize() {
        camera.adjustScreenSize(canvas.getWidth(), canvas.getHeight());
        refreshView();
    }

    private void handleOpenScene(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        FileChooser fileChooser = createSceneFileChooser("Open Scene");
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            SceneModel loadedScene = sceneFileService.load(file);
            scene = loadedScene;
            currentSceneFile = file;
            renderedImage = null;
            loadAdjacentRenderOrInitCamera(file);
            selectViewButton.setSelected(true);
            showSelectView();
        } catch (RayFileException e) {
            showError("Could Not Open Scene", e.getMessage());
        } catch (Exception e) {
            showError("Could Not Open Scene", "Unexpected error: " + e.getMessage());
        }
    }

    private void loadAdjacentRenderOrInitCamera(File sceneFile) {
        File renderFile = adjacentRenderFile(sceneFile);
        if (!renderFile.isFile()) {
            camera = RayCamera.initForScene(scene, canvas.getWidth(), canvas.getHeight());
            return;
        }

        try {
            applyRenderFileData(renderFileService.load(renderFile));
        } catch (RayFileException e) {
            camera = RayCamera.initForScene(scene, canvas.getWidth(), canvas.getHeight());
            showWarning("Render Settings Not Loaded", e.getMessage() + "\nCamera was initialized from the scene.");
        }
    }

    private void handleLoadRenderSettings(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        FileChooser fileChooser = createRenderFileChooser("Load Render Settings");
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            applyRenderFileData(renderFileService.load(file));
            renderedImage = null;
            refreshView();
        } catch (RayFileException e) {
            showError("Could Not Load Render Settings", e.getMessage());
        } catch (Exception e) {
            showError("Could Not Load Render Settings", "Unexpected error: " + e.getMessage());
        }
    }

    private void applyRenderFileData(RenderFileData data) {
        settings = data.settings();
        camera = data.camera();
        camera.adjustScreenSize(canvas.getWidth(), canvas.getHeight());
        showWarnings("Render Settings Warning", data.warnings());
    }

    private void handleSaveRenderSettings(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        FileChooser fileChooser = createRenderFileChooser("Save Render Settings");
        if (currentSceneFile != null) {
            fileChooser.setInitialFileName(baseName(currentSceneFile) + ".render");
        } else {
            fileChooser.setInitialFileName("scene.render");
        }

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            renderFileService.save(ensureRenderExtension(file), settings, camera);
        } catch (RayFileException e) {
            showError("Could Not Save Render Settings", e.getMessage());
        } catch (Exception e) {
            showError("Could Not Save Render Settings", "Unexpected error: " + e.getMessage());
        }
    }

    private void handleInitCamera(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        camera = RayCamera.initForScene(scene, canvas.getWidth(), canvas.getHeight());
        renderedImage = null;
        showSelectView();
    }

    private void handleSettings(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        new RenderSettingsDialog(stage).showAndWait(settings).ifPresent(updated -> {
            settings = updated;
            renderedImage = null;
            refreshView();
        });
    }

    private void showSelectView() {
        cancelRender();
        selectViewButton.setSelected(true);
        canvas.setVisible(true);
        imageView.setVisible(false);
        renderWireframe();
    }

    private void startRender() {
        if (isRendering()) {
            return;
        }
        if (scene.primitives().isEmpty()) {
            showError("Render", "Open a .scene file before rendering.");
            selectViewButton.setSelected(true);
            return;
        }

        int width = Math.max(1, (int) Math.round(canvas.getWidth()));
        int height = Math.max(1, (int) Math.round(canvas.getHeight()));
        renderButton.setSelected(true);
        canvas.setVisible(false);
        imageView.setVisible(true);
        setRenderingControlsDisabled(true);

        renderTask = renderService.createTask(scene, settings, camera, width, height);
        progressBar.progressProperty().bind(renderTask.progressProperty());
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressLabel.setText("0%");
        renderTask.progressProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue.doubleValue();
            if (progress >= 0) {
                progressLabel.setText((int) Math.round(progress * 100) + "%");
            }
        });

        renderTask.setOnSucceeded(event -> {
            RenderResult result = renderTask.getValue();
            renderedImage = postProcessor.toWritableImage(result);
            cleanupRenderTask();
            showRenderedImage();
        });
        renderTask.setOnFailed(event -> {
            Throwable exception = renderTask.getException();
            cleanupRenderTask();
            selectViewButton.setSelected(true);
            showSelectView();
            showError("Render Failed", exception == null ? "Unknown render error." : exception.getMessage());
        });
        renderTask.setOnCancelled(event -> cleanupRenderTask());

        Thread thread = new Thread(renderTask, "raytracing-render-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void cleanupRenderTask() {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        setRenderingControlsDisabled(false);
        renderTask = null;
    }

    private void cancelRender() {
        if (isRendering()) {
            renderTask.cancel();
        }
    }

    private boolean isRendering() {
        return renderTask != null && renderTask.isRunning();
    }

    private void setRenderingControlsDisabled(boolean disabled) {
        openButton.setDisable(disabled);
        loadRenderButton.setDisable(disabled);
        saveRenderButton.setDisable(disabled);
        initButton.setDisable(disabled);
        settingsButton.setDisable(disabled);
        selectViewButton.setDisable(disabled);
        renderButton.setDisable(disabled);
        saveImageButton.setDisable(disabled);
    }

    private void renderWireframe() {
        canvas.setVisible(true);
        imageView.setVisible(false);
        wireframeRenderer.render(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight(), scene, camera);
    }

    private void showRenderedImage() {
        if (renderedImage != null) {
            imageView.setImage(renderedImage);
        }
        canvas.setVisible(false);
        imageView.setVisible(true);
    }

    private void handleSaveImage(ActionEvent event) {
        if (isRendering()) {
            return;
        }
        Image imageToSave;
        if (renderButton.isSelected()) {
            imageToSave = renderedImage;
            if (imageToSave == null) {
                showError("Save Image", "There is no rendered image to save.");
                return;
            }
        } else {
            imageToSave = canvas.snapshot(new SnapshotParameters(), null);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG image (*.png)", "*.png"));
        fileChooser.setInitialFileName("image.png");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            ImageIO.write(toBufferedImage(imageToSave), "png", ensurePngExtension(file));
        } catch (IOException e) {
            showError("Could Not Save Image", e.getMessage());
        }
    }

    private BufferedImage toBufferedImage(Image image) {
        int width = (int) Math.round(image.getWidth());
        int height = (int) Math.round(image.getHeight());
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var reader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return bufferedImage;
    }

    private FileChooser createSceneFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Raytracing scene (*.scene)", "*.scene"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        configureInitialDirectory(fileChooser);
        return fileChooser;
    }

    private FileChooser createRenderFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Raytracing render settings (*.render)", "*.render"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        configureInitialDirectory(fileChooser);
        return fileChooser;
    }

    private void configureInitialDirectory(FileChooser fileChooser) {
        File dataDir = new File("Data");
        if (!dataDir.isDirectory()) {
            dataDir = new File("task5/Data");
        }
        if (dataDir.isDirectory()) {
            fileChooser.setInitialDirectory(dataDir);
        }
    }

    private File adjacentRenderFile(File sceneFile) {
        return new File(sceneFile.getParentFile(), baseName(sceneFile) + ".render");
    }

    private File ensureRenderExtension(File file) {
        if (file.getName().toLowerCase().endsWith(".render")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".render");
    }

    private File ensurePngExtension(File file) {
        if (file.getName().toLowerCase().endsWith(".png")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".png");
    }

    private String baseName(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot <= 0) {
            return name;
        }
        return name.substring(0, dot);
    }

    private void showWarnings(String header, List<String> warnings) {
        if (!warnings.isEmpty()) {
            showWarning(header, String.join("\n", warnings));
        }
    }

    private void showError(String header, String content) {
        showAlert(Alert.AlertType.ERROR, "Error", header, content);
    }

    private void showWarning(String header, String content) {
        showAlert(Alert.AlertType.WARNING, "Warning", header, content);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}
