#ifndef _MovAudioChunkContainer_h_
#define _MovAudioChunkContainer_h_

#include "platform.h"
#include "movaudiochunk.h"

#include <boost/ptr_container/ptr_list.hpp>

/**
 * MovAudioChunkContainer encapsulates the containment of audio chunks.
 * Besides from the chunks the container holds information on how
 * the chunks should be stored in a MOV file (e.g. requested chunk
 * size).
 */
class MovAudioChunkContainer: public boost::ptr_list<MovAudioChunk>
{

public:
    MovAudioChunkContainer(int chunkSize = 160, unsigned char padding = 0xff);
    virtual ~MovAudioChunkContainer();
    int getRequestedChunkSize() const;
    void setRequestedChunkSize(int chunkSize);
    unsigned char getPadding() const;
    void setPadding(unsigned char padding);

    void rechunkalize();
    unsigned nOfRechunked() const;
    int getNextRechunked(char* chunk);

private:
    unsigned m_requestedChunkSize;
    int m_nOfRechunked;
    unsigned char m_padding;
    MovAudioChunkContainer::iterator m_lastChunkIter;
    unsigned m_lastChunkOffset;
};

#endif

