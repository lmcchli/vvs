package com.mobeon.masp.stream.jni;

public interface AbstractCallbackReceiver {
    public void notify(Object requestId, Callback callback);
}
