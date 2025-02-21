/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <cc++/config.h> // For att __EXPORT ska vara definierad (Pointer.h)
#include <cc++/thread.h>

#include "recordjob.h"
#include "rtppayload.h"
#include "java/mediaobject.h"
#include "jniutil.h"
#include "jlogger.h"
#include "wavinfo.h"
#include "audiomediadata.h"
#include "streamcontentinfo.h"
#include "streamconfiguration.h"
#include "streamrtpsession.h"
#include "mediaobjectwriter.h"
#include "mediabuilder.h"
#include "javamediastream.h"
#include "formathandlerfactory.h"
#include "movaudiochunkcontainer.h"
#include "movaudiochunk.h"
#include "videomediadata.h"
#include "inboundsession.h"
#include "streamutil.h"
#include <base_std.h> 

#include "RTPAudioHandler.h"
#include "RTPVideoHandler.h"

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.RecordJob";

RecordJob::RecordJob(JNIEnv* env, InboundSession & session, jobject eventNotifier, jobject callId,
        std::auto_ptr<RecordingProperties>& properties, std::auto_ptr<java::MediaObject>& mediaObject) :
        mSession(session), mIsRecording(false), mCallId(callId), mProperties(properties),
        mNumberOfReceivedAudioPackets(0), mTotalAmountOfAudioTime(0), mAudioData(), mVideoData(), mCurrentFrameData(),
        mRecordVideo(false), mMediaObject(mediaObject), mBuilder(0)
{
    JLogger::jniLogDebug(env, CLASSNAME, "RecordJob - create at %#x", this);
}

RecordJob::~RecordJob()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~RecordJob - delete at %#x", this);
}

void RecordJob::init()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    mMediaObject->resetJniEnv(env);

    StreamRTPSession& audioSession = mSession.getAudioSession();
    StreamRTPSession& videoSession = mSession.getVideoSession();

    base::String fileExt = mMediaObject->getFileExtension();
    if (fileExt == "") {
        mMediaObject->setFileExtension(mSession.getContentInfo().getFileExtension());
        mMediaObject->setContentType(mSession.getContentInfo().getContentType());
    } else {
        JLogger::jniLogTrace(env, CLASSNAME, "File extension already set in media object: %s", fileExt.c_str());
    }

    mBuilder.reset(FormatHandlerFactory::getBuilder(*mMediaObject));

    mRecordVideo = !mBuilder->isAudioOnlyBuilder();

    java::StreamContentInfo& contentInfo = mSession.getContentInfo();

    contentInfo.getAudioPayload()->setPayloadFormat(audioSession);

    if (mRecordVideo) {
        contentInfo.getVideoPayload()->setPayloadFormat(videoSession);
        uint32 sampleRate(videoSession.getCurrentRTPClockRate());

        JLogger::jniLogTrace(env, CLASSNAME, "Video RTP Clockrate=%d", sampleRate);
    }

    // The record should discard any previous packets
    if (!mRecordVideo && mSession.getRTPAudioHandler()->requireNonRecordingPackets()) {
        JLogger::jniLogTrace(env, CLASSNAME, "Flushing audio packets before recording");
        mSession.flushDataBeforeRecording(false);
    } else {
        JLogger::jniLogTrace(env, CLASSNAME, "Purging audio packets before recording");
        audioSession.purgeIncomingQueue();
    }
    mAudioData.clear();

    if (mRecordVideo) {
        mVideoData.clear();
        JLogger::jniLogTrace(env, CLASSNAME, "Requesting I-frame");
        videoSession.purgeIncomingQueue();
        mSession.getJavaMediaStream().sendPictureFastUpdateRequest(env);
    }

    JLogger::jniLogTrace(env, CLASSNAME, "Waiting for %s data.", (mRecordVideo ? "Video" : "audio"));
    JLogger::jniLogTrace(env, CLASSNAME, "timeout %u", (unsigned int) mProperties->getTimeout());

    if (mSession.getRTPAudioHandler() != 0) {
        mSession.getRTPAudioHandler()->initializeRecording();

        // Init silence detection
        mSession.getRTPAudioHandler()->initializeSilenceDetection(mSession.getContentInfo().getPTime(), 0,
                contentInfo.getAudioPayload()->getCodec(), (unsigned int) mProperties->getTimeout(),
                (unsigned int) mProperties->getMaxSilence(),
                (mRecordVideo) ? 0 : mSession.getConfiguration().getSilenceDetectionMode(),
                mSession.getConfiguration().getThreshold(), mSession.getConfiguration().getInitialSilenceFrames(),
                mSession.getConfiguration().getDetectionFrames(), mSession.getConfiguration().getSignalDeadband(),
                mSession.getConfiguration().getSilenceDeadband(),
                mSession.getConfiguration().getSilenceDetectionDebugLevel());

    }

    if (mSession.getRTPVideoHandler() != 0)
        mSession.getRTPVideoHandler()->initializeRecording(mSession.getConfiguration().getMaxWaitForIFrameTimeout());

    mIsRecording = true;
}

