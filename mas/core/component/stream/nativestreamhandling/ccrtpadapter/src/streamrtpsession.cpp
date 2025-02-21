/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include "private.h"
#include "streamrtpsession.h"
#include "dtmfreceiver.h"
#include "streamcontentinfo.h"
#include "streamutil.h"
#include "jlogger.h"
#include "jniutil.h"
#include "sessionsupport.h"
#include "outboundsession.h"
#include "comfortnoisegenerator.h"

#include <ccrtp/ext.h>
#include <ccrtp/rtppkt.h>

#ifndef WIN32 
#ifdef HAVE_STROPTS_H
#if HAVE_STROPTS_H != 0
#include <sys/stropts.h>
#endif
#endif
#endif


using namespace ost;
using namespace std;

static const char* CLASSNAME = "masjni.ccrtpadapter.StreamRTPSession";

#ifdef	WIN32
#include <io.h>
#define socket_errno	WSAGetLastError()
#else
#include <errno.h>
#define socket_errno errno
#endif

//used for inbound stream and stand alone outbound stream, e.g. for TTS sessions or test scenarios
//which only test outbound stream
StreamRTPSession::StreamRTPSession(JNIEnv* env, SessionSupport &session, MODE mode,
        java::StreamContentInfo& contentInfo, java::StreamConfiguration& config, jobject eventNotifier,
        const InetHostAddress& ia, tpport_t dataPort, tpport_t controlPort, RTPApplication& app) :

        MyRTPSessionBase(ia, dataPort, controlPort, MembershipBookkeeping::defaultMembersHashSize, app),
        mSession(session), mRecvCounter(0), mSendCounter(0), mMode(mode),
        mDTMFReceiver(env, contentInfo.getDTMFPayload(), config, eventNotifier), mLastRTPTimestamp(0),
        mLastWallClockTimestamp(0), mReceivedPacket(false), mIsPlaying(false), mIsSRReceived(false), mCNAME("")
{
    JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession - dataPort %d, controlPort %d", dataPort, controlPort);

    JLogger::jniLogDebug(env, CLASSNAME, "StreamRTPSession - create at %#x", this);
}

//used by outbound stream when a corresponding inbound stream exists, which is included to facilitate
//fetching of statistical data needed by the outbound RTCP protocol
StreamRTPSession::StreamRTPSession(JNIEnv* env, SessionSupport &session, MODE mode,
        java::StreamContentInfo& contentInfo, java::StreamConfiguration& config, jobject eventNotifier,
        const InetHostAddress& ia, tpport_t dataPort, tpport_t controlPort, RTPApplication& app, uint32 ssrc,
        StreamRTPSession* inboundStreamRTPSession) :

        MyRTPSessionBase((QueueRTCPManager*) inboundStreamRTPSession, ssrc, ia, dataPort, controlPort,
                MembershipBookkeeping::defaultMembersHashSize, app, inboundStreamRTPSession->getDSO(),
                inboundStreamRTPSession->getCSO()),
        mSession(session), mRecvCounter(0), mSendCounter(0), mMode(mode),
        mDTMFReceiver(env, contentInfo.getDTMFPayload(), config, eventNotifier),
        mLastRTPTimestamp(0), mLastWallClockTimestamp(0), mReceivedPacket(false), mIsPlaying(false), mIsSRReceived(false), mCNAME("")
{
    JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession - dataPort %d, controlPort %d", dataPort, controlPort);

    JLogger::jniLogDebug(env, CLASSNAME, "StreamRTPSession - create at %#x", this);
}

StreamRTPSession::~StreamRTPSession()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "StreamRTPSession - delete at %#x", this);
}

DTMFReceiver& StreamRTPSession::getDTMFReceiver()
{
    return mDTMFReceiver;
}

void StreamRTPSession::setPayload(unsigned payloadType, unsigned clockRate)
{
    DynamicPayloadFormat payloadFormat(payloadType, clockRate);

    setPayloadFormat(payloadFormat);
}

