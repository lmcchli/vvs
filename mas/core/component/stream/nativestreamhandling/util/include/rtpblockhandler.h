#ifndef _RtpBlockHandler_h_
#define _RtpBlockHandler_h_

#include <boost/thread/tss.hpp>
#include <memory> // auto_ptr

class RtpBlockHandler {
public:
    RtpBlockHandler();
    ~RtpBlockHandler();
    // The dataBlockSize represents the amount of memory required for containing the
    // RTP packet/payload data (the content of the MediaObject).
    void initialize(unsigned nOfAudioPackages, unsigned audioBlockSize,
                    unsigned nOfVideoPackages, unsigned videoBlockSize);

    // Thread specific reset ...
    void reset();
    // Thread specific release ...
    void release();
    void addAudioPayload(const char* payload, unsigned length, unsigned rtpTimestampDelta, unsigned timeDelta);
    void addVideoPayload(unsigned frameTime,
                         const char* header, unsigned headerLength, 
                         const char* data, unsigned dataLength);

    // Get data returns next available data chunk
    bool getNextAudioPayload(char*& payload, unsigned& length, unsigned& rtpTimestampDelta, unsigned& timeDelta);
    bool getNextVideoPayload(unsigned& frameTime, char*& payload, unsigned& length);
    unsigned peekFrameTime();

public:
    unsigned getAudioPayloadBlockSize();
    unsigned getVideoPayloadBlockSize();
    unsigned getHeapSize();
    unsigned getAllocateCount();
    unsigned getDeallocateCount();
    unsigned getFixedHeaderSize();
    bool isEmpty();
    void deallocate();

public:
    // This interface is supposed to be used by the RTP Stack when allocating
    // out bound RTP packets.

    // This method allocates a chunk of memory from the rtp packets block (m_heap)
    static void* allocate(size_t size);
    static void deallocate(void* fragment);
    static void deallocate(void* fragment, size_t size);
    static RtpBlockHandler* getSingleton();

protected:
    char* allocateFragment(unsigned size);
    void deallocateFragment(char* fragment, unsigned size);
    unsigned setVideoFrameHeader(unsigned frameTime, unsigned length);
    static bool isOnHeap(char* ptr);

private:
    static const unsigned m_fixedHeaderSize;
    static const unsigned m_packageSize;
    static boost::thread_specific_ptr<RtpBlockHandler> s_pseudoSingleton;
    char* m_blockStart;
    char* m_audioBlockStart;
    char* m_audioBlockEnd;
    char* m_nextAudioPayload;
    char* m_videoBlockStart;
    char* m_videoBlockEnd;
    char* m_nextVideoPayload;
    char* m_heapStart;
    char* m_heapEnd;
    char* m_heapFree;
    char* m_blockEnd;
    unsigned m_audioPayloadBlockSize;
    unsigned m_videoPayloadBlockSize;
    unsigned m_heapSize;
    unsigned m_nOfAllocated;
    unsigned m_nOfDeallocated;
};

#endif
