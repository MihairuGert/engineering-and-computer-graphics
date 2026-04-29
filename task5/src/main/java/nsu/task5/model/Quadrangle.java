package nsu.task5.model;

import nsu.task5.math.VectorMath;
import nsu.task5.render.Hit;
import nsu.task5.render.Ray;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment3D;

import java.util.List;
import java.util.Optional;

public record Quadrangle(
        Point3DModel p1,
        Point3DModel p2,
        Point3DModel p3,
        Point3DModel p4,
        Material material
) implements Primitive {
    @Override
    public BoundingBox boundingBox() {
        return BoundingBox.of(p1, p2).include(p3).include(p4);
    }

    @Override
    public WireframeMesh toWireframeMesh() {
        return new WireframeMesh(List.of(
                new Segment3D(p1, p2),
                new Segment3D(p2, p3),
                new Segment3D(p3, p4),
                new Segment3D(p4, p1)
        ));
    }

    @Override
    public Optional<Hit> intersect(Ray ray) {
        Optional<Hit> first = intersectTriangle(ray, p1, p2, p3);
        Optional<Hit> second = intersectTriangle(ray, p1, p3, p4);

        if (first.isEmpty()) {
            return second;
        }

        if (second.isEmpty()) {
            return first;
        }

        return first.get().t() <= second.get().t()
                ? first
                : second;
    }

    private Optional<Hit> intersectTriangle(
            Ray ray,
            Point3DModel a,
            Point3DModel b,
            Point3DModel c
    ) {
        final double eps = 1e-6;

        var edge1 = b.sub(a);
        var edge2 = c.sub(a);

        var pvec = VectorMath.cross(ray.direction(), edge2);
        double det = VectorMath.dot(edge1, pvec);

        if (Math.abs(det) <= eps) {
            return Optional.empty();
        }

        double invDet = 1.0 / det;

        var tvec = ray.origin().sub(a);

        double u = VectorMath.dot(tvec, pvec) * invDet;

        if (u < -eps || u > 1.0 + eps) {
            return Optional.empty();
        }

        var qvec = VectorMath.cross(tvec, edge1);

        double v = VectorMath.dot(ray.direction(), qvec) * invDet;

        if (v < -eps || u + v > 1.0 + eps) {
            return Optional.empty();
        }

        double t = VectorMath.dot(edge2, qvec) * invDet;

        if (t <= eps) {
            return Optional.empty();
        }

        var point = ray.origin().sum(ray.direction().mul(t));
        var normal = VectorMath.cross(edge1, edge2).normalize();

        return Optional.of(new Hit(t, point, normal, material, this));
    }
}