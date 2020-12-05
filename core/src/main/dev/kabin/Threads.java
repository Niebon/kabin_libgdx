package dev.kabin;

import dev.kabin.components.Component;
import dev.kabin.utilities.shapes.RectInt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Threads {

    // A lock for order sensitive operations.
    public static final Object THREAD_LOCK = new Object();
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
        Component.registerEntityWhereabouts(GlobalData.getRootComponent());
        GlobalData.getRootComponent().clearData();
        System.out.println("init:");
        Component.loadNearbyData(GlobalData.getRootComponent(), GlobalData.currentCameraBounds);

        periodicBackgroundTasks = Executors.newSingleThreadScheduledExecutor(Thread::new);
        periodicBackgroundTasks.scheduleWithFixedDelay(Threads::handle, 0, 1, TimeUnit.SECONDS);
    }

    private static void handle() {
        synchronized (THREAD_LOCK) {
            // Load & unload data.
            Component.registerEntityWhereabouts(GlobalData.getRootComponent());
            System.out.println("handle");
            Component.clearUnusedData(GlobalData.getRootComponent(), GlobalData.currentCameraBounds);
            Component.loadNearbyData(GlobalData.getRootComponent(), GlobalData.currentCameraBounds);
        }
    }

}
