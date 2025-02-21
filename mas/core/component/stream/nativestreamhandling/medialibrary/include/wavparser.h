/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVPARSER_H_
#define WAVPARSER_H_

#include <boost/scoped_ptr.hpp>

#include "mediaparser.h"
#include "wavinfo.h"
#include "jni.h"

class MediaLibraryException;
namespace java {
class MediaObject;
};
class WavReader;

/**
 * Subtype of MediaParser that parses a MediaObject that comprise a WAV file.
 * Returns a WavInfo.
 * <p>
 * The data of the parser MediaObject must be of WAVE type or else
 * MediaObjectException is thrown.
 * <p>
 * Note the <code>init</code>-method that must be called after the constructor.
 * <p>
 * <pre>
 * Usage:
 *      Example, Use the parser to stream a WAVE - MediaObject
 *
 *          WavParser wavParser(myMediaObject);
 *          wavParser.init();
 *
 *          wavParser.parse();
 *
 *          int chunkCount(int getAudioChunkCount());
 *          unsigned chunkSize(0);
 *          const char* buffer(NULL);
 *          for (int i = 0; i < chunkCount; i++) {
 *              buffer = mParser->getAudioChunk(chunkSize, i);
 *                  ... stream data ...
 *          }
 * </pre>
 *
 * @author Mats Egland, J�rgen Terner
 */
class WavParser: public MediaParser
{

public:
    /**
     * Constructor that takes the MediaObject to parse as argument
     *
     * @param mediaObject Reference to the MediaObject to parse
     */
    WavParser(java::MediaObject* mediaObject);

    /**
     * Destructor
     */
    virtual ~WavParser();

    /**
     * Must be called before any other method to initiate the parser.
     *
     * @throws invalid_argument      If the mediaobject given in the
     *                               constructor is mutable.
     * @throws out_of_range          If the mediaobject given in the
     *                               constructor is not long enough
     *                               to contain required information
     *                               (the RIFF header for example).
     * @throws MediaLibraryException If the data in the passed MediaObject
     *                               is not of type WAVE.
     */
    virtual void init();

    /**
     * Overrides the parse function from the base class.
     * <p>
     * After this method returns, the location of the parser
     * will be at the first raw byte of the data chunk, i.e. after the
     * chunkId and chunkSize of the data chunk.
     *
     * @throws std::logic_error      If the <code>init</code>-method has not
     *                               been called.
     * @throws MediaLibraryException If the the format of the data
     * (i.e. the MediaObject) does not comply with the WAV standard.
     * For example if fmt header is missing.
     */
    virtual bool parse();

    /**
     * Returns the media duration in milli seconds.
     * If media contains multi media data/tracks the maximum of the
     * durations is returned.
     *
     * @return the media duration in milli seconds.
     */
    virtual unsigned getDuration();

    /**
     * Tells that the reading should start at the given position.
     * If the media is video, the cursor is adjusted to the closest
     * intra frame.
     *
     * @param cursor Position in octets the reading should start from.
     */
    virtual void setCursor(long cursor);

    /**
     * Get the cursor value.
     * Since the cursor is adjusted to the closest frame for some medias this value
     * may be different than the value passed in setCursor.
     *
     * @param cursor Position in milliseconds the reading should start from.
     */
    virtual long getCursor();

    /* Documented in base class */
    virtual const unsigned char* getAudioChunk(unsigned& chunkSize, int chunkIndex);

    /* Documented in base class */
    virtual int getAudioChunkCount();

    /**
     * Returns the WavReader used by the parser to read the MediaObject.
     * <p>
     * The reader is returned as const as the reader must not be altered,
     * as then the behavior of the parser would be non-deterministic.
     *
     * @return Reference to the used WavReader
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    const WavReader* getWavReader() const;

    /**
     * @return The parse result.
     */
    const WavInfo& getMediaInfo();

    /* Methods for determining the amount of required RTP memory */
    unsigned getAudioBlockSize();
    unsigned getAudioPacketCount();
    unsigned getVideoBlockSize();
    unsigned getVideoPacketCount();

    void getData(RtpBlockHandler& blockHandler);

    virtual const base::String getAudioCodec();

private:
	static const char* WAV_CLASSNAME;
    /** Size of each RTP packet. */
    size_t mPacketSize;

    /** Number of RTP packets. */
    unsigned mPacketCount;

    /**
     * The WavReader used to read the MediaObject.
     */
    boost::scoped_ptr<WavReader> mWavReader;

    /**
     * The WavInfo that is filled with WAV information and returned.
     */
    WavInfo mWavInfo;

    /** The packet with index zero starts at this position. */
    uint32_t mCursor;

    /** Size of data chunk. The word-aligned size, i.e the actual size of the
     * data
     */
    uint32_t mDataChunkSize;

    /** The start of the data chunk */
    uint32_t mDataChunkStartPosition;

    /** <code>true</code> if the <code>init</code>-method has been called. */
    bool mIsInitiated;

    /**
     * Seeks the the fmt chunk parses it and injects the data of it into the
     * mWavInfo member.
     * <p>
     * Assumes that the current location is at the fmt or a chunk prior to the
     * fmt chunk.
     * <p>
     * The location of the reader should be at the id field of the fmt chunk
     * after a successfull call. If not successfull the location should
     * be untouched.
     *
     * @throws MediaLibraryException if the fmt chunk is not found
     */
    void parseFmtChunk();

    /**
     * Seeks the the fact chunk parses it and injects the data of it into the
     * mWavInfo member.
     * Assumes that the current location is at the fact or a chunk prior to the
     * fact chunk.
     * <p>
     * The location of the reader should be at the id field of the fact chunk
     * after a successfull call. If not successfull the location should
     * be untouched.
     *
     * @throws MediaLibraryException if the fact chunk is not found
     */
    void parseFactChunk();

    /**
     * Seeks the data chunk.
     * The location of the reader should be at the id field of the data chunk
     * after a successfull call.If not successfull the location should
     * be untouched.
     * <p>
     * Assumes that the current location is at the data or a chunk prior to the
     * data chunk.
     *
     * @throws MediaLibraryException if the data chunk is not found
     */
    void parseDataChunk();
};

#endif /*WAVPARSER_H_*/