bool RecordJob::storeValidRecording(bool removeSilence)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
	JLogger::jniLogDebug(env, CLASSNAME, "storeValidRecording()");
    if (mSession.getRTPAudioHandler() != 0) {
        mSession.getRTPAudioHandler()->enhanceData(mAudioData);
        mSession.getRTPAudioHandler()->initializeBuilderProperties(mBuilder.get());

        // Trim beginning of audio media according to config
        mSession.getRTPAudioHandler()->trimAudio(mAudioData, mSession.getConfiguration().getAudioSkip(), false);

        // Remove detected silence in end of audio media
        if (removeSilence) {
            mSession.getRTPAudioHandler()->removeSilence(mAudioData);
        }
    }

    if (mSession.getRTPVideoHandler() != 0) {
        mSession.getRTPVideoHandler()->packetizeVideoFrames(mVideoData);
        mSession.getRTPVideoHandler()->initializeBuilderProperties(mBuilder.get());
    }

    // TODO: lessThanMinRecordingDuration does not take removed silence into
    // account.
    if (lessThanMinRecordingDuration()) {
        JLogger::jniLogTrace(env, CLASSNAME, "storeValidRecording() Did not reach minRecordingDuration, cancels the recording");

        StackEventDispatcher::recordFailed(mSession.getEventNotifier(), mCallId,
                StackEventDispatcher::MIN_RECORDING_DURATION,
                "Did not reach minRecordingDuration, cancels the recording", env);
        return false;
    } else {
        JLogger::jniLogTrace(env, CLASSNAME, "storeValidRecording() Received number of packages: %d", mNumberOfReceivedAudioPackets);

        if (mRecordVideo) {
            // Actually, this method works for storing audio only data
            // also (must check currentFrameData for NULL) but the
            // other method is cleaner for audio so it is still used.
            storeAndReleaseVideo();
        } else {
            storeAndReleaseAudio();
        }
    }
	JLogger::jniLogTrace(env, CLASSNAME, "storeValidRecording done()");
    return true;
}

void RecordJob::handlePacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool isVideoPacket)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    /*JLogger::jniLogError(env, CLASSNAME "Handling RTP packet."
     << " SeqNum=" << adu->getSeqNum()
     << " Size=" << adu->getSize()
     << " isVideoPacket=" << isVideoPacket);*/

    if (adu.get() != 0 && (!isVideoPacket || (isVideoPacket && mRecordVideo))) {
        if (!isVideoPacket) {
            handleAudioPacket(adu);
        } else {
            handleVideoPacket(adu);
        }

        if (maxRecordingDurationExceeded()) {
            JLogger::jniLogTrace(env, CLASSNAME, "Reached maxRecordingDuration, record stopped");
            finishRecordingWithEvent(StackEventDispatcher::MAX_RECORDING_DURATION_REACHED, false);
        } else if (maxSilenceDurationExceeded()) {
            JLogger::jniLogTrace(env, CLASSNAME, "Record stopped due to silence detection");
            finishRecordingWithEvent(StackEventDispatcher::MAX_SILENCE_DURATION_REACHED, true);
        }
    }
}

void RecordJob::handleVideoPacket(std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mSession.getRTPVideoHandler() != 0) {
        mSession.getRTPVideoHandler()->recordVideoPacket(adu);
    } else {
        JLogger::jniLogWarn(env, CLASSNAME, "Received video packet but no video handler is available!");
    }
}

void RecordJob::handleAudioPacket(std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mSession.getRTPAudioHandler() != 0) {
        // Save the new packet in the mediaData
        mNumberOfReceivedAudioPackets++;
        mSession.getRTPAudioHandler()->recordAudioPacket(adu, mAudioData);
    } else {
        JLogger::jniLogWarn(env, CLASSNAME, "Received audio packet but no audio handler is available!");
    }
}

