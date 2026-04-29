package nsu.wireframe.io;

import nsu.wireframe.model.AppState;
import nsu.wireframe.model.CameraConfig;
import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.SceneParameters;
import nsu.wireframe.model.ViewParameters;
import nsu.wireframe.model.WireframeModel;
import nsu.wireframe.validation.ValidationException;
import nsu.wireframe.validation.Validators;

import java.util.ArrayList;
import java.util.List;

public class SceneFileParser {
    public static final String HEADER = "ICGWIREFRAME_SCENE_V1";
    private static final String POINTS_BEGIN = "POINTS_BEGIN";
    private static final String POINTS_END = "POINTS_END";

    public AppState parse(List<String> lines) throws SceneFileException {
        if (lines.isEmpty()) {
            throw new SceneFileException("Scene file is empty.");
        }

        int index = 0;
        expectExact(lines, index++, HEADER);

        int k = parseIntKey(lines, index++, "K");
        int n = parseIntKey(lines, index++, "N");
        int m = parseIntKey(lines, index++, "M");
        int m1 = parseIntKey(lines, index++, "M1");
        double zn = parseDoubleKey(lines, index++, "ZN");
        double rotationX = parseDoubleKey(lines, index++, "ROT_X");
        double rotationY = parseDoubleKey(lines, index++, "ROT_Y");
        double rotationZ = parseDoubleKey(lines, index++, "ROT_Z");

        expectExact(lines, index++, POINTS_BEGIN);

        List<ControlPoint> points = new ArrayList<>();
        while (index < lines.size() && !lines.get(index).trim().equals(POINTS_END)) {
            points.add(parsePoint(lines.get(index), index + 1));
            index++;
        }

        if (index >= lines.size()) {
            throw new SceneFileException("Missing " + POINTS_END + " line.");
        }
        index++;

        if (index != lines.size()) {
            throw new SceneFileException("No extra lines are allowed after " + POINTS_END + ".");
        }

        SceneParameters sceneParameters = new SceneParameters(k, n, m, m1);
        ViewParameters viewParameters = new ViewParameters(rotationX, rotationY, rotationZ, zn);

        try {
            Validators.validateSceneParameters(sceneParameters);
            Validators.validateControlPoints(points, k);
            Validators.validateViewParameters(viewParameters);
        } catch (ValidationException e) {
            throw new SceneFileException(e.getMessage(), e);
        }

        return new AppState(sceneParameters, viewParameters, CameraConfig.fixed(), points, WireframeModel.empty());
    }

    private void expectExact(List<String> lines, int index, String expected) throws SceneFileException {
        if (index >= lines.size()) {
            throw new SceneFileException("Expected line " + expected + ", but the file ended.");
        }
        String actual = lines.get(index).trim();
        if (!expected.equals(actual)) {
            throw new SceneFileException("Line " + (index + 1) + ": expected \"" + expected
                    + "\", found \"" + actual + "\".");
        }
    }

    private int parseIntKey(List<String> lines, int index, String key) throws SceneFileException {
        String value = parseKeyValue(lines, index, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SceneFileException("Line " + (index + 1) + ": " + key
                    + " must be an integer.");
        }
    }

    private double parseDoubleKey(List<String> lines, int index, String key) throws SceneFileException {
        String value = parseKeyValue(lines, index, key);
        try {
            double result = Double.parseDouble(value);
            if (!Double.isFinite(result)) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException e) {
            throw new SceneFileException("Line " + (index + 1) + ": " + key
                    + " must be a finite number.");
        }
    }

    private String parseKeyValue(List<String> lines, int index, String key) throws SceneFileException {
        if (index >= lines.size()) {
            throw new SceneFileException("Expected line " + key + "=..., but the file ended.");
        }
        String line = lines.get(index).trim();
        String prefix = key + "=";
        if (!line.startsWith(prefix)) {
            throw new SceneFileException("Line " + (index + 1) + ": expected \"" + prefix + "...\".");
        }
        String value = line.substring(prefix.length()).trim();
        if (value.isEmpty()) {
            throw new SceneFileException("Line " + (index + 1) + ": value for " + key + " is missing.");
        }
        return value;
    }

    private ControlPoint parsePoint(String line, int lineNumber) throws SceneFileException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            throw new SceneFileException("Line " + lineNumber
                    + ": a point must contain two numbers: u v.");
        }
        try {
            double u = Double.parseDouble(parts[0]);
            double v = Double.parseDouble(parts[1]);
            if (!Double.isFinite(u) || !Double.isFinite(v)) {
                throw new NumberFormatException();
            }
            return new ControlPoint(u, v);
        } catch (NumberFormatException e) {
            throw new SceneFileException("Line " + lineNumber
                    + ": point coordinates must be finite numbers.");
        }
    }
}
