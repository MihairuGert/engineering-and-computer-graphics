package nsu.wireframe.model;

public record ControlPoint(double u, double v) {
    public Point2DModel toPoint2D() {
        return new Point2DModel(u, v);
    }

    public boolean isFinite() {
        return Double.isFinite(u) && Double.isFinite(v);
    }
}
