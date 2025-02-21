#include "MockRtpPacket.h"

#include "rtpblockhandler.h"

unsigned MockRtpPacket::counter = 0;

MockRtpPacket::MockRtpPacket() :
    id(++counter)
{
    m_blockHandler = RtpBlockHandler::getSingleton();
}

MockRtpPacket::~MockRtpPacket()
{
  if (m_blockHandler != 0) {
    // Updating deallocation book keeping
    m_blockHandler->deallocate();
    if (m_blockHandler->isEmpty()) {
      // Release block handler instance from thread specific
      // Mostly as feedback to the test program
      m_blockHandler->release();
      // Delete 
      delete m_blockHandler;
      m_blockHandler = 0;
    }
  }
}

void*
MockRtpPacket::operator new(size_t size)
{
    return RtpBlockHandler::allocate(size);
}

void
MockRtpPacket::operator delete(void* data, size_t size)
{
    RtpBlockHandler::deallocate(data);
}
