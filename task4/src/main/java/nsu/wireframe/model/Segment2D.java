package nsu.wireframe.model;

public record Segment2D(Point2DModel start, Point2DModel end, double startDepth, double endDepth) {
    public double depth() {
        return (startDepth + endDepth) / 2.0;
    }
}
