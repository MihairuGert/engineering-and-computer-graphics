package paint.ui.windows;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import paint.ui.Settings;
import paint.ui.StampType;
import paint.ui.UI;

public class SettingsPanel extends VBox {
    private final Settings settings;

    private final ObjectProperty<Color> tempColor = new SimpleObjectProperty<>();
    private final IntegerProperty tempBrushSize = new SimpleIntegerProperty();
    private final IntegerProperty tempLineThickness = new SimpleIntegerProperty();
    private final IntegerProperty tempRadius = new SimpleIntegerProperty();
    private final IntegerProperty tempRotation = new SimpleIntegerProperty();
    private final IntegerProperty tempSidesCount = new SimpleIntegerProperty();
    private final ObjectProperty<StampType> tempStampType = new SimpleObjectProperty<>();

    private ColorPicker colorPicker;

    private Slider brushSlider;
    private TextField brushField;

    private Slider lineSlider;
    private TextField lineField;

    private RadioButton polygonRadio;
    private RadioButton starRadio;
    private ToggleGroup stampTypeGroup;

    private Slider radiusSlider;
    private TextField radiusField;
    private Slider rotationSlider;
    private TextField rotationField;
    private Slider sidesSlider;
    private TextField sidesField;

    public SettingsPanel(Settings settings) {
        this.settings = settings;
        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f4f4f4;");

        resetToSettings();

        createColorControl();
        createBrushControl();
        createLineControl();
        createStampControls();
    }

    private void createColorControl() {
        Label colorLabel = new Label("Цвет:");
        colorPicker = new ColorPicker();
        colorPicker.valueProperty().bindBidirectional(tempColor);
        getChildren().addAll(colorLabel, colorPicker, new Separator());
    }

