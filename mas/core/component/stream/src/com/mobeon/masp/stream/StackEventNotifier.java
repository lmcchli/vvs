/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.jni.AbstractCallbackReceiver;
import com.mobeon.masp.stream.jni.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that handles notifications of stack events. This class also
 * provides the possibility for a thread to wait for a specific event,
 * thus making the request synchronously.
 * <p>
 * Functionality:
 * For all event notifications:
 * <ul>
 * <li>Always dispatch the event to the event dispatcher.</li>
 * <li>If a thread is waiting for a specific event, notify that thread.</li>
 * </ul>
 * A thread can only wait for the following events:
 * <ul>
 * <li>play finished</li>
 * <li>record finished</li>
 * </ul>
 * The event <code>return from call</code> will always notify waiting threads.
 * <p>
 * Example of use:
 * <pre>
 *   public void myOp(Object requestId) {
 *      
 *       getEventNotifier().initCall(requestId);        
 *       try {
 *           getStack().myOp(requestId);
 *           
 *           // This will make the call synchronous 
 *           getEventNotifier().waitForCallToFinish(requestId);
 *       }
 *       catch (Throwable t) {
 *           logger.warn("play: Unexpected exception", t);
 *           getEventNotifier().abortCall(requestId);
 *           throw t;
 *       }
 *   }
 * </pre>
 */
public final class StackEventNotifier implements AbstractCallbackReceiver {
    private static final ILogger LOGGER = ILoggerFactory.getILogger(StackEventNotifier.class);
    
    /** Protects the condition-map. */
    private final Lock LOCK = new ReentrantLock();
    
    /** Contains condition-instances for all current requests. */
    private final Map<Object, Condition> conditions = 
        new HashMap<Object, Condition>();
    
    /**
     * Maximum time a caller is made to wait for a synchronous request 
     * (not used if the request is made asynchronously).
     */
    private long waitTimeout;
    
    /** 
     * This dispatcher should be notified about all events.
     */
    private IEventDispatcher dispatcher;
    
    /**
     * Creates a new notifier instance.
     * 
     * @param dispatcher Used to dispatch events to other components.
     * 
     * @throws IllegalArgumentException If <code>dispatcher</code> is 
     *                                  <code>null</code>.
     */
    public StackEventNotifier(IEventDispatcher dispatcher) {
        this(dispatcher, 
                StreamConfiguration.getInstance().getSyncCallMaxWaitTime());
    }

