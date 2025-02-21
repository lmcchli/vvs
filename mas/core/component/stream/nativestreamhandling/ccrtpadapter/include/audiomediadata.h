/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef AUDIOMEDIADATA_H_
#define AUDIOMEDIADATA_H_

#include <base_std.h>
#include <ccrtp/queuebase.h>

#include "jni.h"
#include "movaudiochunk.h"

/**
 * Media data container. Exists because the class AppDataUnit
 * cannot be extended or created in a useful way and that is needed
 * when real RTP-packets are mixed with generated comfort noise data.
 * 
 * @author J�rgen Terner
 */
class AudioMediaData
{
private:
    AudioMediaData(AudioMediaData &rhs);
    AudioMediaData& operator=(const AudioMediaData &rhs);

    /** RTP timestamp or -1 if generated data. */
    uint32 mTimestamp;
    uint32 mOriginalTimeStamp;

    /** The data in a format understandable for the media library. */
    std::auto_ptr<MovAudioChunk> mChunk;

    /** The extended RTP sequence number. */
    uint32 mExtendedSeqNum;

public:

    /**
     * Creates a new CNData and takes responsibility to delete
     * the given memory.
     * 
     * @param data Pointer to allocated memory.
     * @param size Size of allocated memory.
     */
    AudioMediaData(JNIEnv* env, const uint8* data, size_t size);

    /**
     * Creates a new mediadata and takes responsibility to delete
     * the given memory.
     * 
     * @param data Pointer to allocated memory.
     * @param size Size of allocated memory.
     * @param timestamp rtp timestamp of the media.
     */
    AudioMediaData(JNIEnv* env, const uint8* data, size_t size, uint32 timestamp, uint32 extendedSeqNum);

    /**
     * Creates a new CNData and takes responsibility to delete
     * the given object.
     * 
     * @param data Pointer to allocated memory.
     * @param size Size of allocated memory.
     * @param timestamp rtp timestamp of the media.
     * @param originalTimestamp - rtp timestamp of the media.
     */
    AudioMediaData(JNIEnv* env, const uint8* data, size_t size, uint32 timestamp, uint32 originalTimestamp, uint32 extendedSeqNum);

    /**
     * Destructor.
     */
    ~AudioMediaData();

    /**
     * Deletes the first <code>amount</code> part of the data. 
     * The remaining data will be copied to a new allocated memory area.
     * If amount is >= size, nothing will happen!
     * 
     * @param amount Amount of data to skip in octets. Must be < size or
     *               the operation will be ignored.
     */
    void skip(size_t amount);

    /**
     * Deletes the last <code>amount</code> part of the data. 
     * The remaining data will be copied to a new allocated memory area.
     * If amount is >= size, nothing will happen!
     * 
     * @param amount Amount of data to cut in octets. Must be < size or
     *               the operation will be ignored.
     */
    void cut(size_t amount);

    /**
     * Gets the audio data in a format understandable for the media library.
     * 
     * @return Audio data as a media library type.
     */
    MovAudioChunk& getAudioChunk();

    MovAudioChunk* releaseAudioChunk();

    /**
     * Gets the RTP timestamp for the data if received as an RTP packet.
     * If the data is generated silence, there is no timestamp and -1 will
     * be returned.
     * 
     * @return RTP timestamp for this data or -1 if the data is generated
     *         to replace a silent period.
     */
    uint32 getRTPTimestamp();

    /**
     * Gets the extended RTP sequence number.
     */
    uint32 getExtendedSeqNum();
};
#endif /*AUDIOMEDIADATA_H_*/
