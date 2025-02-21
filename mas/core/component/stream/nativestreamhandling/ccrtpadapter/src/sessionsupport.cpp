/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h>
#include <base_include.h>
#include <fcntl.h>
#include <cc++/process.h>

#include "sessionsupport.h"
#include "inboundsession.h"
#include "jniutil.h"
#include "rtppayload.h"
#include "playjob.h"
#include "recordjob.h"
#include "streamconfiguration.h"
#include "jlogger.h"
#include "streamcontentinfo.h"
#include "mediaenvelope.h"
#include "streamrtpsession.h"
#include "javamediastream.h"
#include "Processor.h"

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.SessionSupport";

SessionSupport::SessionSupport(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream) :
        mJoinLock(1), mEventNotifier(0), mVideoPort(-1), mAudioPort(-1), mJavaMediaStream(javaMediaStream.release()),
        mConfig(java::StreamConfiguration::instance()), mDeltaTimeMs(mConfig.getSendPacketsAhead()), mAppl(0),
        mAudioSession(0), mVideoSession(0), mState(0), mHandleDtmf(true)
{

    mTimeout.tv_sec = mConfig.getPacketPendTimeout() / 1000000;
    mTimeout.tv_usec = mConfig.getPacketPendTimeout() % 1000000;

    // Make a copy of the current configuration that shall be used
    // during this session.

    mSkewMethod = intToSkewMethod(mConfig.getSkewMethod());
    mSkew = mConfig.getSkew();

    mCallSessionId = mJavaMediaStream->getCallSessionId(env);
    JLogger::jniLogDebug(env, CLASSNAME, "SessionSupport - create at %#x", this);
}

// TODO: improve robustness!
// When running out of file descriptors the local one or both RTP sessions
// will be null. This will eventually make MAS crash!
void SessionSupport::createLocalSessions(JNIEnv* env, int audioPort, int videoPort, base::String& cname)
{
    mAudioPort = audioPort;
    mVideoPort = videoPort;
    try {
        if (cname == "") {
            JLogger::jniLogTrace(env, CLASSNAME, "No CNAME specified for stream, creating a default name");
            ostringstream cnameOs;
            createCNAME(cnameOs);
            cname = cnameOs.str();
        }

        // Yes, a reset is needed. If the create fails later on and a
        // new call to create is issued, all old objects must be deleted.
        mAppl.reset(new RTPApplication(cname.c_str()));

        // mAppl.setSDESItem(); XXX ska vi s�tta SDES items? (NAME, EMAIL,
        // PHONE, TOOL etc)

        JLogger::jniLogTrace(env, CLASSNAME, "Created application with CNAME %s", cname.c_str());

        base::String localHostName;
        mConfig.getLocalHostName(localHostName);
        InetHostAddress localHost(localHostName.c_str());
        StreamRTPSession::MODE mode = isOutbound() ? StreamRTPSession::OUTBOUND : StreamRTPSession::INBOUND;

        // Setup audio session
        // When adding to the pool, keep in mind that it is the pool
        // that has the responsibility to delete the session instance.
        JLogger::jniLogTrace(env, CLASSNAME, "Creates audiosession and adds it to session pool");
        mAudioSession.reset(
                new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                        audioPort, audioPort + 1, *mAppl));

        getAudioSession().setExpireTimeout(mConfig.getExpireTimeout());
        getAudioSession().setSendersControlFraction(mConfig.getSendersControlFraction());

        //XXX mAudioSession->setSessionBandwidth

        // Setup video session
        if (videoPort != -1) {
            JLogger::jniLogTrace(env, CLASSNAME, "Creating videosession on host %s using port=%d",
                    localHostName.c_str(), videoPort);
            mVideoSession.reset(
                    new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                            videoPort, videoPort + 1, *mAppl));

            getVideoSession().setExpireTimeout(mConfig.getExpireTimeout());
            getVideoSession().setSendersControlFraction(mConfig.getSendersControlFraction());
        }
    } catch (ost::Socket* error) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        ostringstream message("");
        if (mVideoSession.get() != NULL) {
            message << "SessionSupport::createLocalSessions: Failed to add videosession destination: ";
            buildErrorMessage(message, error, NULL, videoPort, videoPort + 1);
        } else {
            message << "SessionSupport::createLocalSessions: Failed to add audiosession destination: ";
            buildErrorMessage(message, error, NULL, audioPort, audioPort + 1);
        }

        JLogger::jniLogError(env, CLASSNAME, "%s", message.str().c_str());
        throw;
    } catch (ost::SockException const &error) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        ostringstream message("");
        if (mVideoSession.get() != NULL) {
            message << "SessionSupport::createLocalSessions: Failed to add videosession destination: ";
            buildErrorMessage(message, error, NULL, videoPort, videoPort + 1);
        } else {
            message << "SessionSupport::createLocalSessions: Failed to add audiosession destination: ";
            buildErrorMessage(message, error, NULL, audioPort, audioPort + 1);
        }

        JLogger::jniLogError(env, CLASSNAME, "%s", message.str().c_str());
        throw;
    } catch (...) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        JLogger::jniLogError(env, CLASSNAME, "createLocalSessions() v2: audioPort = %d, videoPort = %d, unknown error",
                audioPort, videoPort);
        throw;
    }
}

