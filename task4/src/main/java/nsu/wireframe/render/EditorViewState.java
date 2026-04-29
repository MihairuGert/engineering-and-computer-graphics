package nsu.wireframe.render;

import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.Point2DModel;

import java.util.List;

public class EditorViewState {
    private static final double MIN_SCALE = 20;
    private static final double MAX_SCALE = 2000;
    private static final double NORMALIZE_PADDING = 70;

    private double offsetX;
    private double offsetY;
    private double scale = 160;

    public Point2DModel modelToScreen(Point2DModel point, double width, double height) {
        double screenX = width / 2 + offsetX + point.x() * scale;
        double screenY = height / 2 + offsetY - point.y() * scale;
        return new Point2DModel(screenX, screenY);
    }

    public ControlPoint screenToControlPoint(double screenX, double screenY, double width, double height) {
        double u = (screenX - width / 2 - offsetX) / scale;
        double v = -(screenY - height / 2 - offsetY) / scale;
        return new ControlPoint(u, v);
    }

    public void move(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
    }

    public void zoom(double factor) {
        scale = clamp(scale * factor, MIN_SCALE, MAX_SCALE);
    }

    public void normalize(List<ControlPoint> points, double width, double height) {
        if (points.isEmpty() || width <= NORMALIZE_PADDING * 2 || height <= NORMALIZE_PADDING * 2) {
            offsetX = 0;
            offsetY = 0;
            scale = 160;
            return;
        }

        double minU = points.getFirst().u();
        double maxU = points.getFirst().u();
        double minV = points.getFirst().v();
        double maxV = points.getFirst().v();
        for (ControlPoint point : points) {
            minU = Math.min(minU, point.u());
            maxU = Math.max(maxU, point.u());
            minV = Math.min(minV, point.v());
            maxV = Math.max(maxV, point.v());
        }

        double modelWidth = Math.max(maxU - minU, 0.01);
        double modelHeight = Math.max(maxV - minV, 0.01);
        double availableWidth = width - NORMALIZE_PADDING * 2;
        double availableHeight = height - NORMALIZE_PADDING * 2;
        scale = clamp(Math.min(availableWidth / modelWidth, availableHeight / modelHeight), MIN_SCALE, MAX_SCALE);

        double centerU = (minU + maxU) / 2;
        double centerV = (minV + maxV) / 2;
        offsetX = -centerU * scale;
        offsetY = centerV * scale;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
