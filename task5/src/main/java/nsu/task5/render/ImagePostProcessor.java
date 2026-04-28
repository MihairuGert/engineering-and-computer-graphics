package nsu.task5.render;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import nsu.task5.model.RgbColor;

public class ImagePostProcessor {
    public int[] toArgb(RenderResult result) {
        int width = result.width();
        int height = result.height();
        RgbColor[] source = result.buffer();
        int[] argb = new int[width * height];

        double max = findMax(source);
        double gamma = result.settings().gamma();
        if (gamma <= 0 || !Double.isFinite(gamma)) {
            gamma = 1.0;
        }

        for (int i = 0; i < source.length; i++) {
            RgbColor color = source[i] == null ? RgbColor.BLACK : source[i];
            double r = normalize(color.r(), max);
            double g = normalize(color.g(), max);
            double b = normalize(color.b(), max);

            r = Math.pow(r, 1.0 / gamma);
            g = Math.pow(g, 1.0 / gamma);
            b = Math.pow(b, 1.0 / gamma);

            int red = to255(r);
            int green = to255(g);
            int blue = to255(b);
            argb[i] = 0xFF000000 | (red << 16) | (green << 8) | blue;
        }

        return argb;
    }

    public WritableImage toWritableImage(RenderResult result) {
        WritableImage image = new WritableImage(result.width(), result.height());
        PixelWriter writer = image.getPixelWriter();
        int[] argb = toArgb(result);
        int index = 0;
        for (int y = 0; y < result.height(); y++) {
            for (int x = 0; x < result.width(); x++) {
                writer.setArgb(x, y, argb[index++]);
            }
        }
        return image;
    }

    private double findMax(RgbColor[] colors) {
        double max = 0;
        for (RgbColor color : colors) {
            if (color == null) {
                continue;
            }
            max = Math.max(max, Math.max(color.r(), Math.max(color.g(), color.b())));
        }
        return max;
    }

    private double normalize(double value, double max) {
        if (max <= 0) {
            return 0;
        }
        return RgbColor.clamp01(value / max);
    }

    private int to255(double value) {
        return (int) (RgbColor.clamp01(value) * 255.0 + 0.5);
    }
}
