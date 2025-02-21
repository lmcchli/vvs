#include "movrtppacket.h"

const int MovRtpPacket::MAX_VIDEO_RTP_PACKET_SPLIT = 15;
const int MovRtpPacket::MAX_EACH_VIDEO_RTP_PACKET = 2000;

MovRtpPacket::MovRtpPacket(MovRtpPacketInfo& info) :
        m_info(info), m_data(new unsigned char[MAX_EACH_VIDEO_RTP_PACKET]), m_ownsData(true), m_length(0),
        m_frameTime(0)
{
}

MovRtpPacket::MovRtpPacket() :
        m_data(NULL), m_ownsData(false), m_length(0), m_frameTime(0)
{
}

MovRtpPacket::~MovRtpPacket()
{
    if (m_data != NULL && m_ownsData) {
        delete[] m_data;
    }

    m_data = NULL;
}

bool MovRtpPacket::operator==(const MovRtpPacket& leftSide)
{
    if (m_length != leftSide.m_length)
        return false;
    if (m_frameTime != leftSide.m_frameTime)
        return false;
    for (unsigned i(0); i < m_length; i++) {
        if (m_data[i] != leftSide.m_data[i])
            return false;
    }
    return true;
}

bool MovRtpPacket::operator!=(const MovRtpPacket& leftSide)
{
    return !(*this == leftSide);
}

unsigned char* MovRtpPacket::getData()
{
    return m_data;
}

void MovRtpPacket::setData(char* data)
{
    m_data = (unsigned char *) data;
}

unsigned MovRtpPacket::getLength()
{
    return m_length;
}

void MovRtpPacket::setLength(unsigned length)
{
    m_length = length;
}

void MovRtpPacket::setFrameTime(unsigned frameTime)
{
    m_frameTime = frameTime;
}

unsigned MovRtpPacket::getFrameTime()
{
    return m_frameTime;
}

unsigned short MovRtpPacket::getHeaderInfo()
{
    return m_info.rtpHeaderInfo;
}

unsigned short MovRtpPacket::getSequenceNumber()
{
    return m_info.rtpSequenceNumber;
}

unsigned MovRtpPacket::getTransmissionTime()
{
    return m_info.transmissionTime;
}
