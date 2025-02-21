// Copyright (C) 1999-2005 Open Source Telecom Corporation.
//  
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
// 
// As a special exception, you may use this file as part of a free software
// library without restriction.  Specifically, if other files instantiate
// templates or use macros or inline functions from this file, or you compile
// this file and link it with other files to produce an executable, this
// file does not by itself cause the resulting executable to be covered by
// the GNU General Public License.  This exception does not however    
// invalidate any other reasons why the executable file might be covered by
// the GNU General Public License.    
//
// This exception applies only to the code released under the name GNU
// ccRTP.  If you copy code from other releases into a copy of GNU
// ccRTP, as the General Public License permits, the exception does
// not apply to the code that you add in this way.  To avoid misleading
// anyone as to the status of such modified files, you must delete
// this exception notice from them.
//
// If you write modifications of your own for GNU ccRTP, it is your choice
// whether to permit this exception to apply to your modifications.
// If you do not wish that, delete this exception notice.
//

/**
 * @file rtppkt.cpp
 * @short StaticPayloadFormat, DynamicPayloadFormat, RTPPacket,
 * OutgoingRTPPkt and IncomingRTPPkt classes implementation.
 **/
#include "private.h"
#include <ccrtp/rtppkt.h>

#define TIME_BUFFER_SIZE 30

using namespace std;

