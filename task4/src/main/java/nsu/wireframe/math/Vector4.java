package nsu.wireframe.math;

public class Vector4 {
    public static final int SIZE = 4;
    public static final int X_INDEX = 0;
    public static final int Y_INDEX = 1;
    public static final int Z_INDEX = 2;
    public static final int W_INDEX = 3;

    private final double[] values;

    public Vector4(double x, double y, double z, double w) {
        this.values = new double[SIZE];
        values[X_INDEX] = x;
        values[Y_INDEX] = y;
        values[Z_INDEX] = z;
        values[W_INDEX] = w;
    }

    public Vector4(double[] values) {
        if (values.length != SIZE) {
            throw new IllegalArgumentException("Vector4 must contain exactly " + SIZE + " values.");
        }

        this.values = new double[SIZE];
        System.arraycopy(values, 0, this.values, 0, SIZE);
    }

    public double get(int index) {
        return values[index];
    }

    public void set(int index, double value) {
        values[index] = value;
    }

    public double[] toArray() {
        double[] copy = new double[SIZE];
        System.arraycopy(values, 0, copy, 0, SIZE);
        return copy;
    }
}
