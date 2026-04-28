package nsu.task5.io;

import nsu.task5.model.AxisAlignedBox;
import nsu.task5.model.LightSource;
import nsu.task5.model.Material;
import nsu.task5.model.Primitive;
import nsu.task5.model.Quadrangle;
import nsu.task5.model.RgbColor;
import nsu.task5.model.SceneModel;
import nsu.task5.model.Sphere;
import nsu.task5.model.Triangle;
import nsu.wireframe.model.Point3DModel;

import java.util.ArrayList;
import java.util.List;

public class SceneFileParser {
    public SceneModel parse(List<String> rawLines) throws RayFileException {
        List<ParsedLine> lines = preprocess(rawLines);
        Cursor cursor = new Cursor(lines);

        RgbColor ambient = parseRgb255(cursor.next("ambient light RGB"), 3, "ambient light RGB");
        int lightCount = parseInt(cursor.next("light source count"), "light source count");
        if (lightCount < 0) {
            throw new RayFileException("Line " + cursor.previousLineNumber() + ": light source count must be non-negative.");
        }

        List<LightSource> lights = new ArrayList<>();
        for (int i = 0; i < lightCount; i++) {
            ParsedLine line = cursor.next("light source " + (i + 1));
            double[] values = parseDoubles(line, 6, "light source");
            validateRgb255(line, values[3], values[4], values[5], "light source RGB");
            lights.add(new LightSource(
                    new Point3DModel(values[0], values[1], values[2]),
                    RgbColor.from255(values[3], values[4], values[5])
            ));
        }

        List<Primitive> primitives = new ArrayList<>();
        while (cursor.hasNext()) {
            ParsedLine header = cursor.next("primitive section");
            String[] tokens = split(header);
            if (tokens.length == 0) {
                continue;
            }

            primitives.add(switch (tokens[0].toUpperCase()) {
                case "SPHERE" -> parseSphere(header, cursor);
                case "BOX" -> parseBox(header, cursor);
                case "TRIANGLE" -> parseTriangle(header, cursor);
                case "QUADRANGLE" -> parseQuadrangle(header, cursor);
                default -> throw new RayFileException("Line " + header.lineNumber()
                        + ": unknown primitive type \"" + tokens[0] + "\".");
            });
        }

        return new SceneModel(ambient, lights, primitives);
    }

    private Sphere parseSphere(ParsedLine header, Cursor cursor) throws RayFileException {
        double[] centerValues = parsePrimitiveHeader(header, "SPHERE", 3);
        double radius = parseDouble(cursor.next("sphere radius"), "sphere radius");
        if (radius <= 0) {
            throw new RayFileException("Line " + cursor.previousLineNumber() + ": sphere radius must be positive.");
        }

        return new Sphere(
                new Point3DModel(centerValues[0], centerValues[1], centerValues[2]),
                radius,
                parseMaterial(cursor.next("sphere material"))
        );
    }

    private AxisAlignedBox parseBox(ParsedLine header, Cursor cursor) throws RayFileException {
        double[] minValues = parsePrimitiveHeader(header, "BOX", 3);
        double[] maxValues = parseDoubles(cursor.next("box max point"), 3, "box max point");
        return new AxisAlignedBox(
                new Point3DModel(minValues[0], minValues[1], minValues[2]),
                new Point3DModel(maxValues[0], maxValues[1], maxValues[2]),
                parseMaterial(cursor.next("box material"))
        );
    }

    private Triangle parseTriangle(ParsedLine header, Cursor cursor) throws RayFileException {
        double[] p1 = parsePrimitiveHeader(header, "TRIANGLE", 3);
        double[] p2 = parseDoubles(cursor.next("triangle second point"), 3, "triangle second point");
        double[] p3 = parseDoubles(cursor.next("triangle third point"), 3, "triangle third point");
        return new Triangle(
                point(p1),
                point(p2),
                point(p3),
                parseMaterial(cursor.next("triangle material"))
        );
    }

    private Quadrangle parseQuadrangle(ParsedLine header, Cursor cursor) throws RayFileException {
        double[] p1 = parsePrimitiveHeader(header, "QUADRANGLE", 3);
        double[] p2 = parseDoubles(cursor.next("quadrangle second point"), 3, "quadrangle second point");
        double[] p3 = parseDoubles(cursor.next("quadrangle third point"), 3, "quadrangle third point");
        double[] p4 = parseDoubles(cursor.next("quadrangle fourth point"), 3, "quadrangle fourth point");
        return new Quadrangle(
                point(p1),
                point(p2),
                point(p3),
                point(p4),
                parseMaterial(cursor.next("quadrangle material"))
        );
    }

    private double[] parsePrimitiveHeader(ParsedLine line, String keyword, int valueCount) throws RayFileException {
        String[] tokens = split(line);
        if (tokens.length != valueCount + 1 || !keyword.equalsIgnoreCase(tokens[0])) {
            throw new RayFileException("Line " + line.lineNumber() + ": expected "
                    + keyword + " followed by " + valueCount + " numbers.");
        }

        double[] values = new double[valueCount];
        for (int i = 0; i < valueCount; i++) {
            values[i] = parseFiniteDouble(tokens[i + 1], line.lineNumber(), keyword);
        }
        return values;
    }

    private Material parseMaterial(ParsedLine line) throws RayFileException {
        double[] values = parseDoubles(line, 7, "material");
        return new Material(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
    }

    private RgbColor parseRgb255(ParsedLine line, int count, String label) throws RayFileException {
        double[] values = parseDoubles(line, count, label);
        validateRgb255(line, values[0], values[1], values[2], label);
        return RgbColor.from255(values[0], values[1], values[2]);
    }

    private void validateRgb255(ParsedLine line, double r, double g, double b, String label) throws RayFileException {
        for (double value : new double[]{r, g, b}) {
            if (value < 0 || value > 255) {
                throw new RayFileException("Line " + line.lineNumber() + ": " + label
                        + " values must be in the 0..255 range.");
            }
        }
    }

    private Point3DModel point(double[] values) {
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
        double[] values = parseDoubles(line, 1, label);
        return values[0];
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

    private static class Cursor {
        private final List<ParsedLine> lines;
        private int index;
        private int previousLineNumber;

        private Cursor(List<ParsedLine> lines) {
            this.lines = lines;
        }

        private boolean hasNext() {
            return index < lines.size();
        }

        private ParsedLine next(String expected) throws RayFileException {
            if (!hasNext()) {
                throw new RayFileException("Expected " + expected + ", but the file ended.");
            }
            ParsedLine line = lines.get(index++);
            previousLineNumber = line.lineNumber();
            return line;
        }

        private int previousLineNumber() {
            return previousLineNumber;
        }
    }
}
