package nsu.wireframe.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import nsu.wireframe.math.ColorService;
import nsu.wireframe.math.Matrix4;
import nsu.wireframe.math.ProjectionService;
import nsu.wireframe.math.Vector4;
import nsu.wireframe.model.AppState;
import nsu.wireframe.model.Point2DModel;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment2D;
import nsu.wireframe.model.Segment3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WireframeRenderer {
    private final ProjectionService projectionService;
    private final ColorService colorService;

    public WireframeRenderer(ProjectionService projectionService, ColorService colorService) {
        this.projectionService = projectionService;
        this.colorService = colorService;
    }

    public void render(GraphicsContext gc, double width, double height, AppState state) {
        clear(gc, width, height);
        drawWireframe(gc, width, height, state);
        drawAxesOverlay(gc, width, height, state);
        drawStatus(gc, state);
    }

    private void clear(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
    }

    private void drawWireframe(GraphicsContext gc, double width, double height, AppState state) {
        if (state.getWireframeModel().isEmpty()) {
            return;
        }

        List<Segment3D> transformedSegments = projectionService.applyTransformPipeline(
                state.getWireframeModel(),
                state.getViewParameters(),
                state.getCameraConfig()
        );

        List<Segment2D> projectedSegments = new ArrayList<>();
        for (Segment3D segment : transformedSegments) {
            Optional<Segment2D> projectedSegment = projectionService.projectSegment(
                    segment,
                    width,
                    height,
                    state.getViewParameters(),
                    state.getCameraConfig()
            );
            if (projectedSegment.isEmpty()) {
                continue;
            }

            projectedSegments.add(projectedSegment.get());
        }

        if (projectedSegments.isEmpty()) {
            return;
        }

        double minDepth = projectedSegments.getFirst().startDepth();
        double maxDepth = projectedSegments.getFirst().startDepth();
        for (Segment2D segment : projectedSegments) {
            minDepth = Math.min(minDepth, Math.min(segment.startDepth(), segment.endDepth()));
            maxDepth = Math.max(maxDepth, Math.max(segment.startDepth(), segment.endDepth()));
        }

        PixelWriter writer = gc.getPixelWriter();
        for (Segment2D screenSegment : projectedSegments) {
            drawLineBresenham(
                    writer,
                    (int) Math.round(screenSegment.start().x()),
                    (int) Math.round(screenSegment.start().y()),
                    (int) Math.round(screenSegment.end().x()),
                    (int) Math.round(screenSegment.end().y()),
                    screenSegment.startDepth(),
                    screenSegment.endDepth(),
                    minDepth,
                    maxDepth,
                    width,
                    height
            );
        }
    }

    private void drawLineBresenham(
            PixelWriter writer,
            int x0,
            int y0,
            int x1,
            int y1,
            double startDepth,
            double endDepth,
            double minDepth,
            double maxDepth,
            double width,
            double height
    ) {
        int x = x0;
        int y = y0;

        int dx = x1 - x;
        int dy = y1 - y;

        int xSign = dx > 0 ? 1 : -1;
        int ySign = dy > 0 ? 1 : -1;

        if (ySign * dy < xSign * dx) {
            int err = -xSign*dx;
            int steps = xSign * dx;
            for (int i = 0; i < xSign*dx; i++) {
                x += xSign;
                err += 2 * dy * ySign;
                if (err > 0) {
                    err -= 2 * xSign * dx;
                    y += ySign;
                }
                setPixelColor(writer, x, y, pixelDepth(startDepth, endDepth, i + 1, steps), minDepth, maxDepth, width, height);
            }
        } else {
            int err = -dy * ySign;
            int steps = dy * ySign;
            for (int i = 0; i < dy * ySign; i++) {
                y += ySign;
                err += 2 * dx * xSign;
                if (err > 0) {
                    err -= 2 * dy * ySign;
                    x += xSign;
                }
                setPixelColor(writer, x, y, pixelDepth(startDepth, endDepth, i + 1, steps), minDepth, maxDepth, width, height);
            }
        }
    }

    private double pixelDepth(double startDepth, double endDepth, int step, int steps) {
        if (steps <= 0) {
            return startDepth;
        }
        double t = (double) step / steps;
        return startDepth + (endDepth - startDepth) * t;
    }

    private void setPixelColor(
            PixelWriter writer,
            int x,
            int y,
            double depth,
            double minDepth,
            double maxDepth,
            double width,
            double height
    ) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        double normalizedDepth = normalizeDepth(depth, minDepth, maxDepth);
        writer.setColor(x, y, colorService.colorForDepth(normalizedDepth));
    }

    private double normalizeDepth(double depth, double minDepth, double maxDepth) {
        if (maxDepth == minDepth) {
            return 0.5;
        }
        double normalized = (depth - minDepth) / (maxDepth - minDepth);
        if (normalized < 0) {
            return 0;
        }
        if (normalized > 1) {
            return 1;
        }
        return normalized;
    }

    private void drawAxesOverlay(GraphicsContext gc, double width, double height, AppState state) {
        double originX = 55;
        double originY = height - 55;
        double length = 42;
        Matrix4 modelView = projectionService.buildModelViewMatrix(state.getViewParameters(), state.getCameraConfig());

        Point2DModel origin = axisPoint(new Point3DModel(0, 0, 0), modelView);
        Point2DModel xEnd = axisPoint(new Point3DModel(1, 0, 0), modelView);
        Point2DModel yEnd = axisPoint(new Point3DModel(0, 1, 0), modelView);
        Point2DModel zEnd = axisPoint(new Point3DModel(0, 0, 1), modelView);

        gc.setLineWidth(2);
        drawAxis(gc, originX, originY, length, origin, xEnd, Color.rgb(190, 45, 45), "X");
        drawAxis(gc, originX, originY, length, origin, yEnd, Color.rgb(45, 150, 75), "Y");
        drawAxis(gc, originX, originY, length, origin, zEnd, Color.rgb(45, 80, 190), "Z");
    }

    private Point2DModel axisPoint(Point3DModel point, Matrix4 matrix) {
        Vector4 vector = matrix.transform(new Vector4(point.x(), point.y(), point.z(), 1));
        double w = vector.get(Vector4.W_INDEX);
        if (w == 0) {
            return new Point2DModel(vector.get(Vector4.X_INDEX), vector.get(Vector4.Y_INDEX));
        }
        return new Point2DModel(vector.get(Vector4.X_INDEX) / w, vector.get(Vector4.Y_INDEX) / w);
    }

    private void drawAxis(
            GraphicsContext gc,
            double originX,
            double originY,
            double length,
            Point2DModel origin,
            Point2DModel end,
            Color color,
            String label
    ) {
        double dx = end.x() - origin.x();
        double dy = end.y() - origin.y();
        double endX = originX + dx * length;
        double endY = originY - dy * length;

        gc.setStroke(color);
        gc.strokeLine(originX, originY, endX, endY);
        gc.setFill(color);
        gc.fillText(label, endX + 6, endY + 4);
    }

    private void drawStatus(GraphicsContext gc, AppState state) {
        gc.setFill(Color.rgb(70, 70, 70));
        gc.fillText(
                "K=" + state.getSceneParameters().getK()
                        + "  N=" + state.getSceneParameters().getN()
                        + "  M=" + state.getSceneParameters().getM()
                        + "  M1=" + state.getSceneParameters().getM1()
                        + "  Zn=" + String.format("%.3f", state.getViewParameters().getZn()),
                12,
                20
        );

        if (state.getWireframeModel().isEmpty()) {
            gc.fillText("Geometry will appear after construction and projection methods are implemented.", 12, 42);
        }
    }
}
