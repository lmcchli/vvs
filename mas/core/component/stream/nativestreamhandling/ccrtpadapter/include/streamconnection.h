#ifndef _StreamConnection_h_
#define _StreamConnection_h_

#include "jni.h"

#include <cc++/config.h>
#include <ccrtp/queuebase.h>
#include <base_std.h>

class StreamRTPSession;
namespace java {
class StreamContentInfo;
};

class OutboundSession;
class ControlToken;

/**
 * StreamConnection handles the connection of an input to an output stream.
 *
 * The purpose of this class is to provide means to join input streams to 
 * output streams. The connection is supposed to be owned by the output 
 * stream and known by the input stream (when joined).
 * Packets which are sent through the StreamConnection are repacketed
 * and sent to a session "owned" by a StreamSendJob.
 */
class StreamConnection
{
public:
    /**
     * The constructor.
     *
     * Takes both an audio and a video session. The video session is allowed
     * to be null.
     */
    StreamConnection(OutboundSession& outboundSession, java::StreamContentInfo& inboundStreamContent);

    virtual ~StreamConnection();

    void open();

    void close();
    /**
     * Sending an audio RTP package.
     * The package is re-packeted before it's sent through the audio session.
     * <p>
     * Note that the timestamp is generated from the original senders clock and
     * must be adjusted to the audio sessions clock before sending the packet.
     *
     * @parameter timestamp RTP timestamp for the packet. 
     * @parameter adu       Package data.
     */
    void sendAudioPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu);

    /**
     * Sending an video RTP package.
     * The package is re-packeted before it's sent through the video session.
     * <p>
     * Note that the timestamp is generated from the original senders clock and
     * must be adjusted to the audio sessions clock before sending the packet.
     *
     * @parameter timestamp RTP timestamp for the packet. 
     *                      
     * @parameter adu       Package data.
     */
    void sendVideoPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu);

    /**
     * Sending an DTMF RTP package (RFC2833).
     * The package is re-packeted before it's sent through the audio session.
     * <p>
     * Note that the timestamp is generated from the original senders clock and
     * must be adjusted to the audio sessions clock before sending the packet.
     *
     * @parameter timestamp RTP timestamp for the packet.      
     * @parameter adu       Package data.	 
     * @parameter dtmfPayloadType RTP payload type for the DTMF packet.
     * @parameter masterPayloadType RTP payload type for the audio session.
     * @parameter clocrate clockrate for the audio session.
     */
    void sendDTMFPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu, int masterPayloadType,
            unsigned clockrate);

    void onFirstSend();

    void sendControlToken(ControlToken* token);

private:
    /**
     * A pointer to an active audio RTP session.
     */
    OutboundSession* mSession;

    java::StreamContentInfo& mContentInfo;

    bool mSending;

    bool mSendVideo;

    /**
     * Timestamp for outgoing audio packages.
     */
    unsigned mAudioTimestamp;

    /**
     * Timestamp for outgoing video packages.
     */
    unsigned mVideoTimestamp;

    /**
     * Timestamp for the previous received audio packet.
     */
    uint32 mPreviousInboundAudioTimestamp;

    /**
     * Timestamp for the previous received video packet.
     */
    uint32 mPreviousInboundVideoTimestamp;
};

#endif
