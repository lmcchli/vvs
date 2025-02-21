#include "private.h"
#include <ccrtp/outgoingrtppkt.h>

#include <rtpblockhandler.h>

#ifdef	CCXX_NAMESPACES
namespace ost {
#endif


OutgoingRTPPkt::OutgoingRTPPkt(
	const uint32* const csrcs, uint16 numcsrc, 
        const unsigned char* const hdrext, uint32 hdrextlen,
	const unsigned char* const data, size_t datalen, uint8 paddinglen= 0) :
	RTPPacket((getSizeOfFixedHeader() + sizeof(uint32) * numcsrc 
               + hdrextlen),datalen,paddinglen),
    m_blockHandler(0)
{
	uint32 pointer = (uint32)getSizeOfFixedHeader();
	// add CSCR identifiers (putting them in network order).
	setCSRCArray(csrcs,numcsrc);
	pointer += numcsrc * sizeof(uint32);

	// add header extension.
	setbuffer(hdrext,hdrextlen,pointer);
	setExtension(hdrextlen > 0);
	pointer += hdrextlen;

	// add data.
	setbuffer(data,datalen,pointer);
}

OutgoingRTPPkt::OutgoingRTPPkt(
	const uint32* const csrcs, uint16 numcsrc, 
	const unsigned char* const data, size_t datalen, uint8 paddinglen = 0) :
	RTPPacket((getSizeOfFixedHeader() + sizeof(uint32) *numcsrc),datalen,
              paddinglen),
    m_blockHandler(0)
{
	uint32 pointer = (uint32)getSizeOfFixedHeader();
	// add CSCR identifiers (putting them in network order).
	setCSRCArray(csrcs,numcsrc);
	pointer += numcsrc * sizeof(uint32);

	// not needed, as the RTPPacket constructor sets by default
	// the whole fixed header to 0.
	// getHeader()->extension = 0;

	// add data.
	setbuffer(data,datalen,pointer);
}

OutgoingRTPPkt::OutgoingRTPPkt(const unsigned char* const data, size_t datalen, 
			       uint8 paddinglen) :
	RTPPacket(getSizeOfFixedHeader(),datalen,paddinglen),
    m_blockHandler(0)
{
	// not needed, as the RTPPacket constructor sets by default
	// the whole fixed header to 0.
	//getHeader()->cc = 0;
	//getHeader()->extension = 0;

	setbuffer(data,datalen,getSizeOfFixedHeader());
}

// Ensuring that we never allocate memory for RTP Packets
OutgoingRTPPkt::OutgoingRTPPkt(unsigned char* data, size_t datalen) :
	RTPPacket(getSizeOfFixedHeader(), data, datalen-getSizeOfFixedHeader())
{
    m_blockHandler = RtpBlockHandler::getSingleton();
}

OutgoingRTPPkt::~OutgoingRTPPkt()
{
    if (m_blockHandler != 0) {
        m_blockHandler->deallocate();
	}
}


void*
OutgoingRTPPkt::operator new(size_t size)
{
   return RtpBlockHandler::allocate(size);
}

void
OutgoingRTPPkt::operator delete(void* data, size_t size)
{
    RtpBlockHandler::deallocate(data);
}

void
OutgoingRTPPkt::setCSRCArray(const uint32* const csrcs, uint16 numcsrc)
{
	setbuffer(csrcs, numcsrc * sizeof(uint32),getSizeOfFixedHeader());
	uint32* csrc = const_cast<uint32*>(getCSRCs());
	for ( int i = 0; i < numcsrc; i++ )
		csrc[i] = htonl(csrc[i]);
	getHeader()->cc = numcsrc;
}	

#ifdef	CCXX_NAMESPACES
};
#endif
