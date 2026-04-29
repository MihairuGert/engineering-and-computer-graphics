package nsu.task5.model;

import javafx.scene.paint.Color;

public record RgbColor(double r, double g, double b) {
    public static final RgbColor BLACK = new RgbColor(0, 0, 0);

    public static RgbColor from255(double r, double g, double b) {
        return new RgbColor(r / 255.0, g / 255.0, b / 255.0);
    }

    public static RgbColor fromFx(Color color) {
        return new RgbColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    public Color toFxColor() {
        return Color.color(clamp01(r), clamp01(g), clamp01(b));
    }

    public int red255() {
        return to255(r);
    }

    public int green255() {
        return to255(g);
    }

    public int blue255() {
        return to255(b);
    }

    public RgbColor add(RgbColor other) {
        return new RgbColor(r + other.r, g + other.g, b + other.b);
    }

    private int to255(double value) {
        return (int) (clamp01(value) * 255.0 + 0.5);
    }

    public static double clamp01(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}
