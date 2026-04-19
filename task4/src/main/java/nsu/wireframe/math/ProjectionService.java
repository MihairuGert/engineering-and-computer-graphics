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
        Matrix4 resultMatrix = buildResultTransformMatrix(view, camera);
        return transformSegments(model.getSegments(), resultMatrix);
    }

    public Matrix4 buildRotationMatrix(ViewParameters view) {
        double angleX = Math.toRadians(view.getRotationX());
        double angleY = Math.toRadians(view.getRotationY());
        double angleZ = Math.toRadians(view.getRotationZ());

        Matrix4 rotationX = new Matrix4(new double[][]{
                {1, 0, 0, 0},
                {0, Math.cos(angleX), -Math.sin(angleX), 0},
                {0, Math.sin(angleX), Math.cos(angleX), 0},
                {0, 0, 0, 1}
        });

        Matrix4 rotationY = new Matrix4(new double[][]{
                {Math.cos(angleY), 0, Math.sin(angleY), 0},
                {0, 1, 0, 0},
                {-Math.sin(angleY), 0, Math.cos(angleY), 0},
                {0, 0, 0, 1}
        });

        Matrix4 rotationZ = new Matrix4(new double[][]{
                {Math.cos(angleZ), -Math.sin(angleZ), 0, 0},
                {Math.sin(angleZ), Math.cos(angleZ), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });

        return rotationZ.multiply(rotationY).multiply(rotationX);
    }

    public Matrix4 buildResultTransformMatrix(ViewParameters view, CameraConfig camera) {
        Matrix4 rotationMatrix = buildRotationMatrix(view);
        Matrix4 cameraMatrix = buildCameraMatrix(camera);

        return cameraMatrix.multiply(rotationMatrix);
    }

    private Matrix4 buildCameraMatrix(CameraConfig camera) {
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
        if (point.z() <= 0) {
            return Optional.empty();
        }

        return Optional.of(
                new Point2DModel(
                (view.getZn() * point.x()) / point.z(),
                (view.getZn() * point.y()) / point.z())
        );
    }

    public Point2DModel toScreenCoordinates(Point2DModel projectedPoint, double width, double height) {
        double aspect = width / height;
        double sh = 2.0;
        double sw = 2.0 * aspect;

        double xn = 2.0 * projectedPoint.x() / sw;
        double yn = 2.0 * projectedPoint.y() / sh;

        double screenX = (xn + 1.0) * 0.5 * width;
        double screenY = (1.0 - yn) * 0.5 * height;

        return new Point2DModel(screenX, screenY);
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
