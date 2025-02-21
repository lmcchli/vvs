package com.mobeon.masp.stream.jni;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Map;
import java.util.HashMap;

public final class CallbackDispatcher {
    private static final ILogger logger =
            ILoggerFactory.getILogger(CallbackDispatcher.class);
    private static CallbackDispatcher theSingletonInstance = null;
    static int requestCounter = 0;
    private CallbackTranceiver[] callbackTranceivers;
    private final Map<Integer, Request> pendingCallbacks = new HashMap<Integer, Request>();

    public static CallbackDispatcher getSingleton() {
        if (theSingletonInstance == null) {
            theSingletonInstance = new CallbackDispatcher();
        }
        return theSingletonInstance;
    }

    public void initialize(AbstractStreamHandling streamHandling, int nOfOutputProcessors) {
        callbackTranceivers = new CallbackTranceiver[nOfOutputProcessors];
        for (int index = 0; index < nOfOutputProcessors; index++) {
            callbackTranceivers[index] = new CallbackTranceiver(streamHandling, index);
        }
    }

    public int getDispatchQueueCount() {
        return callbackTranceivers.length;
    }

    protected void setRequestIdCounter(int requestId) {
        requestCounter = requestId;
    }

    public int addRequest(Object requestIdObject, AbstractCallbackReceiver callbackReceiver) {
        synchronized (pendingCallbacks) {
            int requestId = requestCounter++;           // Counter wraps here (16 bits)
            boolean inserted = false;
            while (!inserted) {
                    if (!pendingCallbacks.containsKey(requestId)) {
                        Request request = new Request(requestId,  requestIdObject, callbackReceiver);
                        pendingCallbacks.put(requestId, request);
                        inserted = true;
                    } else {
                        requestId = requestCounter++; 
                        logger.debug(" Already Present " );
                    }
                    if (requestCounter > 0xffff) requestCounter = 0;
            }
            return requestId;
        }
    }

   
    public void notifyCaller(Callback callback) {
        Request request;

        if (logger.isDebugEnabled()) {
            logger.debug("Handling callback notification: Mario");
            logger.debug("  RequestID: " + callback.requestId);
            logger.debug("  Command:   " + callback.command);
            logger.debug("  Status:    " + callback.status);
        }
        synchronized(pendingCallbacks) {
            // TODO: make sure that a request always is removed!
            // There should always be a response to a request.
            // If not we should have a timeout/purge procedure for this.
            switch (callback.command) {
                case Callback.DTMF_COMMAND:
                    logger.warn("DTMF callback is not properly tested");
                    // In this case we have a request which is in progress
                    // DTMF is an event wich can occur under the time when
                    // a command/request is processed (typically during
                    // PLAY/RECORD).
                    request = pendingCallbacks.get(callback.requestId);
                    break;

                default:
                    request = pendingCallbacks.remove(callback.requestId);
                    break;
            }
        }

        if (request != null) {
            request.callbackReceiver.notify(request.sessionId, callback);
        }  else {
            logger.error("No pending request for callback.requestId");
        }

    }

    class Request {
        Object sessionId;
        AbstractCallbackReceiver callbackReceiver;
        int id;

        Request(int id, Object requestId, AbstractCallbackReceiver callbackReceiver) {
            this.id = id;
            this.sessionId = requestId;
            this.callbackReceiver = callbackReceiver;
        }

        public int getId() {
            return id;
        }
    }
}