// TODO: improve robustness!
// When running out of file descriptors the local one or both RTP sessions
// will be null. This will eventually make MAS crash!
void SessionSupport::createLocalSessions(JNIEnv* env, int audioPort, int videoPort, base::String& cname,
        uint32 audio_ssrc, uint32 video_ssrc, StreamRTPSession* audio_stream, StreamRTPSession* video_stream)
{
    mAudioPort = audioPort;
    mVideoPort = videoPort;
    try {
        if (cname == "") {
            JLogger::jniLogTrace(env, CLASSNAME, "No CNAME specified for stream, creating a default name");
            ostringstream cnameOs;
            createCNAME(cnameOs);
            cname = cnameOs.str();
        }

        // Yes, a reset is needed. If the create fails later on and a
        // new call to create is issued, all old objects must be deleted.
        mAppl.reset(new RTPApplication(cname.c_str()));

        // mAppl.setSDESItem(); XXX ska vi s�tta SDES items? (NAME, EMAIL,
        // PHONE, TOOL etc)
        JLogger::jniLogTrace(env, CLASSNAME, "Created application with CNAME %s", cname.c_str());

        base::String localHostName;
        mConfig.getLocalHostName(localHostName);
        InetHostAddress localHost(localHostName.c_str());
        StreamRTPSession::MODE mode = isOutbound() ? StreamRTPSession::OUTBOUND : StreamRTPSession::INBOUND;

        // Setup audio session
        // When adding to the pool, keep in mind that it is the pool
        // that has the responsibility to delete the session instance.
        JLogger::jniLogTrace(env, CLASSNAME, "Creates audiosession and adds it to session pool");

        if (isOutbound()) {
            mAudioSession.reset(
                    new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                            audioPort, audioPort + 1, *mAppl, audio_ssrc, audio_stream));
        } else {
            mAudioSession.reset(
                    new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                            audioPort, audioPort + 1, *mAppl));
        }

        getAudioSession().setExpireTimeout(mConfig.getExpireTimeout());
        getAudioSession().setSendersControlFraction(mConfig.getSendersControlFraction());

        // Setup video session
        if (videoPort != -1) {
            JLogger::jniLogTrace(env, CLASSNAME, "Creating videosession on host %s using port=%d",
                    localHostName.c_str(), videoPort);
            if (isOutbound()) {
                mVideoSession.reset(
                        new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                                videoPort, videoPort + 1, *mAppl, video_ssrc, video_stream));
            } else {
                mVideoSession.reset(
                        new StreamRTPSession(env, *this, mode, *mContentInfo.get(), mConfig, mEventNotifier, localHost,
                                videoPort, videoPort + 1, *mAppl));
            }

            getVideoSession().setExpireTimeout(mConfig.getExpireTimeout());
            getVideoSession().setSendersControlFraction(mConfig.getSendersControlFraction());
        }
    } catch (ost::Socket* error) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        ostringstream message("");
        if (mVideoSession.get() != NULL) {
            message << "SessionSupport::createLocalSessions: Failed to add videosession destination: ";
            buildErrorMessage(message, error, NULL, videoPort, videoPort + 1);
        } else {
            message << "SessionSupport::createLocalSessions: Failed to add audiosession destination: ";
            buildErrorMessage(message, error, NULL, audioPort, audioPort + 1);
        }

        JLogger::jniLogError(env, CLASSNAME, "%s", message.str().c_str());
        throw;
    } catch (ost::SockException const &error) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        ostringstream message("");
        if (mVideoSession.get() != NULL) {
            message << "SessionSupport::createLocalSessions: Failed to add videosession destination: ";
            buildErrorMessage(message, error, NULL, videoPort, videoPort + 1);
        } else {
            message << "SessionSupport::createLocalSessions: Failed to add audiosession destination: ";
            buildErrorMessage(message, error, NULL, audioPort, audioPort + 1);
        }

        JLogger::jniLogError(env, CLASSNAME, "%s", message.str().c_str());
        throw;
    } catch (...) {
        mAudioSession.reset(NULL);
        mVideoSession.reset(NULL);
        mAppl.reset(NULL);

        JLogger::jniLogError(env, CLASSNAME, "createLocalSessions() v2: audioPort = %d, videoPort = %d, unknown error",
                audioPort, videoPort);
        throw;
    }
}

