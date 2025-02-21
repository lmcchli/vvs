/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include "playjob.h"
#include "jniutil.h"
#include "jlogger.h"
#include "rtppayload.h"
#include "rtpblockhandler.h"
#include "mediaenvelope.h"
#include "streamrtpsession.h"
#include "formathandlerfactory.h"
#include "mediaparser.h"
#include "outboundsession.h"
#include "streamcontentinfo.h"
#include <base_std.h> 
#include <boost/ptr_container/ptr_list.hpp>
#include <movrtppacket.h>
#include <medialibraryexception.h>

#include "Callback.h"

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.PlayJob";

PlayJob::PlayJob(JNIEnv *env, OutboundSession &session, unsigned requestId, int playOption, long cursor, std::auto_ptr<MediaEnvelope>& mediaObject) :
        mState(PUT_ON_QUEUE), mRequestId(requestId), mPlayOption(playOption), mPTime(0), mCursor(cursor), mStartAudioTimeStamp(0),
        mSession(session), mPlayVideo(false), mIsPlaying(true), mIsOk(false), m_mediaObject(mediaObject)
{
    JLogger::jniLogDebug(env, CLASSNAME, "PlayJob - create at %#x", this);
}

PlayJob::~PlayJob()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~PlayJob - delete at %#x", this);
}

void PlayJob::init()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "Initializing PlayJob:%d", mRequestId);

    mIsOk = true;
    StreamRTPSession& audioSession(mSession.getAudioSession());
    StreamRTPSession& videoSession(mSession.getVideoSession());

    // Update audio session with proper payload type, if any.
    if (!m_mediaObject->getSessionDescription().getAudioPayload().defined) {
        mIsOk = false;
        JLogger::jniLogWarn(env, CLASSNAME, "Undefined audio payload format ...");
        return;
    }

    mPTime = m_mediaObject->getSessionDescription().getPTime();
    JLogger::jniLogTrace(env, CLASSNAME, "Setting payload type in audio session ...");

    audioSession.setPayload(m_mediaObject->getSessionDescription().getAudioPayload().payloadType,
            m_mediaObject->getSessionDescription().getAudioPayload().clockRate);

    // Update video session with proper payload type, if any.
    // First determine if we are supposed to play video or not
    // TODO: video or not should be determined from media, ie is there a codec to payload 
    // type mapping, then we will play video.
    // Check is there is a corrsponding payload type defined for the video codec
    if (m_mediaObject->getSessionDescription().getVideoPayload().defined) {
        JLogger::jniLogTrace(env, CLASSNAME, "Setting payload type in video session ...");

        videoSession.setPayload(m_mediaObject->getSessionDescription().getVideoPayload().payloadType,
                m_mediaObject->getSessionDescription().getVideoPayload().clockRate);
        mPlayVideo = true;
    } else {
        mPlayVideo = false;
        JLogger::jniLogTrace(env, CLASSNAME, "No video payload format defined, voice only assumed ...");
    }

    DTMFSender& dtmfSender = mSession.getDtmfSender();
    dtmfSender.setMasterPayloadFormat(m_mediaObject->getSessionDescription().getAudioPayload().payloadType,
            m_mediaObject->getSessionDescription().getAudioPayload().clockRate);
    dtmfSender.setDtmfPayloadFormat(m_mediaObject->getSessionDescription().getDtmfPayload().payloadType,
            m_mediaObject->getSessionDescription().getDtmfPayload().clockRate);

}

void PlayJob::onTick()
{
}

bool PlayJob::putPacketsOnQueue()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "Posting RTP Packets on queue. Cursor: %d", mCursor);
    m_mediaObject->getBlockHandler().reset();
    if (mPlayVideo) {
        JLogger::jniLogInfo(env, CLASSNAME, "Queueing outbound audio/video RTP due to PLAY.");
        playVideo();
    } else {
        JLogger::jniLogInfo(env, CLASSNAME, "Queueing outbound audio RTP due to PLAY.");
        playAudioOnly();
    }
    m_mediaObject->getBlockHandler().release();
    return false;
}

