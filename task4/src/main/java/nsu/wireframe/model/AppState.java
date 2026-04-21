package nsu.wireframe.model;

import nsu.wireframe.math.GeometryService;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private SceneParameters sceneParameters;
    private ViewParameters viewParameters;
    private final CameraConfig cameraConfig;
    private List<ControlPoint> controlPoints;
    private WireframeModel wireframeModel;

    public AppState(
            SceneParameters sceneParameters,
            ViewParameters viewParameters,
            CameraConfig cameraConfig,
            List<ControlPoint> controlPoints,
            WireframeModel wireframeModel
    ) {
        this.sceneParameters = sceneParameters.copy();
        this.viewParameters = viewParameters.copy();
        this.cameraConfig = cameraConfig;
        setControlPoints(controlPoints);
        this.wireframeModel = wireframeModel == null ? WireframeModel.empty() : wireframeModel;
    }

    public static AppState createDefault() {
        List<ControlPoint> points = List.of(
                new ControlPoint(-0.20, -0.80),
                new ControlPoint(-0.5, -0.35),
                new ControlPoint(0.35, 0.10),
                new ControlPoint(0.70, 0.55),
                new ControlPoint(0.35, 0.85)
        );
        return new AppState(
                new SceneParameters(points.size(), 8, 12, 2),
                new ViewParameters(0, 0, 0, 1.0),
                CameraConfig.fixed(),
                points,
                WireframeModel.empty()
        );
    }

    public AppState copy() {
        return new AppState(
                sceneParameters.copy(),
                viewParameters.copy(),
                cameraConfig,
                new ArrayList<>(controlPoints),
                wireframeModel
        );
    }

    public void replaceFrom(AppState other) {
        this.sceneParameters = other.sceneParameters.copy();
        this.viewParameters = other.viewParameters.copy();
        setControlPoints(other.controlPoints);
        this.wireframeModel = other.wireframeModel;
    }

    public void rebuildWireframe(GeometryService geometryService) {
        wireframeModel = geometryService.buildWireframe(controlPoints, sceneParameters);
    }

    public SceneParameters getSceneParameters() {
        return sceneParameters;
    }

    public void setSceneParameters(SceneParameters sceneParameters) {
        this.sceneParameters = sceneParameters.copy();
        this.sceneParameters.setK(controlPoints.size());
    }

    public ViewParameters getViewParameters() {
        return viewParameters;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public List<ControlPoint> getControlPoints() {
        return List.copyOf(controlPoints);
    }

    public void setControlPoints(List<ControlPoint> controlPoints) {
        this.controlPoints = new ArrayList<>(controlPoints);
        this.sceneParameters.setK(this.controlPoints.size());
    }

    public WireframeModel getWireframeModel() {
        return wireframeModel;
    }
}
