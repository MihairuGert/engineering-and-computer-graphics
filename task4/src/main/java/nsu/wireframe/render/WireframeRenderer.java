package nsu.wireframe.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nsu.wireframe.math.ColorService;
import nsu.wireframe.math.ProjectionService;
import nsu.wireframe.model.AppState;
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
        drawAxesOverlay(gc, width, height);
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

        double minDepth = projectedSegments.getFirst().depth();
        double maxDepth = projectedSegments.getFirst().depth();
        for (Segment2D segment : projectedSegments) {
            minDepth = Math.min(minDepth, segment.depth());
            maxDepth = Math.max(maxDepth, segment.depth());
        }

        gc.setLineWidth(1);
        for (Segment2D screenSegment : projectedSegments) {
            double normalizedDepth = normalizeDepth(screenSegment.depth(), minDepth, maxDepth);
            gc.setStroke(colorService.colorForDepth(normalizedDepth));
            gc.strokeLine(
                    screenSegment.start().x(),
                    screenSegment.start().y(),
                    screenSegment.end().x(),
                    screenSegment.end().y()
            );
        }
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

    private void drawAxesOverlay(GraphicsContext gc, double width, double height) {
        double originX = 55;
        double originY = height - 55;
        double length = 38;

        gc.setLineWidth(2);
        gc.setStroke(Color.rgb(190, 45, 45));
        gc.strokeLine(originX, originY, originX + length, originY);
        gc.setFill(Color.rgb(190, 45, 45));
        gc.fillText("X", originX + length + 6, originY + 4);

        gc.setStroke(Color.rgb(45, 150, 75));
        gc.strokeLine(originX, originY, originX, originY - length);
        gc.setFill(Color.rgb(45, 150, 75));
        gc.fillText("Y", originX - 4, originY - length - 6);

        gc.setStroke(Color.rgb(45, 80, 190));
        gc.strokeLine(originX, originY, originX + length * 0.65, originY + length * 0.45);
        gc.setFill(Color.rgb(45, 80, 190));
        gc.fillText("Z", originX + length * 0.65 + 6, originY + length * 0.45 + 4);
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
            gc.fillText("Geometry will appear after the TODO construction and projection methods are implemented.", 12, 42);
        }
    }
}
