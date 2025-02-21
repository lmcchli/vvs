#include "mediadescription.h"

void 
MediaDescription::setAudioCodec(const base::String& audioCodec)
{
    m_audioCodec = audioCodec;
}

const base::String& 
MediaDescription::getAudioCodec()
{
    return m_audioCodec;
}

void 
MediaDescription::setVideoCodec(const base::String& videoCodec)
{
    m_videoCodec = videoCodec;
}

const base::String& 
MediaDescription::getVideoCodec()
{
    return m_videoCodec;
}

void 
MediaDescription::setDuration(unsigned duration)
{
    m_duration = duration;
}

unsigned 
MediaDescription::getDuration()
{
    return m_duration;
}
    
unsigned 
MediaDescription::getAudioStartTimeOffset()
{
    return m_audioStartTimeOffset;
}
    
void 
MediaDescription::setAudioStartTimeOffset(unsigned offset)
{
    m_audioStartTimeOffset = offset;
}
    
unsigned 
MediaDescription::getVideoStartTimeOffset()
{
    return m_videoStartTimeOffset;
}

void 
MediaDescription::setVideoStartTimeOffset(unsigned offset)
{
    m_videoStartTimeOffset = offset;
}

