package nsu.wireframe.ui;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public final class AlertUtils {
    private AlertUtils() {
    }

    public static void showError(Window owner, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public static void showInfo(Window owner, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}
