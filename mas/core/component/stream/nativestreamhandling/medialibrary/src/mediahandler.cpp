#include "mediahandler.h"

#include "java/mediaobject.h"
#include "mediaenvelope.h"
#include "mediaparser.h"
#include "formathandlerfactory.h"
#include "rtpblockhandler.h"
#include "jlogger.h"
#include "jniutil.h"
#include "MediaValidator.h"

static const char* CLASSNAME = "masjni.medialibrary.MediaHandler";

MediaHandler::MediaHandler(java::MediaObject& mediaObject, unsigned pTime, unsigned maxPTime, unsigned mtu) :
        m_javaMediaObject(mediaObject), m_mediaParser(0), m_mediaObject(0), m_isOk(false), m_audioPacketCount(0),
        m_audioBlockSize(0), m_videoPacketCount(0), m_videoBlockSize(0), m_pTime(pTime), m_maxPTime(maxPTime),
        m_mtu(mtu)
{
    JLogger::jniLogDebug(mediaObject.getJniEnv(), CLASSNAME, "MediaHandler - create at %#x", this);
}

MediaHandler::~MediaHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(m_javaMediaObject.getJniEnv()), CLASSNAME,
            "~MediaHandler - delete at %#x", this);
}

void MediaHandler::parse(boost::ptr_list<MediaValidator>& mediaValidators, long cursor)
{
    JNIEnv* env = m_javaMediaObject.getJniEnv();

    MediaParser* parser(FormatHandlerFactory::getParser(m_javaMediaObject));
    if (parser == 0) {
        JLogger::jniLogWarn(env, CLASSNAME, "Could not find parser for media object with content type '%s'",
                m_javaMediaObject.getContentType().c_str());
        return;
    }
    m_mediaParser.reset(parser);
    JLogger::jniLogTrace(env, CLASSNAME, "Init parser ...");
    m_mediaParser->init();

    JLogger::jniLogTrace(env, CLASSNAME, "Setting pTime: %d", m_pTime);
    m_mediaParser->setPTime(m_pTime);
    m_mediaParser->setMaxPTime(m_maxPTime);

    m_isOk = m_mediaParser->parse();
    if (m_isOk) {
        for (boost::ptr_list<MediaValidator>::iterator validator = mediaValidators.begin();
                validator != mediaValidators.end(); ++validator) {
            validator->validateMediaProperties(m_mediaParser.get());
        }
        m_mediaParser->setCursor(cursor);

        m_mediaObject.reset(new MediaEnvelope());
        // Retreiving codec information
        m_audioCodec = m_mediaParser->getAudioCodec();
        JLogger::jniLogTrace(env, CLASSNAME, "Audio codec: '%s'", m_audioCodec.c_str());

        m_videoCodec = m_mediaParser->getVideoCodec();
        JLogger::jniLogTrace(env, CLASSNAME, "Video codec: '%s'", m_videoCodec.c_str());

        // Retreiving codec block size
        m_audioBlockSize = m_mediaParser->getAudioBlockSize();
        m_audioPacketCount = m_mediaParser->getAudioPacketCount();
        m_videoBlockSize = m_mediaParser->getVideoBlockSize();
        m_videoPacketCount = m_mediaParser->getVideoPacketCount();

        JLogger::jniLogTrace(env, CLASSNAME, "Audio block size:   %d", m_audioBlockSize);
        JLogger::jniLogTrace(env, CLASSNAME, "Audio packet count: %d", m_audioPacketCount);
        JLogger::jniLogTrace(env, CLASSNAME, "Video block size:   %d", m_videoBlockSize);
        JLogger::jniLogTrace(env, CLASSNAME, "Video packet count: %d", m_videoPacketCount);

        m_mediaObject->getBlockHandler().initialize(m_audioPacketCount, m_audioBlockSize, m_videoPacketCount,
                m_videoBlockSize);
        m_mediaParser->getData(m_mediaObject->getBlockHandler());

        JLogger::jniLogTrace(env, CLASSNAME, "Added data to blockhandler");

        m_mediaObject->setCursor(m_mediaParser->getCursor());
        m_mediaObject->getMediaDescription().setAudioCodec(m_audioCodec);
        m_mediaObject->getMediaDescription().setVideoCodec(m_videoCodec);

        JLogger::jniLogTrace(env, CLASSNAME, "Duration: %d", m_mediaParser->getDuration());

        m_mediaObject->getMediaDescription().setDuration(m_mediaParser->getDuration());
        JLogger::jniLogTrace(env, CLASSNAME, "AudioStartTimeOffset: %d", m_mediaParser->getAudioStartTimeOffset());

        m_mediaObject->getMediaDescription().setAudioStartTimeOffset(m_mediaParser->getAudioStartTimeOffset());
        JLogger::jniLogTrace(env, CLASSNAME, "VideoStartTimeOffset: %d", m_mediaParser->getVideoStartTimeOffset());

        m_mediaObject->getMediaDescription().setVideoStartTimeOffset(m_mediaParser->getVideoStartTimeOffset());
        m_mediaObject->getSessionDescription().setPTime(m_pTime);
    }
}

bool MediaHandler::isOk()
{
    return m_isOk;
}

const base::String&
MediaHandler::getAudioCodec()
{
    return m_audioCodec;
}

unsigned MediaHandler::getAudioBlockSize()
{
    return m_audioBlockSize;
}

unsigned MediaHandler::getAudioPacketCount()
{
    return m_audioPacketCount;
}

const base::String&
MediaHandler::getVideoCodec()
{
    return m_videoCodec;
}

unsigned MediaHandler::getVideoBlockSize()
{
    return m_videoBlockSize;
}

unsigned MediaHandler::getVideoPacketCount()
{
    return m_videoPacketCount;
}

long MediaHandler::getAdjustedCursor()
{
    return (m_mediaParser.get() != 0 ? m_mediaParser->getCursor() : 0);
}

MediaEnvelope*
MediaHandler::getMediaObject()
{
    return m_mediaObject.release();
}