#ifdef	CCXX_NAMESPACES
namespace ost {
#endif

// Default to 8Khz when no value is specified.
const uint32 PayloadFormat::defaultRTPClockRate = 8000;

//uint32 PayloadFormat::staticRates[lastStaticPayloadType]
uint32 StaticPayloadFormat::staticAudioTypesRates[] = {
	// audio types:
	8000,    //  0 - sptPCMU
	0,       //  1 - reserved
	8000,    //  2 - sptG726_32
	8000,    //  3 - sptGSM
	8000,    //  4 - sptG723
	8000,    //  5 - sptDVI4_8000
	16000,   //  6 - sptDVI4_16000
	8000,    //  7 - sptLPC
	8000,    //  8 - sptPCMA
	8000,    //  9 - sptG722
	44100,   // 10 - sptL16_DUAL
	44100,   // 11 - sptL16_MONO
	8000,    // 12 - sptQCELP
	0,       // 13 - reserved
	90000,   // 14 - sptMPA
	8000,    // 15 - sptG728
	11015,   // 16 - sptDVI4_11025
	22050,   // 17 - sptDVI4_22050
        8000     // 18 - sptG729
/*	0,       // reserved
	0,       // unassigned
	0,       // unassigned
	0,       // unassigned
	0        // unassigned
*/
	// All video types have 90000 hz RTP clock rate.
	// If sometime in the future a static video payload type is
	// defined with a different RTP clock rate (quite
	// unprobable). This table and/or the StaticPayloadType
	// constructor must be changed.
};

StaticPayloadFormat::StaticPayloadFormat(StaticPayloadType type)
{ 
	setPayloadType( (type <= lastStaticPayloadType)? type : 0);
	if ( type <= sptG729 ) {
		// audio static type
		setRTPClockRate(staticAudioTypesRates[type]);
	} else {
		// video static type
		setRTPClockRate(90000);
	} 
}

DynamicPayloadFormat::DynamicPayloadFormat(PayloadType type, uint32 rate)
{ 
	PayloadFormat::setPayloadType(type);
	setRTPClockRate(rate);
}

// constructor commonly used for incoming packets
RTPPacket::RTPPacket(const unsigned char* const block, size_t len, 
		     bool duplicate):
	total((uint32)len),
	duplicated(duplicate)
{

	const RTPFixedHeader* const header = 
		reinterpret_cast<const RTPFixedHeader*>(block);
	hdrSize = sizeof(RTPFixedHeader) + (header->cc << 2);
	if ( header->extension ){
		RTPHeaderExt *ext = (RTPHeaderExt *)(block + hdrSize);
		hdrSize += sizeof(uint32) + ntohl(ext->length);
	}
	if ( header->padding ) 
		len -= block[len - 1];
	payloadSize = (uint32)(len - hdrSize);
	
	if ( duplicate ) {
		buffer = new unsigned char[len];
		setbuffer(block,len,0);
	} else {
		buffer = const_cast<unsigned char*>(block);
	}
}

	
// constructor commonly used for outgoing packets
RTPPacket::RTPPacket(size_t hdrlen, size_t plen, uint8 paddinglen) :
	buffer(NULL),
	hdrSize((uint32)hdrlen),
	payloadSize((uint32)plen),
	duplicated(false)
{
	total = (uint32)(hdrlen + payloadSize);
	// compute if there must be padding
	uint8 padding = 0;
	if ( 0 != paddinglen ) {
		padding = paddinglen - (total % paddinglen);
		total += padding;
	}
	// now we know the actual total length of the packet
	buffer = new unsigned char[total];
	*(reinterpret_cast<uint32*>(getHeader())) = 0;
	getHeader()->version = CCRTP_VERSION;
	if ( 0 != padding ) {
		memset(buffer + total - padding,0,padding - 1);
		buffer[total - 1] = padding;
		getHeader()->padding = 1;
	} else {
		getHeader()->padding = 0;
	}
}

RTPPacket::RTPPacket(size_t hdrlen, unsigned char* packet, size_t plen) :
	buffer(packet),
	hdrSize((uint32)hdrlen),
	payloadSize((uint32)plen),
	total(hdrSize+payloadSize),
	duplicated(true)
{
	*(reinterpret_cast<uint32*>(getHeader())) = 0;
	getHeader()->version = CCRTP_VERSION;
	getHeader()->padding = 0;
}


void 
RTPPacket::endPacket()
{
#ifdef	CCXX_EXCEPTIONS
	try {
#endif
		if (!duplicated) delete [] buffer;
#ifdef	CCXX_EXCEPTIONS
	} catch (...) { };
#endif
}

// These masks are valid regardless of endianness.
const uint16 IncomingRTPPkt::RTP_INVALID_PT_MASK = (0x7e);
const uint16 IncomingRTPPkt::RTP_INVALID_PT_VALUE = (0x48);

IncomingRTPPkt::IncomingRTPPkt(const unsigned char* const block, size_t len) :
	RTPPacket(block,len)
{
        time_t currentTime = time(0);

	// first, perform validity check: 
	// 1) check protocol version
        bool notProtoVersion = (getProtocolVersion() != CCRTP_VERSION);
        if (notProtoVersion) {
            struct tm * timeinfo = localtime (&currentTime);
            char tbuf[TIME_BUFFER_SIZE] = {0};
            strftime (tbuf, TIME_BUFFER_SIZE, "%F %T #TZ# ", timeinfo);

            cout << tbuf << "Invalid proto version " << getProtocolVersion() << endl;
            cout.flush();
        }
	// 2) it is not an SR nor an RR
        bool notPayloadType = ((getPayloadType() & RTP_INVALID_PT_MASK) == RTP_INVALID_PT_VALUE);
        if (notPayloadType) {
            struct tm * timeinfo = localtime (&currentTime);
            char tbuf[TIME_BUFFER_SIZE] = {0};
            strftime (tbuf, TIME_BUFFER_SIZE, "%F %T #TZ# ", timeinfo);

            cout << tbuf << "Invalid payload type " << getPayloadType() << endl;
            cout.flush();
        }
	// 3) consistent length field value (taking CC value and P and
        bool notPayloadSize = (getPayloadSize() <= 0);
        if (notPayloadSize) {
            struct tm * timeinfo = localtime (&currentTime);
            char tbuf[TIME_BUFFER_SIZE] = {0};
            strftime (tbuf, TIME_BUFFER_SIZE, "%F %T #TZ# ", timeinfo);

            cout << tbuf << " Invalid payload size " << getPayloadSize() << endl;
            cout.flush();
        }

        if (notProtoVersion || notPayloadType || notPayloadSize) {
            headerValid = false;
            return;
        }

	headerValid = true;
	cachedTimestamp = getRawTimestamp();
	cachedSeqNum = ntohs(getHeader()->sequence);
	cachedSSRC = ntohl(getHeader()->sources[0]);
}

#ifdef	CCXX_NAMESPACES
}
#endif

/** EMACS **
 * Local variables:
 * mode: c++
 * c-basic-offset: 8
 * End:
 */
