package paint.ui.clickable;

import paint.ui.DrawPanel;
import paint.ui.ToolMode;

public abstract class ToolButton extends Clickable {
    public ToolButton(String activeSource, String inactiveSource) {
        super(activeSource, inactiveSource);
    }

    public abstract ToolMode getToolMode();

    public void onActivate(DrawPanel drawPanel) { }
    public void onDeactivate(DrawPanel drawPanel) { }
}
