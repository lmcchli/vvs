/* **********************************************************************
 * Copyright (c) Abcxyz 2009. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.HashMap;

/**
 * utility class for keeping NTF events handlers based on event class
 *
 * @author lmchuzh
 *
 */
public class NtfEventHandlerRegistry
{

    private static HashMap<String, NtfRetryHandling> handlers = new HashMap<String, NtfRetryHandling>();

    private static NtfRetryHandling defaultHandler;

    private static HashMap<String, EventSentListener> listeners = new HashMap<String, EventSentListener>();

    private static EventSentListener defaultListener;

    private static HashMap<String, NtfEventReceiver> receivers = new HashMap<String, NtfEventReceiver>();
    private static NtfEventReceiver defaultReceiver;


    public static void registerDefaultEventReceiver(NtfEventReceiver receiver) {
        defaultReceiver = receiver;
    }

    /**
     *
     * @param eventServiceTypeKey key listed in {@link NtfEventTypes}
     * @param receiver
     */
    public static void registerEventReceiver(String eventServiceTypeKey, NtfEventReceiver receiver) {
    	receivers.put(eventServiceTypeKey, receiver);
    }

    public static NtfEventReceiver getNtfEventReceiver(String eventServiceTypeKey) {
    	NtfEventReceiver receiver = receivers.get(eventServiceTypeKey);
    	if (receiver == null) {
    		return defaultReceiver;
    	}
        return receiver;
    }

    public static NtfEventReceiver getNtfEventReceiver() {
    	return defaultReceiver;
    }


    public static void registerDefaultHandler(NtfRetryHandling handler) {
        defaultHandler = handler;
    }
    /**
     *
     * @param ntfEventClass
     * @param handler
     */
    public static void registerEventHandler(NtfRetryHandling handler) {
    	String eventServiceTypeKey = handler.getEventServiceName();
        handlers.put(eventServiceTypeKey, handler);
    }

    public static NtfRetryHandling getEventHandler() {
        return defaultHandler;
    }


    public static NtfRetryHandling getEventHandler(String eventServiceTypeKey) {
        NtfRetryHandling handler = handlers.get(eventServiceTypeKey);
        if (handler == null) {
            handler = defaultHandler;
        }

        return handler;
    }

    public static void registerDefaultListener(EventSentListener listener) {
        defaultListener = listener;
    }
    /**
     *
     * @param ntfEventClass
     * @param handler
     */
    public static void registerEventSentListener(String eventServiceTypeKey, EventSentListener listener) {
        listeners.put(eventServiceTypeKey, listener);
    }

    public static EventSentListener getEventSentListener() {
        return defaultListener;
    }

    public static EventSentListener getEventSentListener(String eventServiceTypeKey) {
        EventSentListener listener = listeners.get(eventServiceTypeKey);
        if (listener == null) {
            listener = defaultListener;
        }

        return listener;
    }
}
