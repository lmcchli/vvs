/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAPARSER_H_
#define MEDIAPARSER_H_
//#pragma warning( disable : 4290 )

#include "jni.h"
#include "java/mediaobject.h"

#include <stdexcept>
#include <boost/ptr_container/ptr_list.hpp>
#include <base_include.h>

class MovRtpPacket;
class RtpBlockHandler;

namespace java {
class MediaObject;
};

/**
 * Base Class for media parsers. 
 * A MediaParser parses a MediaObject and returns info for the media
 * in a MediaInfo object.
 */
class MediaParser
{
public:

    /**
     * Constructor that creates a MediaParser that parses the
     * passed MediaObject.
     * @param mediaObject reference to the MediaObject to parse.
     */
    MediaParser(java::MediaObject* mediaObject);

    /**
     * Virtual destructor.
     */
    virtual ~MediaParser();

    /**
     * Must be called before any other method to initiate the parser.
     */
    virtual void init() = 0;

    /**
     * Parses the MediaObject.
     */
    virtual bool parse() = 0;

    /**
     * Returns the media duration in milli seconds.
     * If media contains multi media data/tracks the maximum of the
     * durations is returned.
     *
     * @return the media duration in milli seconds.
     */
    virtual unsigned getDuration() = 0;

    /**
     * Tells that the reading should start at the given position.
     * If the media is video, the cursor is adjusted to the closest
     * intra frame.
     * 
     * @param cursor Position in milliseconds the reading should start from.
     */
    virtual void setCursor(long cursor) = 0;

    /**
     * Get the cursor value.
     * Since the cursor is adjusted to the closest frame for some medias this value
     * may be different than the value passed in setCursor.
     * 
     * @param cursor Position in milliseconds the reading should start from.
     */
    virtual long getCursor() = 0;

    /**
     * Stores RTP packet information in the given container for the
     * specified frame.
     * 
     * @param rtpPackets Packet destination.
     * @param frameIndex Index of requested frame.
     */
    virtual void getFrame(boost::ptr_list<MovRtpPacket>& rtpPackets, int frameIndex);

    /**
     * Gets RTP packet payload for the specified audio chunk index.
     * 
     * @param chunkSize  Size of the returned payload data.
     * @param chunkIndex Index of requested audio chunk.
     * 
     * @return Pointer to the requested payload.
     * 
     * @throws logic_error If <code>chunkIndex</code> is out of range.
     */
    virtual const unsigned char* getAudioChunk(unsigned& chunkSize, int chunkIndex) = 0;

    /**
     * @return Number of frames. Always zero if media is audio only.
     */
    virtual int getFrameCount();

    /**
     * @return Number of audio chunks.
     */
    virtual int getAudioChunkCount() = 0;

    /**
     * Gets the audio encoding as defined by IANA (case-sensitive).
     * <p>
     * Example: "PCMU"
     * 
     * @param codec Audio encoding.
     */
    virtual const base::String getAudioCodec() = 0;

    /**
     * Gets the video encoding as defined by IANA (case-sensitive).
     * <p>
     * Example: "H263"
     * 
     * @param codec Audio encoding.
     */
    const base::String& getVideoCodec();

    /** 
     * Gets the recommended length of time in milliseconds 
     * represented by the media in an audio packet.
     * 
     * @return pTime in milliseconds.
     */
    int getPTime();

    /**
     * Gets the maximum length of time in milliseconds 
     * represented by the media in an audio packet.
     * 
     * @return maxPTime in milliseconds.
     */
    int getMaxPTime();

    /** 
     * Sets the recommended length of time in milliseconds 
     * represented by the media in an audio packet.
     * 
     * @param pTime pTime in milliseconds.
     */
    void setPTime(int pTime);

    /** 
     * Sets the maximum length of time in milliseconds 
     * represented by the media in an audio packet.
     * 
     * @param maxPTime maxPTime in milliseconds.
     */
    void setMaxPTime(int maxPTime);

    /**
     * @return <code>true</code> if the media is video, <code>false</code>
     *         if the media is audio only.
     */
    bool isVideo();

    /**
     * @return Playout offset for the audio data relative the beginning 
     *         of the media.
     */
    virtual unsigned getAudioStartTimeOffset();

    /**
     * @return Playout offset for the video data relative the beginning 
     *         of the media.
     */
    virtual unsigned getVideoStartTimeOffset();

    virtual unsigned getAudioBlockSize() = 0;
    virtual unsigned getAudioPacketCount() = 0;

    virtual unsigned getVideoBlockSize() = 0;
    virtual unsigned getVideoPacketCount() = 0;

    virtual void getData(RtpBlockHandler& blockHandler) = 0;

    inline JNIEnv* const getMediaObjectJniEnv()
    {
        return mediaObject->getJniEnv();
    };

protected:
    java::MediaObject* mediaObject;
    base::String m_videoCodec;
    bool m_isVideo;
    int m_pTime;
    int m_maxPTime;
	JNIEnv* m_env;
private:
	static const char *MP_CLASSNAME;
};

#endif /*MEDIAPARSER_H_*/
