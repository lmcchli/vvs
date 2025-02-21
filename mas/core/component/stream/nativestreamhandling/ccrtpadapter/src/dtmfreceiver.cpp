/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "dtmfreceiver.h"

#include "jlogger.h"
#include "jniutil.h"
#include "controltoken.h"
#include "byteutilities.h"
#include "rtppayload.h"
#include "stackeventdispatcher.h"
#include "streamconfiguration.h"

#include <base_std.h> 

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.DTMFReceiver";

DTMFReceiver::DTMFReceiver(JNIEnv* env, java::RTPPayload& dtmfPayloadType, java::StreamConfiguration& config,
        jobject eventNotifier) :

        mIsLittleEndian(Platform::isLittleEndian()), mEventNotifier(eventNotifier), mDTMFPayloadType(dtmfPayloadType), mLastReceivedEvent(
                NULL), mConfiguration(config), mLastEventIsDelivered(false), mLastReceivedEventPacketTimestamp(0)
{
    mReNegotiatedDTMFPayloadType = mDTMFPayloadType.getPayloadType();

    JLogger::jniLogDebug(env, CLASSNAME, "DTMFReceiver - create at %#x", this);
}

DTMFReceiver::~DTMFReceiver()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~DTMFReceiver - delete at %#x", this);
}

bool DTMFReceiver::receivedPacket(IncomingRTPPkt& pkt)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if ((pkt.getPayloadType() != mDTMFPayloadType.getPayloadType())
            && (pkt.getPayloadType() != mReNegotiatedDTMFPayloadType)) {
        checkIfPendingEventTimedout(pkt.getTimestamp());
        return false; // Packet not handled here
    }

    uint16_t eventEndResVol16(0);
    uint16 duration16(0);
    bool eventEnd;
    int event(0);
    int volume(0);
    int duration(0);

    const char* data((const char*) pkt.getPayload());
    ByteUtilities::readW(data, eventEndResVol16, mIsLittleEndian);
    event |= ((eventEndResVol16 & 0xFF00) >> 8);
    volume |= (eventEndResVol16 & 0x003F);
    eventEnd = (eventEndResVol16 & 0x80) > 0;
    ByteUtilities::readW(data + sizeof(uint16), duration16, mIsLittleEndian);
    duration |= duration16;

    JLogger::jniLogTrace(env, CLASSNAME, "Event=%d, volume=%d, duration=%d, end=%d, timestamp=%d", event, volume,
            duration, eventEnd, pkt.getTimestamp());

    if (volume > 55) {
        // Volume to low according to RFC 2833
        JLogger::jniLogTrace(env, CLASSNAME, "Ignored event because volume was lower than -55 dBm0: %d", event);
        return true;
    }

    deliverOrWaitForEndBit(event, volume, duration, eventEnd, pkt, env);
    return true;
}

ControlToken* DTMFReceiver::handleDTMFPacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool sendEvent, JNIEnv* env)
{
    JLogger::jniLogTrace(env, CLASSNAME, "--> handleDTMFPacket");

    if ((adu->getType() != mDTMFPayloadType.getPayloadType()) && (adu->getType() != mReNegotiatedDTMFPayloadType)) {
        checkIfPendingEventTimedout(adu->getOriginalTimestamp());
        return 0; // Packet not handled here
    }

    uint16_t eventEndResVol16(0);
    uint16 duration16(0);
    bool eventEnd;
    int event(0);
    int volume(0);
    int duration(0);

    const char* data((const char*) adu->getData());
    ByteUtilities::readW(data, eventEndResVol16, mIsLittleEndian);
    event |= ((eventEndResVol16 & 0xFF00) >> 8);
    volume |= (eventEndResVol16 & 0x003F);
    eventEnd = (eventEndResVol16 & 0x80) > 0;
    ByteUtilities::readW(data + sizeof(uint16), duration16, mIsLittleEndian);
    duration |= duration16;

    JLogger::jniLogTrace(env, CLASSNAME, "Event=%d, volume=%d, duration=%d, end=%d, timestamp=%d", event, volume,
            duration, eventEnd, adu->getOriginalTimestamp());

    if (volume > 55) {
        // Volume to low according to RFC 2833
        JLogger::jniLogWarn(env, CLASSNAME, "Ignored event because volume was lower than -55 dBm0: %d", event);

        return 0;
    }

    ControlToken* token(deliverOrWaitForEndBit(event, volume, duration, eventEnd, adu, sendEvent, env));
    JLogger::jniLogTrace(env, CLASSNAME, "<-- handleDTMFPacket");
    return token;
}

