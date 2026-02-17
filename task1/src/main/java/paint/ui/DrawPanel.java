package paint.ui;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DrawPanel extends ImageView {
    private WritableImage image;
    private PixelWriter writer;

    public ToolMode getCurrentTool() {
        return currentTool;
    }

    private ToolMode currentTool;
    private final Settings settings;

    public DrawPanel(int width, int height, Settings settings) {
        super();
        this.settings = settings;

        image = new WritableImage(width, height);
        writer = image.getPixelWriter();

        setImage(image);
        currentTool = ToolMode.NONE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                writer.setColor(x, y, Color.WHITE);
            }
        }

        setUpTools();
    }

    public void setCurrentTool(ToolMode mode) {
        currentTool = mode;
    }

    public void newImage(int width, int height) {
        image = new WritableImage(width, height);
        writer = image.getPixelWriter();

        for (int x = 0; x < (int)image.getWidth(); x++) {
            for (int y = 0; y < (int)image.getHeight(); y++) {
                writer.setColor(x, y, Color.WHITE);
            }
        }

        setImage(image);
    }

    public void loadImage(Image newImage) {
        image = new WritableImage((int)newImage.getWidth(), (int)newImage.getHeight());
        writer = image.getPixelWriter();

        for (int x = 0; x < (int)newImage.getWidth(); x++) {
            for (int y = 0; y < (int)newImage.getHeight(); y++) {
                writer.setColor(x, y, newImage.getPixelReader().getColor(x, y));
            }
        }

        setImage(image);
    }

    public WritableImage getImg() {
        return image;
    }

    private double lastX = -1;
    private double lastY = -1;

    public void resetTools() {
        lastY = -1;
        lastX = -1;
    }

    private void setUpTools() {
        setOnMouseClicked(event -> {
            switch (currentTool) {
                case NONE -> {}
                case LINE -> {
                    if (lastX != -1 && lastY != -1) {
                        drawLine((int) lastX,(int) lastY,(int) event.getX(),(int) event.getY(),
                                settings.getCurrentColor(), settings.getLineThickness());
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    }

                    lastX = event.getX();
                    lastY = event.getY();
                }
                case PENCIL -> {
                    if (isInBounds(event.getX(), event.getY())) {
                        drawPoint((int) event.getX(), (int) event.getY(),
                                settings.getCurrentColor(), settings.getBrushSize());
                    }
                }
                case FILL -> {
                    spanFill((int) event.getX(), (int) event.getY(), settings.getCurrentColor());
                }
                case STAMP -> drawStamp((int) event.getX(), (int) event.getY());
            }
        });
        setOnMouseDragged(event -> {
            switch (currentTool) {
                case NONE -> {}
                case PENCIL -> {
                    drawPoint((int) event.getX(), (int) event.getY(),
                            settings.getCurrentColor(), settings.getBrushSize());
                }
            }
        });
    }

    private void drawStamp(int x, int y) {
        double pointAngleDelta = 360f / settings.getSidesCount();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < settings.getSidesCount(); i++) {
            int xp = x + (int) (settings.getRadius() * Math.cos(Math.toRadians(i * pointAngleDelta + settings.getRotation())));
            int yp = y + (int) (settings.getRadius() * Math.sin(Math.toRadians(i * pointAngleDelta + settings.getRotation())));
            points.add(new Point(xp, yp));
        }
        switch (settings.getStampType()) {
            case STAR -> {
                List<Point> pointsLower = new ArrayList<>();
                for (int i = 0; i < settings.getSidesCount(); i++) {
                    int xp = x + (int) (settings.getRadius() * 0.4 * Math.cos(Math.toRadians(i * pointAngleDelta + settings.getRotation() + pointAngleDelta / 2)));
                    int yp = y + (int) (settings.getRadius() * 0.4 * Math.sin(Math.toRadians(i * pointAngleDelta + settings.getRotation() + pointAngleDelta / 2)));
                    pointsLower.add(new Point(xp, yp));
                }
                for (int i = 0; i < points.size(); i++) {
                    var p1 = points.get(i);
                    var lp1 = pointsLower.get(i);
                    var lp2 = pointsLower.get((i + points.size() - 1) % points.size());
                    drawLine(p1.x, p1.y, lp1.x, lp1.y, settings.getCurrentColor(), settings.getLineThickness());
                    drawLine(p1.x, p1.y, lp2.x, lp2.y, settings.getCurrentColor(), settings.getLineThickness());
                }
            }
            case POLYGON -> {
                for (int i = 0; i < points.size(); i++) {
                    var p1 = points.get(i);
                    Point p2;
                    if (i == points.size() - 1) {
                        p2 = points.getFirst();
                    } else {
                        p2 = points.get(i + 1);
                    }
                    drawLine(p1.x, p1.y, p2.x, p2.y, settings.getCurrentColor(), settings.getLineThickness());
                }
            }
        }
    }

    private class Point {
        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private void spanFill(int x, int y, Color color) {
        if (!isInBounds(x, y)) {
            return;
        }
        Color seedColor = image.getPixelReader().getColor(x, y);
        if (seedColor.equals(color))
            return;
        Stack<Point> points = new Stack<>();
        points.add(new Point(x,y));
        while(!points.isEmpty()) {
            Point s = points.pop();
            int lx = s.x;
            int rx = s.x;
            while (needFill(lx-1, s.y, seedColor)) {
                writer.setColor(lx-1, s.y, color);
                lx--;
            }
            while (needFill(rx, s.y, seedColor)) {
                writer.setColor(rx, s.y, color);
                rx++;
            }
            points.addAll(scanSpans(lx, rx-1, s.y+1, seedColor));
            points.addAll(scanSpans(lx, rx-1, s.y-1, seedColor));
        }
    }

    private ArrayList<Point> scanSpans(int lx, int rx, int y, Color color) {
        boolean isSpanAdded = false;
        ArrayList<Point> res = new ArrayList<>();
        for (int x = lx; x <= rx; x++) {
            if (!needFill(x, y, color)) {
                isSpanAdded = false;
            } else if (!isSpanAdded) {
                res.add(new Point(x,y));
                isSpanAdded = true;
            }
        }
        return res;
    }

    private boolean needFill(int x, int y, Color color) {
        if (!isInBounds(x, y))
            return false;
        return image.getPixelReader().getColor(x, y).equals(color);
    }

    private void drawLine(int x0, int y0, int x1, int y1, Color color, int thickness) {
        drawLineBresenham(x0, y0, x1, y1, color, thickness);
    }

    private void drawLineBresenham(int x0, int y0, int x1, int y1, Color color, int thinkness) {
        int x = x0;
        int y = y0;

        int dx = x1 - x;
        int dy = y1 - y;

        int xSign = dx > 0 ? 1 : -1;
        int ySign = dy > 0 ? 1 : -1;

        if (ySign * dy < xSign * dx) {
            int err = -xSign*dx;
            for (int i = 0; i < xSign*dx; i++) {
                x += xSign;
                err += 2 * dy * ySign;
                if (err > 0) {
                    err -= 2 * xSign * dx;
                    y += ySign;
                }
                drawPoint(x, y, color, thinkness/2 + 1);
            }
        } else {
            int err = -dy * ySign;
            for (int i = 0; i < dy * ySign; i++) {
                y += ySign;
                err += 2 * dx * xSign;
                if (err > 0) {
                    err -= 2 * dy * ySign;
                    x += xSign;
                }
                drawPoint(x, y, color, thinkness/2 + 1);
            }
        }
    }

    private void drawPoint(int centerX, int centerY, Color color, int radius) {
        int r = radius / 2;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                if (x*x + y*y <= r*r) {
                    int drawX = centerX + x;
                    int drawY = centerY + y;
                    if (isInBounds(drawX, drawY)) {
                        writer.setColor(drawX, drawY, color);
                    }
                }
            }
        }
    }

    private boolean isInBounds(double x, double y) {
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }
}