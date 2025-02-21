#include "private.h"
#include <ccrtp/outgoingrtppktlink.h>
#include <ccrtp/outgoingrtppkt.h>

#include <rtpblockhandler.h>

#ifdef	CCXX_NAMESPACES
namespace ost {
#endif

OutgoingRTPPktLink::OutgoingRTPPktLink(OutgoingRTPPkt* pkt,
                                       OutgoingRTPPktLink* p, 
                                       OutgoingRTPPktLink* n) 
    : packet(pkt), 
      prev(p), 
      next(n), 
      m_blockHandler(0)
{ 
    m_blockHandler = RtpBlockHandler::getSingleton();
}

OutgoingRTPPktLink::~OutgoingRTPPktLink() 
{
    delete packet;
    if (m_blockHandler != 0) {
        m_blockHandler->deallocate();
    }
}

void*
OutgoingRTPPktLink::operator new(size_t size)
{
   return RtpBlockHandler::allocate(size);
}

void
OutgoingRTPPktLink::operator delete(void* data, size_t size)
{
    RtpBlockHandler::deallocate(data);
}

OutgoingRTPPkt* 
OutgoingRTPPktLink::getPacket() 
{ 
    return packet; 
}

void 
OutgoingRTPPktLink::setPacket(OutgoingRTPPkt* pkt) 
{ 
    packet = pkt; 
}

OutgoingRTPPktLink* 
OutgoingRTPPktLink::getPrev() 
{ 
    return prev; 
}

void 
OutgoingRTPPktLink::setPrev(OutgoingRTPPktLink* p) 
{ 
    prev = p; 
}

OutgoingRTPPktLink* 
OutgoingRTPPktLink::getNext() 
{ 
    return next; 
}

void 
OutgoingRTPPktLink::setNext(OutgoingRTPPktLink* n) 
{ 
    next = n; 
}

#ifdef	CCXX_NAMESPACES
};
#endif



