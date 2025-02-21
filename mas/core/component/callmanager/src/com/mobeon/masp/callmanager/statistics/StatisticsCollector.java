/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.statistics;

import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.DroppedPacketsEvent;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.ProxiedEvent;
import com.mobeon.masp.callmanager.events.ProxyingEvent;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is responsible for collecting statistics and report it to O&M.
 * It implements the {@link CallEventListener} interface for this purpose.
 * At construction, the StatisticsCollector registers itself as a
 * {@link CallEventListener} in {@link CMUtils}. This is done to be able to
 * retrieve all call related events that occur. The events are then used to
 * collect statistics.
 * All events are stored in a queue and processed by threads from a pool.
 * <p>
 * The collected statistics is reported to the
 * {@link com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo}
 * retrieved using
 * {@link com.mobeon.masp.callmanager.CMUtils#getServiceEnablerInfo()}.
 * <p>
 * See {@link #collectStatistics()} for details on how statistics is collected.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class StatisticsCollector implements CallEventListener {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final ConcurrentLinkedQueue<Event> eventQueue =
            new ConcurrentLinkedQueue<Event>();

    private final ServiceEnablerInfo seInfo =
            CMUtils.getInstance().getServiceEnablerInfo();

    public StatisticsCollector() {
        CMUtils.getInstance().addCallEventListener(this);
    }

    public void processCallEvent(Event event) {
        // Add the event to the event queue and retrieve a new thread from a
        // thread pool to process the event.
        eventQueue.add(event);
        ExecutorServiceManager.getInstance().
                getExecutorService(StatisticsCollector.class).
                execute(new EventProcessing());

    }


    //======================= Private methods and classes ======================

    /**
     * Collects statistics based on received events.
     * <p>
     * The following methods are used to collect statistics:
     * <ul>
     * <li> For {@link ConnectedEvent}: <br>
     * {@link #collectStatisticsFromConnectedEvent(ConnectedEvent)}
     * </li>
     * <li> For {@link DisconnectedEvent}: <br>
     * {@link #collectStatisticsFromDisconnectedEvent(DisconnectedEvent)}
     * </li>
     * <li> For {@link ErrorEvent}: <br>
     * {@link #collectStatisticsFromErrorEvent(ErrorEvent)}
     * </li>
     * <li> For {@link FailedEvent}: <br>
     * {@link #collectStatisticsFromFailedEvent(FailedEvent)}
     * </li>
     * <li> For {@link DroppedPacketsEvent}: <br>
     * {@link #collectStatisticsFromDroppedPacketsEvent(DroppedPacketsEvent)}
     * </li>
     * <li> For {@link StatisticsEvent}: <br>
     * {@link #collectStatisticsFromStatisticsEvent(StatisticsEvent)}
     * </li>
     * <li> For {@link ProxyingEvent}: <br>
     * {@link #collectStatisticsFromProxyingEvent(ProxyingEvent)}
     * </li>
     * <li> For {@link ProxiedEvent}: <br>
     * {@link #collectStatisticsFromProxiedEvent(ProxiedEvent)}
     * </li>
     * </ul>
     */
    private void collectStatistics() {
        Event event = eventQueue.poll();

        if (event instanceof FailedEvent) {
            collectStatisticsFromFailedEvent((FailedEvent)event);

        } else if (event instanceof ErrorEvent) {
            collectStatisticsFromErrorEvent((ErrorEvent)event);

        } else if (event instanceof DisconnectedEvent) {
            collectStatisticsFromDisconnectedEvent((DisconnectedEvent)event);

        } else if (event instanceof ConnectedEvent) {
            collectStatisticsFromConnectedEvent((ConnectedEvent)event);

        } else if (event instanceof DroppedPacketsEvent) {
            collectStatisticsFromDroppedPacketsEvent((DroppedPacketsEvent)event);

        } else if (event instanceof StatisticsEvent) {
            collectStatisticsFromStatisticsEvent((StatisticsEvent)event);

        } else if (event instanceof ProxyingEvent) {
            collectStatisticsFromProxyingEvent((ProxyingEvent)event);

        } else if (event instanceof ProxiedEvent) {
            collectStatisticsFromProxiedEvent((ProxiedEvent)event);
        }
    }

    /**
     * Collects statistics from a {@link StatisticsEvent}.
     * <p>
     * The amount of current calls is incremented by one. A StatisticsEvent is
     * generated as soon as a new outbound call is created or as soon as a new
     * SIP INVITE request is received.
     *
     * @param event
     */
    private void collectStatisticsFromStatisticsEvent(StatisticsEvent event) {
        if (log.isDebugEnabled())
            log.debug("Incrementing currentConnections for " + event);
        seInfo.incrementCurrentConnections(event.getCallType(), event.getCallDirection());
    }

    /**
     * Collects statistics from a {@link DroppedPacketsEvent}.
     * <p>
     * The amount for dropped packets is incremented with the amount given in
     * the event.
     * @param event
     */
    private void collectStatisticsFromDroppedPacketsEvent(
            DroppedPacketsEvent event) {

        Call call = event.getCall();

        // Determine O&M call type: voice/video/voice_video_unknown
        CallType callType = getOMCallType(call.getCallType());

        // Determine O&M direction: inbound/outbound
        CallDirection direction = getOMCallDirection(call);

        // Increment statistics for dropped entities
        seInfo.incrementNumberOfConnections(
                callType, CallResult.DROPPEDENTITIES, direction,
                event.getDroppedPackets());
    }

    /**
     * Collects statistics from a {@link ConnectedEvent}.
     * <p>
     * The following statistics is collected from the event:
     * <ul>
     * <li>
     * The number of current connections for unknown call type is decremented.
     * </li>
     * <li>
     * The number of current connections for the current call type is incremented.
     * </li>
     * <li>
     * Statistics is incremented for {@link CallResult.CONNECTED}.
     * </li>
     * </ul>
     * @param event
     */
    private void collectStatisticsFromConnectedEvent(ConnectedEvent event) {

        Call call = event.getCall();

        // Determine O&M call type: voice/video/voice_video_unknown
        CallType callType = getOMCallType(call.getCallType());

        // Determine O&M direction: inbound/outbound
        CallDirection direction = getOMCallDirection(call);

        // When the call was created, the number of current calls was incremented
        // with an unknown call, since the call type was not known. Now we know the
        // call type, decrement the unknown counter and increment the known type.
        if (log.isDebugEnabled())
            log.debug("Decrementing currentConnections for " + CallType.VOICE_VIDEO_UNKNOWN +
                    ", direction " + direction + ". Incrementing counter for " + callType);
        seInfo.decrementCurrentConnections(CallType.VOICE_VIDEO_UNKNOWN, direction);
        seInfo.incrementCurrentConnections(callType, direction);

        // Increment statistics for connected calls
        seInfo.incrementNumberOfConnections(
                callType, CallResult.CONNECTED, direction);
    }

    /**
     * Collects statistics from a {@link DisconnectedEvent}.
     * <p>
     * The disconnected event contains information on whether the call already
     * was disconnected when this event was generated. No statistics is
     * collected if the call already was disconnected.
     * <p>
     * The following statistics is collected from the event (if the call was
     * not already disconnected):
     * <ul>
     * <li>
     * Depending on the reason of the failure, statistics is incremented for
     * either {@link CallResult.ABANDONED},
     * {@link CallResult.FARENDDISCONNECTED}
     * or {@link CallResult.NEARENDDISCONNECTED}.
     * </li>
     * <li>
     * The number of current connections is decremented.
     * If the call type was known when the event was generated, the counter
     * is decremented for that call type. Otherwise the counter for unknown
     * type is decremented.
     * </li>
     * </ul>
     * @param event
     */
    private void collectStatisticsFromDisconnectedEvent(DisconnectedEvent event) {
        if (!event.isAlreadyDisconnected()) {
            Call call = event.getCall();

            // Determine O&M call type: voice/video/voice_video_unknown
            CallType callType = getOMCallType(call.getCallType());

            // Determine O&M direction: inbound/outbound
            CallDirection direction = getOMCallDirection(call);

            // Determine the reason for disconnect: far end / near end / abandoned
            CallResult result = getOMCallResultForDisconnectReason(event.getReason());

            // Increment statistics for disconnected calls if call not already
            // disconnected
            seInfo.incrementNumberOfConnections(callType, result, direction);

            if (((CallImpl)call).isConnected()) {
                // Also decrement current connections since the call is disconnected
                seInfo.decrementCurrentConnections(callType, direction);
            } else {
                // Call is disconnected before is was connected. Decrease the counter
                // for unknown type, since the counter for the actual call type has
                // not yet been increased.
                seInfo.decrementCurrentConnections(CallType.VOICE_VIDEO_UNKNOWN, direction);
            }
        }
    }

    /**
     * Collects statistics from a {@link ErrorEvent}.
     * <p>
     * A call is incremented as new when it has been connected. An
     * {@link ErrorEvent} is sent if a serious error occurrs either before the
     * call has been connected or after.
     * <p>
     * The following statistics is collected from the event:
     * <ul>
     * <li>
     * If the call was not connected, the statistics is incremented
     * for {@link CallResult.FAILED}, and the current connections counter
     * for unknown call type is decremented.
     * </li>
     * <li>
     * If the call was connected, statistics is incremented for
     * {@link CallResult.NEARENDDISCONNECTED}, and the current connections
     * counter for the call type is decremented.
     * </li>
     * <li>
     * Also, statistics is incremented for {@link CallResult.ERROR}.
     * </li>
     * </ul>
     * @param errorEvent
     */
    private void collectStatisticsFromErrorEvent(ErrorEvent errorEvent) {

        Call call = errorEvent.getCall();

        // Determine O&M call type: voice/video/voice_video_unknown
        CallType callType = CallType.VOICE_VIDEO_UNKNOWN;
        boolean callIsConnected = false;
        if (call != null) {
            callType = getOMCallType(call.getCallType());
            callIsConnected = ((CallImpl)call).isConnected();
        }

        // Determine O&M direction: inbound/outbound
        CallDirection direction = getOMCallDirection(errorEvent.getDirection());

        if (!errorEvent.isAlreadyDisconnected()) {
            if ((callIsConnected)) {
                // Increment statistics for the call disconnection
                seInfo.incrementNumberOfConnections(
                        callType, CallResult.NEARENDDISCONNECTED, direction);

                // Also decrement current connections since the call failed
                seInfo.decrementCurrentConnections(callType, direction);
            } else {
                // Increment statistics for the call rejection
                seInfo.incrementNumberOfConnections(
                        callType, CallResult.FAILED, direction);

                // The call has not yet been connected, so the counter for the
                // actual call type is not updated. Decrease only the counter
                // for unknown type.
                seInfo.decrementCurrentConnections(CallType.VOICE_VIDEO_UNKNOWN, direction);
            }
        }

        // Increment statistics for the error itself
        seInfo.incrementNumberOfConnections(callType, CallResult.ERROR, direction);
    }

    /**
     * Collects statistics from a {@link FailedEvent}.
     * <p>
     * A call is incremented as new when it has been connected. A
     * {@link FailedEvent} is sent if the call could not be connected, for
     * example due to call rejection.
     * <p>
     * The following statistics is collected from the event:
     * <ul>
     * <li>
     * Depending on the reason of the failure, statistics is incremented for
     * either {@link CallResult.FAILED} or {@link CallResult.ABANDONED_REJECTED}.
     * </li>
     * <li>
     * The number of current connections is decremented. The call type is not
     * known at this time, since the call is not connected yet. The counter for
     * unknown call type is decremented.
     * </li>
     * </ul>
     * @param failedEvent
     */
    private void collectStatisticsFromFailedEvent(FailedEvent failedEvent) {
        // Determine O&M call type: voice/video/voice_video_unknown
        CallType callType = CallType.VOICE_VIDEO_UNKNOWN;
        Call call = failedEvent.getCall();
        if (call != null) {
            callType = getOMCallType(call.getCallType());
        }

        // Determine the reason for failed: failed / abandoned
        CallResult result = getOMCallResultForFailedReason(failedEvent.getReason());

        // Determine O&M direction: inbound/outbound
        CallDirection direction = getOMCallDirection(failedEvent.getDirection());

        // Increment statistics for the call rejection
        seInfo.incrementNumberOfConnections(
                callType, result, direction);

        // The call is not yet connected, so the counter for the actual
        // call type is not increased. Decrease the counter for unknown.
        seInfo.decrementCurrentConnections(CallType.VOICE_VIDEO_UNKNOWN, direction);
    }

    /**
     * Collects statistics from a {@link ProxyingEvent}.
     * <p>
     * The following statistics is collected from the event:
     * <ul>
     * <li>
     * The number of current connections for unknown call type is decremented.
     * </li>
     * <li>
     * The number of current connections for the current call type is incremented.
     * </li>
     * <li>
     * Statistics is incremented for {@link CallResult.CONNECTED}.
     * </li>
     * </ul>
     * @param event
     */
    private void collectStatisticsFromProxyingEvent(ProxyingEvent event) {

        // Increment statistics for Current calls
        seInfo.decrementCurrentConnections(CallType.VOICE_VIDEO_UNKNOWN, CallDirection.INBOUND);
        seInfo.incrementCurrentConnections(CallType.VOICE, CallDirection.INBOUND);

        // Increment statistics for Total and Accumulated connected calls
        seInfo.incrementNumberOfConnections(CallType.VOICE, CallResult.CONNECTED, CallDirection.INBOUND);
    }

    /**
     * Collects statistics from a {@link ProxiedEvent}.
     * <p>
     * The number of current connections is decremented.
     * </ul>
     * @param event
     */
    private void collectStatisticsFromProxiedEvent(ProxiedEvent event) {

        // Decrease the counter for unknown type (PROXY)
        seInfo.decrementCurrentConnections(CallType.VOICE, CallDirection.INBOUND);
    }

    /**
     * Converts a {@link DisconnectedEvent.Reason} to an O&M {@link CallResult}.
     * The following conversion is done:
     * <ul>
     * <li>
     * {@link DisconnectedEvent.Reason.FAR_END} to
     * {@link CallResult.FARENDDISCONNECTED}.
     * </li>
     * <li>
     * {@link DisconnectedEvent.Reason.NEAR_END} to
     * {@link CallResult.NEARENDDISCONNECTED}.
     * </li>
     * <li>
     * {@link DisconnectedEvent.Reason.FAR_END_ABANDONED} to
     * {@link CallResult.ABANDONED}.
     * </li>
     * </ul>
     * @param reason
     * @return The O&M {@link CallResult} corresponding to tbe given
     * {@link DisconnectedEvent.Reason}.
     */
    public static CallResult getOMCallResultForDisconnectReason(
            DisconnectedEvent.Reason reason) {
        CallResult result = CallResult.FARENDDISCONNECTED;
        switch(reason) {
            case FAR_END:
                result = CallResult.FARENDDISCONNECTED;
                break;
            case NEAR_END:
                result = CallResult.NEARENDDISCONNECTED;
                break;
            case FAR_END_ABANDONED:
                result = CallResult.ABANDONED;
                break;
        }
        return result;
    }

    /**
     * Converts a {@link FailedEvent.Reason} to an O&M {@link CallResult}.
     * The following conversion is done:
     * <ul>
     * <li>
     * {@link FailedEvent.Reason.FAR_END_ABANDONED} to
     * {@link CallResult.ABANDONED_REJECTED}.
     * </li>
     * <li>
     * {@link FailedEvent.Reason.NEAR_END_ABANDONED} to
     * {@link CallResult.ABANDONED_REJECTED}.
     * </li>
     * <li>
     * All other reasons to {@link CallResult.FAILED}.
     * </li>
     * </ul>
     * @param reason
     * @return The O&M {@link CallResult} corresponding to tbe given
     * {@link FailedEvent.Reason}.
     */
    public static CallResult getOMCallResultForFailedReason(
            FailedEvent.Reason reason) {
        CallResult result;
        switch(reason) {
            case FAR_END_ABANDONED:
                result = CallResult.ABANDONED_REJECTED;
                break;
            case NEAR_END_ABANDONED:
                result = CallResult.ABANDONED_REJECTED;
                break;
            default:
                result = CallResult.FAILED;
                break;
        }
        return result;
    }

    /**
     * Retrieves the O&M call direction for a call.
     * @param call
     * @return the call direction of O&M format.
     */
    public static CallDirection getOMCallDirection(Call call) {
        CallDirection result = CallDirection.INBOUND;
        if (call instanceof OutboundCall) {
            result = CallDirection.OUTBOUND;
        }
        return result;
    }

    /**
     * Converts a Call Manager call direction to an O&M call direction.
     * @param direction of Call Manager format.
     * @return direction of O&M format
     */
    public static CallDirection getOMCallDirection(
            com.mobeon.masp.callmanager.CallDirection direction) {
        CallDirection result = CallDirection.INBOUND;
        if (direction == com.mobeon.masp.callmanager.CallDirection.OUTBOUND) {
            result = CallDirection.OUTBOUND;
        }
        return result;
    }

    /**
     * Cconverts a Call Manager call type to an O&M call type
     * @param callType of Call Manager format
     * @return The call type of O&M format
     */
    public static CallType getOMCallType(CallProperties.CallType callType) {
        CallType result = CallType.VOICE_VIDEO_UNKNOWN;

        switch(callType) {
            case VOICE:
                result = CallType.VOICE;
                break;
            case VIDEO:
                result = CallType.VIDEO;
                break;
        }
        return result;
    }

    /**
     * This class is used only to simplify the code. It is used to process an
     * event from a thread pool. Implements Runnable.
     *
     * @author Malin Flodin
     */
    private class EventProcessing implements Runnable {
        public void run() {
            // Clearing session info from logger this is run in a thread picked
            // from a pool and has no session relation.
            log.clearSessionInfo();

            collectStatistics();
        }
    }
}
