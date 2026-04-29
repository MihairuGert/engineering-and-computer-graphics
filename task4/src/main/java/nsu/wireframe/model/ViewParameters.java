package nsu.wireframe.model;

public class ViewParameters {
    private double rotationX;
    private double rotationY;
    private double rotationZ;
    private double zn;

    public ViewParameters(double rotationX, double rotationY, double rotationZ, double zn) {
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.zn = zn;
    }

    public ViewParameters copy() {
        return new ViewParameters(rotationX, rotationY, rotationZ, zn);
    }

    public void resetRotations() {
        rotationX = 0;
        rotationY = 0;
        rotationZ = 0;
    }

    public double getRotationX() {
        return rotationX;
    }

    public void setRotationX(double rotationX) {
        this.rotationX = rotationX;
    }

    public double getRotationY() {
        return rotationY;
    }

    public void setRotationY(double rotationY) {
        this.rotationY = rotationY;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
    }

    public double getZn() {
        return zn;
    }

    public void setZn(double zn) {
        this.zn = zn;
    }
}
