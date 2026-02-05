package paint.ui;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import static paint.ui.Menu.createIcon;
import static paint.ui.Menu.newButtonWithImage;

public class Clickable {
    private boolean isClicked = false;
    private ImageView inactiveLineImg;
    private ImageView activeLineImg;
    public Button button;

    public void setActive() {
        button.setGraphic(activeLineImg);
        isClicked = true;
    }

    public void setInactive() {
        if (!isClicked) {
            return;
        }
        button.setGraphic(inactiveLineImg);
        isClicked = false;
    }

    public boolean isClicked() {
        return isClicked;
    }

    Clickable(String activeSource, String inactiveSource) {
        activeLineImg = createIcon(activeSource);
        inactiveLineImg = createIcon(inactiveSource);
        button = newButtonWithImage(inactiveSource);
    }
}

