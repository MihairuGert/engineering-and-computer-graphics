package nsu.wireframe.model;

import java.util.List;

public class WireframeModel {
    private static final WireframeModel EMPTY = new WireframeModel(List.of());

    private final List<Segment3D> segments;

    public WireframeModel(List<Segment3D> segments) {
        this.segments = List.copyOf(segments);
    }

    public static WireframeModel empty() {
        return EMPTY;
    }

    public List<Segment3D> getSegments() {
        return segments;
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }
}
