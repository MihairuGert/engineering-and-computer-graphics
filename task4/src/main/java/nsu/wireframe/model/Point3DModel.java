package nsu.wireframe.model;

public record Point3DModel(double x, double y, double z) {
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    public Point3DModel sum(Point3DModel p) {
        return new Point3DModel(x + p.x, y + p.y, z + p.z);
    }

    public Point3DModel sub(Point3DModel p) {
        return new Point3DModel(x - p.x, y - p.y, z - p.z);
    }

    public Point3DModel mul(double scalar) {
        return new Point3DModel(x * scalar, y * scalar, z * scalar);
    }

    public Point3DModel normalize() {
        double norm = Math.sqrt(x*x + y*y + z*z);
        return new Point3DModel(x / norm, y / norm, z / norm);
    }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }
}