void RecordJob::onTimerTick(uint64 timeref)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (!mRecordVideo && mSession.getRTPAudioHandler() != 0) {
        mSession.getRTPAudioHandler()->onTimerTick(timeref);
        if (maxSilenceDurationExceeded()) {
            JLogger::jniLogTrace(env, CLASSNAME, "Record stopped due to silence detection");
            finishRecordingWithEvent(StackEventDispatcher::MAX_SILENCE_DURATION_REACHED, true);
        }
    }

}

void RecordJob::stop()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (isRecording()) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to RECORDING_STOPPED");
        finishRecordingWithEvent(StackEventDispatcher::RECORDING_STOPPED, false);
    }
}

void RecordJob::abandoned()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (isRecording()) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to STREAM_ABANDONED");
        finishRecordingWithEvent(StackEventDispatcher::STREAM_ABANDONED, false);
    }
}

void RecordJob::finishRecordingWithEvent(StackEventDispatcher::RecordFinishedEventCause cause, bool removeSilence)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsRecording) {
        if (!mRecordVideo && mSession.getRTPAudioHandler() != 0) {
            mSession.getRTPAudioHandler()->onStopRecording();
        }
        mIsRecording = false;
		JLogger::jniLogTrace(env, CLASSNAME, "RecordJob::finishRecordingWithEvent call storeValidRecording()");
        bool valid = storeValidRecording(removeSilence);
        JLogger::jniLogTrace(env, CLASSNAME, "RecordJob::finishRecordingWithEvent removeSilence=%d", removeSilence);
        JLogger::jniLogTrace(env, CLASSNAME, "Recording valid: %d", valid);
        if (valid) {
			JLogger::jniLogTrace(env, CLASSNAME, "RecordJob::finishRecordingWithEvent call  StackEventDispatcher::recordFinished()");
            StackEventDispatcher::recordFinished(mSession.getEventNotifier(), mCallId, cause, env);
        }
    }
}

void RecordJob::streamDeleted()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (isRecording()) {
        JLogger::jniLogTrace(env, CLASSNAME, "Cause is set to STREAM_DELETED");
        finishRecordingWithEvent(StackEventDispatcher::STREAM_DELETED, false);
    }
}

