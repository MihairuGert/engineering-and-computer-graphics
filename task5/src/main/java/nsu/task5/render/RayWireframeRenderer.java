package nsu.task5.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nsu.task5.model.LightSource;
import nsu.task5.model.Primitive;
import nsu.task5.model.RayCamera;
import nsu.task5.model.SceneModel;
import nsu.wireframe.math.Matrix4;
import nsu.wireframe.math.ProjectionService;
import nsu.wireframe.math.Vector4;
import nsu.wireframe.model.Point2DModel;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment3D;

import java.util.Optional;

public class RayWireframeRenderer {
    private final ProjectionService projectionService = new ProjectionService();

    public void render(GraphicsContext gc, double width, double height, SceneModel scene, RayCamera camera) {
        clear(gc, width, height);
        if (width <= 0 || height <= 0) {
            return;
        }

        Matrix4 cameraMatrix = projectionService.buildCameraMatrix(camera.toCameraConfig());
        gc.setLineWidth(1.0);
        gc.setStroke(Color.rgb(30, 80, 160));

        for (Primitive primitive : scene.primitives()) {
            for (Segment3D segment : primitive.toWireframeMesh().segments()) {
                drawSegment(gc, segment, cameraMatrix, camera, width, height);
            }
        }

        drawLights(gc, scene, cameraMatrix, camera, width, height);
        drawStatus(gc, scene, camera);
    }

    private void clear(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
    }

    private void drawSegment(
            GraphicsContext gc,
            Segment3D segment,
            Matrix4 cameraMatrix,
            RayCamera camera,
            double width,
            double height
    ) {
        Optional<Point2DModel> start = project(segment.start(), cameraMatrix, camera, width, height);
        Optional<Point2DModel> end = project(segment.end(), cameraMatrix, camera, width, height);
        if (start.isEmpty() || end.isEmpty()) {
            return;
        }

        gc.strokeLine(start.get().x(), start.get().y(), end.get().x(), end.get().y());
    }

    private void drawLights(
            GraphicsContext gc,
            SceneModel scene,
            Matrix4 cameraMatrix,
            RayCamera camera,
            double width,
            double height
    ) {
        gc.setStroke(Color.rgb(220, 120, 20));
        gc.setFill(Color.rgb(220, 120, 20));
        for (LightSource light : scene.lights()) {
            Optional<Point2DModel> point = project(light.position(), cameraMatrix, camera, width, height);
            if (point.isEmpty()) {
                continue;
            }
            double x = point.get().x();
            double y = point.get().y();
            double r = 4;
            gc.strokeLine(x - r, y, x + r, y);
            gc.strokeLine(x, y - r, x, y + r);
            gc.fillOval(x - 2, y - 2, 4, 4);
        }
    }

    private Optional<Point2DModel> project(
            Point3DModel point,
            Matrix4 cameraMatrix,
            RayCamera camera,
            double width,
            double height
    ) {
        Vector4 transformed = cameraMatrix.transform(new Vector4(point.x(), point.y(), point.z(), 1));
        double z = transformed.get(Vector4.Z_INDEX);
        if (!Double.isFinite(z) || z <= 0) {
            return Optional.empty();
        }

        double xOnScreen = transformed.get(Vector4.X_INDEX) * camera.zn() / z;
        double yOnScreen = transformed.get(Vector4.Y_INDEX) * camera.zn() / z;
        double x = width / 2.0 + xOnScreen * width / camera.sw();
        double y = height / 2.0 - yOnScreen * height / camera.sh();
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            return Optional.empty();
        }
        return Optional.of(new Point2DModel(x, y));
    }

    private void drawStatus(GraphicsContext gc, SceneModel scene, RayCamera camera) {
        gc.setFill(Color.rgb(70, 70, 70));
        gc.fillText(
                "objects=" + scene.primitives().size()
                        + "  lights=" + scene.lights().size()
                        + "  zn=" + String.format("%.3f", camera.zn()),
                12,
                20
        );
    }
}
