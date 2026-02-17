package paint.ui.clickable;

import paint.ui.ToolMode;

public class Stamp extends ToolButton{
    public Stamp(String activeSource, String inactiveSource) {
        super(activeSource, inactiveSource);
    }

    @Override
    public ToolMode getToolMode() {
        return ToolMode.STAMP;
    }
}
