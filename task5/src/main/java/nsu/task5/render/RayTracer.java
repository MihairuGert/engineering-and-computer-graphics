package nsu.task5.render;

import nsu.task5.math.VectorMath;
import nsu.task5.model.Primitive;
import nsu.task5.model.RgbColor;
import nsu.task5.model.SceneModel;

import java.util.Optional;

public class RayTracer {
    private final SceneModel scene;
    private final RenderSettings settings;

    public RayTracer(SceneModel scene, RenderSettings settings) {
        this.scene = scene;
        this.settings = settings;
    }

    public RgbColor trace(Ray ray, int depth) {

        if (depth <= 0) {
            return new RgbColor(0.0, 0.0, 0.0);
        }

        var closest = findClosest(ray);

        if (closest.isEmpty()) {
            return settings.backgroundColor();
        }

        var hit = closest.get();

        RgbColor local = computeLocalIllumination(hit, ray);

        if (depth == 1) {
            return local;
        }

        var material = hit.material();

        if (material.ksR() == 0.0 && material.ksG() == 0.0 && material.ksB() == 0.0) {
            return local;
        }

        Ray reflectedRay = buildReflectedRay(hit, ray);
        RgbColor reflected = trace(reflectedRay, depth - 1);

        return addReflection(local, reflected, hit);
    }

    private Ray buildReflectedRay(Hit hit, Ray incomingRay) {
        final double eps = 1e-6;

        var d = incomingRay.direction().normalize();
        var n = hit.normal().normalize();

        var reflectedDir = d.sub(n.mul(2.0 * VectorMath.dot(d, n))).normalize();
        var reflectedOrigin = hit.point();

        return new Ray(reflectedOrigin, reflectedDir);
    }

    private RgbColor addReflection(RgbColor local, RgbColor reflected, Hit hit) {
        var material = hit.material();

        return new RgbColor(
                local.r() + material.ksR() * reflected.r(),
                local.g() + material.ksG() * reflected.g(),
                local.b() + material.ksB() * reflected.b()
        );
    }

    private Optional<Hit> findClosest(Ray ray) {
        Optional<Hit> closest = Optional.empty();
        for (Primitive p : scene.primitives()) {
            var hit = p.intersect(ray);

            if (hit.isEmpty()) {
                continue;
            }

            if (closest.isEmpty()) {
                closest = hit;
                continue;
            }

            if (closest.get().t() > hit.get().t()) {
                closest = hit;
            }
        }
        return closest;
    }

    private RgbColor computeLocalIllumination(Hit hit, Ray ray) {
        var material = hit.material();

        double r = scene.ambientLight().r() * material.kdR();
        double g = scene.ambientLight().g() * material.kdG();
        double b = scene.ambientLight().b() * material.kdB();

        for (var light : scene.lights()) {
            var toLight = light.position().sub(hit.point());
            double distance = toLight.length();
            var lightDir = toLight.normalize();

            var shadowOrigin = hit.point();
            var shadowRay = new Ray(shadowOrigin, lightDir);

            if (isInShadow(hit, shadowRay, distance)) {
                continue;
            }

            double nDotL = Math.max(0.0, VectorMath.dot(hit.normal(), lightDir));

            double attenuation = 1.0 / (1.0 + distance);

            var dr = material.kdR() * nDotL;
            var dg = material.kdG() * nDotL;
            var db = material.kdB() * nDotL;

            if (nDotL > 0.0) {
                var viewDir = ray.direction().mul(-1.0).normalize();
                var halfDir = lightDir.sum(viewDir).normalize();

                double nDotH = Math.max(0.0, VectorMath.dot(hit.normal(), halfDir));
                double spec = Math.pow(nDotH, material.power());

                dr += material.ksR() * spec;
                dg += material.ksG() * spec;
                db += material.ksB() * spec;
            }

            r += attenuation * light.color().r() * dr;
            g += attenuation * light.color().g() * dg;
            b += attenuation * light.color().b() * db;
        }

        return new RgbColor(r, g, b);
    }

    private boolean isInShadow(Hit hit, Ray shadowRay, double lightDistance) {
        var shadowHit = findClosest(shadowRay);

        return shadowHit.isPresent() && shadowHit.get().t() < lightDistance;
    }

    public SceneModel scene() {
        return scene;
    }
}
