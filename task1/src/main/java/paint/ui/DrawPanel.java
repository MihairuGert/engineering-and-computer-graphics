package paint.ui;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class DrawPanel extends ImageView {
    private WritableImage image;

    private ToolMode currentTool;

    public DrawPanel(int width, int height) {
        super();

        image = new WritableImage(width, height);
        var pixelWriter = image.getPixelWriter();

        setImage(image);
        currentTool = ToolMode.NONE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(x, y, Color.WHITE);
            }
        }

        setUpTools();
    }

    public void setCurrentTool(ToolMode mode) {
        currentTool = mode;
    }

    public void loadImage(Image newImage) {
        image = new WritableImage((int)newImage.getWidth(), (int)newImage.getHeight());

        for (int x = 0; x < (int)newImage.getWidth(); x++) {
            for (int y = 0; y < (int)newImage.getHeight(); y++) {
                image.getPixelWriter().setColor(x, y, newImage.getPixelReader().getColor(x, y));
            }
        }

        setImage(image);
    }

    private boolean isDrawingLine = false;
    private double lastX = -1;
    private double lastY = -1;

    private void setUpTools() {
        setOnMouseClicked(event -> {
            switch (currentTool) {
                case NONE -> {}
                case LINE -> {
                    if (isDrawingLine) {
                        drawLine((int) lastX,(int) lastY,(int) event.getX(),(int) event.getY());
                        currentTool = ToolMode.NONE;
                        lastX = -1;
                        lastY = -1;
                        isDrawingLine = false;
                        break;
                    }

                    lastX = event.getX();
                    lastY = event.getY();
                    isDrawingLine = true;
                }
            }
        });

    }

    private void drawLine(int x0, int y0, int x1, int y1) {
        drawLineBresenham(x0, y0, x1, y1);
    }

    private void drawLineBresenham(int x0, int y0, int x1, int y1) {
        int x = x0;
        int y = y0;

        int dx = x1 - x;
        int dy = y1 - y;

        int err = -dx;

        for (int i = 0; i < dx; i++) {
            x++;
            err += 2*dy;
            if (err > 0) {
                err -= 2*dx;
                y++;
            }
            image.getPixelWriter().setColor(x, y, Color.RED);
        }
    }
}
