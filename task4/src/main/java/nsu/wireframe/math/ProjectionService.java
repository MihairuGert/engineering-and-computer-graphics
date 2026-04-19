package nsu.wireframe.math;

import nsu.wireframe.model.CameraConfig;
import nsu.wireframe.model.Point2DModel;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.Segment2D;
import nsu.wireframe.model.Segment3D;
import nsu.wireframe.model.ViewParameters;
import nsu.wireframe.model.WireframeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectionService {
    public List<Segment3D> applyTransformPipeline(WireframeModel model, ViewParameters view, CameraConfig camera) {
        Matrix4 modelViewMatrix = buildModelViewMatrix(view, camera);
        return transformSegments(model.getSegments(), modelViewMatrix);
    }

    public Matrix4 buildModelMatrix(ViewParameters view) {
        Matrix4 rotationX = Matrix4.rotationX(Math.toRadians(view.getRotationX()));
        Matrix4 rotationY = Matrix4.rotationY(Math.toRadians(view.getRotationY()));
        Matrix4 rotationZ = Matrix4.rotationZ(Math.toRadians(view.getRotationZ()));

        return rotationZ.multiply(rotationY).multiply(rotationX);
    }

    public Matrix4 buildModelViewMatrix(ViewParameters view, CameraConfig camera) {
        Matrix4 modelMatrix = buildModelMatrix(view);
        Matrix4 cameraMatrix = buildCameraMatrix(camera);

        return cameraMatrix.multiply(modelMatrix);
    }

    public Matrix4 buildCameraMatrix(CameraConfig camera) {
        Point3DModel eye = camera.getPcam();
        Point3DModel target = camera.getPview();
        Point3DModel vup = camera.getVup();

        Point3DModel k = normalize(subtract(target, eye));

        Point3DModel i = normalize(cross(vup, k));
        Point3DModel j = cross(k, i);

        return new Matrix4(new double[][]{
                {i.x(), i.y(), i.z(), -dot(i, eye)},
                {j.x(), j.y(), j.z(), -dot(j, eye)},
                {k.x(), k.y(), k.z(), -dot(k, eye)},
                {0, 0, 0, 1}
        });
    }

    public Matrix4 buildProjectionMatrix(ViewParameters view) {
        return Matrix4.perspective(view.getZn());
    }

    public Matrix4 buildViewportMatrix(double width, double height) {
        return Matrix4.viewport(width, height);
    }

    private Point3DModel subtract(Point3DModel first, Point3DModel second) {
        return new Point3DModel(first.x() - second.x(), first.y() - second.y(), first.z() - second.z());
    }

    private Point3DModel normalize(Point3DModel vector) {
        double length = Math.sqrt(dot(vector, vector));
        if (length == 0) {
            return new Point3DModel(0, 0, 0);
        }
        return new Point3DModel(vector.x() / length, vector.y() / length, vector.z() / length);
    }

    private Point3DModel cross(Point3DModel first, Point3DModel second) {
        return new Point3DModel(
                first.y() * second.z() - first.z() * second.y(),
                first.z() * second.x() - first.x() * second.z(),
                first.x() * second.y() - first.y() * second.x()
        );
    }

    private double dot(Point3DModel first, Point3DModel second) {
        return first.x() * second.x() + first.y() * second.y() + first.z() * second.z();
    }

    public List<Segment3D> transformSegments(List<Segment3D> segments, Matrix4 matrix) {
        List<Segment3D> transformedSegments = new ArrayList<>();
        for (Segment3D segment : segments) {
            transformedSegments.add(new Segment3D(
                    transformPoint(segment.start(), matrix),
                    transformPoint(segment.end(), matrix)
            ));
        }
        return transformedSegments;
    }

    private Point3DModel transformPoint(Point3DModel point, Matrix4 matrix) {
        Vector4 transformed = matrix.transform(new Vector4(point.x(), point.y(), point.z(), 1));
        return new Point3DModel(
                transformed.get(Vector4.X_INDEX),
                transformed.get(Vector4.Y_INDEX),
                transformed.get(Vector4.Z_INDEX)
        );
    }

    public Optional<Segment2D> projectSegment(Segment3D segment, double width, double height, ViewParameters view, CameraConfig camera) {
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }

        Optional<Point2DModel> start = perspectiveProject(segment.start(), view, camera)
                .map(point -> toScreenCoordinates(point, width, height));
        Optional<Point2DModel> end = perspectiveProject(segment.end(), view, camera)
                .map(point -> toScreenCoordinates(point, width, height));

        if (start.isEmpty() || end.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Segment2D(start.get(), end.get(), calculateSegmentDepth(segment)));
    }

    public Optional<Point2DModel> perspectiveProject(Point3DModel point, ViewParameters view, CameraConfig camera) {
        if (!point.isFinite() || point.z() <= 0) {
            return Optional.empty();
        }

        Vector4 clip = buildProjectionMatrix(view).transform(new Vector4(point.x(), point.y(), point.z(), 1));
        Optional<Vector4> projected = divideByW(clip);
        if (projected.isEmpty()) {
            return Optional.empty();
        }

        Vector4 vector = projected.get();
        return Optional.of(new Point2DModel(vector.get(Vector4.X_INDEX), vector.get(Vector4.Y_INDEX)));
    }

    public Point2DModel toScreenCoordinates(Point2DModel projectedPoint, double width, double height) {
        Vector4 screen = buildViewportMatrix(width, height)
                .transform(new Vector4(projectedPoint.x(), projectedPoint.y(), 0, 1));
        double w = screen.get(Vector4.W_INDEX);
        if (w == 0) {
            return new Point2DModel(screen.get(Vector4.X_INDEX), screen.get(Vector4.Y_INDEX));
        }
        return new Point2DModel(screen.get(Vector4.X_INDEX) / w, screen.get(Vector4.Y_INDEX) / w);
    }

    private Optional<Vector4> divideByW(Vector4 vector) {
        double w = vector.get(Vector4.W_INDEX);
        if (!Double.isFinite(w) || w == 0) {
            return Optional.empty();
        }

        double x = vector.get(Vector4.X_INDEX) / w;
        double y = vector.get(Vector4.Y_INDEX) / w;
        double z = vector.get(Vector4.Z_INDEX) / w;
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return Optional.empty();
        }

        return Optional.of(new Vector4(x, y, z, 1));
    }

    public double calculatePointDepth(Point3DModel point) {
        return point.z();
    }

    public double calculateSegmentDepth(Segment3D segment) {
        double startDepth = calculatePointDepth(segment.start());
        double endDepth = calculatePointDepth(segment.end());

        return (startDepth + endDepth) / 2;
    }
}
