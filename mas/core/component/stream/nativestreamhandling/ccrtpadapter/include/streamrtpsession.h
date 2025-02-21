/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STREAMRTPSESSION_H_
#define STREAMRTPSESSION_H_

#include <base_std.h>
#include <base_include.h>
#include <config.h> // F�r att __EXPORT ska vara definierad (Pointer.h)
#include <cc++/config.h>
#include <ccrtp/rtp.h>

#include "dtmfreceiver.h"

namespace java {
class StreamContentInfo;
}

class SessionSupport;
class ProcessorGroup;

/**
 * The main purpose of this class is to act as an interface for 
 * stream RTP sessions. This makes creation of 
 * implementations that inherits from different ccrtp-classes
 * transparent for the rest of the code.
 * <p>
 * This class does, however contains some common functionality as well.
 * 
 * @author Jorgen Terner
 */
using namespace ost;

typedef TRTPSessionBase<SymmetricRTPChannel, SymmetricRTPChannel, AVPQueue> MyRTPSessionBase;

class StreamRTPSession: public MyRTPSessionBase
{
public:

    enum SkewMethod
    {
        LOCAL = 0, RTCP = 1, LOCAL_AND_RTCP = 2
    };

    enum MODE
    {
        INBOUND, OUTBOUND
    };

    bool onGotSDESChunk(SyncSource& source, SDESChunk& chunk, size_t len);

    StreamRTPSession(JNIEnv* env, SessionSupport& session, MODE mode, java::StreamContentInfo& contentInfo,
            java::StreamConfiguration& config, jobject eventNotifier, const InetHostAddress& ia, tpport_t dataPort,
            tpport_t controlPort, RTPApplication& app);

    StreamRTPSession(JNIEnv* env, SessionSupport& session, MODE mode, java::StreamContentInfo& contentInfo,
            java::StreamConfiguration& config, jobject eventNotifier, const InetHostAddress& ia, tpport_t dataPort,
            tpport_t controlPort, RTPApplication& app, uint32 ssrc, StreamRTPSession* inboundStreamRTPSession);

    virtual ~StreamRTPSession();

    void setPayload(unsigned payloadType, unsigned clockRate);
    void setBandwidthModifiers(JNIEnv* env, int bwSender, int bwReceiver);

    DTMFReceiver& getDTMFReceiver();

    uint32 getReceivedCount() const;

    /**
     * Converts an RTP timestamp to the reference timeline (Wall Clock time)
     * as described in "RTP audio and video for the Internet" by Colin Perkins.
     */
    uint32 toWallClockTime(uint32 rtpTimestamp);

    /** Send data if our packet delta is about to expire */
    void sendData();

    bool insertRecvPacket(IncomingRTPPktLink* packetLink);

    size_t sendData(const unsigned char* const buffer, size_t len);

    SOCKET getDataRecvSocket() const;

    SOCKET getControlRecvSocket() const;

    microtimeout_t getSchedulingTimeout();

    size_t dispatchDataPacket();

    size_t dispatchDataPacket(uint32 ts, uint32 deltaTimestamp);

    void purgeOutgoingQueue();

    void checkControlData();

    void receiveControlData();

    void purgeIncomingQueue();

    ssize_t takeInDataPacket(void);

    /**
     * @return Current number of lost packets.
     */
    virtual uint32 getCumulativePacketLost();

    virtual uint32 getObservedPacketCount();

    virtual uint32 getExpectedPacketCount();

    /**
     * Separates and handles DTMF events before packets are added to the 
     * reception queue.
     * 
     * @param pkt Packet just received.
     * 
     * @return <code>true</code> if the packet should be inserted in the
     *         reception queue (thus, it was not a DTMF event). 
     *         <code>false</code> if the packet has been handled and thus
     *         should not be inserted in the reception queue.
     */
    virtual bool onRTPPacketRecv(ost::IncomingRTPPkt& pkt);

    void onPlayStarted();

    /**
     * A packet was added to the inqueue. 
     */
    virtual void onPacketInQueue(IncomingRTPPktLink* packetLink);

    /**
     * Plug-in for processing (acquire information carried in) an
     * incoming RTCP Sender Report. 
     *
     * @param source Synchronization source this report comes from.
     * @param SR     Sender report structure.
     * @param blocks Number of report blocks in the packet.
     **/
    virtual void onGotSR(ost::SyncSource& source, ost::RTCPCompoundHandler::SendReport& SR, uint8 blocks);

    /**
     * Checks if this session can be synchronized with the given session.
     * Two sessions can be synchronized if both sessions have received
     * at least one RTCP SR from the same participant.
     * 
     * @param session Other session.
     * 
     * @return <code>true</code> if this session can be synchronized with
     *         <code>session</code>, <code>false</code> otherwise.
     */
    bool canBeSynchronizedWith(StreamRTPSession& session);

    virtual uint32 getCurrentRTPClockRateMs(void);

    virtual ssize_t recvData(unsigned char* buffer, size_t length, InetHostAddress& host, tpport_t& port);

    virtual ssize_t getNextDataPacketSize() const;

    /**
     * Send request for full frame
     */
    void sendPictureFastUpdateRequest(JNIEnv* env, uint32 ssrc);

    /**
     * Returns synchronization source for last SR
     */
    uint32 getSSRC(JNIEnv *env);

private:
    SessionSupport& mSession;

    uint32 mRecvCounter;

    uint32 mSendCounter;

    MODE mMode;

    /** Handles reception of DTMF events. */
    DTMFReceiver mDTMFReceiver;

    /** 
     * RTP timestamp from the last RTCP SR report. Used to map an RTP
     * timestamp to the reference timeline (Wall Clock time).
     */
    uint32 mLastRTPTimestamp;

    /**
     * Reference timestamp from the last RTCP SR report. Used to map an RTP
     * timestamp to the reference timeline (Wall Clock time).
     */
    long mLastWallClockTimestamp;

    timeval mLastWallClockTimestampTv;

    /** 
     * Used to detect inbound activity for this stream.
     */
    bool mReceivedPacket;

    bool mIsPlaying;

    /** 
     * If <code>true</code> at least one RTCP SR has been received and
     * this session is ready for synchronization.
     */
    bool mIsSRReceived;

    /** CNAME for the last participant that send a RTCP SR. */
    base::String mCNAME;

    /** The latest incoming Synchronization source */
    uint32 mSSRC;

    /**
     * Convert a NTP timestamp, expressed as two 32-bit long words, into a
     * timeval value.
     *
     * @param msw     Integer part of NTP timestamp.
     * @param lsw     Fractional part of NTP timestamp.
     * @param timeval Value corresponding to the given NTP timestamp
     *                is written here.
     */
    void NTP2Timeval(uint32 msw, uint32 lsw, timeval& t);

    /**
     * Send a Full Infra Request packet over RTCP to request a
     * full frame
     * @param ssrc    SSRC of media sender (from which a FIR is needed)
     * @return        number of bytes sent
     */
    size_t dispatchFIR(JNIEnv* env, uint32 ssrc);

    /**
     * Overridden to store incoming SSRCs for use in FIR requests
     */
    virtual void onNewSyncSource(const SyncSource& source);

};

#endif /*STREAMRTPSESSION_H_*/