void PlayJob::playAudioOnly()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    StreamRTPSession &audioSession = mSession.getAudioSession();

    // Getting a starting timestamp
    uint32 timestamp(audioSession.getCurrentTimestamp());

    DTMFSender& dtmfSender = mSession.getDtmfSender();
    uint32 rtpTimestampAfterLastToken = dtmfSender.getRtpTimestampAfterLastToken();

    JLogger::jniLogTrace(env, CLASSNAME, "Current audio timestamp=%d", timestamp);
    JLogger::jniLogTrace(env, CLASSNAME, "rtpTimestampAfterLastToken=%d", rtpTimestampAfterLastToken);

    if (timestamp < rtpTimestampAfterLastToken) {
        // This prompt is sent before a previously sent token is finished,
        // this prompt must be delayed so it is played after the 
        // previous token.
        timestamp = rtpTimestampAfterLastToken;
    }

    mStartAudioTimeStamp = timestamp;

    // This is the main loop, where packets are transmitted.
    int numberOfPackets = 0;

    char* buffer;
    unsigned packetSize;
    unsigned tstampInc;
    unsigned timeDelta;
    while (m_mediaObject->getBlockHandler().getNextAudioPayload(buffer, packetSize, tstampInc, timeDelta)) {
        // Increase timestamp with delta from last packet.
        timestamp += tstampInc;

        // send an RTP packet, providing timestamp and payload.
        audioSession.putData(timestamp, (const unsigned char*) buffer, packetSize);

        /*JLogger::jniLogError(env, CLASSNAME, "Added audio packet with timestamp: %d and size %d tstampInc: %d",
         timestamp, packetSize, tstampInc);*/
        numberOfPackets++;

        if (!mIsPlaying) {
            // Cause is already set in one of the methods cancel, stop
            // or streamDeleted
            JLogger::jniLogTrace(env, CLASSNAME, "Stopped, stops putting more data on queue");
            break;
        }

        // If a token was sent, the timestamp must be increased
        timestamp += dtmfSender.sendNextToken();
    }
    audioSession.onPlayStarted();

    JLogger::jniLogTrace(env, CLASSNAME, "Last audio timestamp=%d", timestamp);
    JLogger::jniLogTrace(env, CLASSNAME, "Waiting for all data to be sent.");
    JLogger::jniLogTrace(env, CLASSNAME, "Should be %d number of packages", numberOfPackets);
}

