#include "mediaparser.h"
#include "jlogger.h"
#include "jniutil.h"

const char* MediaParser::MP_CLASSNAME = "masjni.medialibrary.MediaParser";

MediaParser::MediaParser(java::MediaObject* mediaObject) :
        mediaObject(mediaObject), 
		m_isVideo(false), 
		m_pTime(0), 
		m_maxPTime(0),
		m_env(mediaObject->getJniEnv())
{
    JLogger::jniLogDebug(mediaObject->getJniEnv(), MP_CLASSNAME, "MediaParser - create at %#x", this);
}

MediaParser::~MediaParser()
{
    JLogger::jniLogDebug(m_env, MP_CLASSNAME,
            "~MediaParser - delete at %#x", this);
}

int MediaParser::getFrameCount()
{
    return 0;
}

void MediaParser::getFrame(boost::ptr_list<MovRtpPacket>& rtpPackets, int frameIndex)
{
}

const base::String&
MediaParser::getVideoCodec()
{
    return m_videoCodec;
}

int MediaParser::getPTime()
{
    return m_pTime;
}

int MediaParser::getMaxPTime()
{
    return m_maxPTime;
}

void MediaParser::setPTime(int pTime)
{
    m_pTime = pTime;
}

void MediaParser::setMaxPTime(int maxPTime)
{
    m_maxPTime = maxPTime;
}

bool MediaParser::isVideo()
{
    return m_isVideo;
}

unsigned MediaParser::getAudioStartTimeOffset()
{
    return 0;
}

unsigned MediaParser::getVideoStartTimeOffset()
{
    return 0;
}
