#ifndef _H263RTPHANDLER_H_
#define _H263RTPHANDLER_H_

#include <base_std.h>
#include <ccrtp/rtp.h>
#include <boost/scoped_ptr.hpp>
#include <boost/ptr_container/ptr_list.hpp>

#include "jni.h"
#include "mediabuilder.h"
#include "RTPVideoHandler.h"

class StreamRTPSession;
class VideoMediaData;
class InboundSession;

class H263RTPHandler: public RTPVideoHandler
{
public:
    H263RTPHandler(JNIEnv* env, StreamRTPSession& videoSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& audioPayload);
    virtual ~H263RTPHandler();

    virtual void initializeRecording(long maxWaitForIFrameTimeout);

    virtual void packetizeVideoFrames(boost::ptr_list<boost::ptr_list<VideoMediaData> >& videoData);

protected:
    virtual bool recordVideoPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu);

    boost::ptr_list<VideoMediaData>::iterator getInsertPosition(uint32 extendedSeq,
            boost::ptr_list<VideoMediaData>& videoData, bool& duplicate);

    bool isIFrame(const char* payload);

    bool mFoundStartPacket;
    boost::ptr_list<VideoMediaData> mVideoBuffer;

    /**
     * Time when start waiting for data. Used when
     * checking for maximum wait timeout.
     */
    timeval mStartWaitForDataTimestamp;

    long mMaxWaitForIFrameTimeout;
    bool mShoulWaitForMarkedPacket;
};

#endif