void RecordJob::calculateStartOffsets(boost::ptr_list<boost::ptr_list<VideoMediaData> >& videoData,
        boost::ptr_list<AudioMediaData>& audioData, uint32& audioStartTimeOffset, uint32& videoStartTimeOffset)
{

    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (audioData.empty() || videoData.empty()) {
        return;
    }

    StreamRTPSession::SkewMethod method;
    long skew;
    mSession.getSkew(method, skew);

    //
    // First, compute the offsets according to RTCP information.
    //
    if (((method == StreamRTPSession::LOCAL_AND_RTCP) || (method == StreamRTPSession::RTCP))
            && mSession.canSyncSessions()) {

        // First the timestamp in the first audio resp. video-packets
        // must be found
        uint32 firstVideoPacketTimestamp(0);
        boost::ptr_list<boost::ptr_list<VideoMediaData> >::iterator frameIter;
        for (frameIter = videoData.begin(); frameIter != videoData.end(); ++frameIter) {
            boost::ptr_list<VideoMediaData>& c = *frameIter;
            if (!c.empty()) {
                firstVideoPacketTimestamp = c.front().getRTPTimestamp();
                break;
            }
        }

        JLogger::jniLogTrace(env, CLASSNAME, "RTPTimestamp in first audio packet: %d",
                audioData.front().getRTPTimestamp());
        JLogger::jniLogTrace(env, CLASSNAME, "RTPTimestamp in first video packet: %d", firstVideoPacketTimestamp);

        // First the RTP timestamps are mapped to the common
        // reference timeline.
        StreamRTPSession& audioSession = mSession.getAudioSession();
        StreamRTPSession& videoSession = mSession.getVideoSession();
        uint32 audioRefTimestamp(audioSession.toWallClockTime(audioData.front().getRTPTimestamp()));
        uint32 videoRefTimestamp(videoSession.toWallClockTime(firstVideoPacketTimestamp));

        JLogger::jniLogTrace(env, CLASSNAME, "audioRefTimestamp=%d", audioRefTimestamp);
        JLogger::jniLogTrace(env, CLASSNAME, "videoRefTimestamp=%d", videoRefTimestamp);

        if (audioRefTimestamp > videoRefTimestamp) {
            audioStartTimeOffset = audioRefTimestamp - videoRefTimestamp;
            JLogger::jniLogTrace(env, CLASSNAME, "According to RTCP, video should be sent %d ms ahead of audio",
                    audioStartTimeOffset);
        } else {
            videoStartTimeOffset = videoRefTimestamp - audioRefTimestamp;
            JLogger::jniLogTrace(env, CLASSNAME, "According to RTCP, audio should be sent %d ms ahead of video",
                    videoStartTimeOffset);
        }
    } else if (mSession.getRTPVideoHandler()->haveIgnoredFirstVideoPacket()) {
        // RTCP synchronization is not used. Compute an offset according to
        // the amount of video-data that has been skipped (while waiting
        // for the first I-frame).

        // First the timestamp in the first video-packet must be found
        uint32 firstVideoPacketTimestamp(0);
        boost::ptr_list<boost::ptr_list<VideoMediaData> >::iterator frameIter;
        for (frameIter = videoData.begin(); frameIter != videoData.end(); ++frameIter) {
            boost::ptr_list<VideoMediaData>& c = *frameIter;
            if (!c.empty()) {
                firstVideoPacketTimestamp = c.front().getRTPTimestamp();
                break;
            }
        }
        if (firstVideoPacketTimestamp > mSession.getRTPVideoHandler()->getFirstIgnoredVideoPacketTimestamp()) {
            uint32 rtpTimeDiff(
                    firstVideoPacketTimestamp - mSession.getRTPVideoHandler()->getFirstIgnoredVideoPacketTimestamp());
            StreamRTPSession& videoSession = mSession.getVideoSession();
            uint32 timeDiffMs = rtpTimeDiff / (videoSession.getCurrentRTPClockRateMs());
            JLogger::jniLogTrace(env, CLASSNAME,
                    "According to amount of ignored video, audio should be sent %d ms ahead of video", timeDiffMs);
            videoStartTimeOffset = timeDiffMs;
        }
    }

    //
    // Now, look at the skew offset as well
    //
    if ((method == StreamRTPSession::LOCAL_AND_RTCP) || (method == StreamRTPSession::LOCAL)) {
        if (skew < 0) {
            JLogger::jniLogTrace(env, CLASSNAME, "According to skew, video should be sent %d ms ahead of audio", skew);

            if (videoStartTimeOffset > 0) {
                // According to RTCP, the audio should be sent ahead
                // of the video but according to skew the video should be
                // sent ahead of the audio.
                if (static_cast<int32>(videoStartTimeOffset) > skew) {
                    videoStartTimeOffset += skew; // skew negative
                } else {
                    audioStartTimeOffset = abs(skew) - videoStartTimeOffset;
                    videoStartTimeOffset = 0;
                }
            } else {
                audioStartTimeOffset -= skew; // skew is negative
            }

        } else if (skew > 0) {
            JLogger::jniLogTrace(env, CLASSNAME, "According to skew, audio should be sent %d ms ahead of video", skew);

            if (audioStartTimeOffset > 0) {
                // According to RTCP, the video should be sent ahead
                // of the audio but according to skew the audio should be
                // sent ahead of the video.
                if (static_cast<int32>(audioStartTimeOffset) > skew) {
                    audioStartTimeOffset -= skew;
                } else {
                    videoStartTimeOffset = skew - audioStartTimeOffset;
                    audioStartTimeOffset = 0;
                }
            } else {
                videoStartTimeOffset += skew;
            }
        }
    }
}

