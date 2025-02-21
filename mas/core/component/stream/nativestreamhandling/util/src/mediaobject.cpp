#include "mediaobject.h"

MediaObject::MediaObject()
{
}

MediaObject::~MediaObject()
{
}

RtpBlockHandler&
MediaObject::getBlockHandler()
{
    return m_blockHandler;
}

MediaDescription&
MediaObject::getMediaDescription()
{
    return m_mediaDescription;
}

SessionDescription&
MediaObject::getSessionDescription()
{
    return m_sessionDescription;
}

void MediaObject::setCursor(unsigned cursor)
{
    // TODO: is this in use?
}
