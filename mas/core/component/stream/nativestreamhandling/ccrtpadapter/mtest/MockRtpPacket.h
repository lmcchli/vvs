#ifndef _MockRtpPacket_h_
#define _MockRtpPacket_h_

#ifndef WIN32
#include <unistd.h>
#endif

class RtpBlockHandler;

class MockRtpPacket {
public:
    MockRtpPacket();
    ~MockRtpPacket();

    void* operator new(size_t size);
    void operator delete(void* data, size_t size);

public:
    static unsigned counter;
    unsigned id;
    RtpBlockHandler* m_blockHandler;
};

#endif
