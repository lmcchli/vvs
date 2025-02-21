#include "movaudiochunkcontainer.h"
#include "movaudiochunk.h"

MovAudioChunkContainer::MovAudioChunkContainer(int chunkSize, unsigned char padding) :
        m_requestedChunkSize(chunkSize), m_nOfRechunked(0), m_padding(padding), m_lastChunkOffset(0)
{
}

MovAudioChunkContainer::~MovAudioChunkContainer()
{
}

void MovAudioChunkContainer::rechunkalize()
{
    int totalSize(0);
    MovAudioChunkContainer::iterator iter = begin();
    for (; iter != end(); ++iter) {
        totalSize += iter->getLength();
    }
    m_nOfRechunked = totalSize / m_requestedChunkSize;
    m_nOfRechunked += (totalSize % m_requestedChunkSize ? 1 : 0);
    m_lastChunkIter = begin();
    m_lastChunkOffset = 0;
}

int MovAudioChunkContainer::getNextRechunked(char* chunk)
{
    // The number of copied bytes (and copy index).
    unsigned nOfBytes(0);

    // Start at the previous index
    for (; m_lastChunkIter != end(); ++m_lastChunkIter) {
        MovAudioChunk& audioChunk = *m_lastChunkIter;
        const char* data(audioChunk.getData());
        // Save current index as last index
        // Ensure that we reuse the prevoius offset
        for (int offset(m_lastChunkOffset); offset < audioChunk.getLength(); offset++) {
            // Copy data
            chunk[nOfBytes++] = data[offset];
            // Check if copy is "full"
            if (nOfBytes >= m_requestedChunkSize) {
                // Store current offset as last
                m_lastChunkOffset = offset + 1;
                if (m_lastChunkOffset >= (unsigned) audioChunk.getLength()) {
                    m_lastChunkOffset = 0;
                    ++m_lastChunkIter;
                }
                // Returning the number of read bytes
                return nOfBytes;
            }
        }
        // Reset previous offset ...
        m_lastChunkOffset = 0;
    }
    // Handle padding of silence ...
    for (unsigned index(nOfBytes); index < m_requestedChunkSize; index++) {
        chunk[index] = m_padding;
    }
    // Returning the number of actually read bytes
    return nOfBytes;
}

int MovAudioChunkContainer::getRequestedChunkSize() const
{
    return m_requestedChunkSize;
}

unsigned MovAudioChunkContainer::nOfRechunked() const
{
    return m_nOfRechunked;
}

unsigned char MovAudioChunkContainer::getPadding() const
{
    return m_padding;
}

void MovAudioChunkContainer::setPadding(unsigned char padding)
{
    m_padding = padding;
}

