/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECTREADER_H_
#define MEDIAOBJECTREADER_H_

#include "int.h"
#include "mediabuffer.h"
#include "platform.h"
#include "jni.h"

#include <memory>
#include <typeinfo>

#include <vector>
#include <stdexcept>

class MediaLibraryException;
namespace java {
class MediaObject;
};

#if defined(WIN32)
#pragma warning( disable : 4290 )
#endif

/**
 * Class that reads the data of a MediaObject to provide read-methods.
 * The data is organized in a buffer, that is actually a list of many
 * buffers (MediaBuffers). For the client the data looks like one buffer.
 * <p>
 * Hides MediaObject-details from the client.
 * <p>
 * Note the <code>init</code>-method that must be called after the constructor.
 * <p>
 * Usage:<br>
 * To navigate:<br>
 * Example for the jumpForward(nrOfBytes) and jumpBackward(nrOfBytes)
 * functions:
 * <pre>
 *
 *  MediaObjectReader mor(...);
 *  mor.init();
 *
 * // jumps to the last byte and retreives a pointer to each
 * // character.
 * while (mor.getCurrentLocation() != MediaObjectReader::EOB) {
 *      const char *currentChar = getCurrentLocation();
 *      mor.jumpForward(1);
 *  }
 *
 *  // jumps to the first byte of the buffer
 *  while (mor.getCurrentLocation() != mor.firstByte()) {
 *      mor.jumpBackward(1);
 *  }
 *  // or more simple to get to the start
 *  mor.reset();
 * </pre>
 *
 * @author Mats Egland
 */
class MediaObjectReader
{
public:
    /**
     * End Of Buffer. The current location pointer will point will have this
     * value if end of buffer is reached.
     */
    static const char* EOB;

    /**
     * Constructor that takes a reference to the MediaObject
     * to read from.
     *
     * @param pBuffer The pointer to the start of the buffer
     * @param size    The size of the buffer in bytes
     * @param swap    Whether values should be byte-swapped when readed.
     *                Should be set if there are big-endian/little-endian
     *                differences between the buffer read and the platform.
     */
    MediaObjectReader(java::MediaObject *mediaObject, bool swap);

    /**
     * Virtual Destructor.
     */
    virtual ~MediaObjectReader();

    /**
     * Must be called before any other method to initiate the reader.
     *
     * @throws invalid_argument      If the mediaobject given in the
     *                               constructor is mutable.
     * @throws MediaLibraryException On failure to parse the data in the
     *                               the mediaobject given in the
     *                               constructor.
     */
    virtual void init();

    /**
     * Returns a pointer to the very first byte of the data.
     *
     * @return Pointer to first byte, i.e. start of data.
     */
    const char* firstByte() const;

    /**
     * Returns a pointer to the current location of the reader.
     * <p>
     * Note: The returned pointer is a copy of the internal location pointer.
     * So operations on this pointer will not affect the internal pointer.
     *
     * @return a pointer to the current char (byte) of the reader. This
     * pointer will point to the current character in the read MediaObject.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    const char* getCurrentLocation();

    /**
     * Returns the bytes read , i.e. the difference in bytes between
     * the current location and the beginning of the buffer. The current byte
     * is excluded in the count which means that if the current location is at
     * the first byte, the bytes read is 0.
     * <p>
     * Note: This function does NOT affect the current location of the buffer.
     *
     * @return The nr of bytes of the bytes read in the buffer.
     */
    const size_t bytesRead() const;

    /**
     * Returns the bytes left to read in this buffer counting from,
     * and including the byte at the the current position. The current byte
     * is included in the count which means that if the current position is at
     * the last byte of the buffer the bytes left is 1.
     * <p>
     * Note: This function does NOT affect the current location of the buffer.
     *
     * @return The nr of bytes left in the buffer, i.e. that is not yet read.
     */
    const size_t bytesLeft() const;

    /**
     * Jumps forward in the buffer the given number of bytes
     * and returns the actual bytes jumped including the jump to EOB.
     * <p>
     * If the number of bytes to jump is outside the buffer the
     * location will be sat to EOB.
     * <pre>
     * Example: 4 Bytes of buffer left
     *  1) A jumpForward(3) sets the location to the very last byte and 3
     *     is returned.
     *  2) A jumpForward(4) sets the location to EOB and 4 is returned.
     *  3) A jumpForward(5) sets the location to EOB and 4 is returned.
     * </pre>
     *
     * @return The actual bytes jumped, including the eventual jump from
     *         last byte to EOB.
     *
     * @throws std::logic_error      If the <code>init</code>-method has not
     *                               been called.
     * @throws std::invalid_argument If nrOfByte negative
     */
    size_t jumpForward(int nrOfByte);

