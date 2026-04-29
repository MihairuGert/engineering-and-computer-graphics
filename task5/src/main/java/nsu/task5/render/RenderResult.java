package nsu.task5.render;

import nsu.task5.model.RgbColor;

public record RenderResult(int width, int height, RgbColor[] buffer, RenderSettings settings) {
}
