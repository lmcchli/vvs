package com.mobeon.masp.stream.jni;

import com.mobeon.masp.stream.StreamConfiguration;

public final class NativeStreamHandling implements AbstractStreamHandling  {
    public static void initialize() {
        StreamConfiguration config = StreamConfiguration.getInstance();
        initialize(config.getOutputProcessors(), config.getInputProcessors());
        // TODO: prehaps this should be a singleton?
        CallbackDispatcher.getSingleton().initialize(new NativeStreamHandling(),
                config.getOutputProcessors());
    }

    public Callback getCallback(int queueId) {
        long[] callbackData = new long[2];
        getNativeCallback(queueId, callbackData);
        return new Callback(callbackData);
    }

    /**
     * Initializes the input and output processors.
     * The initialization is more or less creating the processors.
     * @param outputProcessors number of output processors.
     * @param inputProcessors number of input processors.
     */
    private static native void initialize(int outputProcessors, int inputProcessors);
    public static native void getNativeCallback(int queueId, long[] callback);
    public static native void postDummyCallback(int queueId, int requestId);
}
