package paint.ui;

import javafx.scene.paint.Color;

public class Settings {
    private Color currentColor = Color.RED;
    private int brushSize = 5;
    private int lineThickness = 1;

    private int radius = 50;
    private int rotation = 0;
    private int sidesCount = 3;
    private StampType stampType = StampType.POLYGON;

    public int getSidesCount() {
        return sidesCount;
    }

    public void setSidesCount(int sidesCount) {
        this.sidesCount = sidesCount;
    }

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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public StampType getStampType() {
        return stampType;
    }

    public void setStampType(StampType stampType) {
        this.stampType = stampType;
    }

}