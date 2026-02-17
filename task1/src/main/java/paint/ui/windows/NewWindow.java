package paint.ui.windows;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import paint.ui.DrawPanel;

public class NewWindow extends Stage {
    private NewToolbar newToolbar;

    public NewWindow(Stage owner, DrawPanel drawPanel) {
        initOwner(owner);
        initStyle(StageStyle.UTILITY);
        setTitle("Новый рысунок");
        setResizable(false);

        newToolbar = new NewToolbar(this, drawPanel);

        BorderPane root = new BorderPane();
        root.setCenter(newToolbar);

        Scene scene = new Scene(root, 250, 400);
        setScene(scene);
    }

    public void showNew() {
        if (!isShowing()) {
            show();
        } else {
            requestFocus();
        }
    }
}
