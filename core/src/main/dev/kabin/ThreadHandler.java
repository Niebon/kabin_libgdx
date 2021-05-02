package dev.kabin;

import dev.kabin.components.WorldRepresentation;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.fp.BooleanSupplier;
import dev.kabin.util.shapes.primitive.RectInt;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ThreadHandler {

    // A lock for order sensitive operations.
    private final Object threadLock = new Object();
    private final Logger logger = Logger.getLogger(ThreadHandler.class.getName());
    private final Supplier<WorldRepresentation<?, ?>> worldRepresentationSupplier;
    private final Supplier<RectInt> camNbd;
    private final Supplier<DeveloperUI> developerUISupplier;
    private final BooleanSupplier devMod;
    private ScheduledExecutorService periodicBackgroundTasks;

    public ThreadHandler(Supplier<WorldRepresentation<?, ?>> worldRepresentationSupplier,
                         Supplier<RectInt> camNbd,
                         Supplier<DeveloperUI> developerUISupplier,
                         BooleanSupplier devMod) {
        this.worldRepresentationSupplier = worldRepresentationSupplier;
        this.camNbd = camNbd;
        this.developerUISupplier = developerUISupplier;
        this.devMod = devMod;
    }

    public void reload() {
        synchronized (threadLock) {
            if (periodicBackgroundTasks != null) {
                try {
                    if (periodicBackgroundTasks.awaitTermination(10, TimeUnit.SECONDS)) {
                        logger.warning(() -> "Terminated periodic background tasks successfully.");
                    }
                } catch (InterruptedException e) {
                    logger.warning(() -> "Periodic background tasks could not be shut down.");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Optional.ofNullable(worldRepresentationSupplier.get()).ifPresent(WorldRepresentation::clearData);
            handle();

            periodicBackgroundTasks = Executors.newSingleThreadScheduledExecutor(Thread::new);
            periodicBackgroundTasks.scheduleWithFixedDelay(this::handle, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void handle() {
        synchronized (threadLock) {
            try {
                // Load & unload data.
                final WorldRepresentation<?, ?> worldRepresentation = worldRepresentationSupplier.get();
                if (worldRepresentation == null) return;

                worldRepresentation.registerEntityWhereabouts(camNbd.get());
                worldRepresentation.clearUnusedData(camNbd.get());
                worldRepresentation.loadNearbyData(camNbd.get());
                worldRepresentation.sortAllLayers();

                // Save dev session if applicable.
                if (devMod.isTrue()) {
                    GlobalData.saveDevSession(developerUISupplier.get(), devMod.isTrue());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @param r execute the given runnable in such a way that it goes in between periodic background tasks.
     */
    public void synchronize(Runnable r) {
        synchronized (threadLock) {
            r.run();
        }
    }

}
