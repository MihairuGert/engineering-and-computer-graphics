package nsu.task5.io;

import nsu.task5.model.RayCamera;
import nsu.task5.model.RgbColor;
import nsu.task5.render.RenderSettings;
import nsu.wireframe.model.Point3DModel;

import java.util.ArrayList;
import java.util.List;

public class RenderFileParser {
    private static final int EXPECTED_LINE_COUNT = 9;

    public RenderFileData parse(List<String> rawLines) throws RayFileException {
        List<ParsedLine> lines = preprocess(rawLines);
        if (lines.size() != EXPECTED_LINE_COUNT) {
            throw new RayFileException("Render file must contain " + EXPECTED_LINE_COUNT
                    + " non-empty data lines after comments are removed.");
        }

        List<String> warnings = new ArrayList<>();
        RgbColor background = parseRgb255(lines.get(0), "background RGB");
        double gamma = parseDouble(lines.get(1), "gamma");
        if (gamma <= 0) {
            throw new RayFileException("Line " + lines.get(1).lineNumber() + ": gamma must be positive.");
        }

        int rawDepth = parseInt(lines.get(2), "depth");
        int depth = RenderSettings.clampDepth(rawDepth);
        if (rawDepth != depth) {
            warnings.add("Depth " + rawDepth + " is outside 1..10 and was clamped to " + depth + ".");
        }

        String quality = lines.get(3).text().trim();
        if (!RenderSettings.NORMAL_QUALITY.equalsIgnoreCase(quality)) {
            warnings.add("Quality \"" + quality + "\" is read from file but replaced with normal.");
        }

        Point3DModel eye = parsePoint(lines.get(4), "eye");
        Point3DModel view = parsePoint(lines.get(5), "view");
        Point3DModel up = parsePoint(lines.get(6), "up");
        double[] z = parseDoubles(lines.get(7), 2, "ZN ZF");
        double[] screen = parseDoubles(lines.get(8), 2, "SW SH");

        RenderSettings settings = new RenderSettings(background, gamma, depth, RenderSettings.NORMAL_QUALITY);
        RayCamera camera = new RayCamera(eye, view, up, z[0], z[1], screen[0], screen[1]);
        return new RenderFileData(settings, camera, warnings);
    }

    private RgbColor parseRgb255(ParsedLine line, String label) throws RayFileException {
        double[] values = parseDoubles(line, 3, label);
        for (double value : values) {
            if (value < 0 || value > 255) {
                throw new RayFileException("Line " + line.lineNumber() + ": " + label
                        + " values must be in the 0..255 range.");
            }
        }
        return RgbColor.from255(values[0], values[1], values[2]);
    }

    private Point3DModel parsePoint(ParsedLine line, String label) throws RayFileException {
        double[] values = parseDoubles(line, 3, label);
        return new Point3DModel(values[0], values[1], values[2]);
    }

    private int parseInt(ParsedLine line, String label) throws RayFileException {
        String[] tokens = split(line);
        if (tokens.length != 1) {
            throw new RayFileException("Line " + line.lineNumber() + ": expected one integer for " + label + ".");
        }
        try {
            return Integer.parseInt(tokens[0]);
        } catch (NumberFormatException e) {
            throw new RayFileException("Line " + line.lineNumber() + ": " + label + " must be an integer.", e);
        }
    }

    private double parseDouble(ParsedLine line, String label) throws RayFileException {
        return parseDoubles(line, 1, label)[0];
    }

    private double[] parseDoubles(ParsedLine line, int expectedCount, String label) throws RayFileException {
        String[] tokens = split(line);
        if (tokens.length != expectedCount) {
            throw new RayFileException("Line " + line.lineNumber() + ": expected "
                    + expectedCount + " numbers for " + label + ".");
        }

        double[] values = new double[expectedCount];
        for (int i = 0; i < expectedCount; i++) {
            values[i] = parseFiniteDouble(tokens[i], line.lineNumber(), label);
        }
        return values;
    }

    private double parseFiniteDouble(String value, int lineNumber, String label) throws RayFileException {
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new RayFileException("Line " + lineNumber + ": " + label + " contains a non-finite number.", e);
        }
    }

    private String[] split(ParsedLine line) {
        return line.text().trim().split("\\s+");
    }

    private List<ParsedLine> preprocess(List<String> rawLines) {
        List<ParsedLine> lines = new ArrayList<>();
        for (int i = 0; i < rawLines.size(); i++) {
            String line = stripComment(rawLines.get(i)).trim();
            if (!line.isEmpty()) {
                lines.add(new ParsedLine(i + 1, line));
            }
        }
        return lines;
    }

    private String stripComment(String line) {
        int index = line.indexOf("//");
        if (index < 0) {
            return line;
        }
        return line.substring(0, index);
    }

    private record ParsedLine(int lineNumber, String text) {
    }
}