    /**
     * Creates a new notifier instance.
     * 
     * @param dispatcher Used to dispatch events to other components.
     * @param timeout    The maximum time to wait before waking callers
     *                   from a synchronous request.
     * 
     * @throws IllegalArgumentException If <code>dispatcher</code> is 
     *                                  <code>null</code> ot if 
     *                                  <code>timeout</code> is <= 0.
     */
    /* package */ StackEventNotifier(IEventDispatcher dispatcher, long timeout) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("The event dispatcher may not be null.");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("The timeout must be > 0.");
        }
        this.dispatcher = dispatcher;
        this.waitTimeout = timeout;
    }

    public void notify(Object requestId, Callback callback) {
        switch (callback.command) {
            case Callback.PLAY_COMMAND:
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Notify:PLAY");
                switch (callback.status) {
                    case Callback.OK:
                        playFinished(requestId, PlayFinishedEvent.CAUSE.PLAY_FINISHED, callback.data);
                        break;

                    case Callback.OK_CANCELLED:
                        playFinished(requestId, PlayFinishedEvent.CAUSE.PLAY_CANCELLED, callback.data);
                        break;

                    case Callback.OK_STOPPED:
                        playFinished(requestId, PlayFinishedEvent.CAUSE.PLAY_STOPPED, callback.data);
                        break;

                    case Callback.OK_JOINED:
                        playFinished(requestId, PlayFinishedEvent.CAUSE.STREAM_JOINED, callback.data);
                        break;

                    case Callback.OK_DELETED:
                        playFinished(requestId, PlayFinishedEvent.CAUSE.STREAM_DELETED, callback.data);
                        break;

                    case Callback.FAILED:
                    default:
                        playFailed(requestId, 0, "failed ...");
                        break;
                }
                break;

            default:
                LOGGER.error("Unhandled response!");
                break;
        }
    }
        
    /**
     * Sends the event "play finished". Note that the requestId might be 
     * an instance of the <code>MediaObjectPlaySequence</code>-class and
     * must in that case be handled separately.
     * 
     * @param requestId  Identifies the request.
     * @param cause      The operation has finished due to this cause.
     * @param cursor     Current cursor in milliseconds.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *         <code>null</code> or if <code>cause</code> is unknown.
     * 
     * @see MediaObjectPlaySequence
     */
    public void playFinished(Object requestId, PlayFinishedEvent.CAUSE cause, long cursor) {
        updateSessionLogData(requestId);
        if (requestId instanceof MediaObjectPlaySequence) {
            ((MediaObjectPlaySequence)requestId).requestFinished(
                    cause, cursor);
        }
        else {        
            PlayFinishedEvent event = new PlayFinishedEvent(requestId, cause, cursor);
            try {
                dispatcher.fireEvent(event);
            }
            catch (Throwable t) {
                LOGGER.warn("Failed to send event to event dispatcher", t);
            }
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("PLAY FINISHED!");
            
            // If the caller has not already returned by a call to
            // returnFromCall, the caller should always return
            // from the call when the play has finished.
            returnFromCall(requestId);
        }
    }
    
    /**
     * Sends the event "play finished". When this method is called, the 
     * requestId will never be an instance of the 
     * <code>MediaObjectPlaySequence</code>-class and thus, the event
     * will always be sent to the event dispatcher.
     * 
     * @param requestId  Identifies the request.
     * @param cause      The operation has finished due to this cause.
     * @param cursor     Current cursor in milliseconds.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *         <code>null</code> or if <code>cause</code> is unknown.
     *         
     * @see MediaObjectPlaySequence
     */
    /* package */ void playFinished(Object requestId, 
                                    int cause,
                                    long cursor) {
        updateSessionLogData(requestId);
        PlayFinishedEvent event = 
            new PlayFinishedEvent(requestId, cause, cursor);
        try {
            dispatcher.fireEvent(event);
        }
        catch (Throwable t) {
            LOGGER.warn("Failed to send event to event dispatcher", t);
        }

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("PLAY FINISHED!");
        
        // If the caller has not already returned by a call to
        // returnFromCall, the caller should always return
        // from the call when the play has finished.
        returnFromCall(requestId);
    }

    /**
     * Sends the event "record finished".
     * 
     * @param requestId  Identifies the request.
     * @param cause      The operation has finished due to this cause.
     * @param message    Cause description (always null)
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *         <code>null</code> or if <code>cause</code> is unknown.
     */
    public void recordFinished(Object requestId, int cause, String message) {
        updateSessionLogData(requestId);
        RecordFinishedEvent event = new RecordFinishedEvent(requestId, cause);
        try {
            dispatcher.fireEvent(event);
        }
        catch (Throwable t) {
            LOGGER.warn("Failed to send event to event dispatcher", t);
        }
        
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("RECORD FINISHED!");

        // If the caller has not already returned by a call to
        // returnFromCall, the caller should always return
        // from the call when the record has finished.
        returnFromCall(requestId);
    }
    
    /**
     * Sends the event "stream abandoned".
     * 
     * @param stream ...
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *         <code>null</code>.
     */
    public void streamAbandoned(IMediaStream stream) {
        updateSessionLogData(stream);
        StreamAbandonedEvent event = new StreamAbandonedEvent(stream);
        try {
            dispatcher.fireEvent(event);
        }
        catch (Throwable t) {
            LOGGER.warn("Failed to send event to event dispatcher", t);
        }
        
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("STREAM ABANDONED!");

        // After this event is sent an ongoing operation is cancelled
        // and another event is sent, which will signal any waiting caller.
    }

    public void recordStarted(Object requestId) {
        updateSessionLogData(requestId);
        if (requestId == null) {
            throw new IllegalArgumentException("requestId may not be null.");
        }
        // Not implemented yet
        dispatcher.fireEvent(new Event(){}); // XXX Event-klass?
    }

    /**
     * Sends a control token event.
     * 
     * @param digit    DTMF digit as defined in RFC 2833.
     * @param volume   Volume expressed in dBm0 after dropping the sign.
     * @param duration Duration expressed in timestamp units.
     * 
     * @throws IllegalArgumentException If <code>digit</code> could
     *         not be mapped to a DTMFToken.
     */
    public void control(int digit, int volume, int duration) {
        
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("CONTROL TOKEN ARRIVED: " + digit + ":" + volume +
                ":" + duration);
        
        try {
            ControlToken.DTMFToken token = ControlToken.toToken(digit);
            ControlTokenEvent event = new ControlTokenEvent(
                    new ControlToken(token, volume, duration));
            try {
                dispatcher.fireEvent(event);
            }
            catch (Throwable t) {
                LOGGER.warn("Failed to send event to event dispatcher", t);
            }
        }
        catch (IllegalArgumentException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("An unexpected DTMF token arrived: " + digit);
        }
    }

    /**
     * Sends the event "play failed". Note that the requestId might be 
     * an instance of the <code>MediaObjectPlaySequence</code>-class and
     * must in that case be handled separately.
     * 
     * @param requestId  Identifies the request.
     * @param cause      Cause of failure.
     * @param message    Cause description
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     * 
     * @see MediaObjectPlaySequence
     */
    public void playFailed(Object requestId, int cause, String message) {
        updateSessionLogData(requestId);
        // cause is ignored until clients needs to distinguish between
        // different causes of failure.

        if (requestId instanceof MediaObjectPlaySequence) {
            ((MediaObjectPlaySequence)requestId).requestFailed(message);
        }
        else {         
            PlayFailedEvent event = new PlayFailedEvent(requestId, message);
            try {
                dispatcher.fireEvent(event);
            }
            catch (Throwable t) {
                LOGGER.warn("Failed to send event to event dispatcher", t);
            }
    
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("PLAY FAILED!");
           
            // If the caller has not already returned by a call to
            // returnFromCall, the caller should always return
            // from the call if the play has failed.
            returnFromCall(requestId);
        }
    }

    /**
     * Sends the event "play failed". When this method is called, the 
     * requestId will never be an instance of the 
     * <code>MediaObjectPlaySequence</code>-class and thus, the event
     * will always be sent to the event dispatcher.
     * 
     * @param requestId  Identifies the request.
     * @param message    Cause description
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     * 
     * @see MediaObjectPlaySequence
     */
    public void playFailed(Object requestId, String message) {
        updateSessionLogData(requestId);
        PlayFailedEvent event = new PlayFailedEvent(requestId, message);
        try {
            dispatcher.fireEvent(event);
        }
        catch (Throwable t) {
            LOGGER.warn("Failed to send event to event dispatcher", t);
        }

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("PLAY FAILED!");
       
        // If the caller has not already returned by a call to
        // returnFromCall, the caller should always return
        // from the call if the play has failed.
        returnFromCall(requestId);
    }

    /**
     * Sends the event "record failed".
     * 
     * @param requestId  Identifies the request.
     * @param cause      Cause of failure.
     * @param message    Cause description
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    public void recordFailed(Object requestId, int cause, String message) {
        updateSessionLogData(requestId);
        RecordFailedEvent event =
            new RecordFailedEvent(requestId, cause, message);
        try {
            dispatcher.fireEvent(event);
        }
        catch (Throwable t) {
            LOGGER.warn("Failed to send event to event dispatcher", t);
        }

        if (message != null) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("RECORD FAILED: cause=" + cause +
                    ", message=" + message);
        }
        else {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("RECORD FAILED: cause=" + cause);
        }
       
        // If the caller has not already returned by a call to
        // returnFromCall, the caller should always return
        // from the call if the record has failed.
        returnFromCall(requestId);
    }
    
    /**
     * Tells that a specific caller should return. Note that this does
     * not necessarily mean that the processing is done. If some processing
     * is left, it will finish asyncronously.
     * <p>
     * If the caller has already returned, nothing happens.
     * 
     * @param requestId Identifies the request.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    public void returnFromCall(Object requestId) {
        updateSessionLogData(requestId);
        signalCaller(requestId);
    }

    /**
     * Waits for a specific request to finish. Note that the 
     * <code>initCall</code>-method must be called before this method.
     * 
     * @param requestId Identifies the request.
     * 
     * @return <code>true</code> if the wait ended because the thread was
     *         signalled to wake up. <code>false</code> if the wait ended
     *         because of an Interrupt or if the wait timed out.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    public boolean waitForCallToFinish(Object requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId may not be null.");
        }
        LOCK.lock();
        boolean wasSignalled = true;
        try {
            Condition c;
            while (wasSignalled && ((c = this.conditions.get(requestId)) != null)) {
                wasSignalled = c.await(waitTimeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            // XXX rätt hantering?
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Interrupted");
            this.conditions.remove(requestId);
            wasSignalled = false;
        } finally {
            LOCK.unlock();
        }
        return wasSignalled;
    }
    
    /**
     * Prepares the event notifier for a request. This method should
     * always be called before a call to the <code>waitForCallToFinish</code>-
     * method.
     * 
     * @param requestId Identifies the request.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    public void initCall(Object requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId may not be null.");
        }

        // Yes, this method is needed. Otherwise, if the method call
        // itself is fast enough, the returnFromCall-method will be
        // called before the waitForCall-method. In that case, no one
        // will wake up the waiting thread...
        LOCK.lock();
        try {
            Condition c = LOCK.newCondition();
            this.conditions.put(requestId, c);
        }
        finally {
            LOCK.unlock();
        }
    }
    
    /**
     * Informs the event notifier that a request has been aborted. This might
     * be because of an exception during the request. This method should
     * always be called if an exception occurs during a request.
     * 
     * @param requestId Identifies the request.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    public void abortCall(Object requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId may not be null.");
        }
        LOCK.lock();
        try {
            if (this.conditions.containsKey(requestId)) {
                this.conditions.remove(requestId);
            }
        }
        finally {
            LOCK.unlock();
        }
    }

    /**
     * Signals a calling thread that the event the thread is 
     * waiting for has occured.
     * 
     * @param requestId Identifies the request.
     * 
     * @throws IllegalArgumentException If <code>requestId</code> is 
     *                                  <code>null</code>.
     */
    private void signalCaller(Object requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId may not be null.");
        }
        LOCK.lock();
        try {
            Condition c = this.conditions.get(requestId);
            // Is there a thread waiting for an event from this call?
            if (c != null) {
                this.conditions.remove(requestId);
                c.signal();
            }
            else {
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Condition already removed");
            }
        } 
        finally {
            LOCK.unlock();
        }
    }
    
    /**
     * Gives the current number of stored conditions. This method is
     * added to simplify tests for this class.
     *  
     * @return Current number of stored conditions.
     */
    /* package */ int getNumberOfConditions() {
        LOCK.lock();
        try {
            return conditions.size();
        }
        finally {
            LOCK.unlock();
        }
    }

    void updateSessionLogData(Object callId) {
        if (callId == null) {
            LOGGER.debug("Could not update session log data Call ID is null!");
            return;
        }
        ISession session = CallSessionMapper.getInstance().getSession(callId);
        if (session == null) {
            LOGGER.debug("Could not update session log data Session is null!");
            return;
        }
        session.registerSessionInLogger();
    }

    void updateSessionLogData(IMediaStream stream) {
        if (stream == null) {
            LOGGER.debug("Could not update session log data Media Stream is null!");
            return;
        }
        ISession session = CallSessionMapper.getInstance().getSession(stream);
        if (session == null) {
            LOGGER.debug("Could not update session log data Session is null!");
            return;
        }
        session.registerSessionInLogger();
    }
}
