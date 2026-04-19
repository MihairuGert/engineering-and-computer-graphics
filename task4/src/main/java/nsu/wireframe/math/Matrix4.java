package nsu.wireframe.math;

public class Matrix4 {
    private static final int SIZE = 4;

    private final double[][] values;

    public Matrix4(double[][] values) {
        this.values = new double[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(values[row], 0, this.values[row], 0, SIZE);
        }
    }

    public Matrix4() {
        this.values = new double[SIZE][SIZE];
    }

    public static Matrix4 identity() {
        double[][] values = new double[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            values[i][i] = 1.0;
        }
        return new Matrix4(values);
    }

    public static Matrix4 spline() {
        double[][] values = {{-1./6, 3./6, -3./6, 1./6},
                             {3./6, -1, 3./6, 0},
                             {-3./6, 0, 3./6, 0},
                             {1./6, 4./6, 1./6, 0}};
        return new Matrix4(values);
    }

    public Matrix4 multiply(Matrix4 other) {
        Matrix4 res = new Matrix4();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < SIZE; k++) {
                    res.values[i][j] += this.values[i][k]*other.values[k][j];
                }
            }
        }

        return res;
    }

    public Vector4 transform(Vector4 vector) {
        double[] coords = new double[SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                coords[i] += this.values[i][j] * vector.get(j);
            }
        }

        return new Vector4(coords);
    }

    public Vector4 vector4OnMatrix(Vector4 vector) {
        double[] coords = new double[SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                coords[i] += this.values[j][i] * vector.get(j);
            }
        }

        return new Vector4(coords);
    }

    public double get(int row, int column) {
        return values[row][column];
    }
}
