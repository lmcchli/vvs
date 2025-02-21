/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <cc++/config.h> // For att __EXPORT ska vara definierad (Pointer.h)

#include "comfortnoisegenerator.h"
#include "jlogger.h"
#include "jniutil.h"
#include "streamrtpsession.h"
#include "streamcontentinfo.h"
#include "streamutil.h"
#include "rtppayload.h"
#include "audiomediadata.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.ComfortNoiseGenerator";

ComfortNoiseGenerator::ComfortNoiseGenerator(StreamRTPSession& session, StreamContentInfo& contentInfo) :
        mSession(session), mContentInfo(contentInfo), mStartTimestamp(0)
{
    StreamUtil::getTimeOfDay(mLastSentSIDFrameTimestamp);

    JNIEnv* env = JNIUtil::getJavaEnvironment();
    JLogger::jniLogDebug(env, CLASSNAME, "ComfortNoiseGenerator - create at %#x", this);
}

ComfortNoiseGenerator::~ComfortNoiseGenerator()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    JLogger::jniLogDebug(env, CLASSNAME, "~ComfortNoiseGenerator - delete at %#x", this);
}

void ComfortNoiseGenerator::newCNPacket(std::auto_ptr<const ost::AppDataUnit>& adu)
{
}

void ComfortNoiseGenerator::generateCN(uint32 endTimestamp, boost::ptr_list<AudioMediaData>& mediaData)
{
}

void ComfortNoiseGenerator::addSilence(boost::ptr_list<AudioMediaData>& mediaData, uint32 lengthMs, bool addToFront)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    uint32 msToOctetsFactor(mSession.getCurrentRTPClockRateMs());
    uint32 octets(lengthMs * msToOctetsFactor);
    uint8* payload(new uint8[octets]);
    JLogger::jniLogDebug(env, CLASSNAME, "payload - create at %#x - size %d", payload, octets);
    memset(payload, 0xFF, octets); // Silence
    if (addToFront) {
        mediaData.push_front(new AudioMediaData(env, payload, octets));
    } else {
        mediaData.push_back(new AudioMediaData(env, payload, octets));
    }

    JLogger::jniLogTrace(env, CLASSNAME, "Added %d ms of silence", lengthMs);
}

void ComfortNoiseGenerator::sendSIDFrame()
{
    if (shouldSendNewSIDFrame()) {
        /*JLogger::jniLogTrace(NULL, CLASSNAME, "Sending SID Frame");
         mSession->setPayloadFormat(
         *mContentInfo->getCNPayload()->getPayloadFormat());
         size_t packetSize(0);
         uint8* packet(buildSIDFrame(packetSize));
         mSession->putData(mSession->getCurrentTimestamp(), packet, packetSize);
         delete [] packet;*/
        StreamUtil::getTimeOfDay(mLastSentSIDFrameTimestamp);
    }
}

bool ComfortNoiseGenerator::shouldSendNewSIDFrame()
{
    timeval now;
    // This simple implementation just sends the SID frames periodically
    return StreamUtil::timedOut(mLastSentSIDFrameTimestamp, now, 120); // XXX 120?
}

uint8* ComfortNoiseGenerator::buildSIDFrame(size_t& size)
{
    // This simple implementation just sets the noise level to zero
    // and does not include spectral information.
    size = 8;
    uint8* packet(new uint8[size]);
    memset(packet, 0, size); // Noise level = 0
    return packet;
}

void ComfortNoiseGenerator::firstSilentPackage(uint32 firstTimestamp)
{
    mStartTimestamp = firstTimestamp;
}
