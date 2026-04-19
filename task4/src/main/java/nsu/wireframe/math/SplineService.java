package nsu.wireframe.math;

import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.Point2DModel;

import java.util.ArrayList;
import java.util.List;

public class SplineService {
    public List<Point2DModel> calculateSplinePoints(List<ControlPoint> controlPoints, int n) {
        var res = new ArrayList<Point2DModel>();
        var Ms = Matrix4.spline();

        var points = new Vector4[n+1];
        for (int i = 0; i < n + 1; i++) {
            double point = (double)i / n;
            points[i] = Ms.vector4OnMatrix(new Vector4(Math.pow(point, 3), Math.pow(point, 2), point, 1));
        }

        int j = 0;
        for (int i = 3; i < controlPoints.size(); i++) {
            for (; j < points.length; j++) {
                double u = 0;
                double v = 0;
                for (int k = 0; k < 4; k++) {
                    u += points[j].get(k) * controlPoints.get(i - 3 + k).u();
                    v += points[j].get(k) * controlPoints.get(i - 3 + k).v();
                }
                res.add(new Point2DModel(u, v));
            }
            j = 1;
        }

        return res;
    }
}
