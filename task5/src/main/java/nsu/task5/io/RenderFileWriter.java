package nsu.task5.io;

import nsu.task5.model.RayCamera;
import nsu.task5.render.RenderSettings;
import nsu.wireframe.model.Point3DModel;

import java.util.Locale;

public class RenderFileWriter {
    public String write(RenderSettings settings, RayCamera camera) {
        StringBuilder builder = new StringBuilder();
        builder.append(settings.backgroundColor().red255()).append(' ')
                .append(settings.backgroundColor().green255()).append(' ')
                .append(settings.backgroundColor().blue255()).append('\n');
        builder.append(format(settings.gamma())).append('\n');
        builder.append(settings.depth()).append('\n');
        builder.append(RenderSettings.NORMAL_QUALITY).append('\n');
        appendPoint(builder, camera.eye());
        appendPoint(builder, camera.view());
        appendPoint(builder, camera.up());
        builder.append(format(camera.zn())).append(' ').append(format(camera.zf())).append('\n');
        builder.append(format(camera.sw())).append(' ').append(format(camera.sh())).append('\n');
        return builder.toString();
    }

    private void appendPoint(StringBuilder builder, Point3DModel point) {
        builder.append(format(point.x())).append(' ')
                .append(format(point.y())).append(' ')
                .append(format(point.z())).append('\n');
    }

    private String format(double value) {
        return String.format(Locale.US, "%.10g", value);
    }
}
