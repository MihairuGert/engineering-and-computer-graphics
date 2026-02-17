package paint.ui.windows;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import paint.ui.Settings;

public class SettingsDialog extends Stage {
    private final Settings settings;
    private final SettingsPanel settingsPanel;

    public SettingsDialog(Window owner, Settings settings) {
        this.settings = settings;
        this.settingsPanel = new SettingsPanel(settings);

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Настройки");
        setResizable(false);

        BorderPane root = new BorderPane();
        root.setCenter(settingsPanel);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(10));

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Отмена");

        okButton.setOnAction(e -> {
            settingsPanel.applySettings();
            close();
        });

        cancelButton.setOnAction(e -> {
            settingsPanel.resetToSettings();
            close();
        });

        buttonBar.getButtons().addAll(okButton, cancelButton);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 350, 650);
        setScene(scene);
    }
}