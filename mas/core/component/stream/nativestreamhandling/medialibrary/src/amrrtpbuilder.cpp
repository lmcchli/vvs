#include <amrrtpbuilder.h>

#include "AtomName.h"
#include "MoovAtom.h"
#include "TrakAtom.h"

#include "movreader.h"
#include "amrtrackinfo.h"
#include "movaudiochunk.h"
#include "movrtppacketinfo.h"
#include "movrtppacket.h"

#include "rtpblockhandler.h"

#include "medialibraryexception.h"
#include "jlogger.h"
#include "java/mediaobject.h"

const char* AmrRtpPacketBuilder::CLASSNAME = "masjni.medialibrary.AmrRtpPacketBuilder";

AmrRtpPacketBuilder::AmrRtpPacketBuilder(JNIEnv* env, unsigned framesPerPacket,AmrInfo *movInfo, MovReader& reader) :
	m_framesPerPacket(framesPerPacket), m_reader(reader), m_framesInPacket(0), m_numberOfFrames(0),
	m_lastSentFrameNr(0), m_lastNumberOfFrames(0), m_numberOfPackets(0), m_payloadSize(0),
	m_addedSize(0), m_movInfo(movInfo), m_env(env)
{
	m_reader.reset();
	m_payloads = new PayloadInfo[m_framesPerPacket];
	JLogger::jniLogDebug(m_env, CLASSNAME, "m_payloads - create at %#x - with size %d", m_payloads,
	                     m_framesPerPacket);
	unsigned maxFrameSize=m_movInfo->get_MaxFrameSize();
	for (unsigned i = 0; i < m_framesPerPacket; ++i) {
		m_payloads[i].data = new unsigned char[maxFrameSize]; //largest possible framesize for amr type for the CMR
		JLogger::jniLogDebug(m_env, CLASSNAME, "m_payloads[%d].data - create at %#x - with size %d", i,
		                     m_payloads[i].data, maxFrameSize);
	}

	m_payloadBuffer = new char[1 + m_framesPerPacket + m_framesPerPacket * (maxFrameSize)];
	JLogger::jniLogDebug(m_env, CLASSNAME, "m_payloadBuffer - create at %#x - with size %d", m_payloadBuffer,
	                     1 + m_framesPerPacket + m_framesPerPacket * maxFrameSize);
	m_payloadBuffer[0] = (char) 0xf0; //CMR
}

void AmrRtpPacketBuilder::readAndAddFrame(RtpBlockHandler& blockHandler)
{
	unsigned char toc;
	m_reader.read(&toc, 1);
	unsigned frameType = (unsigned) (toc >> 3) & 0xf;

	++m_numberOfFrames;

	if (frameType != m_movInfo->get_noData() ) {
		if (m_framesInPacket == 0)
			++m_numberOfPackets;

		m_payloads[m_framesInPacket].toc = toc;
		m_payloads[m_framesInPacket].dataSize = m_movInfo->getFrameSize(frameType);
		m_payloadSize += m_payloads[m_framesInPacket].dataSize;

		if (m_payloads[m_framesInPacket].dataSize > 0)
			m_reader.read(m_payloads[m_framesInPacket].data, m_payloads[m_framesInPacket].dataSize);

		++m_framesInPacket;

		if (frameType == m_movInfo->get_sid())
			addAndResetPacket(blockHandler);
	} else if (m_framesInPacket > 0) {
		addAndResetPacket(blockHandler);
	}

	if (m_framesInPacket == m_framesPerPacket)
		addAndResetPacket(blockHandler);
}

void AmrRtpPacketBuilder::AmrRtpPacketBuilder::readAndCalculateSizes()
{
	unsigned char toc;
	m_reader.read(&toc, 1);
	unsigned frameType = (unsigned) (toc >> 3) & 0xf;
	JLogger::jniLogTrace(m_env, CLASSNAME, "readAndCalculateSizes() toc = 0x%x, frametype = 0x%x ",toc,frameType);

	if (frameType != m_movInfo->get_noData()) {
		if (m_framesInPacket == m_framesPerPacket)
			resetPacket();

		if (m_framesInPacket == 0)
			++m_numberOfPackets;

		m_payloads[m_framesInPacket].dataSize = m_movInfo->getFrameSize(frameType);
		JLogger::jniLogTrace(m_env, CLASSNAME, "readAndCalculateSizes() dataSize= %d",m_payloads[m_framesInPacket].dataSize);

		m_payloadSize += m_payloads[m_framesInPacket].dataSize;

		m_payloads[m_framesInPacket].toc = toc;

		if (m_payloads[m_framesInPacket].dataSize > 0)
			m_reader.read(m_payloads[m_framesInPacket].data, m_payloads[m_framesInPacket].dataSize);

		++m_framesInPacket;
		++m_numberOfFrames;

		if (frameType == m_movInfo->get_sid())
			resetPacket();
	} else if (m_framesInPacket > 0)
		resetPacket();
}

