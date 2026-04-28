package nsu.task5.model;

import nsu.wireframe.model.Segment3D;

import java.util.List;

public record WireframeMesh(List<Segment3D> segments) {
    public WireframeMesh {
        segments = List.copyOf(segments);
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }
}
