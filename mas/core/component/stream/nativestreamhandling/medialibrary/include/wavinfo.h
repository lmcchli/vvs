/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVINFO_H_
#define WAVINFO_H_
#include "int.h"
#include "mediainfo.h"
#include "platform.h"

namespace CompressionCode {
enum CompressionCode
{
    UNKNOWN = 0, PCM = 1, ALAW = 6, ULAW = 7, ADPCM = 17
};
};
/**
 * Contains information for a WAVE file. 
 * 
 * This class contains WAVE information of a wave file such 
 * as compression code and sample rate. It also has a pointer
 * to the data chunk. 
 * 
 * @see MediaInfo
 * 
 * @author Mats Egland
 * 
 */
class MEDIALIB_CLASS_EXPORT WavInfo: public MediaInfo
{

public:
    /**
     * Constructs a WavInfo with the foolowing default values:
     * <ul>
     * <li>Compression code=7</li>
     * <li>Number of channels=1</li>
     * <li>Sample rate=8000</li>
     * <li>Average bytes per second=8000</li>
     * <li>Block align=1</li>
     * <li>Significant bits per sample=8</li>
     * </ul>
     */
    WavInfo() :
            mCompressionCode(CompressionCode::ULAW), mNumChannels(1), mByteRate(8000), mBlockAlign(1), mBitsPerSample(8)
    {
    }
    /**
     * Virtual destructor
     */
    virtual ~WavInfo()
    {/* nothing to delete */
    }

    /**
     * Returns the Riff length (in bytes), i.e. the size of the RIFF chunk minus the
     * 8 bits of header. It is also the size of the file minus 8 bytes for the two
     * fields not included in this count: ChunkId and ChunkSize.
     * 
     * @return The length in bytes of the RIFF chunk
     */
    uint32_t getRiffLength() const;
    /**
     * Sets the Riff length (in bytes), i.e. the size of the RIFF chunk minus the
     * 8 bits of header. It is also the size of the file minus 8 bytes for the two
     * fields not included in this count: ChunkId and ChunkSize.
     * 
     * @param riffLength The length in bytes of the RIFF chunk
     */
    void setRiffLength(uint32_t riffLength);

    /**
     * Sets the compression code, i.e. the encoding used, of the Wav. For example
     * 1 for PCMU.
     * 
     * @param cc The compression code as an integer.
     */
    void setCompressionCode(CompressionCode::CompressionCode cc);

    /**
     * Returns the compression code of the WAV.
     * 
     * @return The compression code as a integer.
     */
    CompressionCode::CompressionCode getCompressionCode() const;

    /**
     * Sets the number of channels of the WAV. 
     * 
     * @param nc The number of channels.
     */
    void setNumChannels(uint16_t nc);

    /**
     * returns the number of channels in the WAV.
     * 
     * @return the number of channels in the WAV.
     */
    uint16_t getNumChannels() const;

    /**
     * Sets the byte rate, i.e. bytes/sec, that play every second.
     * = SampleRate * NumChannels * BitsPerSample/8
     * 
     * @param br The byte rate to set.
     */
    void setByteRate(uint32_t br);

    /**
     * Returns the byte rate, i.e. bytes/se, that play every second.
     * = SampleRate * NumChannels * BitsPerSample/8
     * 
     * @return The byte rate.
     */
    uint32_t getByteRate() const;

    /**
     * Sets the block align parameter for the WAV. The block align value
     * is the number of bytes per sample, including all channels.
     * = NumChannels * (BitsPerSample/8)
     * 
     * @param ba the block align value to set.
     */
    void setBlockAlign(uint16_t ba);

    /**
     * Returns the block align parameter for the WAV. The block align value
     * is the number of bytes per sample, including all channels.
     * = NumChannels * (BitsPerSample/8)
     * 
     * @return The value of the block align of the WAV
     */
    uint16_t getBlockAlign() const;

    /**
     * Returns the bits per sample, i.e. the bit resolution of a sample point. 
     * A 16-bit waveform would give have this value sat to 16.
     * 
     * 8 = 8 bits, 16 = 16 bits etc
     * 
     * @return the bits per sample in the WAV.
     */
    uint16_t getBitsPerSample() const;

    /**
     * Sets the bits per sample, i.e. the bit resolution of a sample point. 
     * A 16-bit waveform would give have this value sat to 16.
     * 
     * 8 = 8 bits, 16 = 16 bits etc
     * 
     * @param bss The bits per sample value. 
     */
    void setBitsPerSample(uint16_t bss);

    /**
     * Sets the size of the data chunk. The size should be the value read in the size 
     * field of the data chunk (word-aligned) and by this the 8 bytes 
     * of the id and size fields is not included.
     * 
     * The size is data aligned, i.e it is the actual word-aligned size
     * 
     * @param dataChunkSize Word aligned size of data chunk
     */
    void setDataChunkSize(uint32_t dataChunkSize);
    /**
     * Returns the word-aligned, i.e. actual size of the data chunk,
     * minus the bytes of the id and size fields (8 bytes).
     * @return The size of the 'raw' data in the data chunk.
     */
    uint32_t getDataChunkSize() const;

    /**
     * Sets the number of samples in the data chunk.
     * 
     * @param nrOfSamples The total number of samples in the
     *                    data chunk. (From the fact chunk).
     */
    void setNumberOfSamples(uint32_t nrOfSamples);
    /**
     * Returns the number of samples in the data chunk.
     * 
     * @returns The total number of samples in the
     *          data chunk. (From the fact chunk).
     */
    uint32_t getNumberOfSamples();
private:

    /* The length of the RiffChunk, minus the 8 bits not included in this count*/
    uint32_t mRiffLength;

    /* ----- fmt header ------- */
    /** 
     * What type of encoding file is using, PCM=1 
     */
    CompressionCode::CompressionCode mCompressionCode;

    /**
     *  The number of channels 1 = mono, 2=stereo
     */
    uint16_t mNumChannels;

    /** 
     * The number of bytes that play every second. 
     * := SampleRate * NumChannels * BitsPerSample/8
     */
    uint32_t mByteRate;

    /**
     * The block align value
     * is the number of bytes per sample, including all channels.
     * = NumChannels * (BitsPerSample/8)
     */
    uint16_t mBlockAlign;

    /**
     * The bit resolution of a sample point, i.e. a 16-bit waveform
     * would have this value set to 16.
     * 
     * 8 = 8 bits, 16 = 16 bits, etc
     */
    uint16_t mBitsPerSample;

    /**
     * The size of the data chunk in bytes minus the id and size fields (8 bytes).
     * The size is data aligned, i.e it is the actual word-aligned size.
     */
    uint32_t mDataChunkSize;
    /**
     * The number of samples in data chunk. 
     */
    uint32_t mNumberOfSamples;
};
#endif /*WAVINFO_H_*/
