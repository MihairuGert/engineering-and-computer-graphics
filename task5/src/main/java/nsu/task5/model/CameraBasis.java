package nsu.task5.model;

import nsu.wireframe.model.Point3DModel;

public record CameraBasis(Point3DModel forward, Point3DModel right, Point3DModel up) {
}