void PlayJob::playVideo()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    StreamRTPSession &audioSession = mSession.getAudioSession();
    StreamRTPSession &videoSession = mSession.getVideoSession();
    RtpBlockHandler& blockHandler(m_mediaObject->getBlockHandler());
    SessionDescription& sessionDescription(m_mediaObject->getSessionDescription());
    MediaDescription& mediaDescription(m_mediaObject->getMediaDescription());

    // Audio and video packets are put on their respective sessions
    // output queues by the same thread. Therefore this method tries
    // to put, for each video packet, enough audio packets to cover
    // roughly the same period of time (ms - not timestamp units!).
    // The timing when the packets are put on their queues does not
    // have to be exact, the timestamps in the packets is the important
    // thing. 

    uint32 videoTimestamp(videoSession.getCurrentTimestamp());
    JLogger::jniLogTrace(env, CLASSNAME, "Video timestamp before adjustment: %d", videoTimestamp);
    uint32 audioTimestamp(audioSession.getCurrentTimestamp());
    unsigned videoClockRate(sessionDescription.getVideoPayload().clockRate);
    unsigned audioClockRate(sessionDescription.getAudioPayload().clockRate);

    // frametime multiplied with this factor converts the 
    // time from milliseconds to timestamp units
    unsigned videoMsToTsUnitFactor(videoClockRate / 1000);
    unsigned audioMsToTsUnitFactor(audioClockRate / 1000);

    // Adjust according to specified skew
    StreamRTPSession::SkewMethod method;
    long skew;
    mSession.getSkew(method, skew);
    if ((method == StreamRTPSession::LOCAL_AND_RTCP) || (method == StreamRTPSession::LOCAL)) {
        if (skew < 0) {
            audioTimestamp -= (skew * audioMsToTsUnitFactor);
            JLogger::jniLogTrace(env, CLASSNAME, "Adjusted audio timestamp from %d to %d",
                    audioSession.getCurrentTimestamp(), audioTimestamp);
        } else if (skew > 0) {
            videoTimestamp += (skew * videoMsToTsUnitFactor);
            JLogger::jniLogTrace(env, CLASSNAME, "Adjusted video timestamp from %d to %d",
                    audioSession.getCurrentTimestamp(), videoTimestamp);
        }
    }

    // Now, adjust to offset according to info in media 
    unsigned audioOffsetMs(mediaDescription.getAudioStartTimeOffset());
    unsigned videoOffsetMs(mediaDescription.getVideoStartTimeOffset());
    if (audioOffsetMs > 0) {
        audioTimestamp += (audioOffsetMs * audioMsToTsUnitFactor);
        JLogger::jniLogTrace(env, CLASSNAME, "Adjusted audio timestamp to %d according to audio offset in media.",
                audioTimestamp);
    } else if (videoOffsetMs > 0) {
        videoTimestamp += (videoOffsetMs * videoMsToTsUnitFactor);
        JLogger::jniLogTrace(env, CLASSNAME, "Adjusted video timestamp to %d according to video offset in media.",
                videoTimestamp);
    }

    DTMFSender& dtmfSender = mSession.getDtmfSender();
    uint32 rtpTimestampAfterLastToken = dtmfSender.getRtpTimestampAfterLastToken();
    if (audioTimestamp < rtpTimestampAfterLastToken) {
        // This prompt is sent before a previously sent token is finished,
        // this prompt must be delayed so it is played after the 
        // previous token.
        videoTimestamp += (rtpTimestampAfterLastToken - audioTimestamp) *
        // Must compensate for the difference in clock rate
                videoClockRate / audioClockRate;
        audioTimestamp = rtpTimestampAfterLastToken;
    }

    mStartAudioTimeStamp = audioTimestamp;

    int totalNrOfVideoPackets(0);

    // Used to put approximate the same amount of audio as video on their
    // respective queues.
    uint32 totalFrameTimeMs(0);
    uint32 totalAudioLenghtMs(0);

    bool videoDataLeft(true);
    int currentVideoFrame(0);

    int totalNrOfAudioPackets(0);

    bool audioDataLeft(true);

    JLogger::jniLogTrace(env, CLASSNAME, "Cursor: %d", mCursor);
    JLogger::jniLogTrace(env, CLASSNAME, "Audio TS: %d", audioTimestamp);
    JLogger::jniLogTrace(env, CLASSNAME, "Video TS: %d", videoTimestamp);

    unsigned mediaFrameTime;
    char* payload;
    unsigned length;
    unsigned frameTime(0);
    while (videoDataLeft || audioDataLeft) {
        bool sameFrame(true);
        do {
            if ( (videoDataLeft = blockHandler.getNextVideoPayload(mediaFrameTime, payload, length)) ) {
                if (0 != mediaFrameTime) {
                    sameFrame = false;
                    totalFrameTimeMs += mediaFrameTime;
                    frameTime = mediaFrameTime * videoMsToTsUnitFactor;
                }
                videoSession.setMark(!sameFrame);
                unsigned videoDataLength(length);

                // send an RTP packet, providing timestamp and payload.
                unsigned char* videoPacketData((unsigned char*) payload);
                //JLogger::jniLogError(env, CLASSNAME, "Video Data Length: %d", videoDataLength);
                videoSession.putData(videoTimestamp, videoPacketData, videoDataLength);
                totalNrOfVideoPackets++;
                //JLogger::jniLogTrace(env, CLASSNAME, "Put video: %d packets: %d", videoTimestamp, totalNrOfVideoPackets);
                if (!mIsPlaying) {
                    // Cause is already set in one of the methods cancel, stop
                    // or streamDeleted
                    JLogger::jniLogTrace(env, CLASSNAME, "Stopped, stops putting more data on queue");
                    break;
                }
            }
        } while (videoDataLeft && sameFrame);
        videoTimestamp += frameTime;
        currentVideoFrame++;

        if (mIsPlaying && audioDataLeft) {

            unsigned chunkSize(0);
            char* audioData;
            while (audioDataLeft && (!videoDataLeft || (totalFrameTimeMs > totalAudioLenghtMs))) {
                // Put enough audio packets to cover roughly the same period of 
                // time as the last video frame.
                unsigned tstampInc;
                unsigned timeDelta;
                if ( (audioDataLeft = blockHandler.getNextAudioPayload(audioData, chunkSize, tstampInc, timeDelta)) ) {
                    audioTimestamp += tstampInc;

                    audioSession.putData(audioTimestamp, (unsigned char*) audioData, chunkSize);
                    //JLogger::jniLogError(env, CLASSNAME, "timeDelta: %d", timeDelta);
                    totalAudioLenghtMs += timeDelta;

                    totalNrOfAudioPackets++;
                }
            }
        }

        if (!mIsPlaying) {
            // Cause is already set in one of the methods cancel, stop
            // or streamDeleted
            JLogger::jniLogTrace(env, CLASSNAME, "Stopped, stops putting more data on queue");
            break;
        }
    }

    JLogger::jniLogTrace(env, CLASSNAME, "Waiting for all data to be sent.");
    JLogger::jniLogTrace(env, CLASSNAME, "Should be %d number of video packages and %d audio packets.",
            totalNrOfVideoPackets, totalNrOfAudioPackets);

    audioSession.onPlayStarted();
    videoSession.onPlayStarted();
}

