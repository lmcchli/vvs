#include "mediaenvelope.h"

MediaEnvelope::MediaEnvelope()
{
}

MediaEnvelope::~MediaEnvelope()
{
}

RtpBlockHandler&
MediaEnvelope::getBlockHandler()
{
    return m_blockHandler;
}

MediaDescription&
MediaEnvelope::getMediaDescription()
{
    return m_mediaDescription;
}

SessionDescription&
MediaEnvelope::getSessionDescription()
{
    return m_sessionDescription;
}

void MediaEnvelope::setCursor(unsigned cursor)
{
    // TODO: is this in use?
}
