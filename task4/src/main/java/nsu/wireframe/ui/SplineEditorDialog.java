package nsu.wireframe.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import nsu.wireframe.math.SplineService;
import nsu.wireframe.model.AppState;
import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.SceneParameters;
import nsu.wireframe.render.EditorRenderer;
import nsu.wireframe.render.EditorViewState;
import nsu.wireframe.validation.ValidationException;
import nsu.wireframe.validation.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SplineEditorDialog extends Stage {
    private static final double POINT_HIT_RADIUS = 10;

    private final AppState workingState;
    private final Consumer<AppState> applyCallback;
    private final Canvas canvas;
    private final EditorViewState viewState;
    private final EditorRenderer editorRenderer;
    private final Label kValueLabel;
    private final TextField nField;
    private final TextField mField;
    private final TextField m1Field;
    private final ToggleButton moveModeButton;

    private int selectedPointIndex = -1;
    private boolean panning;
    private double lastMouseX;
    private double lastMouseY;

    private final boolean autoApplyEnabled = false;

    public SplineEditorDialog(Window owner, AppState initialState, Consumer<AppState> applyCallback) {
        this.workingState = initialState.copy();
        this.applyCallback = applyCallback;
        this.canvas = new Canvas(560, 420);
        this.viewState = new EditorViewState();
        this.editorRenderer = new EditorRenderer(new SplineService());
        this.kValueLabel = new Label();
        this.nField = new TextField(Integer.toString(workingState.getSceneParameters().getN()));
        this.mField = new TextField(Integer.toString(workingState.getSceneParameters().getM()));
        this.m1Field = new TextField(Integer.toString(workingState.getSceneParameters().getM1()));
        this.moveModeButton = new ToggleButton("Move");

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Parameters / Generatrix Editor");
        setMinWidth(760);
        setMinHeight(560);

        BorderPane root = new BorderPane();
        root.setTop(createTopPanel());
        root.setCenter(createCanvasPane());
        root.setBottom(createButtons());

        configureMouse();
        updateKLabel();

        Scene scene = new Scene(root, 820, 620);
        setScene(scene);
        setOnShown(event -> {
            viewState.normalize(workingState.getControlPoints(), canvas.getWidth(), canvas.getHeight());
            redraw();
        });
    }

    private VBox createTopPanel() {
        GridPane parameterGrid = new GridPane();
        parameterGrid.setHgap(8);
        parameterGrid.setVgap(8);
        parameterGrid.setPadding(new Insets(10));

        parameterGrid.add(new Label("K:"), 0, 0);
        parameterGrid.add(kValueLabel, 1, 0);
        parameterGrid.add(new Label("N:"), 2, 0);
        parameterGrid.add(nField, 3, 0);
        parameterGrid.add(new Label("M:"), 4, 0);
        parameterGrid.add(mField, 5, 0);
        parameterGrid.add(new Label("M1:"), 6, 0);
        parameterGrid.add(m1Field, 7, 0);

        Button zoomInButton = new Button("+");
        zoomInButton.setTooltip(new Tooltip("Zoom in the editor view"));
        zoomInButton.setOnAction(event -> {
            viewState.zoom(1.2);
            redraw();
        });

        Button zoomOutButton = new Button("-");
        zoomOutButton.setTooltip(new Tooltip("Zoom out the editor view"));
        zoomOutButton.setOnAction(event -> {
            viewState.zoom(1.0 / 1.2);
            redraw();
        });

        Button normalizeButton = new Button("Normalize");
        normalizeButton.setTooltip(new Tooltip("Fit points into the editor area automatically"));
        normalizeButton.setOnAction(event -> {
            viewState.normalize(workingState.getControlPoints(), canvas.getWidth(), canvas.getHeight());
            redraw();
        });

        moveModeButton.setTooltip(new Tooltip("Move the editor view with the left mouse button"));

        HBox viewTools = new HBox(8, moveModeButton, zoomInButton, zoomOutButton, normalizeButton);
        viewTools.setPadding(new Insets(0, 10, 10, 10));
        viewTools.setAlignment(Pos.CENTER_LEFT);

        return new VBox(parameterGrid, viewTools);
    }

    private StackPane createCanvasPane() {
        StackPane pane = new StackPane(canvas);
        pane.setPadding(new Insets(8));
        pane.setStyle("-fx-background-color: #eeeeee;");
        canvas.widthProperty().bind(pane.widthProperty().subtract(16));
        canvas.heightProperty().bind(pane.heightProperty().subtract(16));
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> redraw());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> redraw());
        return pane;
    }

    private HBox createButtons() {
        Button okButton = new Button("OK");
        okButton.setOnAction(event -> applyChanges(true));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(event -> applyChanges(false));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(8, spacer, okButton, cancelButton, applyButton);
        buttons.setPadding(new Insets(10));
        buttons.setAlignment(Pos.CENTER_RIGHT);
        return buttons;
    }

    private void configureMouse() {
        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                handleRightClick(event.getX(), event.getY());
                event.consume();
            }
        });

        canvas.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            selectedPointIndex = findPointAt(event.getX(), event.getY());
            panning = selectedPointIndex < 0 && moveModeButton.isSelected();
            redraw();
        });

        canvas.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }

            if (selectedPointIndex >= 0) {
                ControlPoint point = viewState.screenToControlPoint(
                        event.getX(),
                        event.getY(),
                        canvas.getWidth(),
                        canvas.getHeight()
                );
                updatePoint(selectedPointIndex, point);
            } else if (panning) {
                viewState.move(event.getX() - lastMouseX, event.getY() - lastMouseY);
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                redraw();
            }
        });

        canvas.setOnMouseReleased(event -> {
            selectedPointIndex = -1;
            panning = false;
            redraw();
        });
    }

    private void handleRightClick(double x, double y) {
        int pointIndex = findPointAt(x, y);
        if (pointIndex >= 0) {
            removePoint(pointIndex);
            return;
        }

        addPoint(viewState.screenToControlPoint(x, y, canvas.getWidth(), canvas.getHeight()));
    }

    private int findPointAt(double x, double y) {
        List<ControlPoint> points = workingState.getControlPoints();
        for (int i = 0; i < points.size(); i++) {
            var screenPoint = viewState.modelToScreen(points.get(i).toPoint2D(), canvas.getWidth(), canvas.getHeight());
            double dx = screenPoint.x() - x;
            double dy = screenPoint.y() - y;
            if (Math.hypot(dx, dy) <= POINT_HIT_RADIUS) {
                return i;
            }
        }
        return -1;
    }

    private void addPoint(ControlPoint point) {
        List<ControlPoint> points = new ArrayList<>(workingState.getControlPoints());
        points.add(point);
        workingState.setControlPoints(points);
        handleWorkingStateChanged();
    }

    private void removePoint(int pointIndex) {
        List<ControlPoint> points = new ArrayList<>(workingState.getControlPoints());
        if (points.size() <= SceneParameters.MIN_K) {
            AlertUtils.showError(this, "Cannot Delete Point", "K must be at least " + SceneParameters.MIN_K + ".");
            return;
        }
        points.remove(pointIndex);
        workingState.setControlPoints(points);
        handleWorkingStateChanged();
    }

    private void updatePoint(int pointIndex, ControlPoint point) {
        List<ControlPoint> points = new ArrayList<>(workingState.getControlPoints());
        points.set(pointIndex, point);
        workingState.setControlPoints(points);
        handleWorkingStateChanged();
    }

    private void handleWorkingStateChanged() {
        updateKLabel();
        redraw();
        if (autoApplyEnabled) {
            tryApplyWithoutClosing();
        }
    }

    private void tryApplyWithoutClosing() {
        try {
            readParametersFromFields();
            applyCallback.accept(workingState.copy());
        } catch (ValidationException ignored) {
            // Auto-apply is disabled by default. If enabled, invalid input is simply not applied.
        }
    }

    private void applyChanges(boolean closeAfterApply) {
        try {
            readParametersFromFields();
            Validators.validateState(workingState);
            applyCallback.accept(workingState.copy());
            if (closeAfterApply) {
                close();
            }
        } catch (ValidationException e) {
            AlertUtils.showError(this, "Invalid Parameters", e.getMessage());
        } catch (Exception e) {
            AlertUtils.showError(this, "Could Not Apply Parameters", "Unexpected error: " + e.getMessage());
        }
    }

    private void readParametersFromFields() throws ValidationException {
        SceneParameters parameters = new SceneParameters(
                workingState.getControlPoints().size(),
                Validators.parseIntAtLeast(nField.getText(), "N", SceneParameters.MIN_N),
                Validators.parseIntAtLeast(mField.getText(), "M", SceneParameters.MIN_M),
                Validators.parseIntAtLeast(m1Field.getText(), "M1", SceneParameters.MIN_M1)
        );
        workingState.setSceneParameters(parameters);
    }

    private void updateKLabel() {
        kValueLabel.setText(Integer.toString(workingState.getControlPoints().size()));
    }

    private void redraw() {
        editorRenderer.render(
                canvas.getGraphicsContext2D(),
                canvas.getWidth(),
                canvas.getHeight(),
                workingState.getControlPoints(),
                workingState.getSceneParameters(),
                viewState,
                selectedPointIndex
        );
    }
}
