package nsu.task5.model;

import nsu.task5.render.Hit;
import nsu.task5.render.Ray;

import java.util.Optional;

public interface Primitive {
    BoundingBox boundingBox();

    WireframeMesh toWireframeMesh();

    Optional<Hit> intersect(Ray ray);
}
