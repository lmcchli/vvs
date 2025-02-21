/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVBUILDER_H_
#define WAVBUILDER_H_

//#include <cc++/config.h>
#include "platform.h"
#include "int.h"
#include "mediabuilder.h"
#include "wavinfo.h"
#include "jni.h"

#include <sys/types.h>

class MediaInfo;

/**
 * This class contains utility-methods for building media files on
 * the Wav-format.
 *
 */
class MEDIALIB_CLASS_EXPORT WavBuilder: public MediaBuilder
{
private:
    const MovAudioChunkContainer* mAudioChunks;
    unsigned mDuration;
    int mAudioCompression;
    WavInfo mInfo;

public:
    /**
     * No instances of this class should be created.
     */
    WavBuilder(JNIEnv* env);
    virtual ~WavBuilder();

    /* Doc in baseclass. */
    void setAudioCodec(const base::String& codecName);

    /* Doc in baseclass. */
    void setAudioChunks(const MovAudioChunkContainer& audioChunks);

    /* Doc in baseclass. */
    bool store(MediaObjectWriter& movWriter);

    /* Doc in baseclass. */
    const unsigned getDuration();

    /**
     * Returns the WAV infomation.
     */
    MediaInfo& getInfo();

    /* Doc in baseclass. */
    bool isAudioOnlyBuilder();

private:
    /**
     * Creates a Wav header and stores the header data in a dynamically
     * allocated memory area.
     * <p>
     * Note that the caller is responsible for the allocated memory.
     *
     * @param totalMediaSize Size of the data chunk in octets (8 bits).
     * @param wavHeaderData  Contains the adress to the memory allocated
     *                       by this method after the method call.
     *
     * @return The size of the header data in octets (8 bits).
     */
    unsigned createHeader(unsigned totalMediaSize, uint8* wavHeaderData);

    static void writeDW(uint8 *ptr, uint32_t &udw);
    static void writeW(uint8 *ptr, uint16_t &udw);
};

inline const unsigned WavBuilder::getDuration()
{
    return mDuration;
}

inline MediaInfo& WavBuilder::getInfo()
{
    return mInfo;
}

inline bool WavBuilder::isAudioOnlyBuilder()
{
    return true;
}

#endif /*WAVBUILDER_H_*/
