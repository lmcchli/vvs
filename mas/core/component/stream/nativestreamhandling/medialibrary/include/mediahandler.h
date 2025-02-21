#ifndef _MediaHandler_h_
#define _MediaHandler_h_

#include <memory>
#include <base_include.h>
#include <base_std.h>

#include <boost/ptr_container/ptr_list.hpp>

#include "jni.h"

namespace java {
class MediaObject;
};

class MediaEnvelope;
class MediaParser;
class MediaValidator;

// The MediaHandler is responsible for parsing a MediaObject into streamable RTP packets.
class MediaHandler
{
public:
    MediaHandler(java::MediaObject& mediaObject, unsigned pTime, unsigned maxPTime, unsigned mtu);
    ~MediaHandler();

    void parse(boost::ptr_list<MediaValidator>& mediaValidators, long cursor = 0);
    bool isOk();

    const base::String& getAudioCodec();
    unsigned getAudioBlockSize();
    unsigned getAudioPacketCount();

    const base::String& getVideoCodec();
    unsigned getVideoBlockSize();
    unsigned getVideoPacketCount();

    long getAdjustedCursor();

    MediaEnvelope* getMediaObject();

private:
    java::MediaObject& m_javaMediaObject;
    std::auto_ptr<MediaParser> m_mediaParser;
    std::auto_ptr<MediaEnvelope> m_mediaObject;
    bool m_isOk;
    // Audio specific properties
    base::String m_audioCodec;
    unsigned m_audioPacketCount;
    unsigned m_audioBlockSize;
    // Video specific properties
    base::String m_videoCodec;
    unsigned m_videoPacketCount;
    unsigned m_videoBlockSize;
    unsigned m_pTime;
    unsigned m_maxPTime;
    unsigned m_mtu;
};

#endif
