#include "sessiondescription.h"

SessionDescription::SessionDescription()
{
    m_audioPayload.defined = false;
    m_videoPayload.defined = false;
}

void SessionDescription::setAudioPayload(unsigned payloadType, unsigned clockRate)
{
    m_audioPayload.defined = true;
    m_audioPayload.payloadType = payloadType;
    m_audioPayload.clockRate = clockRate;
}

SessionDescription::RtpPayload&
SessionDescription::getAudioPayload()
{
    return m_audioPayload;
}

void SessionDescription::setPTime(unsigned pTime)
{
    m_pTime = pTime;
}

unsigned SessionDescription::getPTime()
{
    return m_pTime;
}

void SessionDescription::setVideoPayload(unsigned payloadType, unsigned clockRate)
{
    m_videoPayload.defined = true;
    m_videoPayload.payloadType = payloadType;
    m_videoPayload.clockRate = clockRate;
}

SessionDescription::RtpPayload&
SessionDescription::getVideoPayload()
{
    return m_videoPayload;
}

void SessionDescription::setDtmfPayload(unsigned payloadType, unsigned clockRate)
{
    m_dtmfPayload.defined = true;
    m_dtmfPayload.payloadType = payloadType;
    m_dtmfPayload.clockRate = clockRate;
}

SessionDescription::RtpPayload&
SessionDescription::getDtmfPayload()
{
    return m_dtmfPayload;
}

