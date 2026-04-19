package nsu.wireframe.math;

import javafx.scene.paint.Color;

public class ColorService {
    public Color colorForDepth(double normalizedDepth) {
        double value = clamp(normalizedDepth);
        return Color.color(value, 1.0 - value, 0.35);
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}
