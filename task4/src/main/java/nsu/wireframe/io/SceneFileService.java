package nsu.wireframe.io;

import nsu.wireframe.model.AppState;
import nsu.wireframe.validation.ValidationException;
import nsu.wireframe.validation.Validators;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class SceneFileService {
    private final SceneFileParser parser = new SceneFileParser();
    private final SceneFileWriter writer = new SceneFileWriter();

    public AppState load(File file) throws SceneFileException {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            return parser.parse(lines);
        } catch (IOException e) {
            throw new SceneFileException("Could not read file: " + e.getMessage(), e);
        }
    }

    public void save(File file, AppState state) throws SceneFileException {
        try {
            Validators.validateState(state);
            Files.writeString(file.toPath(), writer.write(state), StandardCharsets.UTF_8);
        } catch (ValidationException e) {
            throw new SceneFileException("Current scene state is invalid: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SceneFileException("Could not write file: " + e.getMessage(), e);
        }
    }
}
