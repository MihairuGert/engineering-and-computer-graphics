package nsu.task5.io;

import nsu.task5.model.RayCamera;
import nsu.task5.render.RenderSettings;

import java.util.List;

public record RenderFileData(RenderSettings settings, RayCamera camera, List<String> warnings) {
    public RenderFileData {
        warnings = List.copyOf(warnings);
    }
}
