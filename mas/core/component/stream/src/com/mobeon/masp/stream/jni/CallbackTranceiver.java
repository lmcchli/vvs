package com.mobeon.masp.stream.jni;

import com.mobeon.masp.util.executor.ExecutorServiceManager;

import java.util.concurrent.ExecutorService;

public class CallbackTranceiver implements Runnable {
    AbstractStreamHandling streamHandling;
    int inputQueue;
    boolean isRunning = false;

    CallbackTranceiver(AbstractStreamHandling streamHandling, int queueId) {
        this.streamHandling = streamHandling;
        inputQueue = queueId;
        ExecutorService service =
                ExecutorServiceManager.getInstance().getExecutorService(CallbackTranceiver.class);
        service.execute(this);
    }

    public void run() {
        isRunning = true;

        while (isRunning) {
            Callback callback = streamHandling.getCallback(inputQueue);
            if (callback.isOk) {
                CallbackDispatcher.getSingleton().notifyCaller(callback);
            } else {
                isRunning = false;
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
