/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import java.util.TimerTask;

/**
 * TODO: Drop 4! Document.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class RegistrationTimerTask extends TimerTask {
    private final SspInstance sspInstance;
    private final Type type;

    public enum Type {
        REGISTER_RETRY, REGISTER_BACKOFF
    }

    public RegistrationTimerTask(SspInstance sspInstance, Type type) {
        this.sspInstance = sspInstance;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void run() {
        sspInstance.handleTimeout(type);
    }
}