void RecordJob::storeAndReleaseVideo()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    java::StreamContentInfo &contentInfo = mSession.getContentInfo();

    if (!mCurrentFrameData.empty()) {
        std::auto_ptr<boost::ptr_list<VideoMediaData> > temp(new boost::ptr_list<VideoMediaData>());
        temp->transfer(temp->end(), mCurrentFrameData);
        mVideoData.push_back(temp.release());
    }

    // First, calculate the playout delay between the audio and
    // video data.
    uint32 audioStartTimeOffset(0);
    uint32 videoStartTimeOffset(0);
    calculateStartOffsets(mVideoData, mAudioData, audioStartTimeOffset, videoStartTimeOffset);

    JLogger::jniLogTrace(env, CLASSNAME, "audioStartTimeOffset: %d", audioStartTimeOffset);
    JLogger::jniLogTrace(env, CLASSNAME, "videoStartTimeOffset: %d", videoStartTimeOffset);

    // Now start to store audio and video data
    auto_ptr<MediaObjectWriter> writer(FormatHandlerFactory::getWriter(*mMediaObject));

    try {
        mBuilder->setAudioCodec(contentInfo.getAudioPayload()->getCodec());

        mBuilder->setVideoCodec(contentInfo.getVideoPayload()->getCodec());
    } catch (exception &e) {
        JLogger::jniLogError(env, CLASSNAME, "Exception caught in storeAndReleaseAudio: %s", e.what());
    } catch (...) {
        JLogger::jniLogError(env, CLASSNAME, "Unknown exception caught in storeAndReleaseAudio");
    }

    MediaInfo& info = mBuilder->getInfo();

    info.setAudioSampleRate(mSession.getAudioSession().getCurrentRTPClockRate());
    info.setVideoSampleRate(mSession.getVideoSession().getCurrentRTPClockRate());

    // Set video data
    // Now, the frametime of all packets has to be calculated.
    // The current frametime value contains the RTP timestamp for
    // the packet.
    {
        boost::ptr_list<boost::ptr_list<VideoMediaData> >::iterator frameIter;
        unsigned frameTime(0);
        uint32 rtpTimeDiff(0);

        // The frametime only needs to be stored in the first packet
        // of a frame. It is calculated using the difference in
        // RTP timestamp between the FIRST packet in the next frame and
        // the RTP timestamp for the FIRST packet in the current frame.
        // All packets in a frame should have the same RTP timestamp.
        VideoMediaData* firstPacketPreviousFrame = 0;
        VideoMediaData* firstPacketLastFrame = 0;
        StreamRTPSession& videoSession = mSession.getVideoSession();

        for (frameIter = mVideoData.begin(); frameIter != mVideoData.end(); ++frameIter) {
            boost::ptr_list<VideoMediaData>& c = *frameIter;
            if (!c.empty()) {
                boost::ptr_list<VideoMediaData>::iterator movPktIter;
                movPktIter = c.begin();
                firstPacketLastFrame = &(*movPktIter);
                if (firstPacketPreviousFrame != 0) {
                    rtpTimeDiff = abs(
                            (long) (firstPacketLastFrame->getRTPTimestamp()
                                    - firstPacketPreviousFrame->getRTPTimestamp()));
                    frameTime = rtpTimeDiff * 1000 / videoSession.getCurrentRTPClockRate();
                    firstPacketPreviousFrame->setFrameTime(frameTime);
                    /*JLogger::jniLogTrace(env, CLASSNAME, "RTP Time diff = %d, FrameTime set to %d", rtpTimeDiff, frameTime);*/
                }
                firstPacketPreviousFrame = firstPacketLastFrame;
            }
        }
        if (firstPacketLastFrame != NULL) {
            // XXX The frametime of the last packet cannot be calculated
            // when there is no next packet. It is not interesting either
            // when playing the media. Can this cause problems?
            firstPacketLastFrame->setFrameTime(0);
        }
    }
    // Convert to medialibrary format
    std::auto_ptr<boost::ptr_list<boost::ptr_list<MovRtpPacket> > > videoFrames(
            new boost::ptr_list<boost::ptr_list<MovRtpPacket> >);
    boost::ptr_list<boost::ptr_list<VideoMediaData> >::iterator frameIter;

    for (frameIter = mVideoData.begin(); frameIter != mVideoData.end(); ++frameIter) {
        boost::ptr_list<VideoMediaData>& pktContainer = *frameIter;
        std::auto_ptr<boost::ptr_list<MovRtpPacket> > movRtpPacketContainer(new boost::ptr_list<MovRtpPacket>());

        boost::ptr_list<VideoMediaData>::iterator movPktIter;
        for (movPktIter = pktContainer.begin(); movPktIter != pktContainer.end(); ++movPktIter) {
            movRtpPacketContainer->push_back(movPktIter->releasePacket());
        }

        videoFrames->push_back(movRtpPacketContainer.release());
    }
    mBuilder->setVideoFrames(*videoFrames);

    // Now, set audio/video offsets
    JLogger::jniLogTrace(env, CLASSNAME, "setAudioStartTimeOffset=%d", audioStartTimeOffset);
    JLogger::jniLogTrace(env, CLASSNAME, "setVideoStartTimeOffset=%d", videoStartTimeOffset);

    mBuilder->setVideoStartTimeOffset(videoStartTimeOffset);
    mBuilder->setAudioStartTimeOffset(audioStartTimeOffset);

    // Set audio data
    MovAudioChunkContainer audioChunks(mSession.getRTPAudioHandler()->getAudioPacketSize());
    boost::ptr_list<AudioMediaData>::iterator iter;
    for (iter = mAudioData.begin(); iter != mAudioData.end(); ++iter) {
        audioChunks.push_back(iter->releaseAudioChunk());
    }
    mBuilder->setAudioChunks(audioChunks);

    // Store audio and video data
    writer->open();
    mBuilder->store(*(writer.get()));
    mMediaObject->setLength(mBuilder->getDuration());
    writer->close();

    JLogger::jniLogTrace(env, CLASSNAME, "Finished writing video data to MediaObject");
}

