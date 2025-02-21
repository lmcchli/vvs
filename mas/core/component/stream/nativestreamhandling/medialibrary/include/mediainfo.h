/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAINFO_H_
#define MEDIAINFO_H_

#include "platform.h"
#include "int.h"

/**
 * Base class for media info such as WavInfo etc.
 * This class should contain common information for media types.
 * @author
 */
class MEDIALIB_CLASS_EXPORT MediaInfo
{
public:
    MediaInfo() :
            mAudioSampleRate(8000), mVideoSampleRate(90000)
    {
    }
    virtual ~MediaInfo()
    {
    }

    /**
     * Sets the sample rate of the media, i.e. number of samples per second.
     * 
     * @param sr The samplerate of the media in samples/sec
     */
    void setAudioSampleRate(uint32_t sr);

    /**
     * Returns the sample rate of the media. 
     * 
     * @return The sample rate, samples/sec, for the media.
     */
    uint32_t getAudioSampleRate() const;

    /**
     * Sets the sample rate of the media, i.e. number of samples per second.
     * 
     * @param sr The samplerate of the media in samples/sec
     */
    void setVideoSampleRate(uint32_t sr);

    /**
     * Returns the sample rate of the media. 
     * 
     * @return The sample rate, samples/sec, for the media.
     */
    uint32_t getVideoSampleRate() const;

private:
    uint32_t mAudioSampleRate;
    uint32_t mVideoSampleRate;
    // Common media info
};

inline void MediaInfo::setAudioSampleRate(uint32_t sr)
{
    mAudioSampleRate = sr;
}

inline uint32_t MediaInfo::getAudioSampleRate() const
{
    return mAudioSampleRate;
}

inline void MediaInfo::setVideoSampleRate(uint32_t sr)
{
    mVideoSampleRate = sr;
}

inline uint32_t MediaInfo::getVideoSampleRate() const
{
    return mVideoSampleRate;
}

#endif /*MEDIAINFO_H_*/
