package paint.ui;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class DrawPanel extends ImageView {
    private WritableImage image;
    private PixelWriter writer;

    public ToolMode getCurrentTool() {
        return currentTool;
    }

    private ToolMode currentTool;

    public DrawPanel(int width, int height) {
        super();

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

    private double lastX = -1;
    private double lastY = -1;

    private void setUpTools() {
        setOnMouseClicked(event -> {
            switch (currentTool) {
                case NONE -> {}
                case LINE -> {
                    if (lastX != -1 && lastY != -1) {
                        drawLine((int) lastX,(int) lastY,(int) event.getX(),(int) event.getY());
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    }

                    lastX = event.getX();
                    lastY = event.getY();
                }
                case PENCIL -> {
                    if (isInBounds(event.getX(), event.getY()))
                        writer.setColor((int) event.getX(),(int) event.getY(), Color.RED);
                }
            }
        });
        setOnMouseDragged(event -> {
            switch (currentTool) {
                case NONE -> {}
                case PENCIL -> {
                    for (int i = -5; i < 5; i++) {
                        for (int j = -4; j < 4; j++) {
                            if (isInBounds(event.getX() + i, event.getY() + j))
                                writer.setColor((int) event.getX() + i,(int) event.getY() + j, Color.RED);
                        }
                    }
                }
            }
        });

    }

    private void drawLine(int x0, int y0, int x1, int y1) {
        drawLineBresenham(x0, y0, x1, y1);
        //        int dx = x1 - x0;
//        int dy = y1 - y0;
//
//        if (dy > 0)
//            if (dx > 0)
//                if (dx >= dy)
//                    drawLineBresenham(x0, y0, x1, y1);
//                else
//                    drawLineBresenham(y0, x0, y1, x1);
//            else
//                if (dx >= dy)
//                    drawLineBresenham(x0, y0, x1, y1);
//                else
//                    drawLineBresenham(y0, x0, y1, x1);

    }

    private void drawLineBresenham(int x0, int y0, int x1, int y1) {
        int x = x0;
        int y = y0;

        int dx = x1 - x0;
        int dy = y1 - y0;

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

    private boolean isInBounds(double x, double y) {
        return x >= 0 && x <= image.getWidth()
                && y >= 0 && y <= image.getHeight();
    }
}
