/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVREADER_H_
#define WAVREADER_H_

#include "mediaobjectreader.h"
#include "jni.h"

#include <base_include.h>

class MediaLibraryException;
namespace java {
class MediaObject;
};

/**
 * This class extends MediaObjectReader to add WAV-specific functions.
 * <p>
 * Note the <code>init</code>-method that must be called after the constructor.
 * 
 * @author Mats Egland
 */
class WavReader: public MediaObjectReader
{
public:
    /**
     * Constructor that takes the MediaObject to read as parameter.
     * 
     * @param mediaObject The MediaObject to read
     */
    WavReader(java::MediaObject *mediaObject);

    /**
     * Virtual Destructor
     */
    virtual ~WavReader();

    /**
     * Must be called before any other method to initiate the reader.
     * 
     * @throws invalid_argument      If the mediaobject given in the 
     *                               constructor is mutable.
     * @throws out_of_range          If the mediaobject given in the
     *                               constructor is not long enough
     *                               to contain required information
     *                               (the RIFF header for example).
     * @throws MediaLibraryException If the data in the MediaObject 
     *                               passed to the constructor is not of 
     *                               type WAVE.
     */
    virtual void init();

    /**
     * Returns the size of the RIFF Chunk. 
     */
    uint32_t getRiffSize();

    /**
     * Read the current chunk id into the passed string reference.
     * <p>
     * IMPORTANT: This function assumes that the current location is at 
     * the beginning of a chunk, i.e the current location must point at the id.
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic. This method does not affect the current location.
     * 
     * @throws std::logic_error  If the <code>init</code>-method has not 
     *                           been called. 
     * @throws std::out_of_range If there is not four bytes left, 
     * i.e. the four bytes required for the chunk id, of the data 
     * in the MediaObject. 
     */
    void getChunkId(base::String &str);

    /**
     * Compares the given string with the id of the current chunk. 
     * Only the numbers of characters in the passed str will be compared.
     * For example if "fm" is passed only two characters in the
     * chunk id is compared. 
     * <p>
     * IMPORTANT: This function assumes that the current location is at the
     * beginning of a chunk, i.e the current location must point at the id. 
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic.
     * <p>
     * This method does not affect the current location.
     * 
     * @param str the string to compare, 
     * @param ignoreCase controls if the comparison is case-sensitive or not.
     * If true the comparison ignore case, if false the comparison
     * is case-sensitive.
     * 
     * @return true if the string is equal to the id of the chunk 
     * 
     * @throws std::logic_error If the <code>init</code>-method has not 
     *                          been called. 
     * @throw std::out_of_range If the comparison tries to reach outside
     * the MediaObject's boundary or if the length of the passed string
     * is greater than 4 bytes. 
     */
    bool compareChunkId(const char *str, bool ignoreCase);

    /**
     * Reads the size of the current chunk into the given
     * parameter. This is the actual value of the WAVE, so the 
     * actual size of the chunk may be word aligned, to get the
     * actual size of the chunk use the getAlignedChunkSize() method.
     * <p>
     * This method assumes that the current location
     * of the internal buffer points to the beginning of the chunk, 
     * i.e. at the chunk id. 
     * <p>
     * IMPORTANT: This function assumes that the current location is at the 
     * beginning of a chunk, i.e the current location must point at the id. 
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic.
     * <p>
     * This method will restore the internal state of the buffer location
     * so that it does not affect the current location. 
     * 
     * @param chunkSize The parameter to be filled with the aligned size of 
     *                  chunk. 
     * 
     * @throws std::logic_error If the <code>init</code>-method has not 
     *                          been called. 
     * @throw std::out_of_range If there is not 8 bytes left of the MediaObject. I.e.
     * the bytes of the chunk id and the chunk size.
     */
    void getChunkSize(uint32_t &chunkSize);

    /**
     * Reads the aligned (i.e. the actual) size of the current chunk into the
     * given parameter. If the size read from the WAVE is odd it is word 
     * aligned (i.e.) added with 1 before returned. 
     * <p>
     * This method assumes that the current location
     * of the internal buffer points to the beginning of the chunk, 
     * i.e. at the chunk id. 
     * <p>
     * IMPORTANT: This function assumes that the current location is at the 
     * beginning of a chunk, i.e the current location must point at the id. 
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic.
     * <p>
     * This method will restore the internal state of the buffer location
     * so that it does not affect the current location. 
     * 
     * @param chunkSize The parameter to be filled with the aligned size of 
     *                  chunk. 
     * 
     * @throws std::logic_error If the <code>init</code>-method has not 
     *                          been called. 
     * @throw std::out_of_range If there is not 8 bytes left of the MediaObject. I.e.
     *                          the bytes of the chunk id and the chunk size.
     */
    void getAlignedChunkSize(uint32_t &chunkSize);

    /**
     * Moves the current location of the to the next wave chunk. 
     * If no more chunks exist, false is returned, the location will then
     * be at the byte after the current chunk, probably MediaObjectReader::EOB.
     * <p>
     * IMPORTANT: This function assumes that the current location is at the 
     * beginning of a chunk, i.e the current location must point at the id. 
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic.
     * <p>
     * Note: If the current location is the start of the buffer, i.e the 
     * RIFF chunk, the first chunk will be its first child, which is
     * typically the format chunk. 
     *    
     * @return true if successfull, ie there was another chunk. 
     *              If no more chunks exists false is returned.
     * 
     * @throws std::logic_error If the <code>init</code>-method has not 
     *                          been called. 
     */
    bool nextChunk();

    /**
     * Seeks the MediaObject for the specified chunk, beginning at
     * the current location and updates the location to point to
     * the chunk if found. If not found the location is untouched.  
     * <p>
     * If the current location matches the seeked chunk the current
     * location is returned, i.e. the current chunk is returned.
     * <p>
     * IMPORTANT: This function assumes that the current location is at 
     * the beginning of a chunk, i.e the current location must point at the id.
     * If function is called when this requirement is not met, the behaviour 
     * is not deterministic. 
     * 
     * @param chunkId The chunk id to seek for
     * @param ignoreCase If true the id-comparison ignore case, otherwise
     * the comparison is case-sensitive.
     * 
     * @return true if the chunk is found
     * 
     * @throws std::logic_error If the <code>init</code>-method has not 
     *                          been called. 
     * @throw std::out_of_range If length of passed id is greater than 4, or
     * end of data in MediaObject was unexpectedly reached.
     */
    bool seekChunk(const char *chunkId, bool ignoreCase);

private:
    /** The size of the Riff Chunk */
    uint32_t mRiffSize;

    /** <code>true</code> if the <code>init</code>-method has been called. */
    bool mIsInitiated;
};

inline uint32_t WavReader::getRiffSize()
{
    return mRiffSize;
}

#endif /*WAVREADER_H_*/