void DTMFReceiver::deliverOrWaitForEndBit(int event, int volume, int duration, int eventEnd, IncomingRTPPkt& pkt,
        JNIEnv *env)
{
    bool shouldSendOnKeyDown(mConfiguration.isDispatchDTMFOnKeyDown());
    if (mLastReceivedEvent.get() != NULL) {
        // Check if this is a new event
        if ( // Do not check if the packet has the marker bit set.
             // At least Kapanga sets the marker bit in all packets
             // up to the end packets .
             // pkt.isMarked() ||
        (mLastReceivedEvent->getDigit() != event) || (mLastReceivedEventPacketTimestamp != pkt.getTimestamp())) {
            // A marked packet indicates a new event (RFC 2833) (se note
            // abound marker bit above however...).
            // If the digit is the same but the timestamp differ,
            // this is also a new event. This case indicates that the
            // first packet of this event was lost.
            JLogger::jniLogTrace(env, CLASSNAME, "A packet for a new event arrived: %d", event);
            JLogger::jniLogTrace(env, CLASSNAME, "pkt.isMarked=%d", pkt.isMarked());
            JLogger::jniLogTrace(env, CLASSNAME, "Previous Digit=%d, Current=%d", mLastReceivedEvent->getDigit(),
                    event);
            JLogger::jniLogTrace(env, CLASSNAME, "Previous timestamp=%d, Current=%d", mLastReceivedEventPacketTimestamp,
                    pkt.getTimestamp());

            if (!mLastEventIsDelivered) {
                // A packet with the end bit set was expected here
                // before a new event. This is probably due to packet loss.
                if (shouldSendOnKeyDown) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "DTMF event has not been delivered on key down as it should have (configuration changed?), it is delivered now instead:%d",
                            mLastReceivedEvent->getDigit());
                } else {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "A packet with the end bit was expected but a new event was received instead, delivering the old event now: %d",
                            mLastReceivedEvent->getDigit());
                }
                // XXX perhaps check timeout...if it is not done previously...
                // If the packet is to old, it is probably not interesting...?
                StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                mLastEventIsDelivered = true;
            }

            mLastReceivedEvent.reset(new ControlToken(event, volume, duration, env));
            mLastReceivedEventPacketTimestamp = pkt.getTimestamp();
            if (shouldSendOnKeyDown || eventEnd) {
                JLogger::jniLogTrace(env, CLASSNAME, "A new event is delivered: %d", event);
                StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                mLastEventIsDelivered = true;
            } else {
                // Wait to dispatch this event until a packet for the same
                // event arrives with the end bit set, a new event arrives
                // (the packet with the end bit set was lost) or until
                // a timeout is reached (the packet with the end bit is
                // considered lost).
                mLastEventIsDelivered = false;
                JLogger::jniLogTrace(env, CLASSNAME,
                        "A packet for a new event arrived but the event is not delivered yet: %d", event);
            }
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "A packet for an old event arrived: %d", event);

            // This packet belongs to the last received event
            if (!mLastEventIsDelivered) {
                mLastReceivedEvent->setDuration(mLastReceivedEvent->getDuration() + duration);
                if (shouldSendOnKeyDown) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "DTMF event has not been delivered on key down as it should have (configuration changed?), it is delivered now instead: %d",
                            event);
                    StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                    mLastEventIsDelivered = true;
                } else if (eventEnd) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "A packet with the end bit was received for an old event, it is now delivered: %d", event);

                    StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                    mLastEventIsDelivered = true;
                } else {
                    // Still no end bit set, continue to wait
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Another packet was received for an event without the end bit set, continue to wait for the end bit: %d",
                            event);
                }
            } else {
                // The event is already delivered, ignore packet
                JLogger::jniLogTrace(env, CLASSNAME, "Received packet for already sent event, ignored: %d", event);
            }
        }
    } else {
        JLogger::jniLogTrace(env, CLASSNAME,
                "A packet for a new event arrived: %d. This is the first DTMF packet ever.", event);

        // No event has been sent previously
        mLastReceivedEvent.reset(new ControlToken(event, volume, duration, env));
        mLastReceivedEventPacketTimestamp = pkt.getTimestamp();
        if (shouldSendOnKeyDown || eventEnd) {
            JLogger::jniLogTrace(env, CLASSNAME, "A new event is delivered: %d", event);
            StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
            mLastEventIsDelivered = true;
        } else {
            // Wait to dispatch this event until a packet for the same
            // event arrives with the end bit set, a new event arrives
            // (the packet with the end bit set was lost) or until
            // a timeout is reached (the packet with the end bit is
            // considered lost).
            mLastEventIsDelivered = false;
            JLogger::jniLogTrace(env, CLASSNAME, "A new event is not delivered yet: %d", event);
        }
    }
}

