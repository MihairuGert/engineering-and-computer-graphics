package nsu.task5.model;

import nsu.task5.math.VectorMath;
import nsu.task5.render.Hit;
import nsu.task5.render.Ray;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Sphere(Point3DModel center, double radius, Material material) implements Primitive {
    private static final int SEGMENTS = 24;
    private static final int RINGS = 12;

    @Override
    public BoundingBox boundingBox() {
        Point3DModel delta = new Point3DModel(radius, radius, radius);
        return BoundingBox.of(
                new Point3DModel(center.x() - delta.x(), center.y() - delta.y(), center.z() - delta.z()),
                new Point3DModel(center.x() + delta.x(), center.y() + delta.y(), center.z() + delta.z())
        );
    }

    @Override
    public WireframeMesh toWireframeMesh() {
        List<Segment3D> segments = new ArrayList<>();
        addLatitudeSegments(segments);
        addLongitudeSegments(segments);
        return new WireframeMesh(segments);
    }

    @Override
    public Optional<Hit> intersect(Ray ray) {
        final double eps = 1e-6;

        double a = VectorMath.dot(ray.direction(), ray.direction());

        if (Math.abs(a) < eps) {
            return Optional.empty();
        }

        double b = 2 * VectorMath.dot(ray.origin().sub(center), ray.direction());
        double c = VectorMath.dot(ray.origin().sub(center), ray.origin().sub(center)) - radius*radius;

        double D = b*b - 4*a*c;

        if (D < 0) {
            return Optional.empty();
        }

        double t1 = (-b - Math.sqrt(D)) / (2*a);
        double t2 = (-b + Math.sqrt(D)) / (2*a);
        double t;

        if (t1 > eps) {
            t = t1;
        } else if (t2 > eps) {
            t = t2;
        } else {
            return Optional.empty();
        }

        var point = ray.origin().sum(ray.direction().mul(t));
        var normal = point.sub(center).normalize();

        return Optional.of(new Hit(t, point, normal, material, this));
    }

    private void addLatitudeSegments(List<Segment3D> segments) {
        for (int ring = 1; ring < RINGS; ring++) {
            double phi = -Math.PI / 2.0 + Math.PI * ring / RINGS;
            Point3DModel previous = spherePoint(phi, 0);
            for (int segment = 1; segment <= SEGMENTS; segment++) {
                double theta = 2.0 * Math.PI * segment / SEGMENTS;
                Point3DModel next = spherePoint(phi, theta);
                segments.add(new Segment3D(previous, next));
                previous = next;
            }
        }
    }

    private void addLongitudeSegments(List<Segment3D> segments) {
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double theta = 2.0 * Math.PI * segment / SEGMENTS;
            Point3DModel previous = spherePoint(-Math.PI / 2.0, theta);
            for (int ring = 1; ring <= RINGS; ring++) {
                double phi = -Math.PI / 2.0 + Math.PI * ring / RINGS;
                Point3DModel next = spherePoint(phi, theta);
                segments.add(new Segment3D(previous, next));
                previous = next;
            }
        }
    }

    private Point3DModel spherePoint(double phi, double theta) {
        double cosPhi = Math.cos(phi);
        return new Point3DModel(
                center.x() + radius * cosPhi * Math.cos(theta),
                center.y() + radius * cosPhi * Math.sin(theta),
                center.z() + radius * Math.sin(phi)
        );
    }
}