void StreamRTPSession::setBandwidthModifiers(JNIEnv* env, int bwSender, int bwReceiver)
{
    int totalbw = getSessionBandwidth();  // bps
    JLogger::jniLogTrace(env, CLASSNAME, "BandwidthModifier - Totalbw: %d, Senderbw(RS): %d, Receiverbw(RR): %d",
            totalbw, bwSender, bwReceiver);
    if (totalbw == 0)
        return;
    if (bwSender == -1 || bwReceiver == -1)
        return;

    float controlbw = ((float) (bwSender + bwReceiver)) / totalbw;
    setControlBandwidth(controlbw);

    float controlfrac;
    if (bwSender == 0 && bwReceiver == 0) {
        controlfrac = 0;
    } else {
        controlfrac = ((float) bwSender) / ((float) bwSender + bwReceiver);
    }
    setSendersControlFraction(controlfrac);

    JLogger::jniLogTrace(env, CLASSNAME, "BandwidthModifier - controlbw: %d, controlfrac: %d", controlbw, controlfrac);
}

bool StreamRTPSession::onGotSDESChunk(SyncSource& source, SDESChunk& chunk, size_t len)
{
    bool result = QueueRTCPManager::onGotSDESChunk(source, chunk, len);
    return result;
}

bool StreamRTPSession::onRTPPacketRecv(IncomingRTPPkt& pkt)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    mReceivedPacket = true;
    mRecvCounter++;
    static int prev = 0;
    if (pkt.getPayloadType() == 105 || pkt.getPayloadType() == 34) {
        if (pkt.getSeqNum() != (prev + 1)) {
            if (pkt.getSeqNum() == prev) {
                JLogger::jniLogTrace(env, CLASSNAME, "Clone received at seq.no: %d", prev);
            } else {
                JLogger::jniLogTrace(env, CLASSNAME, "Gap in seq.no between: %d,%d", prev, pkt.getSeqNum());
            }
        }
        prev = pkt.getSeqNum();
    }
    //    return !mDTMFReceiver.receivedPacket(pkt);
    return true;
}

uint32 StreamRTPSession::getReceivedCount() const
{
    return mRecvCounter;
}

bool StreamRTPSession::canBeSynchronizedWith(StreamRTPSession& session)
{
    if (!(session.mIsSRReceived && mIsSRReceived)) {
        return false;
    }
    return mCNAME == session.mCNAME;
}

void StreamRTPSession::onGotSR(ost::SyncSource& source, ost::RTCPCompoundHandler::SendReport& SR, uint8 blocks)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    Participant* p(source.getParticipant());
    if (p != 0) {
        base::String name = p->getSDESItem(SDESItemTypeCNAME).c_str();
        if (mCNAME != "") {
            if (mCNAME != name) {
                JLogger::jniLogWarn(env, CLASSNAME,
                        "RTCP SR was sent from CNAME=%s, previous RTCP SR was sent from %s, this can cause problems when synchronizing streams.",
                        name.c_str(), mCNAME.c_str());
            }
        } else {
            mCNAME = name;
            JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession::onGotSR(%#x) Participant CNAME=%s, SSRC=%d", this,
                    mCNAME.c_str(), source.getID());
        }
        NTP2Timeval(SR.sinfo.NTPMSW, SR.sinfo.NTPLSW, mLastWallClockTimestampTv);
        mLastRTPTimestamp = ntohl(SR.sinfo.RTPTimestamp);
        mLastWallClockTimestamp = mLastWallClockTimestampTv.tv_sec * 1000l + mLastWallClockTimestampTv.tv_usec / 1000l;
        mIsSRReceived = true;
    } else {
        JLogger::jniLogTrace(env, CLASSNAME, "RTCP SR received from unknown participant, ignored");
        JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession::onGotSR(%#x) unknown SSRC was %u", this,
                source.getID());
        MyRTPSessionBase::onGotSR(source, SR, blocks);
    }
}

uint32 StreamRTPSession::toWallClockTime(uint32 rtpTimestamp)
{
    uint32 result(0);
    if (rtpTimestamp > mLastRTPTimestamp) {
        result = mLastWallClockTimestamp + (rtpTimestamp - mLastRTPTimestamp) / (getCurrentRTPClockRate() / 1000); // milliseconds
    } else {
        result = mLastWallClockTimestamp - (mLastRTPTimestamp - rtpTimestamp) / (getCurrentRTPClockRate() / 1000); // milliseconds
    }

    return result;
}

void StreamRTPSession::NTP2Timeval(uint32 msw, uint32 lsw, timeval& t)
{
    t.tv_sec = msw - NTP_EPOCH_OFFSET;
    t.tv_usec = (uint32) (((double) lsw) * 1000000.0) / ((uint32) (~0));
}

uint32 StreamRTPSession::getCumulativePacketLost()
{
    MembershipBookkeeping::SyncSourceLink* link = getFirst();
    uint32 lost(0);
    while (link != 0) {
        lost += link->getCumulativePacketLost();
        link = link->getNext();
    }
    return lost;
}

