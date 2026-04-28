package nsu.task5.render;

import javafx.concurrent.Task;
import nsu.task5.model.RayCamera;
import nsu.task5.model.SceneModel;

public class RenderService {
    public Task<RenderResult> createTask(
            SceneModel scene,
            RenderSettings settings,
            RayCamera camera,
            int width,
            int height
    ) {
        SceneModel sceneSnapshot = scene;
        RenderSettings settingsSnapshot = settings.copy();
        RayCamera cameraSnapshot = camera.copy();

        return new Task<>() {
            @Override
            protected RenderResult call() throws Exception {
                updateProgress(0, height);
                try (ParallelRenderer renderer = new ParallelRenderer()) {
                    RenderResult result = renderer.render(
                            sceneSnapshot,
                            settingsSnapshot,
                            cameraSnapshot,
                            width,
                            height,
                            this::updateProgress,
                            this::isCancelled
                    );
                    updateProgress(height, height);
                    return result;
                }
            }
        };
    }
}
