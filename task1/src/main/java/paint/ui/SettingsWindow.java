package paint.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsWindow extends Stage {
    private SettingsToolbar settingsToolbar;
    private Settings settings;

    public SettingsWindow(Settings settings, DrawPanel drawPanel) {
        this.settings = settings;

        initStyle(StageStyle.UTILITY);
        setTitle("Paint Settings");
        setResizable(false);

        settingsToolbar = new SettingsToolbar(settings, drawPanel);

        BorderPane root = new BorderPane();
        root.setCenter(settingsToolbar);

        Scene scene = new Scene(root, 250, 400);
        setScene(scene);

        setOnShown(e -> settingsToolbar.updateFromSettings());
    }

    public void showSettings() {
        if (!isShowing()) {
            show();
        } else {
            requestFocus();
            settingsToolbar.updateFromSettings();
        }
    }
}