uint32 StreamRTPSession::getObservedPacketCount()
{
    MembershipBookkeeping::SyncSourceLink* link = getFirst();
    uint32 observed(0);
    while (link != 0) {
        observed += link->getObservedPacketCount();
        link = link->getNext();
    }
    return observed;
}

uint32 StreamRTPSession::getExpectedPacketCount()
{
    MembershipBookkeeping::SyncSourceLink* link = getFirst();
    uint32 expected(0);
    while (link != 0) {
        expected += link->getMaxSeqNum() + link->getSeqNumAccum() - link->getBaseSeqNum() + 1;
        link = link->getNext();
    }
    return expected;
}

bool StreamRTPSession::insertRecvPacket(IncomingRTPPktLink* packetLink)
{
    bool result = IncomingDataQueue::insertRecvPacket(packetLink);
    // EC200, TR HJ84150
    // IncomingDataQueue::insertRecvPacket deletes the packetLink when returning false.
    if (result == true) {
        onPacketInQueue(packetLink);
    }
    return result;
}

size_t StreamRTPSession::sendData(const unsigned char* const buffer, size_t len)
{
    return getDSO()->send(buffer, len);
}

void StreamRTPSession::sendData()
{
    if (!isSending()) {
        if (mSession.isComfortNoiseEnabled()) {
            mSession.getComfortNoiseGenerator();
            //TODO: Generate comfortnoise. We get here because
            //we has nothing to send, and comfortnoise is enabled.
            //I suggest generating, 500ms of noise or something similar
        }
        if (mIsPlaying) {
            mIsPlaying = false;
            static_cast<OutboundSession&>(mSession).getPlayer().onPlayFinished();
            return;
        }

    } else {
        mSendCounter++;
        dispatchDataPacket(getCurrentTimestamp(), 0);
        if (mIsPlaying && mSendCounter % 10 == 0) //Don't tick always...
            static_cast<OutboundSession&>(mSession).getPlayer().onTick();

    }
    //TODO: Must we consider the deltaTimestamp ? Doesn't the
    //RTP stack do that for us ? It does look weird to me
    //dispatchDataPacket(getCurrentTimestamp(), deltaTimestamp);
}

uint32 StreamRTPSession::getCurrentRTPClockRateMs(void)
{
    return getCurrentRTPClockRate() / 1000;
}

void StreamRTPSession::purgeOutgoingQueue()
{
    MyRTPSessionBase::purgeOutgoingQueue();
}

void StreamRTPSession::purgeIncomingQueue()
{
    MyRTPSessionBase::purgeIncomingQueue();
}

SOCKET StreamRTPSession::getDataRecvSocket() const
{
    return MyRTPSessionBase::getDataRecvSocket();
}

SOCKET StreamRTPSession::getControlRecvSocket() const
{
    return MyRTPSessionBase::getControlRecvSocket();
}

microtimeout_t StreamRTPSession::getSchedulingTimeout()
{
    return MyRTPSessionBase::getSchedulingTimeout();
}

size_t StreamRTPSession::dispatchDataPacket()
{
    return MyRTPSessionBase::dispatchDataPacket();
}

size_t StreamRTPSession::dispatchDataPacket(uint32 ts, uint32 deltaTimestamp)
{
    return MyRTPSessionBase::dispatchDataPacket(ts, deltaTimestamp);
}

ssize_t StreamRTPSession::takeInDataPacket()
{
    return MyRTPSessionBase::takeInDataPacket();
}

void StreamRTPSession::onPacketInQueue(IncomingRTPPktLink* packetLink)
{
    //TODO: This maybe should be grafted into the fast path to record
    //but we'll probably do this in another way first..
}

void StreamRTPSession::onPlayStarted()
{
    mIsPlaying = true;
}

void StreamRTPSession::checkControlData()
{
    bool isOutbound = mSession.isOutbound();

    if (isOutbound) {
        controlTransmissionService();
    } else {
        controlReceptionService();
    }
}

void StreamRTPSession::receiveControlData()
{
    controlReceptionService();
}

ssize_t StreamRTPSession::getNextDataPacketSize() const
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
#if defined(WIN32) || defined(LINUX)
    int len;  //FIONREAD 'returns' and _int_ even on LINUX x86_64
    int rtn = ccioctl(getDataRecvSocket(),FIONREAD,(void*)&len);
    if(rtn == -1) {
        JLogger::jniLogInfo(env, CLASSNAME, "Unexpected socket state on socket %d, the message was %s", getDataRecvSocket(), strerror(socket_errno));
        return -1;
    }
    return len;
