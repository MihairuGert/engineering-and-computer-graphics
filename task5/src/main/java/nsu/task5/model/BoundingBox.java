package nsu.task5.model;

import nsu.wireframe.model.Point3DModel;

import java.util.Optional;

public record BoundingBox(Point3DModel min, Point3DModel max) {
    public static BoundingBox of(Point3DModel first, Point3DModel second) {
        return new BoundingBox(
                new Point3DModel(
                        Math.min(first.x(), second.x()),
                        Math.min(first.y(), second.y()),
                        Math.min(first.z(), second.z())
                ),
                new Point3DModel(
                        Math.max(first.x(), second.x()),
                        Math.max(first.y(), second.y()),
                        Math.max(first.z(), second.z())
                )
        );
    }

    public BoundingBox include(Point3DModel point) {
        return new BoundingBox(
                new Point3DModel(
                        Math.min(min.x(), point.x()),
                        Math.min(min.y(), point.y()),
                        Math.min(min.z(), point.z())
                ),
                new Point3DModel(
                        Math.max(max.x(), point.x()),
                        Math.max(max.y(), point.y()),
                        Math.max(max.z(), point.z())
                )
        );
    }

    public BoundingBox include(BoundingBox other) {
        return include(other.min).include(other.max);
    }

    public BoundingBox expand(double factor) {
        Point3DModel center = center();
        double hx = (max.x() - min.x()) * factor / 2.0;
        double hy = (max.y() - min.y()) * factor / 2.0;
        double hz = (max.z() - min.z()) * factor / 2.0;
        return new BoundingBox(
                new Point3DModel(center.x() - hx, center.y() - hy, center.z() - hz),
                new Point3DModel(center.x() + hx, center.y() + hy, center.z() + hz)
        );
    }

    public Point3DModel center() {
        return new Point3DModel(
                (min.x() + max.x()) / 2.0,
                (min.y() + max.y()) / 2.0,
                (min.z() + max.z()) / 2.0
        );
    }

    public double sizeX() {
        return max.x() - min.x();
    }

    public double sizeY() {
        return max.y() - min.y();
    }

    public double sizeZ() {
        return max.z() - min.z();
    }

    public double maxSize() {
        return Math.max(sizeX(), Math.max(sizeY(), sizeZ()));
    }

    public static Optional<BoundingBox> combine(Iterable<BoundingBox> boxes) {
        BoundingBox combined = null;
        for (BoundingBox box : boxes) {
            combined = combined == null ? box : combined.include(box);
        }
        return Optional.ofNullable(combined);
    }
}