void SessionSupport::shutdownAndDelete(JNIEnv* env, int requestId)
{
    std::auto_ptr<Command> cmd(new UnRegisterCommand(*this, requestId));
    ProcessorGroup::instance().dispatchCommand(env, cmd);
}

SessionSupport::~SessionSupport()
{
    // All dynamically allocated references are handles by auto-pointers.
    joinUnlock();

    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~SessionSupport - delete at %#x", this);
}

StreamRTPSession::SkewMethod SessionSupport::intToSkewMethod(int method)
{
    switch (method)
    {
    case 0:
        return StreamRTPSession::LOCAL;
    case 1:
        return StreamRTPSession::RTCP;
    case 2:
        return StreamRTPSession::LOCAL_AND_RTCP;

        // Should never happen
    default:
        return StreamRTPSession::LOCAL;
    }
}

//TODO: Ensure skews usage is thread-safe ( if it's ever used )
void SessionSupport::setSkew(int method, long skew)
{
    mSkewMethod = intToSkewMethod(method);
    mSkew = skew;
}

void SessionSupport::init(JNIEnv* env)
{
    try {
        java::StreamConfiguration::instance();
    } catch (...) {
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION,
                "SessionSupport::init: Unknown exception while initiating the thread pool.", env);
        // This is a serious error, if initiation of the thread pool failes,
        // no work can be done.
        throw;
    }
}

void SessionSupport::shutdown()
{
    try {
        ProcessorGroup::shutdown();
    } catch (...) {
        // Ignore this error. This method is called when the system
        // is shutting down.
    }
}
//TODO: Implement. Only called from callmanager for
//statistics purposes. Keep in min that this is called
//from a Java context

uint32 SessionSupport::getCumulativePacketLost()
{
    /*    uint32 lost(getAudioSession().getCumulativePacketLost());
     if (hasVideo()) {
     lost += getVideoSession().getCumulativePacketLost();
     }
     JLogger::jniLogTrace(NULL, CLASSNAME, "Lost packets: %d", lost);
     return lost;
     */
    return 0;
}

//TODO: Implement this method. Keep in min that it is called from
//Java via CCRTPAdapter, however it's never used currently 20060510

uint8 SessionSupport::getFractionLost()
{
    /*uint32 observed(getAudioSession().getObservedPacketCount());
     uint32 expected(getAudioSession().getExpectedPacketCount());

     if (hasVideo()) {
     observed += getVideoSession().getObservedPacketCount();
     expected += getVideoSession().getExpectedPacketCount();
     }
     uint32 fractionLost(0);
     if ((expected != 0) && (observed <= expected)) {
     fractionLost = ((expected-observed) / expected) * 256;
     }
     JLogger::jniLogError(NULL, CLASSNAME, "FractionLost=%d", fractionLost);
     return fractionLost;
     */
    return 0;
}

long SessionSupport::stop(JNIEnv* env, jobject callId)
{
    base::String msg("This operation is not implemented.");
    JNIUtil::throwStackException(StackException::UNSUPPORTED_OPERATION, msg.c_str(), env);
    return 0;
}

void SessionSupport::buildErrorMessage(ostringstream& oss, ost::Socket* error, const char* host, int rtpPort,
        int rtcpPort)
{
    if (error != NULL) {
        oss << error->getErrorString() << ", Error number: " << error->getErrorNumber() << ", System error: "
                << error->getSystemErrorString();
    }
    if (host != NULL) {
        oss << ", Host=" << host;
    }
    if (error != NULL) {
        oss << ", System error number: " << error->getSystemError();
    }
    oss << ", RTPPort=" << rtpPort << ", RTCPPort=" << rtcpPort;
}

void SessionSupport::buildErrorMessage(ostringstream& oss, ost::SockException error, const char* host, int rtpPort,
        int rtcpPort)
{
    oss << error.getString() << ", Error number: " << error.getSocketError() << ", System error: "
            << error.getSystemErrorString();
    oss << ", System error number: " << error.getSystemError();

    if (host != NULL) {
        oss << ", Host=" << host;
    }

    oss << ", RTPPort=" << rtpPort << ", RTCPPort=" << rtcpPort;
}

