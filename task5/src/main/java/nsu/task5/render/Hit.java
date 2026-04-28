package nsu.task5.render;

import nsu.task5.model.Material;
import nsu.task5.model.Primitive;
import nsu.wireframe.model.Point3DModel;

public record Hit(
        double t,
        Point3DModel point,
        Point3DModel normal,
        Material material,
        Primitive primitive
) {
}
