/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIABUFFER_H_
#define MEDIABUFFER_H_

#include <stdexcept>
#if defined(WIN32)
#pragma warning( disable : 4290 )
#endif
#include "int.h"
#include "jni.h"
/**
 * Class that represent a buffer of bytes and function to navigate in the
 * buffer and read values, compare strings etc.
 * 
 * Has an internal pointer (char*) to the actual buffer. The actual buffer
 * memory is not allocated by this class, instead the buffer along with its size
 * is passed in via the construtor.
 * 
 * The class has the constant MediaBuffer::EOB to indicate end of buffer. The MediaBuffer::EOB
 * is a NULL value. 
 *   
 * <p>
 * Usage:
 *   
 * <pre>
 * 
 *  MediaBuffer mb(...)
 *  
 *  // reads all words in buffer
 *  uint16_t uw;
 *  while (mediaBuffer.readW(uw) != MediaBuffer::EOB) {
 *      // do stuff with uw     
 *  }
 *  
 *  // iterates over each byte in the buffer and retreives a pointer to each byte
 *  while (mediaBuffer.getCurrentLocation() != MediaBuffer::EOB) {
 *      mediaBuffer.jumpForward(1);
 *      const char* location = mediaBuffer.getCurrentLocation();
 *  }
 *  
 *  // jumps to the last byte in the buffer, i.e the byte preceding MediaBuffer::EOB
 *  mb.gotoLastByte();
 *  
 *  // jumps to the first byte of the buffer and retreives a pointer to each byte
 *  while (mediaBuffer.getCurrentLocation() != mediaBuffer.firstByte()) {
 *      mediaBuffer.jumpBackward(1);
 *      const char *currentChar = mediaBuffer.getCurrentLocation();
 *  }
 *   
 * 
 *  // or more simple...
 *  mb.gotoFirstByte();
 * </pre>
 * 
 * @author Mats Egland
 */

class MediaBuffer
{
public:
    /**
     * End Of Buffer. The current location pointer will point will have this value
     * if end of buffer is reached.
     */
    static const char* EOB;
    /**
     * Constructor that takes a pointer to the media buffer to wrap
     * and the size of it.
     * 
     * The size of the buffer must be passed in order to 
     * determine if access is in valid ranges.
     * 
     * 
     * @param pFirstByte The pointer to the start of the buffer
     * @size The size of the buffer in bytes
     * @swap Whether values should be byte-swapped when readed. Should be
     * set if there are big-endian/little-endian differences between 
     * the buffer read and the platform. 
     */
    MediaBuffer(JNIEnv* env, const char* const pFirstByte, const size_t size, bool swap);

    /**
     * Virtual Destructor.
     */
    virtual ~MediaBuffer();

    bool isOk();

    /**
     * Returns a pointer to the start of the internal buffer, i.e. to 
     * the very first byte in the buffer.
     * 
     * Note: This function does NOT affect the current location of the buffer.
     * 
     * @return a pointer to the first byte
     */
    const char* firstByte() const;

    /**
     * Returns a pointer to the very last byte of the buffer.
     * 
     * Note: This function does NOT affect the current location of the buffer.
     * 
     * @return a pointer to the last byte.
     */
    const char* lastByte() const;

