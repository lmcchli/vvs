#ifndef _MediaObject_h_
#define _MediaObject_h_

#include "rtpblockhandler.h"
#include "mediadescription.h"
#include "sessiondescription.h"

#include <memory>

// The MediaEnvelope is an envelope containing pre generated
// streamable RTP packets and information about media and session.
//
// The purpose of this class is mainly to encapsulate
// RTP block data and media/session description into one
// object instead of cluttering the RTP block data with
// properties not belonging to RTP packets.
class MediaEnvelope {
public:
    MediaEnvelope();
    ~MediaEnvelope();

public:
    RtpBlockHandler& getBlockHandler();
    MediaDescription& getMediaDescription();
    SessionDescription& getSessionDescription();

public:
    void setCursor(unsigned cursor);

private:
    RtpBlockHandler m_blockHandler;
    MediaDescription m_mediaDescription;
    SessionDescription m_sessionDescription;
};

#endif
