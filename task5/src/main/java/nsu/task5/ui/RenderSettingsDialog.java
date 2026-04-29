package nsu.task5.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import nsu.task5.model.RgbColor;
import nsu.task5.render.RenderSettings;

import java.util.Optional;

public class RenderSettingsDialog {
    private final Window owner;

    public RenderSettingsDialog(Window owner) {
        this.owner = owner;
    }

    public Optional<RenderSettings> showAndWait(RenderSettings current) {
        Dialog<RenderSettings> dialog = new Dialog<>();
        dialog.setTitle("Render Settings");
        dialog.setHeaderText("Render Settings");
        if (owner != null) {
            dialog.initOwner(owner);
        }

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        ColorPicker backgroundPicker = new ColorPicker(current.backgroundColor().toFxColor());
        TextField gammaField = new TextField(Double.toString(current.gamma()));
        Spinner<Integer> depthSpinner = new Spinner<>();
        depthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                RenderSettings.MIN_DEPTH,
                RenderSettings.MAX_DEPTH,
                current.depth()
        ));
        depthSpinner.setEditable(true);
        TextField qualityField = new TextField(RenderSettings.NORMAL_QUALITY);
        qualityField.setDisable(true);
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));
        grid.add(new Label("Background"), 0, 0);
        grid.add(backgroundPicker, 1, 0);
        grid.add(new Label("Gamma"), 0, 1);
        grid.add(gammaField, 1, 1);
        grid.add(new Label("Depth"), 0, 2);
        grid.add(depthSpinner, 1, 2);
        grid.add(new Label("Quality"), 0, 3);
        grid.add(qualityField, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Node applyButton = dialog.getDialogPane().lookupButton(applyButtonType);
        applyButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (parseGamma(gammaField.getText()).isEmpty()) {
                errorLabel.setText("Gamma must be a positive finite number.");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType != applyButtonType) {
                return null;
            }
            RenderSettings updated = current.copy();
            Color background = backgroundPicker.getValue();
            updated.setBackgroundColor(RgbColor.fromFx(background));
            updated.setGamma(parseGamma(gammaField.getText()).orElse(current.gamma()));
            updated.setDepth(depthSpinner.getValue());
            updated.setQuality(RenderSettings.NORMAL_QUALITY);
            return updated;
        });

        return dialog.showAndWait();
    }

    private Optional<Double> parseGamma(String text) {
        try {
            double gamma = Double.parseDouble(text.trim());
            if (gamma <= 0 || !Double.isFinite(gamma)) {
                return Optional.empty();
            }
            return Optional.of(gamma);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
