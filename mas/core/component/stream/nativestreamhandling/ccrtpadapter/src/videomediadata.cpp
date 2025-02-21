/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <cc++/config.h> // For att __EXPORT ska vara definierad (Pointer.h)
#include <ccrtp/queuebase.h>

#include "videomediadata.h"

#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.VideoMediaData";

VideoMediaData::VideoMediaData(JNIEnv* env, std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeqNum) :
        mAdu(adu), mPacket(new MovRtpPacket()), mExtendedSeqNum(extendedSeqNum)
{
    mPacket->setData((char *) mAdu->getData());
    mPacket->setLength(mAdu->getSize());
    // The frametime is not known yet. It is calculated later
    // when all data is known.
    mPacket->setFrameTime(0);

    JLogger::jniLogDebug(env, CLASSNAME, "VideoMediaData - create at %#x", this);
}

MovRtpPacket& VideoMediaData::getPacket()
{
    return *mPacket;
}

MovRtpPacket* VideoMediaData::releasePacket()
{
    return mPacket.release();
}

const ost::AppDataUnit& VideoMediaData::getAdu()
{
    return *mAdu;
}

uint32 VideoMediaData::getRTPTimestamp()
{
    return mAdu->getOriginalTimestamp();
}

void VideoMediaData::setFrameTime(uint32 frameTime)
{
    mPacket->setFrameTime(frameTime);
}

VideoMediaData::~VideoMediaData()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~VideoMediaData - delete at %#x", this);
}

uint32 VideoMediaData::getExtendedSeqNum()
{
    return mExtendedSeqNum;
}
