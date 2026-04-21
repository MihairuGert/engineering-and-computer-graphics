package nsu.wireframe.io;

import nsu.wireframe.model.AppState;
import nsu.wireframe.model.ControlPoint;
import nsu.wireframe.model.SceneParameters;
import nsu.wireframe.model.ViewParameters;

public class SceneFileWriter {
    public String write(AppState state) {
        SceneParameters scene = state.getSceneParameters();
        ViewParameters view = state.getViewParameters();

        StringBuilder builder = new StringBuilder();
        builder.append(SceneFileParser.HEADER).append('\n');
        builder.append("K=").append(state.getControlPoints().size()).append('\n');
        builder.append("N=").append(scene.getN()).append('\n');
        builder.append("M=").append(scene.getM()).append('\n');
        builder.append("M1=").append(scene.getM1()).append('\n');
        builder.append("ZN=").append(view.getZn()).append('\n');
        builder.append("ROT_X=").append(view.getRotationX()).append('\n');
        builder.append("ROT_Y=").append(view.getRotationY()).append('\n');
        builder.append("ROT_Z=").append(view.getRotationZ()).append('\n');
        builder.append("POINTS_BEGIN").append('\n');
        for (ControlPoint point : state.getControlPoints()) {
            builder.append(point.u())
                    .append(' ')
                    .append(point.v())
                    .append('\n');
        }
        builder.append("POINTS_END").append('\n');
        return builder.toString();
    }
}
