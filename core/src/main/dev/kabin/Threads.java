package dev.kabin;

import dev.kabin.components.Component;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.shapes.RectInt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Threads {

    private static final Logger LOGGER = Logger.getLogger(Threads.class.getName());
    private static final RectInt cameraRectangle = RectInt.centeredAt(0, 0, GlobalData.artWidth, GlobalData.artHeight);
    private static ScheduledExecutorService periodicBackgroundTasks;

    public static void init() {
        if (periodicBackgroundTasks != null) {
            try {
                periodicBackgroundTasks.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.warning(() -> "Periodic background tasks could not be shut down.");
                e.printStackTrace();
                System.exit(1);
            }
        }
        periodicBackgroundTasks = Executors.newSingleThreadScheduledExecutor(Thread::new);
        periodicBackgroundTasks.scheduleWithFixedDelay(Threads::handle, 0, 1, TimeUnit.SECONDS);
    }

    private static void handle() {
        // Load & unload data.
        Component.registerEntityWhereabouts(GlobalData.getRootComponent());
        Component.clearUnusedData(GlobalData.currentCameraBounds);
        Component.loadNearbyData(GlobalData.currentCameraBounds);
    }

}
