/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef DTMFRECEIVER_H_
#define DTMFRECEIVER_H_

#include <base_std.h>
#include <jni.h>
#include <ccrtp/rtp.h>

class ControlToken;
namespace java {
class RTPPayload;
class StreamConfiguration;
}
;

/**
 * Receives DTMF events. This class implements the algorithm described in 
 * RFC 2833. It does not, however, implement the redundancy mechanism 
 * <p>
 * This implementation does not consider duplicate packets (packets with the
 * same sequence number). This is handled by the stack implementation.
 * 
 * @author J�rgen Terner
 */
class DTMFReceiver
{
private:
    DTMFReceiver(DTMFReceiver& rhs);
    DTMFReceiver& operator=(const DTMFReceiver& rhs);

    /** 
     * <code>true</code> if the code is executed on a little-endian machine,
     * <code>false</code> if the machine is big-endian. 
     */
    bool mIsLittleEndian;

    /** Used when dispatching received events to Java-space. */
    jobject mEventNotifier;

    /** RTP payload type for DTMF (dynamic type). */
    java::RTPPayload& mDTMFPayloadType;
    int mReNegotiatedDTMFPayloadType;

    /** 
     * Last received DTMF event. 
     * <p>
     * Part of the state protected by the state mutex.
     */
    std::auto_ptr<ControlToken> mLastReceivedEvent;

    /** Configuration for the current session. */
    java::StreamConfiguration& mConfiguration;

    /** 
     * If <code>true</code> the last received event has been delivered. 
     * <p>
     * Part of the state protected by the state mutex.
     */
    bool mLastEventIsDelivered;

    /** 
     * Timestamp for last received DTMF event packet. This is used to
     * determine if an event should be sent even if an end-packet has
     * not been received yet. If events should be dispatched on key-up
     * and no DTMF-packets have been received since this time during
     * a timeout period, the packet with the end bit set is considered
     * to be lost and the event is dispathed.
     * <p>
     * Part of the state protected by the state mutex.
     */
    uint32 mLastReceivedEventPacketTimestamp;

    /**
     * If an event should be dispatched on key-up but no packet with the
     * end bit set has been received, the event is dispathed after a 
     * timeout.
     * 
     * @param timestamp RTP timestamp for the last received RTP packet.
     */
    void checkIfPendingEventTimedout(uint32 timestamp);

    /**
     * When the first packet for an event has been received, the event is
     * stored as the "last received event". It is dispatched immediately,
     * if stream is configured to dispatch events on key-down, otherwise
     * it is dispatched when a packet with the end bit set arrives.
     * 
     * @param event     Digit in the new DTMF event packet.
     * @param volume    Volume in the new DTMF event packet.
     * @param duration  Duration in the new DTMF event packet.
     * @param eventEnd  Value for the end bit in the new DTMF event packet.
     * @param pkt       RTP packet.
     */
    void deliverOrWaitForEndBit(int event, int volume, int duration, int eventEnd, ost::IncomingRTPPkt& pkt,
            JNIEnv* env);
    ControlToken* deliverOrWaitForEndBit(int event, int volume, int duration, int eventEnd,
            std::auto_ptr<const ost::AppDataUnit>& adu, bool sendEvent, JNIEnv* env);

public:
    /**
     * Creates a new instance.
     * 
     * @param dtmfPayloadType RTP payload type for DTMF (dynamic type).
     * @param config          Configuration for the current session.
     * @param eventNotifier   Used when dispatching received events to 
     *                        Java-space.
     */
    DTMFReceiver(JNIEnv* env, java::RTPPayload& dtmfPayloadType, java::StreamConfiguration& config,
            jobject eventNotifier);

    /**
     * Destructor.
     */
    ~DTMFReceiver();

    /**
     * Evaluates an RTP packet and if DTMF, handles it as appropriate.
     * 
     * @param pkt       The new RTP packet.
     * 
     * @return <code>true</code> if the packet was processed by this method,
     *         <code>false</code> if the packet was ignored by this method.
     */
    bool receivedPacket(ost::IncomingRTPPkt& pkt);
    ControlToken* handleDTMFPacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool sendEvent, JNIEnv* env);

    int getPayloadType();
    void setDTMFPayloadType(int payloadType);

};

#endif /*DTMFRECEIVER_H_*/
