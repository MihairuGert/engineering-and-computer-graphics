package nsu.task5.math;

import nsu.wireframe.model.Point3DModel;

public final class VectorMath {
    private static final double EPS = 1e-9;

    private VectorMath() {
    }

    public static Point3DModel add(Point3DModel first, Point3DModel second) {
        return new Point3DModel(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    public static Point3DModel subtract(Point3DModel first, Point3DModel second) {
        return new Point3DModel(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    public static Point3DModel multiply(Point3DModel vector, double factor) {
        return new Point3DModel(vector.x() * factor, vector.y() * factor, vector.z() * factor);
    }

    public static double dot(Point3DModel first, Point3DModel second) {
        return first.x() * second.x() + first.y() * second.y() + first.z() * second.z();
    }

    public static Point3DModel cross(Point3DModel first, Point3DModel second) {
        return new Point3DModel(
                first.y() * second.z() - first.z() * second.y(),
                first.z() * second.x() - first.x() * second.z(),
                first.x() * second.y() - first.y() * second.x()
        );
    }

    public static double length(Point3DModel vector) {
        return Math.sqrt(dot(vector, vector));
    }

    public static Point3DModel normalize(Point3DModel vector) {
        double length = length(vector);
        if (length < EPS || !Double.isFinite(length)) {
            return new Point3DModel(0, 0, 0);
        }
        return multiply(vector, 1.0 / length);
    }

    // Rodrigues
    public static Point3DModel rotateAroundAxis(Point3DModel vector, Point3DModel axis, double angle) {
        Point3DModel unitAxis = normalize(axis);
        if (length(unitAxis) < EPS) {
            return vector;
        }

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Point3DModel first = multiply(vector, cos);
        Point3DModel second = multiply(cross(unitAxis, vector), sin);
        Point3DModel third = multiply(unitAxis, dot(unitAxis, vector) * (1 - cos));
        return add(add(first, second), third);
    }

    public static double distance(Point3DModel first, Point3DModel second) {
        return length(subtract(first, second));
    }

    public static boolean isZero(Point3DModel vector) {
        return length(vector) < EPS;
    }
}
