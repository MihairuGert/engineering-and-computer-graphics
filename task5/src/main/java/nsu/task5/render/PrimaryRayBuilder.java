package nsu.task5.render;

import nsu.task5.model.RayCamera;
import nsu.wireframe.model.Point3DModel;

public class PrimaryRayBuilder {
    public Ray buildPrimaryRay(int pixelX, int pixelY, int width, int height, RayCamera camera, RenderSettings settings) {
        Point3DModel eye = camera.eye();

        var forward = camera.basis().forward();
        var up = camera.basis().up();
        var right = camera.basis().right();

        double nx = (pixelX + 0.5) / width;
        double ny = (pixelY + 0.5) / height;

        double screenX = (nx - 0.5) * camera.sw();
        double screenY = (0.5 - ny) * camera.sh();

        var pointOnScreen = eye.sum(forward.mul(camera.zn())).sum(right.mul(screenX)).sum(up.mul(screenY));

        var direction = pointOnScreen.sub(eye).normalize();

        return new Ray(eye, direction);
    }
}