void PlayJob::cancel()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsPlaying) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to PLAY_CANCELLED");
        sendPlayFinished(Callback::OK_CANCELLED);
    }
}

bool PlayJob::isDone()
{
    return !mIsPlaying;
}

void PlayJob::stop()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    if (mIsPlaying) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to PLAY_STOPPED");
        sendPlayFinished(Callback::OK_STOPPED);
    }
}

void PlayJob::joined()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    if (mIsPlaying) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to PLAY_STREAM_JOINED");
        sendPlayFinished(Callback::OK_JOINED);
    }
}

void PlayJob::streamDeleted()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsPlaying) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to PLAY_STREAM_DELETED");
        sendPlayFinished(Callback::OK_DELETED);
    }
}

long PlayJob::getCursor()
{
    return mCursor;
}

void PlayJob::setCursor(long cursor)
{
    mCursor = cursor;
}

base::String PlayJob::getId()
{
    ostringstream os;
    os << "PlayJob: " << this;
    return os.str();
}

bool PlayJob::isVideo()
{
    return mPlayVideo;
}

bool PlayJob::isOk()
{
    return mIsOk;
}

void PlayJob::sendPlayFinished(int cause)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsPlaying) {
        mIsPlaying = false;

        StreamRTPSession &audioSession = mSession.getAudioSession();
        StreamRTPSession &videoSession = mSession.getVideoSession();

        // Determine final cursor position...
        // Modulo arithmetic, assume max one wrap around....
        uint32 tstampDiff = audioSession.getCurrentTimestamp() - mStartAudioTimeStamp;

        long timePlayed = 1000 * tstampDiff / m_mediaObject->getSessionDescription().getAudioPayload().clockRate;

        JLogger::jniLogTrace(env, CLASSNAME, "Purging outgoing queue(s) due to play finished");
        audioSession.purgeOutgoingQueue();
        if (mPlayVideo) {
            videoSession.purgeOutgoingQueue();
        }

        JLogger::jniLogTrace(env, CLASSNAME, "Issuing play finished");
        mSession.postCallback(new Callback(env, mRequestId, Callback::PLAY_COMMAND, cause, mCursor + timePlayed));
    }
}

void PlayJob::sendPlayFailed(const char * msg)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsPlaying) {
        mIsPlaying = false;
        JLogger::jniLogTrace(env, CLASSNAME, "Issuing play failed: [%s]", msg);
        mSession.postCallback(new Callback(env, mRequestId, Callback::PLAY_COMMAND, Callback::FAILED));
    }
}
