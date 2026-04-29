package nsu.wireframe.validation;

import nsu.wireframe.model.AppState;
import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.SceneParameters;
import nsu.wireframe.model.ViewParameters;

import java.util.List;

public final class Validators {
    private Validators() {
    }

    public static void validateState(AppState state) throws ValidationException {
        validateSceneParameters(state.getSceneParameters());
        validateControlPoints(state.getControlPoints(), state.getSceneParameters().getK());
        validateViewParameters(state.getViewParameters());
    }

    public static void validateSceneParameters(SceneParameters parameters) throws ValidationException {
        validateMin(parameters.getK(), "K", SceneParameters.MIN_K);
        validateMin(parameters.getN(), "N", SceneParameters.MIN_N);
        validateMin(parameters.getM(), "M", SceneParameters.MIN_M);
        validateMin(parameters.getM1(), "M1", SceneParameters.MIN_M1);
    }

    public static void validateControlPoints(List<ControlPoint> points, int expectedK) throws ValidationException {
        if (points.size() != expectedK) {
            throw new ValidationException("The number of points must match K: expected "
                    + expectedK + ", found " + points.size() + ".");
        }
        validateMin(points.size(), "K", SceneParameters.MIN_K);
        for (int i = 0; i < points.size(); i++) {
            ControlPoint point = points.get(i);
            if (point == null || !point.isFinite()) {
                throw new ValidationException("Control point #" + (i + 1)
                        + " must contain two finite numbers.");
            }
        }
    }

    public static void validateViewParameters(ViewParameters parameters) throws ValidationException {
        validateFinite(parameters.getRotationX(), "ROT_X");
        validateFinite(parameters.getRotationY(), "ROT_Y");
        validateFinite(parameters.getRotationZ(), "ROT_Z");
        validateFinite(parameters.getZn(), "ZN");
        if (parameters.getZn() <= 0) {
            throw new ValidationException("ZN must be a positive number.");
        }
    }

    public static int parseIntAtLeast(String text, String name, int minValue) throws ValidationException {
        try {
            int value = Integer.parseInt(text.trim());
            validateMin(value, name, minValue);
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be an integer greater than or equal to " + minValue + ".");
        }
    }

    private static void validateMin(int value, String name, int minValue) throws ValidationException {
        if (value < minValue) {
            throw new ValidationException(name + " must be at least " + minValue + ".");
        }
    }

    private static void validateFinite(double value, String name) throws ValidationException {
        if (!Double.isFinite(value)) {
            throw new ValidationException(name + " must be a finite number.");
        }
    }
}
