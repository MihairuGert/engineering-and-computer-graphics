package nsu.wireframe.model;

public record Point2DModel(double x, double y) {
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y);
    }
}
