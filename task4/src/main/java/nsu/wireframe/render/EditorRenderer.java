package nsu.wireframe.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nsu.wireframe.math.SplineService;
import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.Point2DModel;
import nsu.wireframe.model.SceneParameters;

import java.util.List;

public class EditorRenderer {
    private final SplineService splineService;

    public EditorRenderer(SplineService splineService) {
        this.splineService = splineService;
    }

    public void render(
            GraphicsContext gc,
            double width,
            double height,
            List<ControlPoint> controlPoints,
            SceneParameters parameters,
            EditorViewState viewState,
            int selectedPointIndex
    ) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        drawAxes(gc, width, height, viewState);
        drawControlPolyline(gc, width, height, controlPoints, viewState);
        drawSplinePreview(gc, width, height, controlPoints, parameters, viewState);
        drawControlPoints(gc, width, height, controlPoints, viewState, selectedPointIndex);
    }

    private void drawAxes(GraphicsContext gc, double width, double height, EditorViewState viewState) {
        Point2DModel origin = viewState.modelToScreen(new Point2DModel(0, 0), width, height);
        gc.setStroke(Color.rgb(210, 210, 210));
        gc.setLineWidth(1);
        gc.strokeLine(0, origin.y(), width, origin.y());
        gc.strokeLine(origin.x(), 0, origin.x(), height);

        gc.setFill(Color.rgb(120, 120, 120));
        gc.fillText("u", width - 20, origin.y() - 6);
        gc.fillText("v", origin.x() + 6, 16);
    }

    private void drawControlPolyline(GraphicsContext gc, double width, double height, List<ControlPoint> points, EditorViewState viewState) {
        if (points.size() < 2) {
            return;
        }

        gc.setStroke(Color.rgb(120, 120, 120));
        gc.setLineWidth(1);
        for (int i = 0; i < points.size() - 1; i++) {
            Point2DModel start = viewState.modelToScreen(points.get(i).toPoint2D(), width, height);
            Point2DModel end = viewState.modelToScreen(points.get(i + 1).toPoint2D(), width, height);
            gc.strokeLine(start.x(), start.y(), end.x(), end.y());
        }
    }

    private void drawSplinePreview(
            GraphicsContext gc,
            double width,
            double height,
            List<ControlPoint> controlPoints,
            SceneParameters parameters,
            EditorViewState viewState
    ) {
        List<Point2DModel> splinePoints = splineService.calculateSplinePoints(controlPoints, parameters.getN());
        if (splinePoints.size() < 2) {
            return;
        }

        gc.setStroke(Color.rgb(20, 130, 95));
        gc.setLineWidth(2);
        for (int i = 0; i < splinePoints.size() - 1; i++) {
            Point2DModel start = viewState.modelToScreen(splinePoints.get(i), width, height);
            Point2DModel end = viewState.modelToScreen(splinePoints.get(i + 1), width, height);
            gc.strokeLine(start.x(), start.y(), end.x(), end.y());
        }
    }

    private void drawControlPoints(
            GraphicsContext gc,
            double width,
            double height,
            List<ControlPoint> points,
            EditorViewState viewState,
            int selectedPointIndex
    ) {
        for (int i = 0; i < points.size(); i++) {
            Point2DModel point = viewState.modelToScreen(points.get(i).toPoint2D(), width, height);
            boolean selected = i == selectedPointIndex;

            gc.setFill(selected ? Color.rgb(255, 220, 70) : Color.rgb(210, 60, 55));
            gc.fillOval(point.x() - 5, point.y() - 5, 10, 10);
            gc.setStroke(Color.rgb(40, 40, 40));
            gc.strokeOval(point.x() - 5, point.y() - 5, 10, 10);
        }
    }
}
