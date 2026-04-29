package nsu.wireframe.math;

import javafx.scene.paint.Color;

public class ColorService {
    public Color colorForDepth(double normalizedDepth) {
        double value = clamp(normalizedDepth);
        if (value < 0.5) {
            double t = value * 2;
            return interpolate(Color.rgb(30, 90, 255), Color.rgb(40, 230, 120), t);
        }

        double t = (value - 0.5) * 2;
        return interpolate(Color.rgb(40, 230, 120), Color.rgb(255, 45, 35), t);
    }

    private Color interpolate(Color start, Color end, double t) {
        return Color.color(
                start.getRed() + (end.getRed() - start.getRed()) * t,
                start.getGreen() + (end.getGreen() - start.getGreen()) * t,
                start.getBlue() + (end.getBlue() - start.getBlue()) * t
        );
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
