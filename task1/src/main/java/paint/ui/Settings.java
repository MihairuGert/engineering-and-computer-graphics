package paint.ui;

import javafx.scene.paint.Color;

// add otmena
public class Settings {
    private Color currentColor = Color.RED;
    private int brushSize = 5;
    private int lineThickness = 1;

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int size) {
        this.brushSize = Math.max(1, Math.min(50, size));
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(int thickness) {
        this.lineThickness = Math.max(1, Math.min(10, thickness));
    }
}