    /**
     * Returns the bytes read , i.e. the difference in bytes between 
     * the current location and the beginning of the buffer. 
     * If at EOB bytesRead() should return total number of bytes. 
     * 
     * @return The number of bytes read. 
     */
    const size_t bytesRead() const;
    /**
     * Returns the bytes left to read in this buffer counting from, 
     * and including the byte at the the current position. 
     * The EOB is not counted, i.e. returns 0 if at EOB, returns 1
     * if at last byte.
     * 
     * @return the number of bytes left in buffer including the current
     *         byte, but not counting MediaBuffer::EOB. 
     */
    const size_t bytesLeft() const;
    /**
     * Returns the size of the buffer in bytes. EOB not included.
     * 
     * @return The number of bytes in buffer, not counting EOB.
     */
    size_t getBufferSize();
    /**
     *  Returns a pointer to the current location of the internal buffer.
     *  Note: The returned pointer is a copy of the internal location pointer.
     *  So operations on this pointer will not affect the internal pointer.
     */
    const char* getCurrentLocation() const;
    /**
     * Returns the index (0...n) of the current location. Can be used
     * with the gotoIndex(n) method.
     * @return the index of the current location in the buffer
     */
    const size_t getCurrentIndex() const;
    /**
     * Jumps to the index specified and returns a pointer to the new location.
     * 
     * If the index is outside buffer MediaBuffer::EOB will be returned.
     * 
     * @return The updated location of the buffer.
     * 
     * @throws std::invalid_argument if index negative
     */
    const char* gotoIndex(int index) throw (std::invalid_argument);
    /** 
     * Jumps forward in the internal buffer the given number of bytes
     * and returns the actual bytes jumped including the jump to MediaBuffer::EOB.
     * 
     * If the number of bytes to jump is outside the buffer the 
     * location will be sat to MediaBuffer::EOB.  
     * 
     * <p> <br>
     * Example: 4 Bytes of buffer, i.e. bytesLeft() method returns 4.
     *  1) A jumpForward(3) sets the location to the very last byte and 3 is returned. 
     *  2) A jumpForward(4) sets the location to MediaBuffer::EOB and 4 is returned.
     * <br>
     * 
     * @return The actual bytes jumped, including the eventual jump from 
     *         last byte to MediaBuffer::EOB.
     *
     */
    size_t jumpForward(size_t nrOfByte);
    /** 
     * Jumps backward in the buffer the given number of bytes
     * and returns the actual bytes jumped.
     * 
     * This function will allow the current location to point as 
     * far as to the very first byte of the buffer (i.e. not beyond the
     * byte). This means that the current location of the
     * buffer can only jump to the pointer returned from the
     * firstByte() function.
     * 
     * <p> <br>
     * Example: At the second byte, i.e. bytesRead() returns 1. 
     *   1) A jumpBackward(1) takes us to the first byte and 1 is returned.
     *   2) A jumpBackward(2) takes us to the first byte and 1 is returned.
     * <br>
     * 
     * @return Number of bytes actually jumped.
     * 
     */
    size_t jumpBackward(size_t nrOfByte);
    /** 
     * Sets the current location of the buffer to the first byte of the
     * buffer and returns a pointer to it.
     * 
     * @return pointer to the first byte of the buffer.
     */
    const char* gotoFirstByte();
    /**
     * Sets the current location of the buffer to the last byte of the
     * buffer, i.e. the location will point AT the last byte and 
     * returns a pointer to it.
     * 
     * @return pointer to the last byte of the buffer.
     */
    const char* gotoLastByte();
    /**
     * Sets the current location of the buffer to EOB. 
     * 
     * @return pointer to MediaBuffer::EOB
     */
    const char* gotoEOB();
    /** 
     * Reads the next word from the buffer into the parameter uw, 
     * and moves the current location to the next word. 
     * 
     * If less than 2 bytes is left of the buffer no value is 
     * read into the uw parameter and MediaBuffer::EOB is returned; 
     * - AND the current location will be set to MediaBuffer::EOB. 
     * 
     * If exactly 2 bytes is left of the buffer, the read 
     * is successfull and the current location
     * will be moved to MediaBuffer::EOB.
     * 
     * @return The new location of the buffer, or MediaBuffer::EOB of end
     *         of buffer reached.
     */
    const char* readW(uint16_t &uw);

    /** 
     * Reads the next double word from the buffer into the parameter duw, 
     * and moves the current location to the next double word. 
     * 
     * If less than 4 bytes is left of the buffer no value is 
     * read into the uw parameter and MediaBuffer::EOB is returned; 
     * - AND the current location will be set to MediaBuffer::EOB. 
     * 
     * If exactly 4 bytes is left of the buffer, the read will 
     * be successfull and the current location
     * will be moved to MediaBuffer::EOB.
     * 
     * @return The new location of the buffer, or MediaBuffer::EOB of end
     *         of buffer reached.
     */
    const char* readDW(uint32_t &udw);
protected:

    const bool swap; /* do byte- or word-swap */
    const size_t bufferSize; /* Number of bytes in the buffer not including EOB*/
    const char * const mpFirstByte; /* pointer to the very first byte in buffer */
    const char * const mpLastByte; /* pointer to the very last byte of the buffer*/
    const char* mark; /* A marker, can be set arbitrarily. Points at start of buffer on default */
    const char* mpLocation; /* Points to the current location of the buffer. Is incremented on reads */

private:
    JNIEnv* mEnv;
};
#endif /*MEDIABUFFER_H_*/
