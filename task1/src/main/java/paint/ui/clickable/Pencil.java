package paint.ui.clickable;

import paint.ui.ToolMode;

public class Pencil extends ToolButton {
    public Pencil(String activeSource, String inactiveSource) {
        super(activeSource, inactiveSource);
    }

    @Override
    public ToolMode getToolMode() {
        return ToolMode.PENCIL;
    }
}
