package paint.ui;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

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

    private void drawLine(int x0, int y0, int x1, int y1, Color color, int thickness) {
        drawLineBresenham(x0, y0, x1, y1, color, thickness);
    }

    private void drawLineBresenham(int x0, int y0, int x1, int y1, Color color, int thickness) {
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
                writer.setColor(x, y, color);
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
                writer.setColor(x, y, color);
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