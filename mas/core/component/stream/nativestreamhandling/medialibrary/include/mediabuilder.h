/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIABUILDER_H_
#define MEDIABUILDER_H_

#include <stdexcept>

#include "platform.h"
#include "movrtppacket.h"
#include "movrtppacket.h"  // MovRtpPacketContainer
#include "mediaobjectwriter.h"
#include "movinfo.h"
#include "movaudiochunkcontainer.h"

#include <vector>
#include <base_include.h>
#include <boost/ptr_container/ptr_list.hpp>

typedef boost::ptr_list<boost::ptr_list<MovRtpPacket> > MovVideoFrameContainer;

/**
 * Base Class for media builders. 
 */
class MEDIALIB_CLASS_EXPORT MediaBuilder
{
public:

    /**
     * Virtual destructor.
     */
    virtual ~MediaBuilder()
    {/* Nothing to delete*/
    }

    /**
     * Sets the audio encoding as defined by IANA (case-sensitive).
     * <p>
     * Example: "PCMU"
     * 
     * @param codec Audio encoding.
     */
    virtual void setAudioCodec(const base::String& codec) = 0;

    /**
     * Sets the video encoding as defined by IANA (case-sensitive).
     * <p>
     * Example: "H263"
     * 
     * @param codec Video encoding.
     */
    virtual void setVideoCodec(const base::String& codec);

    /**
     * Sets the list of audio chunks.
     */
    virtual void setAudioChunks(const MovAudioChunkContainer& audioChunks) = 0;

    /**
     * Adds the RTP packets that builds a video frame.
     */
    virtual void setVideoFrames(const MovVideoFrameContainer& videoFrames);

    /**
     * Stores media data.
     */
    virtual bool store(MediaObjectWriter& writer) = 0;

    /**
     * @return Media duration in milliseconds.
     */
    virtual const unsigned getDuration() = 0;

    /**
     * @param offset Playout offset for the audio data relative the 
     *               beginning of the media.
     */
    virtual void setAudioStartTimeOffset(unsigned offset);

    /**
     * @param offset Playout offset for the video data relative the 
     *               beginning of the media.
     */
    virtual void setVideoStartTimeOffset(unsigned offset);

    /**
     * @param Frame payload.
     * 
     * @return <code>true</code> if the payload is an I-frame.
     */
    virtual bool isIFrame(const char* payload);

    /**
     * @return New empty media information instance.
     */
    virtual MediaInfo& getInfo() = 0;

    /**
     * @return <code>true</code> if this is a builder for audio only media,
     *         <code>false</code> if this is a builder for video media.
     */
    virtual bool isAudioOnlyBuilder() = 0;
};

// Not all media have video encodings so a default implementation is given here
inline void MediaBuilder::setVideoCodec(const base::String& codec)
{
}

// Not all media have video frames (audio) so a default implementation is 
// given here
inline void MediaBuilder::setVideoFrames(const MovVideoFrameContainer& videoFrames)
{
}

// Not all media formats can specify this (wav for example) so a default 
// implementation is given here
inline void MediaBuilder::setAudioStartTimeOffset(unsigned offset)
{
}

// Not all media formats can specify this (wav for example) so a default 
// implementation is given here
inline void MediaBuilder::setVideoStartTimeOffset(unsigned offset)
{
}

// Not all media have video frames (audio) so a default implementation is 
// given here
inline bool MediaBuilder::isIFrame(const char* payload)
{
    return true;
}

#endif /*MEDIABUILDER_H_*/
