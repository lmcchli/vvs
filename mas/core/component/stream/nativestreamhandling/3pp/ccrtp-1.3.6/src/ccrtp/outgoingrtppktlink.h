#ifndef _OutgoingRTPPktLink_h_
#define _OutgoingRTPPktLink_h_

// The contents of this file is cut out from ccrtp/oqueue.h and slighty modified.
// new and delete operators are implemented

class RtpBlockHandler;

#ifdef	CCXX_NAMESPACES
namespace ost {
#endif

class OutgoingRTPPkt;

class __EXPORT OutgoingRTPPktLink 
{
public:
    OutgoingRTPPktLink(OutgoingRTPPkt* pkt,
        OutgoingRTPPktLink* p, 
        OutgoingRTPPktLink* n);
    ~OutgoingRTPPktLink();

    void* operator new(size_t size);
    void operator delete(void* data, size_t size);

    OutgoingRTPPkt* getPacket();

    void setPacket(OutgoingRTPPkt* pkt);

    OutgoingRTPPktLink* getPrev();

    void setPrev(OutgoingRTPPktLink* p);

    OutgoingRTPPktLink* getNext();

    void setNext(OutgoingRTPPktLink* n);

    // the packet this link refers to.
    OutgoingRTPPkt* packet;
    // global outgoing packets queue.
    OutgoingRTPPktLink* prev;
    OutgoingRTPPktLink* next;
private:
    RtpBlockHandler* m_blockHandler;
};

#ifdef	CCXX_NAMESPACES
};
#endif

#endif
