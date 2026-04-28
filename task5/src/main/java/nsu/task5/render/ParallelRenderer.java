package nsu.task5.render;

import nsu.task5.model.RayCamera;
import nsu.task5.model.RgbColor;
import nsu.task5.model.SceneModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelRenderer implements AutoCloseable {
    private final ExecutorService executor;
    private final PrimaryRayBuilder rayBuilder;

    public ParallelRenderer() {
        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.rayBuilder = new PrimaryRayBuilder();
    }

    public RenderResult render(
            SceneModel scene,
            RenderSettings settings,
            RayCamera camera,
            int width,
            int height,
            ProgressCallback progressCallback,
            CancellationToken cancellationToken
    ) throws InterruptedException {
        RgbColor[] buffer = new RgbColor[width * height];
        RayTracer rayTracer = new RayTracer(scene, settings);
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);
        List<Future<Integer>> futures = new ArrayList<>();
        AtomicBoolean failed = new AtomicBoolean(false);

        for (int y = 0; y < height; y++) {
            final int row = y;
            futures.add(completionService.submit(renderRowTask(
                    row,
                    width,
                    height,
                    camera,
                    settings,
                    rayTracer,
                    buffer,
                    cancellationToken,
                    failed
            )));
        }

        int completedRows = 0;
        long lastProgressNanos = 0;
        try {
            while (completedRows < height) {
                if (cancellationToken.isCancelled()) {
                    cancelFutures(futures);
                    throw new InterruptedException("Render cancelled.");
                }

                Future<Integer> completed = completionService.take();
                completed.get();
                completedRows++;

                long now = System.nanoTime();
                if (now - lastProgressNanos > 50_000_000L || completedRows == height) {
                    progressCallback.update(completedRows, height);
                    lastProgressNanos = now;
                }
            }
        } catch (ExecutionException e) {
            failed.set(true);
            cancelFutures(futures);
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("Render worker failed: " + cause.getMessage(), cause);
        }

        return new RenderResult(width, height, buffer, settings.copy());
    }

    private Callable<Integer> renderRowTask(
            int row,
            int width,
            int height,
            RayCamera camera,
            RenderSettings settings,
            RayTracer rayTracer,
            RgbColor[] buffer,
            CancellationToken cancellationToken,
            AtomicBoolean failed
    ) {
        return () -> {
            if (failed.get() || cancellationToken.isCancelled()) {
                throw new InterruptedException("Render cancelled.");
            }

            int offset = row * width;
            for (int x = 0; x < width; x++) {
                if (cancellationToken.isCancelled()) {
                    throw new InterruptedException("Render cancelled.");
                }

                Ray ray = rayBuilder.buildPrimaryRay(x, row, width, height, camera, settings);
                buffer[offset + x] = rayTracer.trace(ray, settings.depth());
            }
            return row;
        };
    }

    private void cancelFutures(List<Future<Integer>> futures) {
        for (Future<Integer> future : futures) {
            future.cancel(true);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void update(int completed, int total);
    }

    @FunctionalInterface
    public interface CancellationToken {
        boolean isCancelled();
    }
}
