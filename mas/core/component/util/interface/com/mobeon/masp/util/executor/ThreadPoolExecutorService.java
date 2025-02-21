package com.mobeon.masp.util.executor;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolExecutorService extends ThreadPoolExecutor {
    private static ILogger logger = ILoggerFactory.getILogger(ThreadPoolExecutorService.class);
    private String name;

    public ThreadPoolExecutorService(int corePoolSize, int maximumPoolSize, String name) {
        super(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ExecutorServiceManager.ContextThreadFactory());
        this.name = name;
        logger.debug("Created a ThreadPoolExecutorService: [" + name + "]");
    }

    public void execute(Runnable command) {
        if (this.getActiveCount() == this.getCorePoolSize()) {
            logger.warn("Number of active threads for thread pool " + name + " has reached the core pool size: " + this.getActiveCount() + ". Queue size is: " + getQueue().size());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Adding job to thread pool[" + name + "]");
            logger.debug("Active threads:    " + this.getActiveCount());
            logger.debug("Pool size:         " + this.getPoolSize());
            logger.debug("Pending jobs:      " + this.getQueue().size());
            logger.debug("Maximum pool size: " + this.getMaximumPoolSize());
        }
        super.execute(command);
    }
}