    private void createBrushControl() {
        Label brushLabel = new Label("Размер кисти (1-50):");
        brushSlider = new Slider(1, 50, tempBrushSize.get());
        brushSlider.setShowTickLabels(true);
        brushSlider.setShowTickMarks(true);
        brushSlider.setMajorTickUnit(10);
        brushSlider.setMinorTickCount(5);

        brushField = new TextField();
        brushField.setPrefWidth(60);
        brushField.setText(String.valueOf(tempBrushSize.get()));

        brushSlider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            brushField.setText(String.valueOf(intVal));
            tempBrushSize.set(intVal);
        });

        brushField.setOnAction(e -> validateAndSetBrush());
        brushField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateAndSetBrush();
        });

        HBox brushBox = new HBox(10, brushSlider, brushField);
        getChildren().addAll(brushLabel, brushBox, new Separator());
    }

    private void validateAndSetBrush() {
        try {
            int val = Integer.parseInt(brushField.getText().trim());
            if (val < 1 || val > 50) {
                UI.showErr("Ошибка!", "Размер кисти должен быть целым числом от 1 до 50.");
                brushField.setText(String.valueOf(tempBrushSize.get()));
                return;
            }
            tempBrushSize.set(val);
            brushSlider.setValue(val);
        } catch (NumberFormatException e) {
            UI.showErr("Ошибка!","Введите целое число.");
            brushField.setText(String.valueOf(tempBrushSize.get()));
        }
    }

    private void createLineControl() {
        Label lineLabel = new Label("Толщина линии (1-10):");
        lineSlider = new Slider(1, 10, tempLineThickness.get());
        lineSlider.setShowTickLabels(true);
        lineSlider.setShowTickMarks(true);
        lineSlider.setMajorTickUnit(2);

        lineField = new TextField();
        lineField.setPrefWidth(60);
        lineField.setText(String.valueOf(tempLineThickness.get()));

        lineSlider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            lineField.setText(String.valueOf(intVal));
            tempLineThickness.set(intVal);
        });

        lineField.setOnAction(e -> validateAndSetLine());
        lineField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateAndSetLine();
        });

        HBox lineBox = new HBox(10, lineSlider, lineField);
        getChildren().addAll(lineLabel, lineBox);
    }

    private void validateAndSetLine() {
        try {
            int val = Integer.parseInt(lineField.getText().trim());
            if (val < 1 || val > 10) {
                UI.showErr("Ошибка!","Толщина линии должна быть целым числом от 1 до 10.");
                lineField.setText(String.valueOf(tempLineThickness.get()));
                return;
            }
            tempLineThickness.set(val);
            lineSlider.setValue(val);
        } catch (NumberFormatException e) {
            UI.showErr("Ошибка!","Введите целое число.");
            lineField.setText(String.valueOf(tempLineThickness.get()));
        }
    }

    private void createStampControls() {
        Label typeLabel = new Label("Тип штампа:");
        polygonRadio = new RadioButton("Полигон");
        starRadio = new RadioButton("Звезда");
        stampTypeGroup = new ToggleGroup();
        polygonRadio.setToggleGroup(stampTypeGroup);
        starRadio.setToggleGroup(stampTypeGroup);

        if (tempStampType.get() == StampType.POLYGON) {
            polygonRadio.setSelected(true);
        } else {
            starRadio.setSelected(true);
        }

        stampTypeGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            if (newToggle == polygonRadio) {
                tempStampType.set(StampType.POLYGON);
            } else if (newToggle == starRadio) {
                tempStampType.set(StampType.STAR);
            }
        });

        tempStampType.addListener((obs, old, val) -> {
            if (val == StampType.POLYGON) {
                polygonRadio.setSelected(true);
            } else {
                starRadio.setSelected(true);
            }
        });

        HBox typeBox = new HBox(10, polygonRadio, starRadio);
        getChildren().addAll(typeLabel, typeBox, new Separator());

        Label radiusLabel = new Label("Радиус (1-200):");
        radiusSlider = new Slider(1, 200, tempRadius.get());
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setMajorTickUnit(50);
        radiusSlider.setMinorTickCount(5);

        radiusField = new TextField();
        radiusField.setPrefWidth(60);
        radiusField.setText(String.valueOf(tempRadius.get()));

        radiusSlider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            radiusField.setText(String.valueOf(intVal));
            tempRadius.set(intVal);
        });

        radiusField.setOnAction(e -> validateAndSetRadius());
        radiusField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateAndSetRadius();
        });

        HBox radiusBox = new HBox(10, radiusSlider, radiusField);
        getChildren().addAll(radiusLabel, radiusBox);

        Label rotationLabel = new Label("Поворот (0-360):");
        rotationSlider = new Slider(0, 360, tempRotation.get());
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setShowTickMarks(true);
        rotationSlider.setMajorTickUnit(90);
        rotationSlider.setMinorTickCount(9);

        rotationField = new TextField();
        rotationField.setPrefWidth(60);
        rotationField.setText(String.valueOf(tempRotation.get()));

        rotationSlider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            rotationField.setText(String.valueOf(intVal));
            tempRotation.set(intVal);
        });

        rotationField.setOnAction(e -> validateAndSetRotation());
        rotationField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateAndSetRotation();
        });

        HBox rotationBox = new HBox(10, rotationSlider, rotationField);
        getChildren().addAll(rotationLabel, rotationBox);

        Label sidesLabel = new Label("Количество сторон (3-16):");
        sidesSlider = new Slider(3, 20, tempSidesCount.get());
        sidesSlider.setShowTickLabels(true);
        sidesSlider.setShowTickMarks(true);
        sidesSlider.setMajorTickUnit(5);
        sidesSlider.setMinorTickCount(4);

        sidesField = new TextField();
        sidesField.setPrefWidth(60);
        sidesField.setText(String.valueOf(tempSidesCount.get()));

        sidesSlider.valueProperty().addListener((obs, old, val) -> {
            int intVal = val.intValue();
            sidesField.setText(String.valueOf(intVal));
            tempSidesCount.set(intVal);
        });

        sidesField.setOnAction(e -> validateAndSetSides());
        sidesField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) validateAndSetSides();
        });

        HBox sidesBox = new HBox(10, sidesSlider, sidesField);
        getChildren().addAll(sidesLabel, sidesBox);
    }

    private void validateAndSetRadius() {
        try {
            int val = Integer.parseInt(radiusField.getText().trim());
            if (val < 1 || val > 200) {
                UI.showErr("Ошибка!", "Радиус должен быть целым числом от 1 до 200.");
                radiusField.setText(String.valueOf(tempRadius.get()));
                return;
            }
            tempRadius.set(val);
            radiusSlider.setValue(val);
        } catch (NumberFormatException e) {
            UI.showErr("Ошибка!", "Введите целое число.");
            radiusField.setText(String.valueOf(tempRadius.get()));
        }
    }

    private void validateAndSetRotation() {
        try {
            int val = Integer.parseInt(rotationField.getText().trim());
            if (val < 0 || val > 360) {
                UI.showErr("Ошибка!", "Поворот должен быть целым числом от 0 до 360.");
                rotationField.setText(String.valueOf(tempRotation.get()));
                return;
            }
            tempRotation.set(val);
            rotationSlider.setValue(val);
        } catch (NumberFormatException e) {
            UI.showErr("Ошибка!", "Введите целое число.");
            rotationField.setText(String.valueOf(tempRotation.get()));
        }
    }

    private void validateAndSetSides() {
        try {
            int val = Integer.parseInt(sidesField.getText().trim());
            if (val < 3 || val > 16) {
                UI.showErr("Ошибка!", "Количество сторон должно быть целым числом от 3 до 16.");
                sidesField.setText(String.valueOf(tempSidesCount.get()));
                return;
            }
            tempSidesCount.set(val);
            sidesSlider.setValue(val);
        } catch (NumberFormatException e) {
            UI.showErr("Ошибка!", "Введите целое число.");
            sidesField.setText(String.valueOf(tempSidesCount.get()));
        }
    }

    public void resetToSettings() {
        tempColor.set(settings.getCurrentColor());
        tempBrushSize.set(settings.getBrushSize());
        tempLineThickness.set(settings.getLineThickness());
        tempRadius.set(settings.getRadius());
        tempRotation.set(settings.getRotation());
        tempSidesCount.set(settings.getSidesCount());
        tempStampType.set(settings.getStampType());
    }

    public void applySettings() {
        settings.setCurrentColor(tempColor.get());
        settings.setBrushSize(tempBrushSize.get());
        settings.setLineThickness(tempLineThickness.get());
        settings.setRadius(tempRadius.get());
        settings.setRotation(tempRotation.get());
        settings.setSidesCount(tempSidesCount.get());
        settings.setStampType(tempStampType.get());
    }
}