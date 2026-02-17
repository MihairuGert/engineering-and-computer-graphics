package paint.ui.clickable;

import paint.ui.DrawPanel;
import paint.ui.ToolMode;

public class Line extends ToolButton{

    public Line(String activeSource, String inactiveSource) {
        super(activeSource, inactiveSource);
    }

    @Override
    public ToolMode getToolMode() {
        return ToolMode.LINE;
    }

    @Override
    public void onActivate(DrawPanel drawPanel) {
        drawPanel.resetTools();
    }
}
