package com.mobeon.common.util.executor;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorService extends ThreadPoolExecutor {
    private static ILogger logger = ILoggerFactory.getILogger(ThreadPoolExecutorService.class);
    private String name;
    private int peakActivethread=0;

    public ThreadPoolExecutorService(int corePoolSize, int maximumPoolSize, String name) {
        super(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ExecutorServiceManager.ContextThreadFactory());
        this.name = name;
        peakActivethread=0;
        logger.debug("Created a ThreadPoolExecutorService: [" + name + "]");
    }

    public void execute(Runnable command) {
        if (this.getActiveCount() == this.getCorePoolSize())
            logger.warn("Number of active threads for thread pool " + name + " has reached the core pool size: " + this.getActiveCount());

        if(peakActivethread<this.getActiveCount())
        {
        	peakActivethread=this.getActiveCount();
        }

        if (logger.isDebugEnabled()) {

        	logger.debug("Thread pool: "+name+
                	", Active threads: "+ this.getActiveCount()+
                	", Peak Active threads: "+ peakActivethread+
                	", Pending jobs: " + this.getQueue().size()+
                	", Pool size: " + this.getPoolSize()+
                	", Maximum pool size: " + this.getMaximumPoolSize()+
                	", Largest pool size: " + this.getLargestPoolSize()+
                	", Completed Task Count: " + this.getCompletedTaskCount()+
                	", Task Count: " + this.getTaskCount());
        }
        super.execute(command);
    }
}