    /**
     * Jumps backward in the buffer the given number of bytes
     * and returns the actual bytes jumped.
     * <p>
     * This function will allow the current location to point as
     * far as to the very first byte of the buffer (i.e. not beyond the
     * byte). This means that the current location of the
     * buffer can only jump to the pointer returned from the
     * firstByte() function.
     * <pre>
     * Example: At the second byte, i.e. bytesRead() returns 1.
     *   1) A jumpBackward(1) takes us to the first byte and 1 is returned.
     *   2) A jumpBackward(2) takes us to the first byte and 1 is returned.
     * </pre>
     *
     * @return Number of bytes actually jumped.
     *
     * @throws std::logic_error      If the <code>init</code>-method has not
     *                               been called.
     * @throws std::invalid_argument If nrOfByte negative
     */
    size_t jumpBackward(int nrOfByte);

    /**
     * Marks the current position. Can be used with the gotoMark method
     * to bookmark the buffer.
     * <p>
     * The initial position of the mark is at the beginning of the first
     * buffer.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    void setMark();

    /**
     * Sets the position in the media object to position.
     * If the position is outside the buffer the
     * location will be sat to EOB.
     *
     * @param position The position to goto. 0 is first byte in media object.
     *
     */
    size_t seek(size_t position);

    /**
     * Returns the current position in the media object.
     * The position can be an integer equal or greater than 0, or EOB
     *
     * @return the current position in the media object.
     *
     */
    size_t tell();

    /**
     * Set the current location to the mark set with the mark() method.
     * Jumps to the beginning of the buffer if mark has not been called.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    void gotoMark();

    /**
     * resets the current location to the very start of the MediaObject
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    void reset();

    /**
     * Reads the next word into the parameter uw,
     * and moves the current location to the next word.
     *
     * If less than 2 bytes is left of data, no value is
     * read into the uw parameter and EOB is returned;
     * - AND the current location will be set to EOB.
     *
     * If exactly 2 bytes is left of data, the read
     * is successfull and the current location
     * will be moved to EOB.
     *
     * @return The new location of the buffer, or EOB of end
     *         of buffer reached.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    const char* readW(uint16_t &uw);

    /**
     * Reads the next double word into the parameter duw,
     * and moves the current location to the next double word.
     *
     * If less than 4 bytes is left of data, no value is
     * read into the uw parameter and EOB is returned;
     * - AND the current location will be set to EOB.
     *
     * If exactly 4 bytes is left of data, the read
     * is successfull and the current location
     * will be moved to EOB.
     *
     * @return The new location of the buffer, or EOB of end
     *         of buffer reached.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    const char* readDW(uint32_t &duw);

    /**
     * Requests a a pointer to data at the current location of size "size"
     * and sets the location to the next byte, i.e. the byte after the data
     * returned, or EOB if end of buffer reached.
     * <p>
     * The actual size returned is given by the actualSize parameter. If the
     * actualSize is less than the size the size was outside the MediaObject's
     * data area.
     * <p>
     * Important: The pointer returned CAN point to the internal MediaObject's
     * data, this is to avoid unnessacary copying, so be carefull with
     * it so that the data of the MediaObject is unchanged. The method will
     * always try to return a pointer to the internal MediaObject's data, but
     * if the requested data crosses between the MediaObject's data areas
     * this cannot be done and instead a copy of the data is returned.
     * <p>
     * <pre>
     * WARNING: THE DATA RETURNED IS ONLY VALID UNTIL THE NEXT CALL
     *          TO THIS FUNCTION. Be sure to use the data before the
     *          next call to getData.
     * ----------------------------------------------------------------
     * Usage:
     *    size_t requestedSize = xxx;
     *    const char* pData;
     *    while ((pData = reader.getData(requestedSize, bytesReturned)) !=
     *           MediaObjectReader::EOB) {
     *      // ... do stuff with data, stream it or something
     *
     *    }
     * ------------------------------------------------------------------------
     * </pre>
     *
     * @param size       Requested buffer size.
     * @param actualSize The actual bytes returned.
     *
     * @return Pointer to data buffer, or EOB if at end of buffer and no more
     *         data.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    const char* getData(size_t size, unsigned &actualSize);
    // lmcstlo

    //const char* getData(size_t size, unsigned int &actualSize);

    /**
     * Compares the length nr of the passed characters
     * with the characters at the current location and returns
     * true if equal.
     *
     * Does not affect location in buffer.
     *
     * @param str           the string to compare content against.
     * @param ignoreCase    If true the comparison ignore case, if false
     *                      it is case-sensitive.
     *
     * @return true if passed characters of matches length numbers
     * of characters at the current location
     *
     * @throws std::logic_error  If the <code>init</code>-method has not
     *                           been called.
     * @throws std::out_of_range If the comparison tries to reach outside
     * the MediaObject's boundary or if the passed nr of bytes to compare
     * (the length parameter) is higher than strlen of passed string
     * (the str parameter)
     */
    bool compareStr(const char *str, size_t length, bool ignoreCase);

