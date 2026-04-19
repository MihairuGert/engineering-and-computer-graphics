package nsu.wireframe.math;

import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.Point2DModel;
import nsu.wireframe.model.Point3DModel;
import nsu.wireframe.model.SceneParameters;
import nsu.wireframe.model.Segment3D;
import nsu.wireframe.model.WireframeModel;

import java.util.ArrayList;
import java.util.List;

public class GeometryService {
    private final SplineService splineService;

    public GeometryService(SplineService splineService) {
        this.splineService = splineService;
    }

    public WireframeModel buildWireframe(List<ControlPoint> controlPoints, SceneParameters parameters) {
        List<Point2DModel> splinePoints = splineService.calculateSplinePoints(controlPoints, parameters.getN());
        List<List<Point3DModel>> generatrices = buildGeneratrices(splinePoints, parameters.getM());

        List<Segment3D> segments = new ArrayList<>();
        segments.addAll(buildCircleSegments(generatrices, parameters.getN(), parameters.getM1()));

        return normalizeToUnitBox(new WireframeModel(segments));
    }

    public List<List<Point3DModel>> buildGeneratrices(List<Point2DModel> splinePoints, int m) {
        var res = new ArrayList<List<Point3DModel>>();

        for (int i = 0; i < m; i++) {
            var points = new ArrayList<Point3DModel>();
            for (Point2DModel splinePoint : splinePoints) {
                double x = splinePoint.y() * Math.cos(((double) i * 2 * Math.PI) / m);
                double y = splinePoint.y() * Math.sin(((double) i * 2 * Math.PI) / m);
                double z = splinePoint.x();
                points.add(new Point3DModel(x, y, z));
            }
            res.add(points);
        }

        return res;
    }

    public List<Segment3D> buildCircleSegments(List<List<Point3DModel>> generatrices, int n, int m1) {
        var res = new ArrayList<Segment3D>();

        if (generatrices.isEmpty()) {
            return res;
        }

        for (List<Point3DModel> generatrix : generatrices) {
            for (int pointIndex = 0; pointIndex < generatrix.size() - 1; pointIndex++) {
                res.add(new Segment3D(generatrix.get(pointIndex), generatrix.get(pointIndex + 1)));
            }
        }

        double angleStepBetweenGeneratrices = 2 * Math.PI / generatrices.size();
        for (int i = 0; i < generatrices.size(); i++) {
            List<Point3DModel> current = generatrices.get(i);
            double startAngle = i * angleStepBetweenGeneratrices;
            double segmentAngleStep = angleStepBetweenGeneratrices / m1;

            for (int j = 0; j < current.size(); j += n) {
                double radius = Math.hypot(current.get(j).x(), current.get(j).y());
                double z = current.get(j).z();
                Point3DModel start = current.get(j);

                for (int segmentIndex = 1; segmentIndex <= m1; segmentIndex++) {
                    double angle = startAngle + segmentAngleStep * segmentIndex;
                    Point3DModel end = new Point3DModel(
                            radius * Math.cos(angle),
                            radius * Math.sin(angle),
                            z
                    );
                    res.add(new Segment3D(start, end));
                    start = end;
                }
            }
        }

        return res;
    }

    public WireframeModel normalizeToUnitBox(WireframeModel model) {
        double xMin = Double.POSITIVE_INFINITY, xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;
        double zMin = Double.POSITIVE_INFINITY, zMax = Double.NEGATIVE_INFINITY;

        for (Segment3D s : model.getSegments()) {
            xMin = Math.min(Math.min(s.start().x(), xMin), s.end().x());
            xMax = Math.max(Math.max(s.start().x(), xMax), s.end().x());

            yMin = Math.min(Math.min(s.start().y(), yMin), s.end().y());
            yMax = Math.max(Math.max(s.start().y(), yMax), s.end().y());

            zMin = Math.min(Math.min(s.start().z(), zMin), s.end().z());
            zMax = Math.max(Math.max(s.start().z(), zMax), s.end().z());
        }

        var cx = (xMin + xMax) / 2;
        var cy = (yMin + yMax) / 2;
        var cz = (zMin + zMax) / 2;

        var d = Math.max(Math.max(xMax - xMin, yMax - yMin), zMax - zMin);
        var k = 2 / d;

        var newSegments3D = new ArrayList<Segment3D>();

        for (Segment3D s : model.getSegments()) {
            var xS = k * (s.start().x() - cx);
            var yS = k * (s.start().y() - cy);
            var zS = k * (s.start().z() - cz);

            var xE = k * (s.end().x() - cx);
            var yE = k * (s.end().y() - cy);
            var zE = k * (s.end().z() - cz);

            newSegments3D.add(new Segment3D(
                    new Point3DModel(xS, yS, zS),
                    new Point3DModel(xE, yE, zE)));
        }

        return new WireframeModel(newSegments3D);
    }
}
