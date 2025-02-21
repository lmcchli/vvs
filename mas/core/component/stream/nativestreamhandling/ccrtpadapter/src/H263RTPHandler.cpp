#include "H263RTPHandler.h"
#include "jlogger.h"
#include "jniutil.h"
#include "videomediadata.h"
#include "mediabuilder.h"
#include "streamutil.h"
#include <int.h>
#include <byteutilities.h>

static const char* CLASSNAME = "masjni.ccrtpadapter.H263RTPHandler";

H263RTPHandler::H263RTPHandler(JNIEnv* env, StreamRTPSession& videoSession, java::StreamContentInfo& contentInfo,
        java::RTPPayload& videoPayload) :
        RTPVideoHandler(videoSession, contentInfo, videoPayload)
{
    JLogger::jniLogDebug(env, CLASSNAME, "H263RTPHandler - create at %#x", this);
}

H263RTPHandler::~H263RTPHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~H263RTPHandler - delete at %#x", this);
}

void H263RTPHandler::initializeRecording(long maxWaitForIFrameTimeout)
{
    RTPVideoHandler::initializeRecording(maxWaitForIFrameTimeout);

    // recording starts with first I-frame.
    mFoundStartPacket = false;

    // If no I-frame arrives in time, recording starts after next marked packet.
    mShoulWaitForMarkedPacket = false;

    if (!mVideoBuffer.empty())
        mVideoBuffer.clear();

    StreamUtil::getTimeOfDay(mStartWaitForDataTimestamp);

    mMaxWaitForIFrameTimeout = maxWaitForIFrameTimeout;

    mHaveIgnoredFirstVideoPacket = false;
}

bool H263RTPHandler::recordVideoPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    timeval now;

    bool addPacket(true);

    uint32 extendedSeq;
    bool restart;
    if (getExtendedSequenceNumber(adu, extendedSeq, restart)) {
        if (restart) {
            mFoundStartPacket = false;
            mVideoBuffer.clear();
        }

        bool duplicate;
        boost::ptr_list<VideoMediaData>::iterator pos = getInsertPosition(extendedSeq, mVideoBuffer, duplicate);

        if (!duplicate) {
            //JLogger::jniLogTrace(NULL, CLASSNAME, "Adding packet to buffer SeqNum=%d", extendedSeq);

            std::auto_ptr<VideoMediaData> vmd(new VideoMediaData(env, adu, extendedSeq));

            const ost::AppDataUnit& adu = vmd->getAdu();

            if (mFoundStartPacket || isIFrame((const char*) adu.getData())) {
                if (!mFoundStartPacket) {
                    JLogger::jniLogTrace(env, CLASSNAME, "First I-frame arrived. SeqNum=%d", extendedSeq);

                    mFoundStartPacket = true;

                    if (!mVideoBuffer.empty()) {
                        if (!mHaveIgnoredFirstVideoPacket) {
                            mHaveIgnoredFirstVideoPacket = true;
                            mFirstIgnoredVideoPacketTimestamp = mVideoBuffer.front().getRTPTimestamp();
                        }

                        // remove all earlier packets
                        mVideoBuffer.erase(mVideoBuffer.begin(), pos);
                    }
                }
            } else if (mShoulWaitForMarkedPacket && adu.isMarked()) {
                JLogger::jniLogTrace(env, CLASSNAME, "First marked packet after timeout arrived. SeqNum=%d",
                        extendedSeq);

                mShoulWaitForMarkedPacket = false;
                mFoundStartPacket = true;

                if (!mVideoBuffer.empty()) {
                    if (!mHaveIgnoredFirstVideoPacket) {
                        mHaveIgnoredFirstVideoPacket = true;
                        mFirstIgnoredVideoPacketTimestamp = mVideoBuffer.front().getRTPTimestamp();
                    }

                    // remove all earlier packets
                    mVideoBuffer.erase(mVideoBuffer.begin(), pos);
                }

                // dont add this packet, packet after this is the start packet.
                addPacket = false;
            } else if (StreamUtil::timedOut(mStartWaitForDataTimestamp, now, mMaxWaitForIFrameTimeout)) {
                JLogger::jniLogTrace(env, CLASSNAME,
                        "Timedout waiting for first I-frame. Will start record after next marked packet if no I-frame arrives. SeqNum=%d",
                        extendedSeq);

                mShoulWaitForMarkedPacket = true;
            } else {
                JLogger::jniLogTrace(env, CLASSNAME, "Packet received before first I-frame, ignored. SeqNum=%d",
                        extendedSeq);
            }

            if (addPacket)
                mVideoBuffer.insert(pos, vmd.release());

            return true;
        }
    }

    return false;
}

