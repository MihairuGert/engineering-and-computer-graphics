package nsu.task5.model;

import nsu.task5.math.VectorMath;
import nsu.wireframe.model.CameraConfig;
import nsu.wireframe.model.Point3DModel;

public class RayCamera {
    private static final double MIN_ZN = 0.01;
    private static final double MIN_ZF = 0.02;

    private Point3DModel eye;
    private Point3DModel view;
    private Point3DModel up;
    private double zn;
    private double zf;
    private double sw;
    private double sh;

    public RayCamera(
            Point3DModel eye,
            Point3DModel view,
            Point3DModel up,
            double zn,
            double zf,
            double sw,
            double sh
    ) {
        this.eye = eye;
        this.view = view;
        this.up = up;
        this.zn = Math.max(MIN_ZN, zn);
        this.zf = Math.max(Math.max(MIN_ZF, this.zn + MIN_ZN), zf);
        this.sw = Math.max(MIN_ZN, sw);
        this.sh = Math.max(MIN_ZN, sh);
        correctUp();
    }

    public static RayCamera defaultCamera() {
        return new RayCamera(
                new Point3DModel(-5, 0, 0),
                new Point3DModel(0, 0, 0),
                new Point3DModel(0, 0, 1),
                1,
                20,
                1.6,
                1.2
        );
    }

    public RayCamera copy() {
        return new RayCamera(eye, view, up, zn, zf, sw, sh);
    }

    public static RayCamera initForScene(SceneModel scene, double width, double height) {
        BoundingBox box = scene.boundingBox()
                .orElse(BoundingBox.of(new Point3DModel(-1, -1, -1), new Point3DModel(1, 1, 1)))
                .expand(1.05);

        Point3DModel center = box.center();
        double radius = Math.max(0.5, box.maxSize() / 2.0);
        double distance = Math.max(4.0, radius * 4.0);
        double zn = Math.max(0.1, distance - radius * 2.0);
        double zf = distance + radius * 4.0;
        double aspect = safeAspect(width, height);
        double sh = Math.max(0.5, radius * 2.4 * zn / Math.max(zn, distance - radius));
        double sw = sh * aspect;

        return new RayCamera(
                new Point3DModel(center.x() - distance, center.y(), center.z()),
                center,
                new Point3DModel(0, 0, 1),
                zn,
                zf,
                sw,
                sh
        );
    }

    public CameraBasis basis() {
        Point3DModel forward = VectorMath.normalize(VectorMath.subtract(view, eye));
        if (VectorMath.isZero(forward)) {
            forward = new Point3DModel(1, 0, 0);
        }

        Point3DModel right = VectorMath.normalize(VectorMath.cross(up, forward));
        if (VectorMath.isZero(right)) {
            right = VectorMath.normalize(VectorMath.cross(new Point3DModel(0, 1, 0), forward));
        }
        if (VectorMath.isZero(right)) {
            right = new Point3DModel(0, 1, 0);
        }

        Point3DModel correctedUp = VectorMath.normalize(VectorMath.cross(forward, right));
        if (VectorMath.isZero(correctedUp)) {
            correctedUp = new Point3DModel(0, 0, 1);
        }
        return new CameraBasis(forward, right, correctedUp);
    }

    public CameraConfig toCameraConfig() {
        CameraBasis basis = basis();
        return new CameraConfig(eye, view, basis.up());
    }

    public void correctUp() {
        up = basis().up();
    }

    public void rotateAroundView(double yawRadians, double pitchRadians) {
        CameraBasis basis = basis();
        Point3DModel viewToEye = VectorMath.subtract(eye, view);
        Point3DModel rotated = VectorMath.rotateAroundAxis(viewToEye, basis.up(), yawRadians);
        rotated = VectorMath.rotateAroundAxis(rotated, basis.right(), pitchRadians);
        up = VectorMath.rotateAroundAxis(up, basis.right(), pitchRadians);
        eye = VectorMath.add(view, rotated);
        correctUp();
    }

    public void moveZn(double factor) {
        zn = Math.clamp(zn * factor, MIN_ZN, Math.max(MIN_ZN, zf - MIN_ZN));
    }

    public void moveEyeAlongViewDirection(double distance) {
        Point3DModel direction = VectorMath.normalize(VectorMath.subtract(view, eye));
        eye = VectorMath.add(eye, VectorMath.multiply(direction, distance));
        if (VectorMath.distance(eye, view) < MIN_ZN) {
            eye = VectorMath.subtract(view, VectorMath.multiply(direction, MIN_ZN));
        }
        correctUp();
    }

    public void pan(double rightDistance, double upDistance) {
        CameraBasis basis = basis();
        Point3DModel delta = VectorMath.add(
                VectorMath.multiply(basis.right(), rightDistance),
                VectorMath.multiply(basis.up(), upDistance)
        );
        eye = VectorMath.add(eye, delta);
        view = VectorMath.add(view, delta);
    }

    public void adjustScreenSize(double width, double height) {
        double aspect = safeAspect(width, height);
        double baseSize = Math.max(MIN_ZN, Math.min(sw, sh));
        if (aspect >= 1.0) {
            sh = baseSize;
            sw = baseSize * aspect;
        } else {
            sw = baseSize;
            sh = baseSize / aspect;
        }
    }

    private static double safeAspect(double width, double height) {
        if (width <= 0 || height <= 0) {
            return 4.0 / 3.0;
        }
        return width / height;
    }

    public Point3DModel eye() {
        return eye;
    }

    public Point3DModel view() {
        return view;
    }

    public Point3DModel up() {
        return up;
    }

    public double zn() {
        return zn;
    }

    public double zf() {
        return zf;
    }

    public double sw() {
        return sw;
    }

    public double sh() {
        return sh;
    }
}
