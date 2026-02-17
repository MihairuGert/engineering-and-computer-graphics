package paint.ui.clickable;

import paint.ui.ToolMode;

public class Fill extends ToolButton {
    public Fill(String activeSource, String inactiveSource) {
        super(activeSource, inactiveSource);
    }

    @Override
    public ToolMode getToolMode() {
        return ToolMode.FILL;
    }
}