unsigned AmrRtpPacketBuilder::AmrRtpPacketBuilder::readFrameType()
{
	unsigned char toc;
	m_reader.read(&toc, 1);
	unsigned frameType = (unsigned) (toc >> 3) & 0xf;

	if (frameType != m_movInfo->get_noData()) {
		m_payloads[m_framesInPacket].dataSize = m_movInfo->getFrameSize(frameType);

		if (m_payloads[m_framesInPacket].dataSize > 0)
			m_reader.read(m_payloads[m_framesInPacket].data, m_payloads[m_framesInPacket].dataSize);
	}

	return frameType;
}

AmrRtpPacketBuilder::~AmrRtpPacketBuilder()
{
	deletePayloads();
	JLogger::jniLogDebug(m_env, CLASSNAME, "~m_payloadBuffer - delete at %#x", m_payloadBuffer);
	if ( m_payloadBuffer !=0 ) {
		delete[] m_payloadBuffer;
		m_payloadBuffer = 0;
		JLogger::jniLogDebug(m_env, CLASSNAME, "~m_payloadBuffer - delete at %#x", m_payloadBuffer);
	}
	JLogger::jniLogDebug(m_env, CLASSNAME, "~AmrRtpPacketBuilder - delete at %#x", this);
}

void AmrRtpPacketBuilder::flushLastPacket(RtpBlockHandler& blockHandler)
{
	if (m_framesInPacket > 0)
		addAndResetPacket(blockHandler);
}

unsigned AmrRtpPacketBuilder::getNumberOfPackets()
{
	return AmrRtpPacketBuilder::m_numberOfPackets;
}

unsigned AmrRtpPacketBuilder::getNumberOfFrames()
{
	return m_numberOfFrames;
}

unsigned AmrRtpPacketBuilder::getPayloadSize()
{
	return m_payloadSize;
}


void AmrRtpPacketBuilder::deletePayloads()
{
	if (m_payloads == 0 ) return;
	for (unsigned i = 0; i < m_framesPerPacket; ++i) {
		if ( m_payloads[i].data != 0 ) {
			JLogger::jniLogDebug(m_env, CLASSNAME, "~m_payloads[%d].data - delete at %#x", i, m_payloads[i].data);
			delete[] m_payloads[i].data;
			m_payloads[i].data = 0;
		}
	}

	JLogger::jniLogDebug(m_env, CLASSNAME, "~m_payloads - delete at %#x", m_payloads);
	delete[] m_payloads;
	m_payloads = 0;
}

void AmrRtpPacketBuilder::addAndResetPacket(RtpBlockHandler& blockHandler)
{
	for (unsigned i = 1; i < m_framesInPacket; ++i)
		m_payloadBuffer[i] = (char) (m_payloads[i - 1].toc | 0x80);

	m_payloadBuffer[m_framesInPacket] = (char) (m_payloads[m_framesInPacket - 1].toc & 0x7f);

	unsigned addedPayload = 0;
	for (unsigned i = 0; i < m_framesInPacket; ++i) {
		memcpy(m_payloadBuffer + 1 + m_framesInPacket + addedPayload, m_payloads[i].data, m_payloads[i].dataSize);
		addedPayload += m_payloads[i].dataSize;
	}

	m_addedSize += 1 + m_framesInPacket + addedPayload;
	//samples per 20ms frame - amr is always 20ms.  for amr this is 160, for amr-wb 320.
	//interestingly enough if you shorten this time it will play the rtp faster, speeding up the
	//recording - see rfc 3267 section 4.2
	unsigned samplesper20msFrame= m_movInfo->get_samplesPer20msFrame();

	//these times are expressed in samples in the 20ms period.
	unsigned tstampInc = (m_numberOfFrames - m_framesInPacket - m_lastSentFrameNr) * samplesper20msFrame;
	unsigned timeDelta = (m_numberOfFrames - m_lastNumberOfFrames) * samplesper20msFrame;

	blockHandler.addAudioPayload(m_payloadBuffer, 1 + m_framesInPacket + addedPayload, tstampInc, timeDelta);

	m_lastSentFrameNr = m_numberOfFrames - m_framesInPacket;
	m_lastNumberOfFrames = m_numberOfFrames;

	resetPacket();
}

void AmrRtpPacketBuilder::resetPacket()
{
	m_framesInPacket = 0;
}



