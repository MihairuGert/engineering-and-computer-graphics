package nsu.task5.model;

import nsu.task5.render.Hit;
import nsu.task5.render.Ray;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment3D;

import java.util.List;
import java.util.Optional;

public record AxisAlignedBox(Point3DModel min, Point3DModel max, Material material) implements Primitive {
    @Override
    public BoundingBox boundingBox() {
        return BoundingBox.of(min, max);
    }

    @Override
    public WireframeMesh toWireframeMesh() {
        Point3DModel p000 = new Point3DModel(min.x(), min.y(), min.z());
        Point3DModel p100 = new Point3DModel(max.x(), min.y(), min.z());
        Point3DModel p010 = new Point3DModel(min.x(), max.y(), min.z());
        Point3DModel p110 = new Point3DModel(max.x(), max.y(), min.z());
        Point3DModel p001 = new Point3DModel(min.x(), min.y(), max.z());
        Point3DModel p101 = new Point3DModel(max.x(), min.y(), max.z());
        Point3DModel p011 = new Point3DModel(min.x(), max.y(), max.z());
        Point3DModel p111 = new Point3DModel(max.x(), max.y(), max.z());

        return new WireframeMesh(List.of(
                new Segment3D(p000, p100),
                new Segment3D(p100, p110),
                new Segment3D(p110, p010),
                new Segment3D(p010, p000),
                new Segment3D(p001, p101),
                new Segment3D(p101, p111),
                new Segment3D(p111, p011),
                new Segment3D(p011, p001),
                new Segment3D(p000, p001),
                new Segment3D(p100, p101),
                new Segment3D(p110, p111),
                new Segment3D(p010, p011)
        ));
    }

    @Override
    public Optional<Hit> intersect(Ray ray) {
        final double eps = 1e-6;

        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        double ox = ray.origin().x();
        double oy = ray.origin().y();
        double oz = ray.origin().z();

        double dx = ray.direction().x();
        double dy = ray.direction().y();
        double dz = ray.direction().z();

        double[] resultX = intersectAxis(ox, dx, min.x(), max.x(), eps);
        if (resultX == null) {
            return Optional.empty();
        }

        double[] resultY = intersectAxis(oy, dy, min.y(), max.y(), eps);
        if (resultY == null) {
            return Optional.empty();
        }

        double[] resultZ = intersectAxis(oz, dz, min.z(), max.z(), eps);
        if (resultZ == null) {
            return Optional.empty();
        }

        tMin = Math.max(tMin, resultX[0]);
        tMax = Math.min(tMax, resultX[1]);

        tMin = Math.max(tMin, resultY[0]);
        tMax = Math.min(tMax, resultY[1]);

        tMin = Math.max(tMin, resultZ[0]);
        tMax = Math.min(tMax, resultZ[1]);

        if (tMin > tMax) {
            return Optional.empty();
        }

        if (tMax <= eps) {
            return Optional.empty();
        }

        double t = tMin > eps ? tMin : tMax;

        var point = ray.origin().sum(ray.direction().mul(t));
        var normal = normalAt(point, eps);

        return Optional.of(new Hit(t, point, normal, material, this));
    }

    private double[] intersectAxis(
            double origin,
            double direction,
            double minValue,
            double maxValue,
            double eps
    ) {
        if (Math.abs(direction) < eps) {
            if (origin < minValue || origin > maxValue) {
                return null;
            }

            return new double[]{
                    Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY
            };
        }

        double t1 = (minValue - origin) / direction;
        double t2 = (maxValue - origin) / direction;

        if (t1 > t2) {
            double tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        return new double[]{t1, t2};
    }

    private Point3DModel normalAt(Point3DModel point, double eps) {
        if (Math.abs(point.x() - min.x()) < eps) {
            return new Point3DModel(-1.0, 0.0, 0.0);
        }

        if (Math.abs(point.x() - max.x()) < eps) {
            return new Point3DModel(1.0, 0.0, 0.0);
        }

        if (Math.abs(point.y() - min.y()) < eps) {
            return new Point3DModel(0.0, -1.0, 0.0);
        }

        if (Math.abs(point.y() - max.y()) < eps) {
            return new Point3DModel(0.0, 1.0, 0.0);
        }

        if (Math.abs(point.z() - min.z()) < eps) {
            return new Point3DModel(0.0, 0.0, -1.0);
        }

        if (Math.abs(point.z() - max.z()) < eps) {
            return new Point3DModel(0.0, 0.0, 1.0);
        }

        return new Point3DModel(0.0, 0.0, 1.0);
    }
}
