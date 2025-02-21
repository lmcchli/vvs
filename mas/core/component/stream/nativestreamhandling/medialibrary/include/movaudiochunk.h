#ifndef _MovAudioChunk_h_
#define _MovAudioChunk_h_

#include "platform.h"

/**
 * MovAudioChunk is a chunk of audio data.
 */
class MEDIALIB_CLASS_EXPORT MovAudioChunk
{
public:
    /**
     * The constructor.
     * Input:
     *   data an audio chunk
     *   length the chunk size
     */
    MovAudioChunk(char* data, int length);

    virtual ~MovAudioChunk();

    /**
     * A data setter.
     * Input:
     *   data an audio chunk
     *   length the chunk size
     */
    void setData(char* data, int length);

    /**
     * A chunk data getter.
     */
    const char* getData() const;

    /**
     * A chunk data size getter.
     */
    int getLength() const;

    /**
     * Equality operator.
     */
    bool operator==(const MovAudioChunk& leftSide);

    /**
     * Inequality operator.
     */
    bool operator!=(const MovAudioChunk& leftSide);

private:
    /**
     * The chunk data (a pointer to).
     */
    char* m_data;

    /**
     * The chunk size
     */
    int m_length;
};

#endif
