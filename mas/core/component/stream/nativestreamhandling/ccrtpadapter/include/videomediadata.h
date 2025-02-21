/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef VIDEOMEDIADATA_H_
#define VIDEOMEDIADATA_H_

#include <base_std.h>

#include <ccrtp/queuebase.h>
#include <boost/ptr_container/ptr_list.hpp>

#include "jni.h"
#include "movrtppacket.h"

/**
 * Media data container. Exists because the class AppDataUnit
 * cannot be extended or created in a useful way. This class
 * keeps track of the RTP timestamps that is needed when frametimes
 * are calculated.
 * 
 * @author Jorgen Terner
 */
class VideoMediaData
{
private:
    std::auto_ptr<const ost::AppDataUnit> mAdu;

    /** The data in a format understandable for the media library. */
    std::auto_ptr<MovRtpPacket> mPacket;

    /** The extended RTP sequence number. */
    uint32 mExtendedSeqNum;

public:
    /**
     * Creates a new CNData and takes responsibility to delete
     * the given object.
     * 
     * @param adu Payload data.
     */
    VideoMediaData(JNIEnv *env, std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeqNum);

    /**
     * Destructor.
     */
    ~VideoMediaData();

    /**
     * Gets the video data in a format understandable for the media library.
     * 
     * @return Video data as a media library type.
     */
    MovRtpPacket& getPacket();

    /*
     * Release the packet from the auto_ptr. Ownership changes.
     * @return mPacket ptr
    */
    MovRtpPacket* releasePacket();

    /**
     * Gets the RTP timestamp for the data if received as an RTP packet.
     * 
     * @return RTP timestamp for this data.
     */
    uint32 getRTPTimestamp();

    const ost::AppDataUnit& getAdu();

    /**
     * Sets the frametime in the media library packet type.
     * 
     * @param frameTime Frametime in milliseconds (duration of this frame).
     */
    void setFrameTime(uint32 frameTime);

    /**
     * Gets the extended RTP sequence number.
     */
    uint32 getExtendedSeqNum();
};
#endif /*VIDEOMEDIADATA_H_*/
