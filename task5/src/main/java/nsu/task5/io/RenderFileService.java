package nsu.task5.io;

import nsu.task5.model.RayCamera;
import nsu.task5.render.RenderSettings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class RenderFileService {
    private final RenderFileParser parser = new RenderFileParser();
    private final RenderFileWriter writer = new RenderFileWriter();

    public RenderFileData load(File file) throws RayFileException {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            return parser.parse(lines);
        } catch (IOException e) {
            throw new RayFileException("Could not read render file: " + e.getMessage(), e);
        }
    }

    public void save(File file, RenderSettings settings, RayCamera camera) throws RayFileException {
        try {
            Files.writeString(file.toPath(), writer.write(settings, camera), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RayFileException("Could not write render file: " + e.getMessage(), e);
        }
    }
}
