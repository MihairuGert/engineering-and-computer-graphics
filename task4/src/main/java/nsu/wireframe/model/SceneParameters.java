package nsu.wireframe.model;

public class SceneParameters {
    public static final int MIN_K = 4;
    public static final int MIN_N = 1;
    public static final int MIN_M = 2;
    public static final int MIN_M1 = 1;

    private int k;
    private int n;
    private int m;
    private int m1;

    public SceneParameters(int k, int n, int m, int m1) {
        this.k = k;
        this.n = n;
        this.m = m;
        this.m1 = m1;
    }

    public SceneParameters copy() {
        return new SceneParameters(k, n, m, m1);
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getM1() {
        return m1;
    }

    public void setM1(int m1) {
        this.m1 = m1;
    }
}
