package com.mobeon.masp.util.executor;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.*;

/**
 * The <code>TimoutRetrier&lt;V&gt;</code> executes a <code>Callable&lt;V&gt;</code> with retries upon failure.
 * Failure is indicated by the <code>Callable</code> throwing a <code>RetryException</code> wrapping the
 * original exception. The maximum number of tries, and the maximum time span during which retries should occur
 * is specified upon creation. It is also possible to specify a total timeout, which will cancel the task when
 * expiring.
 *
 * @author mande
 */
public class TimeoutRetrier<V> implements Callable<V> {
    private static final ILogger log = ILoggerFactory.getILogger(TimeoutRetrier.class);

    /**
     * The task to perform
     */
    Callable<V> task;
    /**
     * The maximum number of times to try performing the task
     */
    private int tryLimit;
    /**
     * The maximum number of milliseconds to try performing the task upon failure
     */
    private int tryTimeLimit;
    /**
     * The maximum number of milliseconds to execute the task before it is cancelled
     */
    private int timeout;

    /**
     * Creates a new TimeoutRetrier
     * @param task the task to perform
     * @param tryLimit the maximum number of tries to perform
     * @param tryTimeLimit the maximum time in milliseconds during which retries should occur
     * @param timeout the maximum time in milliseconds before the task is cancelled
     */
    public TimeoutRetrier(Callable<V> task, int tryLimit, int tryTimeLimit, int timeout) {
        this.task = task;
        this.tryLimit = tryLimit;
        this.tryTimeLimit = tryTimeLimit;
        this.timeout = timeout;
        if (TimeoutRetrier.log.isDebugEnabled()) {
            TimeoutRetrier.log.debug(this.toString());
        }
    }

    /**
     * Executes the task
     * @return the result from the task
     * @throws java.util.concurrent.ExecutionException if the task threw an exception.
     * @throws java.util.concurrent.TimeoutException if the submitted timeout expires. Not thrown if timeout is 0.
     * @throws InterruptedException if the thread executing the task is interrupted. Not thrown if timeout is 0.
     */
    public V call() throws ExecutionException, TimeoutException, InterruptedException {
        ExecutorService executorService = ExecutorServiceManager.getInstance().getExecutorService(TimeoutRetrier.class);
        long startTime = System.currentTimeMillis();
        int tries = 0;
        while (true) {
            Future<V> future = null;
            try {
                future = executorService.submit(task);
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RetryException) {
                    RetryException retryException = (RetryException)e.getCause();
                    tries++;
                    if(TimeoutRetrier.log.isDebugEnabled()) TimeoutRetrier.log.debug("Call failed on try " + Integer.toString(tries));
                    if (!retry(tries, startTime)) {
                        // If no more retries are allowed, rethrow exception
                        if (TimeoutRetrier.log.isDebugEnabled()) TimeoutRetrier.log.debug("Rethrow original exception");
                        throw new ExecutionException(retryException.getCause());
                    }
                } else {
                    throw e;
                }
            } catch (TimeoutException e) {
                // Cancel task
                if (TimeoutRetrier.log.isDebugEnabled()) TimeoutRetrier.log.debug("Time out. Cancelling task");
                future.cancel(true);
                throw e;
            }
        }
    }

    /**
     * Checks the condition for retrying the call
     * @param tries maximum number of tries
     * @param startTime maximum time in milliseconds during which retries should occur
     * @return true, if retry should occur, otherwise false
     */
    private boolean retry(int tries, long startTime) {
        if (tries >= this.tryLimit) {
            TimeoutRetrier.log.debug("Number of tries exceeded.");
            return false;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        TimeoutRetrier.log.debug("Elapsed time: " + Long.toString(elapsedTime));
        if (elapsedTime > tryTimeLimit) {
            TimeoutRetrier.log.debug("Time limit exceeded.");
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder description = new StringBuilder(getClass().getSimpleName());
        description.append("(").append(task).append(", ").append(tryLimit).append(", ").append(tryTimeLimit);
        if (timeout > 0) {
            description.append(", ").append(timeout);
        }
        description.append(")");
        return description.toString();
    }

}