ControlToken* DTMFReceiver::deliverOrWaitForEndBit(int event, int volume, int duration, int eventEnd,
        std::auto_ptr<const ost::AppDataUnit>& adu, bool sendEvent, JNIEnv* env)
{
    ControlToken* controlToken(0);
    bool shouldSendOnKeyDown(mConfiguration.isDispatchDTMFOnKeyDown());
    if (mLastReceivedEvent.get() != NULL) {
        // Check if this is a new event
        if ( // Do not check if the packet has the marker bit set.
             // At least Kapanga sets the marker bit in all packets
             // up to the end packets .
             // pkt.isMarked() ||
        (mLastReceivedEvent->getDigit() != event)
                || (mLastReceivedEventPacketTimestamp != adu->getOriginalTimestamp())) {
            // A marked packet indicates a new event (RFC 2833) (se note
            // abound marker bit above however...).
            // If the digit is the same but the timestamp differ,
            // this is also a new event. This case indicates that the
            // first packet of this event was lost.
            JLogger::jniLogTrace(env, CLASSNAME, "A packet for a new event arrived: %d", event);
            JLogger::jniLogTrace(env, CLASSNAME, "pkt.isMarked=%d", adu->isMarked());
            JLogger::jniLogTrace(env, CLASSNAME, "Previous Digit=%d, Current=%d", mLastReceivedEvent->getDigit(),
                    event);
            JLogger::jniLogTrace(env, CLASSNAME, "Previous timestamp=%d, Current=%d", mLastReceivedEventPacketTimestamp,
                    adu->getOriginalTimestamp());

            if (!mLastEventIsDelivered) {
                // A packet with the end bit set was expected here
                // before a new event. This is probably due to packet loss.
                if (shouldSendOnKeyDown) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "DTMF event has not been delivered on key down as it should have (configuration changed?), it is delivered now instead: %d",
                            mLastReceivedEvent->getDigit());
                } else {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "A packet with the end bit was expected but a new event was received instead, delivering the old event now: %d",
                            mLastReceivedEvent->getDigit());
                }
                // XXX perhaps check timeout...if it is not done previously...
                // If the packet is to old, it is probably not interesting...?
                if (sendEvent) {
                    StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                }
                mLastEventIsDelivered = true;
            }

            mLastReceivedEvent.reset(new ControlToken(event, volume, duration, env));
            controlToken = new ControlToken(event, volume, duration, env);
            mLastReceivedEventPacketTimestamp = adu->getOriginalTimestamp();
            if (shouldSendOnKeyDown || eventEnd) {
                if (sendEvent) {
                    JLogger::jniLogTrace(env, CLASSNAME, "A new event is delivered: %d", event);
                    StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                }
                mLastEventIsDelivered = true;
            } else {
                // Wait to dispatch this event until a packet for the same
                // event arrives with the end bit set, a new event arrives
                // (the packet with the end bit set was lost) or until
                // a timeout is reached (the packet with the end bit is
                // considered lost).
                mLastEventIsDelivered = false;
                JLogger::jniLogTrace(env, CLASSNAME,
                        "A packet for a new event arrived but the event is not delivered yet: %d", event);
            }
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "A packet for an old event arrived: %d", event);

            // This packet belongs to the last received event
            if (!mLastEventIsDelivered) {
                mLastReceivedEvent->setDuration(mLastReceivedEvent->getDuration() + duration);
                if (shouldSendOnKeyDown) {
                    if (sendEvent) {
                        JLogger::jniLogTrace(env, CLASSNAME,
                                "DTMF event has not been delivered on key down as it should have (configuration changed?), it is delivered now instead: %d",
                                event);
                        StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                    }
                    mLastEventIsDelivered = true;
                } else if (eventEnd) {
                    if (sendEvent) {
                        JLogger::jniLogTrace(env, CLASSNAME,
                                "A packet with the end bit was received for an old event, it is now delivered: %d",
                                event);
                        StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
                    }
                    mLastEventIsDelivered = true;
                } else {
                    // Still no end bit set, continue to wait
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Another packet was received for an event without the end bit set, continue to wait for the end bit: %d",
                            event);
                }
            } else {
                JLogger::jniLogTrace(env, CLASSNAME, "Received packet for already sent event, ignored: %d", event);
            }
        }
    } else {
        JLogger::jniLogTrace(env, CLASSNAME,
                "A packet for a new event arrived: %d. This is the first DTMF packet ever.", event);
        // No event has been sent previously
        mLastReceivedEvent.reset(new ControlToken(event, volume, duration, env));
        controlToken = new ControlToken(event, volume, duration, env);
        mLastReceivedEventPacketTimestamp = adu->getOriginalTimestamp();
        if (shouldSendOnKeyDown || eventEnd) {
            if (sendEvent) {
                JLogger::jniLogTrace(env, CLASSNAME, "A new event is delivered: %d", event);
                StackEventDispatcher::sendToken(mEventNotifier, mLastReceivedEvent, env);
            }
            mLastEventIsDelivered = true;
        } else {
            // Wait to dispatch this event until a packet for the same
            // event arrives with the end bit set, a new event arrives
            // (the packet with the end bit set was lost) or until
            // a timeout is reached (the packet with the end bit is
            // considered lost).
            mLastEventIsDelivered = false;
            JLogger::jniLogTrace(env, CLASSNAME, "A new event is not delivered yet: %d", event);
        }
    }
    return controlToken;
}

void DTMFReceiver::checkIfPendingEventTimedout(uint32 timestamp)
{
    // XXX check if an unsent event is waiting for a packet with the end
    // bit set but has timedout.

}

int DTMFReceiver::getPayloadType()
{
    return mDTMFPayloadType.getPayloadType();
}

void DTMFReceiver::setDTMFPayloadType(int payloadType)
{
    mReNegotiatedDTMFPayloadType = payloadType;
}
