/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h> 

#include "dtmfsender.h"
#include "jniutil.h"
#include "jlogger.h"
#include "controltoken.h"
#include "byteutilities.h"

#include "streamrtpsession.h"
#include "outboundsession.h"
#include "Processor.h"

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.DTMFSender";

DTMFSender::DTMFSender(JNIEnv* env, OutboundSession& session) :
        mSession(session), mIsLittleEndian(Platform::isLittleEndian()), mTokens(), mRtpTimestampAfterLastToken(0)
{
    JLogger::jniLogDebug(env, CLASSNAME, "DTMFSender - create at %#x", this);
}

DTMFSender::~DTMFSender()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~DTMFSender - delete at %#x", this);
}

void DTMFSender::setMasterPayloadFormat(unsigned payloadType, unsigned clockRate)
{
    m_masterPayloadType = payloadType;
    m_masterClockRate = clockRate;
}

void DTMFSender::setDtmfPayloadFormat(unsigned payloadType, unsigned clockRate)
{
    m_dtmfPayloadType = payloadType;
    m_dtmfClockRate = clockRate;
}

void DTMFSender::setPTime(unsigned pTime)
{
    m_pTime = pTime;
}

void DTMFSender::setClockRate(unsigned clockRate)
{
    m_dtmfClockRate = clockRate;
}

void DTMFSender::addToSendList(std::auto_ptr<boost::ptr_list<ControlToken> >& tokens)
{
    while (!tokens->empty()) {
        mTokens.push_back(tokens->pop_front().release());
    }
}

uint32 DTMFSender::sendToken(ControlToken* token)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "--> sendToken() %#x:%d", this, token);

    uint32 tokenDuration(token->getDuration());
    if (token->getDigit() == ControlToken::SILENCE_BETWEEN_TOKENS) {
        JLogger::jniLogTrace(env, CLASSNAME, "Silence-token: increases timestamp.");

        // Insert "silence" between tokens, that is, move the
        // timestamp forward in time
        mRtpTimestampAfterLastToken += token->getDuration();
        return tokenDuration;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "CR:pTime=%d:%d", m_dtmfClockRate, m_pTime);
    uint32 clockRate(m_dtmfClockRate);
    uint32 pTime(m_pTime);
    int packetsPerSecond(1000 / pTime);
    uint16 tstampInc((unsigned short) (clockRate / packetsPerSecond));
    uint32 timestampUnitsSent(0);

    JLogger::jniLogTrace(env, CLASSNAME, "Retreiving audio session ...");

    StreamRTPSession& dtmfSession(mSession.getAudioSession());
    dtmfSession.setPayload(m_dtmfPayloadType, m_dtmfClockRate);
    dtmfSession.setMark(true); // First packet shall have the marker bit set.

    //    int numberOfPackets(0);
    uint32 timestamp(dtmfSession.getCurrentTimestamp());
    if (timestamp < mRtpTimestampAfterLastToken) {
        // This token is sent before the previous token is finished,
        // this token must be delayed so it is played after the 
        // previous one.
        timestamp = mRtpTimestampAfterLastToken;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "Preparing packages ...");

    // This is the main loop, where packets are transmitted.
    uint8 buffer[4];
    uint32 packetDuration;
    while (timestampUnitsSent < tokenDuration) {
        packetDuration = token->getDuration() - timestampUnitsSent;
        if (packetDuration <= tstampInc) {
            // This is the last packet
            buildPayload(buffer, token->getDigit(), token->getVolume(), packetDuration, true);
        } else {
            buildPayload(buffer, token->getDigit(), token->getVolume(), tstampInc, false);
        }

        // Each packet shall have the same timestamp
        JLogger::jniLogTrace(env, CLASSNAME, "Calling put data");
        dtmfSession.putData(timestamp, (const unsigned char*) buffer, 4, true);
        timestampUnitsSent += tstampInc;
    }

    // Now, all packets are sent. If no other events are waiting to be sent, 
    // the last packet shall be resent three times (RFC 2833).
    /*
     bool empty(true);
     empty = mTokens.empty();

     if (empty) {
     for (int i = 0; i < 3; i++) {
     dtmfSession.putData(timestamp,
     (const unsigned char*)buffer, 4);
     }
     }
     */
    mRtpTimestampAfterLastToken = timestamp + tokenDuration;
    dtmfSession.setPayload(m_masterPayloadType, m_masterClockRate);
    JLogger::jniLogTrace(env, CLASSNAME, "<-- sendToken()");
    return tokenDuration;
}

uint32 DTMFSender::sendNextToken()
{

    std::auto_ptr<ControlToken> token;
    if (!mTokens.empty()) {
        token.reset(mTokens.pop_front().release());
    }

    if (token.get() == NULL) {
        return 0;
    }

    return sendToken(token.get());
}

void DTMFSender::buildPayload(uint8 (&buffer)[4], int digitIn, int volumeIn, int durationIn, bool endBitIn)
{
    uint8 digit = (unsigned char) (digitIn & 0x000000FF);
    uint8 endResVolume = (unsigned char) (volumeIn & 0x0000003F);
    uint8 endRes = (unsigned char) (endBitIn ? 0x80 : 0); // 0x80: End bit=1, Reserved=0
    endResVolume |= endRes; // Mask in the endRes bits
    uint16_t duration = (unsigned short) (durationIn & 0x0000FFFF);

    memcpy(&buffer, &digit, 1);
    memcpy(&buffer[1], &endResVolume, 1);
    writeW(&buffer[2], duration);
}

void DTMFSender::writeW(uint8 *ptr, uint16_t &uw)
{
    if (mIsLittleEndian) {
        ByteUtilities::swapW(uw);
    }
    memcpy(ptr, &uw, sizeof(uint16_t));
}

uint32 DTMFSender::getRtpTimestampAfterLastToken()
{
    return mRtpTimestampAfterLastToken;
}

