package dev.kabin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Threads {

    // A lock for order sensitive operations.
    private static final Object THREAD_LOCK = new Object();
    private static final Logger LOGGER = Logger.getLogger(Threads.class.getName());
    private static ScheduledExecutorService periodicBackgroundTasks;

    public static void init() {
        synchronized (THREAD_LOCK) {
            if (periodicBackgroundTasks != null) {
                try {
                    if (periodicBackgroundTasks.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOGGER.warning(() -> "Terminated periodic background tasks successfully.");
                    }
                } catch (InterruptedException e) {
                    LOGGER.warning(() -> "Periodic background tasks could not be shut down.");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            GlobalData.getWorldState().clearData();
            handle();

            periodicBackgroundTasks = Executors.newSingleThreadScheduledExecutor(Thread::new);
            periodicBackgroundTasks.scheduleWithFixedDelay(Threads::handle, 0, 1, TimeUnit.SECONDS);
        }
    }

    private static void handle() {
        synchronized (THREAD_LOCK) {
            // Load & unload data.
            GlobalData.getWorldState().registerEntityWhereabouts();
            GlobalData.getWorldState().clearUnusedData(GlobalData.currentCameraBounds);
            GlobalData.getWorldState().loadNearbyData(GlobalData.currentCameraBounds);
        }
    }

    /**
     * @param r execute the given runnable in such a way that it goes in between periodic background tasks.
     */
    public static void synchronize(Runnable r) {
        synchronized (THREAD_LOCK) {
            r.run();
        }
    }

}
