package nsu.wireframe.model;

public class CameraConfig {
    private final Point3DModel pcam;
    private final Point3DModel pview;
    private final Point3DModel vup;

    public CameraConfig(Point3DModel pcam, Point3DModel pview, Point3DModel vup) {
        this.pcam = pcam;
        this.pview = pview;
        this.vup = vup;
    }

    public static CameraConfig fixed() {
        return new CameraConfig(
                new Point3DModel(-10, 0, 0),
                new Point3DModel(10, 0, 0),
                new Point3DModel(0, 1, 0)
        );
    }

    public Point3DModel getPcam() {
        return pcam;
    }

    public Point3DModel getPview() {
        return pview;
    }

    public Point3DModel getVup() {
        return vup;
    }
}
