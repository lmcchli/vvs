#ifndef _OutgoingRTPPkt_h_
#define _OutgoingRTPPkt_h_

// The contents of this file is cut out from ccrtp/rtppkt.h and slighty modified.
// new and delete operators are implemented

#include <ccrtp/rtppkt.h>

class RtpBlockHandler;

#ifdef CCXX_NAMESPACES
namespace ost {
#endif

 /**
 * @class OutgoingRTPPkt
 * @short RTP packets being sent.
 *
 * This class is intented to construct packet objects just before they
 * are inserted into the sending queue, so that they are processed in
 * a understandable and format independent manner inside the stack.
 *
 * @author Federico Montesino Pouzols <fedemp@altern.org> 
 **/
class __EXPORT OutgoingRTPPkt : public RTPPacket
{
public:
	/**
	 * Construct a new packet to be sent, containing several
	 * contributing source identifiers, header extensions and
	 * payload. A new copy in memory (holding all this components
	 * along with the fixed header) is created.
	 *
	 * @param csrcs array of countributing source 32-bit
	 *        identifiers, in host order.
	 * @param numcsrc number of CSRC identifiers in the array.
	 * @param hdrext whole header extension.
	 * @param hdrextlen size of whole header extension, in octets.
	 * @param data payload.
	 * @param datalen payload length, in octets.
	 * @param paddinglen pad packet to a multiple of paddinglen.
	 *
	 * @note For efficiency purposes, since this constructor is
	 * valid for all packets but is too complex for the common
	 * case, two simpler others are provided.
	 **/
	OutgoingRTPPkt(const uint32* const csrcs, uint16 numcsrc, 
		       const unsigned char* const hdrext, uint32 hdrextlen,
		       const unsigned char* const data, size_t datalen,
		       uint8 paddinglen);

	/**
	 * Construct a new packet to be sent, containing several
	 * contributing source identifiers and payload. A new copy in
	 * memory (holding all this components along with the fixed
	 * header) is created.
	 *
	 * @param csrcs array of countributing source 32-bit
	 * identifiers, in host order.
	 * @param numcsrc number of CSRC identifiers in the array.
	 * @param data payload.
	 * @param datalen payload length, in octets.
	 * @param paddinglen pad packet to a multiple of paddinglen.
	 **/
	OutgoingRTPPkt(const uint32* const csrcs, uint16 numcsrc, 
		       const unsigned char* const data, size_t datalen,
		       uint8 paddinglen);
		
	/**
	 * Construct a new packet (fast variant, with no contributing
	 * sources and no header extension) to be sent. A new copy in
	 * memory (holding the whole packet) is created.
	 *
	 * @param data payload.
	 * @param datalen payload length, in octets.
	 * @param paddinglen pad packet to a multiple of paddinglen.
	 **/
	OutgoingRTPPkt(const unsigned char* const data, size_t datalen, 
		       uint8 paddinglen=0);

	/**
	 * Construct a new packet (fast variant, with no contributing
	 * sources and no header extension) to be sent. A new copy in
	 * memory (holding the whole packet) is NOT created.
	 *
	 * @param data payload.
	 * @param datalen payload length, in octets.
	 * @param paddinglen pad packet to a multiple of paddinglen.
	 **/
	OutgoingRTPPkt(unsigned char* data, size_t datalen);

	~OutgoingRTPPkt();

    void* operator new(size_t size);
    void operator delete(void* data, size_t size);

    /**
	 * @param pt Packet payload type.
	 **/
	inline void
	setPayloadType(PayloadType pt)
	{ getHeader()->payload = pt; };
		
	/**
	 * @param seq Packet sequence number, in host order.
	 **/
	inline void
	setSeqNum(uint16 seq)
	{
		cachedSeqNum = seq;
		getHeader()->sequence = htons(seq); 
	}

	/**
	 * @param pts Packet timestamp, in host order.
	 **/
	inline void
	setTimestamp(uint32 pts)
	{ 
		cachedTimestamp = pts; 
		getHeader()->timestamp = htonl(pts);
	}

	/**
	 * Set synchronization source numeric identifier.
	 *
	 * @param ssrc 32-bit Synchronization SouRCe numeric
	 * identifier, in host order.
	 **/
	inline void 
	setSSRC(uint32 ssrc) const
	{ getHeader()->sources[0] = htonl(ssrc); }

	/**
	 * Set synchronization source numeric identifier. Special
	 * version to save endianness conversion.
	 *
	 * @param ssrc 32-bit Synchronization SouRCe numeric
	 * identifier, in network order.
	 **/
	inline void
	setSSRCNetwork(uint32 ssrc) const
	{ getHeader()->sources[0] = ssrc; }
	
	/**
	 * Specify the value of the marker bit. By default, the marker
	 * bit of outgoing packets is false/0. This method allows to
	 * explicity specify and change that value.
	 *
	 * @param mark value for the market bit.
	 */
	inline void
	setMarker(bool mark)
	{ getHeader()->marker = mark; }

	/**
	 * Outgoing packets are equal if their sequence numbers match.
	 **/
	inline bool 
	operator==(const OutgoingRTPPkt &p) const
	{ return ( this->getSeqNum() == p.getSeqNum() ); }

	/**
	 * Outgoing packets are not equal if their sequence numbers differ.
	 **/
	inline bool
	operator!=(const OutgoingRTPPkt &p) const
	{ return ( this->getSeqNum() != p.getSeqNum() ); }

private:
	/**
	 * Copy constructor from objects of its same kind, declared
	 * private to avoid its use.  
	 **/
	OutgoingRTPPkt(const OutgoingRTPPkt &o);
	
	/**
	 * Assignment operator from objects of its same kind, declared
	 * private to avoid its use.  
	 **/
	OutgoingRTPPkt&
	operator=(const OutgoingRTPPkt &o);

	/**
	 * Set the list of CSRC identifiers in an RTP packet,
	 * switching host to network order.
	 **/
	void setCSRCArray(const uint32* const csrcs, uint16 numcsrc);
private:
    RtpBlockHandler* m_blockHandler;
};



#ifdef  CCXX_NAMESPACES
};
#endif


#endif
