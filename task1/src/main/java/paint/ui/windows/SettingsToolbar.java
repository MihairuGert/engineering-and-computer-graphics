package paint.ui.windows;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import paint.ui.DrawPanel;
import paint.ui.Settings;

public class SettingsToolbar extends VBox {
    private ColorPicker colorPicker;
    private Slider brushSizeSlider;
    private Slider lineThicknessSlider;

    private Settings settings;
    private DrawPanel drawPanel;

    public SettingsToolbar(Settings settings, DrawPanel drawPanel) {
        super();
        this.settings = settings;
        this.drawPanel = drawPanel;

        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f4f4f4;");

        initializeComponents();
        setupEventHandlers();
    }

    private void initializeComponents() {
        Label colorLabel = new Label("Цвыт:");
        colorPicker = new ColorPicker(settings.getCurrentColor());
        colorPicker.setPrefWidth(150);

        Label brushLabel = new Label("Рызмер кысти:");
        brushSizeSlider = new Slider(1, 50, settings.getBrushSize());
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setMajorTickUnit(10);
        brushSizeSlider.setMinorTickCount(5);
        brushSizeSlider.setSnapToTicks(true);

        Label brushValueLabel = new Label(settings.getBrushSize() + "px");

        Label lineLabel = new Label("Рызмер лынии:");
        lineThicknessSlider = new Slider(1, 10, settings.getLineThickness());
        lineThicknessSlider.setShowTickLabels(true);
        lineThicknessSlider.setShowTickMarks(true);
        lineThicknessSlider.setMajorTickUnit(2);

        Label lineValueLabel = new Label(settings.getLineThickness() + "px");

        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brushValueLabel.setText(String.format("%dpx", newVal.intValue()));
        });

        lineThicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lineValueLabel.setText(String.format("%dpx", newVal.intValue()));
        });

        getChildren().addAll(
                colorLabel, colorPicker,
                new Separator(Orientation.HORIZONTAL),
                brushLabel, brushSizeSlider, brushValueLabel,
                new Separator(Orientation.HORIZONTAL),
                lineLabel, lineThicknessSlider, lineValueLabel
        );
    }

    private void setupEventHandlers() {
        colorPicker.setOnAction(e -> {
            settings.setCurrentColor(colorPicker.getValue());
        });

        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setBrushSize(newVal.intValue());
        });

        lineThicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setLineThickness(newVal.intValue());
        });
    }

    public void updateFromSettings() {
        colorPicker.setValue(settings.getCurrentColor());
        brushSizeSlider.setValue(settings.getBrushSize());
        lineThicknessSlider.setValue(settings.getLineThickness());
    }
}