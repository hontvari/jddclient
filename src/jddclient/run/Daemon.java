package jddclient.run;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jddclient.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is not thread safe, if it is actually used ({@link #start} is
 * called), then it must be synchronized externally.
 */
public class Daemon {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private int periodInSeconds = 600;

    private Client client;

    private ScheduledThreadPoolExecutor executor;

    public void start() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(new Task(), 2, periodInSeconds,
                TimeUnit.SECONDS);
        logger.info("Scheduler started");
    }

    public void stop() {
        logger.info("Stopping scheduler...");
        executor.shutdownNow();
        try {
            boolean completed = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!completed) {
                logger.warn("Executor couldn't be stopped in time");
            } else {
                logger.info("Scheduler stopped");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    private class Task implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(Task.class);

        @Override
        public void run() {
            try {
                client.update();
            } catch (Throwable e) {
                logger.error("Unexcpected exception in daemon task", e);
            }
        }

    }

    /**
     * @category GETSET
     */
    public void setPeriod(String period) {
        int supplied = Integer.valueOf(period);
        int min = logger.isDebugEnabled() ? 1 : 10;
        if (supplied < min) {
            logger.debug("Configured period is dangerously short, using " + min + " instead");
            this.periodInSeconds = min;
            return;
        }
        this.periodInSeconds = supplied;
    }

    /**
     * @category GETSET
     */
    public void setClient(Client client) {
        this.client = client;
    }

}
