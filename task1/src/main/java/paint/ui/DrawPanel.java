package paint.ui;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class DrawPanel extends ImageView {
    private WritableImage image;
//    private PixelWriter pixelWriter;
//    private PixelReader pixelReader;

    public DrawPanel(int width, int height) {
        super();

        image = new WritableImage(width, height);
        var pixelWriter = image.getPixelWriter();

        setImage(image);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(x, y, Color.WHITE);
            }
        }
    }

    public ImageView getImageView() {
        return new ImageView(image);
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
}
