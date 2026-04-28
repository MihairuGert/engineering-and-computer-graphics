package nsu.task5.render;

import nsu.task5.model.RgbColor;

public class RenderSettings {
    public static final int MIN_DEPTH = 1;
    public static final int MAX_DEPTH = 10;
    public static final String NORMAL_QUALITY = "normal";

    private RgbColor backgroundColor;
    private double gamma;
    private int depth;
    private String quality;

    public RenderSettings(RgbColor backgroundColor, double gamma, int depth, String quality) {
        this.backgroundColor = backgroundColor;
        this.gamma = gamma;
        this.depth = clampDepth(depth);
        this.quality = NORMAL_QUALITY.equalsIgnoreCase(quality) ? NORMAL_QUALITY : NORMAL_QUALITY;
    }

    public static RenderSettings defaults() {
        return new RenderSettings(RgbColor.from255(20, 24, 30), 1.0, 3, NORMAL_QUALITY);
    }

    public RenderSettings copy() {
        return new RenderSettings(backgroundColor, gamma, depth, quality);
    }

    public static int clampDepth(int depth) {
        return Math.clamp(depth, MIN_DEPTH, MAX_DEPTH);
    }

    public RgbColor backgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(RgbColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public double gamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma > 0 && Double.isFinite(gamma) ? gamma : 1.0;
    }

    public int depth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = clampDepth(depth);
    }

    public String quality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = NORMAL_QUALITY;
    }
}