#else
    size_t len;
    int rtn = ccioctl(getDataRecvSocket(), I_NREAD, len);
    if (rtn == -1)
        return -1;
    return len;
#endif
}

ssize_t StreamRTPSession::recvData(unsigned char* buffer, size_t length, InetHostAddress& host, tpport_t& port)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    struct sockaddr_in sin_from;
    socklen_t sin_sz = sizeof(sin_from);

    int32 rtn = (int32) ::recvfrom(dso->getRecvSocket(), (char*) buffer, length, 0, (sockaddr*) &sin_from, &sin_sz);
    if (rtn >= 0 && (unsigned) rtn >= sizeof(struct RTPPacket::RTPFixedHeader) && sin_sz > 0) {
        port = ntohs(sin_from.sin_port);
        host = IPV4Host(sin_from.sin_addr);
    } else {
        if (sin_sz != 0)
            JLogger::jniLogTrace(env, CLASSNAME,
                    "StreamRTPSession: recvfrom didn't yield usable data. Return is %d and sin_sz is %d, expected packet size was %d",
                    rtn, sin_sz, length);
        port = 0;
        rtn = -1;
    }

    return rtn;

}

/**
 * Send request for full frame
 */
void StreamRTPSession::sendPictureFastUpdateRequest(JNIEnv *env, uint32 ssrc)
{
    size_t numsent = dispatchFIR(env, ssrc);
    JLogger::jniLogTrace(env, CLASSNAME,
            "StreamRTPSession::sendPictureFastUpdateRequest(%#x) FIR sent for SSRC %d,%d bytes.", this, ssrc, numsent);
}

/**
 * Send a Full Intra-frame Request packet over RTCP to request a
 * full frame (RFC 4585 + draft-ietf-avt-avpf-ccm)
 * (this is a copy & modify of
 * QueueRTCPManager::dispatchControlPacket())
 *
 * @param ssrc Media sender SSRC
 */