void RecordJob::storeAndReleaseAudio()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
	 JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio()");

    java::StreamContentInfo &contentInfo = mSession.getContentInfo();

    auto_ptr<MediaObjectWriter> writer(FormatHandlerFactory::getWriter(*mMediaObject));

    // Set audio data
    try {
		JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() mBuilder->setAudioCodec(contentInfo.getAudioPayload()->getCodec() %s ",contentInfo.getAudioPayload()->getCodec().c_str());
        mBuilder->setAudioCodec(contentInfo.getAudioPayload()->getCodec());
    } catch (exception &e) {
        JLogger::jniLogError(env, CLASSNAME, "Exception caught in storeAndReleaseAudio: %s", e.what());
    } catch (...) {
        JLogger::jniLogError(env, CLASSNAME, "Unknown exception caught in storeAndReleaseAudio");
    }
	JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() audioChunks(mSession.getRTPAudioHandler()->getAudioPacketSize() %u",mSession.getRTPAudioHandler()->getAudioPacketSize());
    MovAudioChunkContainer audioChunks(mSession.getRTPAudioHandler()->getAudioPacketSize());
    boost::ptr_list<AudioMediaData>::iterator iter;
	int count=0;
    for (iter = mAudioData.begin(); iter != mAudioData.end(); ++iter) {
        audioChunks.push_back(iter->releaseAudioChunk());
		count++;
    }
	JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() audioChunks added to list %d",count);
	JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() mBuilder->setAudioChunks(audioChunks) ");
    mBuilder->setAudioChunks(audioChunks);
    MediaInfo& info = mBuilder->getInfo();
    info.setAudioSampleRate(mSession.getAudioSession().getCurrentRTPClockRate());
	JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() current RTP clock Rate %d",mSession.getAudioSession().getCurrentRTPClockRate());
    JLogger::jniLogTrace(env, CLASSNAME, "storeAndReleaseAudio() writer.open()");
	writer->open();
	JLogger::jniLogTrace(env, CLASSNAME, "mBuilder->store(*(writer.get()))");
    mBuilder->store(*(writer.get()));
	JLogger::jniLogTrace(env, CLASSNAME, "mMediaObject->setLength(mBuilder->getDuration());");
    mMediaObject->setLength(mBuilder->getDuration());
    writer->close();

    JLogger::jniLogTrace(env, CLASSNAME, "Finished writing audio data to MediaObject");
}

base::String RecordJob::getId()
{
    ostringstream os;
    os << "RecordJob: " << this;
    return os.str();
}

bool RecordJob::maxRecordingDurationExceeded()
{
    if (mSession.getRTPAudioHandler() != 0) {
        return mSession.getRTPAudioHandler()->getRecordedTime() >= (unsigned int) mProperties->getMaxRecordingDuration();
    } else {
        return false;
    }
}

bool RecordJob::maxSilenceDurationExceeded()
{
    if (mSession.getRTPAudioHandler() != 0) {
        return mSession.getRTPAudioHandler()->getSilenceDetected();
    } else {
        return false;
    }
}

bool RecordJob::lessThanMinRecordingDuration()
{
    return mSession.getRTPAudioHandler()->getRecordedTime() < (unsigned int) mProperties->getMinRecordingDuration();
}

jobject RecordJob::getCallId()
{
    return mCallId;
}

bool RecordJob::isRecording()
{
    return mIsRecording;
}

void RecordJob::sendRecordFailed(StackEventDispatcher::RecordFailedEventCause cause, const char * msg, JNIEnv* env)
{
    StackEventDispatcher::recordFailed(mSession.getEventNotifier(), mCallId, cause, msg, env);
}
