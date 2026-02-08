package paint.ui.clickable;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import static paint.ui.Menu.createIcon;
import static paint.ui.Menu.newButtonWithImage;

public class Clickable {
    private boolean isClicked = false;
    private ImageView inactiveLineImg;
    private ImageView activeLineImg;
    private Tooltip tip;
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

    public Clickable(String inactiveSource) {
        inactiveLineImg = createIcon(inactiveSource);
        button = newButtonWithImage(inactiveSource);
    }

    Clickable(String activeSource, String inactiveSource) {
        activeLineImg = createIcon(activeSource);
        inactiveLineImg = createIcon(inactiveSource);
        button = newButtonWithImage(inactiveSource);
    }

    public void setTip(String tipMsg) {
        tip = new Tooltip(tipMsg);
        tip.setShowDelay(Duration.millis(0.3));
        tip.setHideDelay(Duration.millis(0.3));
        button.setTooltip(tip);
    }
}