size_t StreamRTPSession::dispatchFIR(JNIEnv *env, uint32 ssrc)
{
    rtcpInitial = false;
    // Keep in mind: always include a report (in SR or RR) and at
    // least a SDES with the local CNAME. It is mandatory.

    // (A) SR or RR, depending on whether we sent.
    // pkt will point to the packets of the compound

    RTCPPacket* pkt = reinterpret_cast<RTCPPacket*>(rtcpSendBuffer);
    // Fixed header of the first report
    pkt->fh.padding = 0;
    pkt->fh.version = CCRTP_VERSION;
    // length of the RTCP compound packet. It will increase till
    // the end of this routine. Both sender and receiver report
    // carry the general 32-bit long fixed header and a 32-bit
    // long SSRC identifier.
    uint16 len = sizeof(RTCPFixedHeader) + sizeof(uint32);

    // the fields block_count and length will be filled in later
    // now decide whether to send a SR or a SR
    if (lastSendPacketCount != getSendPacketCount()) {
        // we have sent rtp packets since last RTCP -> send SR
        lastSendPacketCount = getSendPacketCount();
        pkt->fh.type = RTCPPacket::tSR;
        pkt->info.SR.ssrc = getLocalSSRCNetwork();

        // Fill in sender info block. It would be more
        // accurate if this were done as late as possible.
        timeval now;
        gettimeofday(&now, NULL);
        // NTP MSB and MSB: dependent on current payload type.
        pkt->info.SR.sinfo.NTPMSW = htonl(now.tv_sec + NTP_EPOCH_OFFSET);
        pkt->info.SR.sinfo.NTPLSW = htonl((uint32) (((double) (now.tv_usec) * (uint32) (~0)) / 1000000.0));
        // RTP timestamp
        uint32 tstamp = now.tv_usec - getInitialTime().tv_usec;
        tstamp *= (getCurrentRTPClockRate() / 1000);
        tstamp /= 1000;
        tstamp += (now.tv_sec - getInitialTime().tv_sec) * getCurrentRTPClockRate();
        tstamp += getInitialTimestamp();
        pkt->info.SR.sinfo.RTPTimestamp = htonl(tstamp);
        // sender's packet and octet count
        pkt->info.SR.sinfo.packetCount = htonl(getSendPacketCount());
        pkt->info.SR.sinfo.octetCount = htonl(getSendOctetCount());
        len += sizeof(SenderInfo);
    } else {
        // RR
        pkt->fh.type = RTCPPacket::tRR;
        pkt->info.RR.ssrc = getLocalSSRCNetwork();
    }

    // (B) put report blocks
    // After adding report blocks, we have to leave room for at
    // least a CNAME SDES item
    uint16 available = (uint16) (getPathMTU() - lowerHeadersSize - len
            - (sizeof(RTCPFixedHeader) + 2 * sizeof(uint8) + getApplication().getSDESItem(SDESItemTypeCNAME).length())
            - 100);

    // if we have to go to a new RR packet
    bool another = false;
    uint16 prevlen = 0;
    RRBlock* reports;
    if (RTCPPacket::tRR == pkt->fh.type)
        reports = pkt->info.RR.blocks;
    else
        // ( RTCPPacket::tSR == pkt->fh.type )
        reports = pkt->info.SR.blocks;
    do {
        uint8 blocks = 0;
        pkt->fh.block_count = blocks = packReportBlocks(reports, len, available);
        // the length field specifies 32-bit words
        pkt->fh.length = htons(((len - prevlen) >> 2) - 1);
        prevlen = len;
        if (31 == blocks) {
            // we would need room for a new RR packet and
            // a CNAME SDES
            if (len < (available - (sizeof(RTCPFixedHeader) + sizeof(uint32) + sizeof(RRBlock)))) {
                another = true;
                // Header for this new packet in the compound
                pkt = reinterpret_cast<RTCPPacket*>(rtcpSendBuffer + len);
                pkt->fh.version = CCRTP_VERSION;
                pkt->fh.padding = 0;
                pkt->fh.type = RTCPPacket::tRR;
                pkt->info.RR.ssrc = getLocalSSRCNetwork();
                // appended a new Header and a report block

                len += sizeof(RTCPFixedHeader) + sizeof(uint32);
                reports = pkt->info.RR.blocks;
            } else {
                another = false;
            }
        } else {
            another = false;
        }
    } while ((len < available) && another);

    // (C) SDES (CNAME)
    // each SDES chunk must be 32-bit multiple long
    // fill the padding with 0s
    packSDES(len);

    // Append the FIR packet
    pkt = reinterpret_cast<RTCPPacket*>(rtcpSendBuffer + len);
    pkt->fh.version = CCRTP_VERSION;
    pkt->fh.padding = 0;
    // FMT=4
    pkt->fh.block_count = RTCPPacket::fFIR;
    // PT=PSFB (206)
    pkt->fh.type = RTCPPacket::tPSFB;
    pkt->info.PSFB_FIR.ssrc = getLocalSSRCNetwork();
    pkt->info.PSFB_FIR.ssrcMediaSource = 0;
    // Media sender SSRC from input argument
    JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession::dispatchFIR(%#x) inserting SSRC in FIR: %d", this, ssrc);
    pkt->info.PSFB_FIR.fciEntries[0].ssrc = ssrc;
    // TODO: This should be increased by one as per media sender SSRC (store in map?)
    static unsigned char seqno = 0;
    pkt->info.PSFB_FIR.fciEntries[0].seqNo = seqno++;
    pkt->info.PSFB_FIR.fciEntries[0].reserved = 0;
    // length in packet = 2+2*N, where N is number of fci entries (1)
    pkt->fh.length = 2 + 2 * 1;
    len += sizeof(RTCPFixedHeader) + sizeof(PSFB_FIR_Packet);

    // actually send the packet.
    size_t count = sendControlToDestinations(rtcpSendBuffer, len);
    ctrlSendCount++;
    // Everything went right, update the RTCP average size
    updateAvgRTCPSize(len);

    return count;
}

/**
 * Returns synchronization source for last SR
 */
uint32 StreamRTPSession::getSSRC(JNIEnv *env)
{
    JLogger::jniLogTrace(env, CLASSNAME, "StreamRTPSession::getSSRC(%#x) returning SSRC: %d", this, mSSRC);
    return mSSRC;
}

/**
 * Overridden to save the last SSRC
 */
void StreamRTPSession::onNewSyncSource(const SyncSource& source)
{
    mSSRC = source.getID();
    JLogger::jniLogTrace(JNIUtil::getJavaEnvironment(), CLASSNAME,
            "StreamRTPSession::onNewSyncSource(%#x) got new SSRC: %d", this, mSSRC);
}
