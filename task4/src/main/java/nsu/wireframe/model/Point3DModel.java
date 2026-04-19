package nsu.wireframe.model;

public record Point3DModel(double x, double y, double z) {
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }
}