void SessionSupport::createCNAME(ostringstream& oss)
{
    base::String username;
    findUserName(username);
    InetHostAddress iha;
    const char *p = iha.getHostname();
    // Returned hostname can be NULL
    base::String hostName;
    if (p != NULL) {
        hostName = p;
    }
    oss << username << "@" << hostName;
}

StreamRTPSession& SessionSupport::getAudioSession()
{
    return *mAudioSession;
}

StreamRTPSession& SessionSupport::getVideoSession()
{
    return *mVideoSession;
}

#ifndef WIN32
void SessionSupport::findUserName(base::String &username)
{
    // LOGNAME environment var has two advantages:
    // 1) avoids problems of getlogin(3) and cuserid(3)
    // 2) unlike getpwuid, takes into account user
    //    customization of the environment.
    // Try both LOGNAME and USER env. var.
    const char *user = Process::getEnv("LOGNAME");
    if ((user == 0) || !strcmp(user, ""))
        user = Process::getEnv("USER");
    if (user)
        username = user;
    else
        username = "";
}

#else

void SessionSupport::findUserName(base::String &username) {
    unsigned long len = 0;
    if ( GetUserName(NULL,&len) && (len > 0) ) {
        char *n = new char[len];
        GetUserName(n,&len);
        username = n;
        delete [] n;
        n = NULL;
    } else {
        username = "";
    }
}
#endif // #ifndef WIN32

void SessionSupport::getSkew(StreamRTPSession::SkewMethod& method, long& skew)
{
    method = mSkewMethod;
    skew = mSkew;
}

bool SessionSupport::hasVideo()
{
    return mContentInfo->isVideo();
}

java::StreamConfiguration& SessionSupport::getConfiguration()
{
    return mConfig;
}

java::StreamContentInfo& SessionSupport::getContentInfo()
{
    return *mContentInfo;
}

void SessionSupport::setContentInfo(std::auto_ptr<java::StreamContentInfo> contentInfo)
{
    mContentInfo = contentInfo;
}

void SessionSupport::setEventNotifier(jobject eventNotifier)
{
    mEventNotifier = eventNotifier;
}

jobject SessionSupport::getEventNotifier()
{
    return mEventNotifier;
}

JavaMediaStream& SessionSupport::getJavaMediaStream()
{
    return *mJavaMediaStream;
}

RTPApplication& SessionSupport::getRTPApplication()
{
    return *mAppl;
}

std::vector<SOCKET> SessionSupport::getAllSockets()
{
    std::vector<SOCKET> result(0);
    result.push_back(mAudioSession->getControlRecvSocket());
    result.push_back(mAudioSession->getDataRecvSocket());
    if (hasVideo()) {
        result.push_back(mVideoSession->getControlRecvSocket());
        result.push_back(mVideoSession->getDataRecvSocket());
    }
    return result;
}

void SessionSupport::onControlTick(uint64 timeref)
{
    getAudioSession().checkControlData();
    if (hasVideo())
        getVideoSession().checkControlData();
}

bool SessionSupport::pendingEvent(uint32 mask)
{
    return (mState & mask) != 0;
}

uint32 SessionSupport::setEvent(uint32 mask)
{
    return (mState |= mask);
}

uint32 SessionSupport::setEvent(uint32 mask, int requestId)
{
    mRequestId = requestId;
    return (mState |= mask);
}

void SessionSupport::clearEvent(uint32 mask)
{
    mState &= ~mask;
}

//Prevents shutdown,join and unjoin during ongoing join
void SessionSupport::joinLock()
{
    mJoinLock.wait();
}

void SessionSupport::joinUnlock()
{
    mJoinLock.post();
}

void SessionSupport::setHandleDtmf(bool value)
{
    mHandleDtmf = value;
}

bool SessionSupport::getHandleDtmf()
{
    return mHandleDtmf;
}

const base::String& SessionSupport::getCallSessionId(JNIEnv* env, bool reread)
{
    if (reread) {
        mCallSessionId = mJavaMediaStream->getCallSessionId(env);
    }
    return mCallSessionId;
}

int SessionSupport::getRequestId()
{
    return mRequestId;
}

void SessionSupport::deleteJavaStream(JNIEnv* env)
{
    // Set jnienv before deletion
    mJavaMediaStream->updateJniEnv(env);
    mJavaMediaStream.reset(0);
}

int SessionSupport::getAudioPort()
{
    return mAudioPort;
}

int SessionSupport::getVideoPort()
{
    return mVideoPort;
}

