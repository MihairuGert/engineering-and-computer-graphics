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

    public static Matrix4 rotationX(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double[][] values = {
                {1, 0, 0, 0},
                {0, cos, -sin, 0},
                {0, sin, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4(values);
    }

    public static Matrix4 rotationY(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double[][] values = {
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4(values);
    }

    public static Matrix4 rotationZ(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double[][] values = {
                {cos, -sin, 0, 0},
                {sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4(values);
    }

    public static Matrix4 perspective(double zn) {
        double[][] values = {
                {zn, 0, 0, 0},
                {0, zn, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 1, 0}
        };
        return new Matrix4(values);
    }

    public static Matrix4 viewport(double width, double height) {
        double aspect = width / height;
        double sw = 2.0 * aspect;
        double sh = 2.0;

        double[][] values = {
                {width / sw, 0, 0, width / 2.0},
                {0, -height / sh, 0, height / 2.0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
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
