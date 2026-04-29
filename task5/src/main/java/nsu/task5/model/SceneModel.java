package nsu.task5.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneModel {
    private final RgbColor ambientLight;
    private final List<LightSource> lights;
    private final List<Primitive> primitives;

    public SceneModel(RgbColor ambientLight, List<LightSource> lights, List<Primitive> primitives) {
        this.ambientLight = ambientLight;
        this.lights = List.copyOf(lights);
        this.primitives = List.copyOf(primitives);
    }

    public static SceneModel empty() {
        return new SceneModel(RgbColor.BLACK, List.of(), List.of());
    }

    public RgbColor ambientLight() {
        return ambientLight;
    }

    public List<LightSource> lights() {
        return lights;
    }

    public List<Primitive> primitives() {
        return primitives;
    }

    public Optional<BoundingBox> boundingBox() {
        List<BoundingBox> boxes = new ArrayList<>();
        for (Primitive primitive : primitives) {
            boxes.add(primitive.boundingBox());
        }
        return BoundingBox.combine(boxes);
    }
}
