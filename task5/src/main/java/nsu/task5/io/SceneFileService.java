package nsu.task5.io;

import nsu.task5.model.SceneModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class SceneFileService {
    private final SceneFileParser parser = new SceneFileParser();

    public SceneModel load(File file) throws RayFileException {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            return parser.parse(lines);
        } catch (IOException e) {
            throw new RayFileException("Could not read scene file: " + e.getMessage(), e);
        }
    }
}