    /**
     * Returns the total size in bytes of the data in the underlying
     * MediaObject.
     *
     * @return the total number of bytes of the data in the MediaObject.
     */
    long getTotalSize() const;

    /**
     * TODO MOVE TO PROTECTED
     * Reads size nr of bytes of at the current location into the
     * passed buffer. If the number of bytes requested is not available
     * the method reads all that are available and returns the
     * actual bytes read.
     * <p>
     * This function is to be used when values crosses mediabuffers.
     * The function will read more buffers from the MediaObject if needed.
     * <p>
     * NOTE: This method doesn't affect the current location.
     *
     * @param buf The buffer to be filled
     *
     * @return The number of bytes actually read
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    size_t readInto(char buffer[], size_t size);

protected:
    // The MediaObject to read from
    java::MediaObject *mediaObject;

    // Vector of the mediaBuffers read from the mediaObject
    std::vector<MediaBuffer*> mediaBuffers;

    // If bytes should be swapped when read
    bool swap;

    // Index of the current MediaBuffer
    size_t mCurrentMediaBufferIndex;

    /**
     * Pointer to the current MediaBuffer
     */
    MediaBuffer* mpCurrentMediaBuffer;

    /**
     * Pointer to the very first byte.
     */
    const char* mpFirstByte;

    // Mark indexes
    size_t markMediaBufferIndex; // The mark in the MediaBuffer
    size_t markBufferIndex;      // The mark of what MediaBuffer
    size_t markBytesRead;        // The bytes read at the mark
    size_t markBytesLeft;        // The bytes left at the mark

    /**
     * The bytes read , i.e. the difference in bytes between the beginning
     * of the data and the current location of the data.
     * 0 <= bytesRead <= totalSize-1
     *
     * bytesRead = totalSize-bytesLeft
     */
    size_t mBytesRead;

    /**
     * The bytes left to read ,
     * i.e. the difference in bytes between the very last byte
     * of the data and the current location of the data.
     *
     * 1 <= bytesleft <= totalsize
     * bytesLeft = totalSize-bytesRead
     */
    size_t mBytesLeft;

    /* ---- Functions ------*/
    /**
     * Returns the current MediaBuffer.
     */
    MediaBuffer* getCurrentMediaBuffer() const;

    /**
     * Reads the next buffer from the MediaObject, creates a MediaBuffer
     * of it and inserts it into the mediaBuffer vector
     * <p>
     * NOTE: This method doesn't affect the current location.
     *
     * @return true if read was successfull, false if no more buffers
     * are left to read in the MediaObject.
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    bool readNextBuffer();

    /**
     * Compares the char at the current location with the passed char.
     *
     * @return true if the character passed is same as current char
     *
     * @throws std::logic_error If the <code>init</code>-method has not
     *                          been called.
     */
    bool compareTo(const char c, bool ignoreCase);

private:
    /**
     * Pointer to the last buffer read with the <code>getData</code>-method.
     * This might be dynamically allocated if the requested data was located
     * in two or more buffers. In this case
     * <code>lastBufferShouldBeDeleted</code> is <code>true</code> and this
     * pointer should be deleted in the next call to getData
     * (or in the destructor).
     */
    const char* mLastBuffer;

    /**
     * Used by the getData method to mark returned dynamically allocated
     * memory to be deleted.
     */

    bool mLastBufferShouldBeDeleted;

    /** The total number of bytes in the underlying MediaObject's data */
    long mTotalSize;

    /** <code>true</code> if the <code>init</code>-method has been called. */
    bool mIsInitiated;

};
#endif /*MEDIAOBJECTREADER_H_*/