void H263RTPHandler::packetizeVideoFrames(boost::ptr_list<boost::ptr_list<VideoMediaData> >& videoData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "packetizeVideoFrames");

    // If we haven't fount a packet to start recording with, erase buffered packets
    if (!mFoundStartPacket)
        mVideoBuffer.clear();

    while (!mVideoBuffer.empty()) {
        //JLogger::jniLogTrace(NULL, CLASSNAME, "First packet in frame: %d", mVideoBuffer.front().getExtendedSeqNum());

        boost::ptr_list<VideoMediaData> mCurrentFrameData;
        boost::ptr_list<VideoMediaData>::iterator packet = mVideoBuffer.begin();
        bool foundFrame = false;
        while (!foundFrame && packet != mVideoBuffer.end()) {
            if (packet->getAdu().isMarked()) {
                videoData.push_back(new boost::ptr_list<VideoMediaData>());
                videoData.back().transfer(videoData.back().end(), mVideoBuffer.begin(), ++packet, mVideoBuffer);
                //JLogger::jniLogTrace(env, CLASSNAME, "added %d packets to frame", videoData.back().size());

                foundFrame = true;
            } else {
                //JLogger::jniLogTrace(env, CLASSNAME, "Packet not marked");
                ++packet;
            }
        }

        if (!foundFrame) {
            videoData.push_back(new boost::ptr_list<VideoMediaData>());
            videoData.back().transfer(videoData.back().end(), mVideoBuffer);
        }
    }
}

boost::ptr_list<VideoMediaData>::iterator H263RTPHandler::getInsertPosition(uint32 extendedSeq,
        boost::ptr_list<VideoMediaData>& videoData, bool& duplicate)
{
    duplicate = false;

    if (videoData.empty() || extendedSeq > videoData.back().getExtendedSeqNum()) {
        return videoData.end();
    }

    bool found = false;
    bool done = false;

    boost::ptr_list<VideoMediaData>::iterator p = videoData.end();
    --p;

    do {
        if (extendedSeq > p->getExtendedSeqNum()) {
            found = true;
        } else if (extendedSeq < p->getExtendedSeqNum()) {
            if (p == videoData.begin()) {
                done = true;
            } else {
                --p;
            }
        } else {
            duplicate = true;
        }
    } while (!done && !found && !duplicate);

    if (found)
        ++p;

    return p;
}

bool H263RTPHandler::isIFrame(const char* payload)
{
    // The "I"-bit indicates that the frame is intra-codes and
    // thus is an I-frame (See RFC 2190 for details).
    // In mode A, the I-bit is number 12 in the first word
    // In mode B and C, the I-bit is number 1 in the third word
    bool isLittleEndian(Platform::isLittleEndian());
    uint16_t word(0);

    ByteUtilities::readW((const char*) payload, word, isLittleEndian);
    bool isModeA((word & 0x8000) == 0);
    bool isIntraCoded(false);
    if (isModeA) {
        isIntraCoded = (word & 0x10) == 0;
    } else {
        // First read the third word
        ByteUtilities::readW((const char*) (payload + 4), word, isLittleEndian);
        isIntraCoded = (word & 0x8000) == 0;
    }
    return isIntraCoded;
}
