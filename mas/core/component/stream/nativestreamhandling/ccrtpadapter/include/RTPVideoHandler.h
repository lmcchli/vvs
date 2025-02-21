#ifndef _RTPVIDEOHANDLER_H_
#define _RTPVIDEOHANDLER_H_

#include <boost/ptr_container/ptr_list.hpp>
#include "RTPHandler.h"

class VideoMediaData;

class RTPVideoHandler: public RTPHandler
{
public:
    RTPVideoHandler(StreamRTPSession& videoSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& videoPayload);

    virtual ~RTPVideoHandler();

    virtual void initializeRecording(long maxWaitForIFrameTimeout)
    {
        RTPHandler::initializeRecording();
    };

    void recordVideoPacket(std::auto_ptr<const ost::AppDataUnit>& adu);

    virtual void packetizeVideoFrames(boost::ptr_list<boost::ptr_list<VideoMediaData> >& videoData) = 0;

    bool haveIgnoredFirstVideoPacket()
    {
        return mHaveIgnoredFirstVideoPacket;
    };
    uint32 getFirstIgnoredVideoPacketTimestamp()
    {
        return mFirstIgnoredVideoPacketTimestamp;
    };

protected:

    virtual bool recordVideoPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu) = 0;

    /**
     * True if at least one video packet have been ignored. Used together
     * with mFirstIgnoredVideoPacketTimestamp.
     */
    bool mHaveIgnoredFirstVideoPacket;

    /**
     * RTP timestamp for first ignored video packet. Can be used
     * to compute the offset between audio and video if RTCP is not used.
     */
    uint32 mFirstIgnoredVideoPacketTimestamp;
};

#endif
