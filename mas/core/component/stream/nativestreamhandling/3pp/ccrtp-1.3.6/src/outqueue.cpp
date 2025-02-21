// Copyright (C) 2001,2002,2004,2005 Federico Montesino Pouzols <fedemp@altern.org>
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

#include "private.h"
#include <ccrtp/oqueue.h>
#include <ccrtp/outgoingrtppkt.h>
#include <ccrtp/outgoingrtppktlink.h>

#ifdef  CCXX_NAMESPACES
namespace ost {
#endif

const size_t OutgoingDataQueueBase::defaultMaxSendSegmentSize = 65536;

OutgoingDataQueueBase::OutgoingDataQueueBase()
{
	// segment data in packets of no more than 65536 octets.
	setMaxSendSegmentSize(getDefaultMaxSendSegmentSize());
}

DestinationListHandler::DestinationListHandler() :
	destList(), destinationLock()
{
}

DestinationListHandler::~DestinationListHandler()
{
	TransportAddress* tmp = NULL;
	writeLockDestinationList();
	for (std::list<TransportAddress*>::iterator i = destList.begin();
	     destList.end() != i; i++) {
		tmp = *i;
		delete tmp;
		tmp = NULL;
	}
	unlockDestinationList();
}

bool 
DestinationListHandler::addDestinationToList(const InetAddress& ia, 
					     tpport_t data, tpport_t control)
{	
	TransportAddress* addr = new TransportAddress(ia,data,control);
	writeLockDestinationList();
	destList.push_back(addr);
	unlockDestinationList();
	return true;
}

bool
DestinationListHandler::removeDestinationFromList(const InetAddress& ia, 
						  tpport_t dataPort,  
						  tpport_t controlPort)
{
	bool result = false;
	writeLockDestinationList();
	TransportAddress* tmp;
	for (std::list<TransportAddress*>::iterator i = destList.begin();
	     destList.end() != i && !result; i++) {
		tmp = *i;
		if ( ia == tmp->getNetworkAddress() &&
		     dataPort == tmp->getDataTransportPort() &&
		     controlPort == tmp->getControlTransportPort() ) {
			// matches. -> remove it.
			result = true;
			destList.erase(i);
			delete tmp;
		}
	}
	unlockDestinationList();
	return result;
}

/// Schedule at 8 ms.
const microtimeout_t OutgoingDataQueue::defaultSchedulingTimeout = 8000;
/// Packets unsent will expire after 40 ms.
const microtimeout_t OutgoingDataQueue::defaultExpireTimeout = 40000;

OutgoingDataQueue::OutgoingDataQueue():
	OutgoingDataQueueBase(),
	DestinationListHandler(),
	sendLock(),
	sendFirst(NULL), sendLast(NULL)
{
	setInitialTimestamp(random32());
	setSchedulingTimeout(getDefaultSchedulingTimeout());
	setExpireTimeout(getDefaultExpireTimeout());

	sendInfo.packetCount = 0;
	sendInfo.octetCount = 0;
	sendInfo.sendSeq = random16();    // random initial sequence number
	sendInfo.sendCC = 0;    // initially, 0 CSRC identifiers follow the fixed heade
	sendInfo.paddinglen = 0;          // do not add padding bits.
	sendInfo.marked = false;
	sendInfo.complete = true;
	// the local source is the first contributing source
	sendInfo.sendSources[0] = getLocalSSRC();
	// this will be an accumulator for the successive cycles of timestamp
	sendInfo.overflowTime.tv_sec = getInitialTime().tv_sec;
	sendInfo.overflowTime.tv_usec = getInitialTime().tv_usec;
}

void
OutgoingDataQueue::purgeOutgoingQueue()
{
	OutgoingRTPPktLink* sendnext;
	// flush the sending queue (delete outgoing packets
	// unsent so far)
	sendLock.writeLock();
    if (sendFirst) {
        sendInfo.sendSeq = sendFirst->packet->getSeqNum();
    }
	while ( sendFirst )
 	{
		sendnext = sendFirst->getNext();
		delete sendFirst;
		sendFirst = sendnext;
	}
	sendLast = 0;
	sendLock.unlock();
}

bool
OutgoingDataQueue::addDestination(const InetHostAddress& ia, 
				  tpport_t dataPort,
				  tpport_t controlPort)
{
	if ( 0 == controlPort )
		controlPort = dataPort + 1;
	bool result = addDestinationToList(ia,dataPort,controlPort);
	if ( result && isSingleDestination() ) {
		setDataPeer(ia,dataPort);
		setControlPeer(ia,controlPort);
	}
	return result;
}

bool
OutgoingDataQueue::addDestination(const InetMcastAddress& ia, 
				  tpport_t dataPort,
				  tpport_t controlPort)
{
	if ( 0 == controlPort )
		controlPort = dataPort + 1;
	bool result = addDestinationToList(ia,dataPort,controlPort);
	if ( result && isSingleDestination() ) {
		setDataPeer(ia,dataPort);
		setControlPeer(ia,controlPort);
	}
	return result;
}

bool
OutgoingDataQueue::forgetDestination(const InetHostAddress& ia, 
				     tpport_t dataPort,
				     tpport_t controlPort)
{
	if ( 0 == controlPort )
		controlPort = dataPort + 1;
	return DestinationListHandler::
		removeDestinationFromList(ia,dataPort,controlPort);
}

bool
OutgoingDataQueue::forgetDestination(const InetMcastAddress& ia, 
				     tpport_t dataPort,
				     tpport_t controlPort)
{
	if ( 0 == controlPort )
		controlPort = dataPort + 1;
	return DestinationListHandler::
		removeDestinationFromList(ia,dataPort,controlPort);
}

bool
OutgoingDataQueue::isSending(void) const
{
	if(sendFirst)
		return true;

	return false;
}

microtimeout_t
OutgoingDataQueue::getSchedulingTimeout(void)
{
	struct timeval send, now;
	uint32 rate;
	uint32 rem;

	for(;;)
	{
		// if there is no packet to send, use the default scheduling
		// timeout
		if( !sendFirst )
			return schedulingTimeout;

		uint32 stamp = sendFirst->getPacket()->getTimestamp();
		stamp -= getInitialTimestamp();
		rate = getCurrentRTPClockRate();

		// now we want to get in <code>send</code> _when_ the
		// packet is scheduled to be sent.

		// translate timestamp to timeval
		send.tv_sec = stamp / rate;
		rem = stamp % rate;
		send.tv_usec = (1000ul*rem) / (rate/1000ul); // 10^6 * rem/rate

		// add timevals. Overflow holds the inital time
		// plus the time accumulated through successive
		// overflows of timestamp. See below.
		timeradd(&send,&(sendInfo.overflowTime),&send);
		gettimeofday(&now, NULL);

		// Problem: when timestamp overflows, time goes back.
		// We MUST ensure that _send_ is not too lower than
		// _now_, otherwise, we MUST keep how many time was
		// lost because of overflow. We assume that _send_
		// 5000 seconds lower than now suggests timestamp
		// overflow.  (Remember than the 32 bits of the
		// timestamp field are 47722 seconds under a sampling
		// clock of 90000 hz.)  This is not a perfect
		// solution. Disorderedly timestamped packets coming
		// after an overflowed one will be wrongly
		// corrected. Nevertheless, this may only corrupt a
		// handful of those packets every more than 13 hours
		// (if timestamp started from 0).
		if ( now.tv_sec - send.tv_sec > 5000){
			timeval overflow;
			overflow.tv_sec =(~static_cast<uint32>(0)) / rate;
			overflow.tv_usec = (~static_cast<uint32>(0)) % rate *
				1000000ul / rate;
			do {
				timeradd(&send,&overflow,&send);
				timeradd(&(sendInfo.overflowTime),&overflow,
					 &(sendInfo.overflowTime));
			} while ( now.tv_sec - send.tv_sec > 5000 );
		}
		
		// This tries to solve the aforementioned problem
		// about disordered packets coming after an overflowed
		// one. Now we apply the reverse idea.
		if ( send.tv_sec - now.tv_sec > 20000 ){
			timeval overflow;
			overflow.tv_sec = (~static_cast<uint32>(0)) / rate;
			overflow.tv_usec = (~static_cast<uint32>(0)) % rate *
				1000000ul / rate;
			timersub(&send,&overflow,&send);
		}

		// A: This sets a maximum timeout of 1 hour.
		if ( send.tv_sec - now.tv_sec > 3600 ){
			return 3600000000ul;
		}
		int32 diff = 
			((send.tv_sec - now.tv_sec) * 1000000ul) +
			send.tv_usec - now.tv_usec;
		// B: wait <code>diff</code> usecs more before sending
		if ( diff >= 0 ){
			return static_cast<microtimeout_t>(diff);
		}

		// C: the packet must be sent right now
		if ( (diff < 0) &&
		     static_cast<microtimeout_t>(-diff) <= getExpireTimeout() ){
			return 0;
		}

		// D: the packet has expired -> delete it.
		sendLock.writeLock();
		OutgoingRTPPktLink* packet = sendFirst;
		sendFirst = sendFirst->getNext();
		onExpireSend(*(packet->getPacket()));  // new virtual to notify
		delete packet;
		if ( sendFirst )
			sendFirst->setPrev(NULL);
		else
			sendLast = NULL;
		sendLock.unlock();
	}
	I( false );
	return 0;
}

void
OutgoingDataQueue::putData(uint32 stamp, const unsigned char *data, 
			   size_t datalen, bool isTransfer)
{
	if ( !data || !datalen )
		return;

    OutgoingRTPPkt* packet;
    if (isTransfer) {
        packet = new OutgoingRTPPkt((const unsigned char * const)data, datalen, 0);
    } else {
        packet = new OutgoingRTPPkt((unsigned char*)data, datalen);
    }

    packet->setPayloadType(getCurrentPayloadType());
    packet->setSeqNum(sendInfo.sendSeq++);
    packet->setTimestamp(stamp + getInitialTimestamp());
    packet->setSSRCNetwork(getLocalSSRCNetwork());
    if (getMark()) {
        packet->setMarker(true); 
        setMark(false);
    } else {
        packet->setMarker(false);
    }

    // insert the packet into the "tail" of the sending queue
    sendLock.writeLock();
    OutgoingRTPPktLink *link = new OutgoingRTPPktLink(packet,sendLast,NULL);
    if (sendLast)
        sendLast->setNext(link);
    else
        sendFirst = link;
    sendLast = link;
    sendLock.unlock();
}

size_t
OutgoingDataQueue::dispatchDataPacket(uint32 ts, uint32 deltaTimestamp)
{
	sendLock.writeLock();

	while (true) {
	OutgoingRTPPktLink* packetLink = sendFirst;
	
	if ( !packetLink ){
		sendLock.unlock();
		return 0;
	}
	
	OutgoingRTPPkt* packet = packetLink->getPacket();
	if (packet->getTimestamp() - getInitialTimestamp() > ts - deltaTimestamp) {
		sendLock.unlock();
		//	std::cerr << "No packets are ready to be sent: TS=" << ts << ", TIMESTAMP=" << (packet->getTimestamp() - getInitialTimestamp()) <<std::endl;
		return 0;
	}
	//	std::cerr << "TS=" << ts << ", TIMESTAMP=" << (packet->getTimestamp()-getInitialTimestamp()) << std::endl;
	lockDestinationList();
	if ( isSingleDestination() ) {
		TransportAddress* tmp = destList.front();
		// if going from multi destinations to single destinations.
		setDataPeer(tmp->getNetworkAddress(),
			    tmp->getDataTransportPort());

		sendData(packet->getRawPacket(),
			 packet->getRawPacketSize());
	} else {
		// when no destination has been added, NULL == dest.
		for (std::list<TransportAddress*>::iterator i = 
			     destList.begin(); destList.end() != i; i++) {
			TransportAddress* dest = *i;
			setDataPeer(dest->getNetworkAddress(),
				    dest->getDataTransportPort());
			sendData(packet->getRawPacket(),
				 packet->getRawPacketSize());
		}
	}
	unlockDestinationList();

	// unlink the sent packet from the queue and destroy it. Also
	// record the sending.
	sendFirst = sendFirst->getNext();
	if ( sendFirst ) {
		sendFirst->setPrev(NULL);
	} else {
		sendLast = NULL;
	}
	// for general accounting and RTCP SR statistics 
	sendInfo.packetCount++;
	sendInfo.octetCount += packet->getPayloadSize();
	delete packetLink;

	}
	sendLock.unlock();
	return 1;
}

size_t
OutgoingDataQueue::dispatchDataPacket()
{
	sendLock.writeLock();
	OutgoingRTPPktLink* packetLink = sendFirst;
	
	if ( !packetLink ){
		sendLock.unlock();
		return 0;
	}
	
	OutgoingRTPPkt* packet = packetLink->getPacket();
	uint32 rtn = packet->getPayloadSize();
	lockDestinationList();
	if ( isSingleDestination() ) {
		TransportAddress* tmp = destList.front();
		// if going from multi destinations to single destinations.
		setDataPeer(tmp->getNetworkAddress(),
			    tmp->getDataTransportPort());

		sendData(packet->getRawPacket(),
			 packet->getRawPacketSize());
	} else {
		// when no destination has been added, NULL == dest.
		for (std::list<TransportAddress*>::iterator i = 
			     destList.begin(); destList.end() != i; i++) {
			TransportAddress* dest = *i;
			setDataPeer(dest->getNetworkAddress(),
				    dest->getDataTransportPort());
			sendData(packet->getRawPacket(),
				 packet->getRawPacketSize());
		}
	}
	unlockDestinationList();

	// unlink the sent packet from the queue and destroy it. Also
	// record the sending.
	sendFirst = sendFirst->getNext();
	if ( sendFirst ) {
		sendFirst->setPrev(NULL);
	} else {
		sendLast = NULL;
	}
	// for general accounting and RTCP SR statistics 
	sendInfo.packetCount++;
	sendInfo.octetCount += packet->getPayloadSize();
	delete packetLink;

	sendLock.unlock();
	return rtn;
}

size_t
OutgoingDataQueue::setPartial(uint32 stamp, unsigned char *data, 
			      size_t offset, size_t max)
{
	sendLock.writeLock();
	OutgoingRTPPktLink* packetLink = sendFirst;
	while ( packetLink )
	{
		uint32 pstamp = packetLink->getPacket()->getTimestamp();
		if ( pstamp > stamp )
			packetLink = NULL;
		if ( pstamp >= stamp )
			break;

		packetLink = packetLink->getNext();
	}
	if ( !packetLink )
	{
		sendLock.unlock();
		return 0;
	}

	OutgoingRTPPkt* packet = packetLink->getPacket();
	if ( offset >= packet->getPayloadSize() )
		return 0;

	if ( max > packet->getPayloadSize() - offset )
		max = packet->getPayloadSize() - offset;

	memcpy((unsigned char*)(packet->getPayload()) + offset,
	       data, max);
	sendLock.unlock();
	return max;
}

#ifdef  CCXX_NAMESPACES
}
#endif

/** EMACS **
 * Local variables:
 * mode: c++
 * c-basic-offset: 8
 * End:
 */
