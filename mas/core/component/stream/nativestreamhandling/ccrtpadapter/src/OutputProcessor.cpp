#include "OutputProcessor.h"
#include "streamrtpsession.h"
#include "streamutil.h"
#include "jlogger.h"
#include "jniutil.h"

#define TIMESLOT_RESOLUTION_MICROS (TIMESLOT_RESOLUTION*1000)

static const char* CLASSNAME = "masjni.ccrtpadapter.OutputProcessor";

OutputProcessor::OutputProcessor(int id) :
        Processor("OutputProcessor", id), mNewSessions(), mFutureTimeslots(), mNextSlot(0), mMaxSendList(0),
        m_callbacks(new CallbackQueue(mEnv))
{
    JLogger::jniLogDebug(mEnv, CLASSNAME, "OutputProcessor - create at %#x", this);
}

OutputProcessor::~OutputProcessor()
{
    JLogger::jniLogDebug(mEnv, CLASSNAME, "~OutputProcessor - delete at %#x", this);
}

void OutputProcessor::setupProcessors(boost::ptr_vector<Processor> &processors, int nOfProcessors)
{
    JNIEnv* env = ProcessorGroup::instance().getJNIEnv();
    JLogger::jniLogTrace(env, CLASSNAME, "OutputProcessor - no. of threads: %d", nOfProcessors);
    for (int i = 0; i < nOfProcessors; i++) {
        OutputProcessor *proc = new OutputProcessor(i);
        processors.push_back(proc);
        proc->start();
    }
}

void OutputProcessor::shutdownProcessors(boost::ptr_vector<Processor> &processors)
{
    //TODO: stop all running threads
}

void OutputProcessor::sendAndSchedule(OutboundSession &session)
{
    if (session.pendingEvent(EVENT_ANY)) {
        OutboundSession* ptr = &session;

        if (session.pendingEvent(EVENT_DELETED)) {
            int requestId(session.getRequestId());
            session.clearEvent(EVENT_DELETED);
            delete ptr;
            ptr = NULL;
            m_callbacks->push(new Callback(mRuntimeEnv, requestId, Callback::DELETE_COMMAND, Callback::OK));
            return;
        }

        if (session.pendingEvent(EVENT_JOINED)) {
            session.clearEvent(EVENT_JOINED);
            session.performJoin();
        }
    }

    StreamRTPSession& audioSession = session.getAudioSession();
    StreamRTPSession& videoSession = session.getVideoSession();
    bool hasVideo = session.hasVideo();

    if (&audioSession == 0) {
        JLogger::jniLogWarn(mRuntimeEnv, CLASSNAME, "Illegal audio-session, %p", &audioSession);
        return;
    }
    if (hasVideo && &videoSession == 0) {
        JLogger::jniLogWarn(mRuntimeEnv, CLASSNAME, "Illegal video-session, %p", &audioSession);
        return;
    }

    audioSession.sendData();
    microtimeout_t audioSchedTimeout = audioSession.getSchedulingTimeout();
    microtimeout_t schedTimeout;

    if (hasVideo) {
        videoSession.sendData();
        microtimeout_t videoSchedTimeout = videoSession.getSchedulingTimeout();
        schedTimeout = audioSchedTimeout < videoSchedTimeout ? audioSchedTimeout : videoSchedTimeout;
    } else {
        schedTimeout = audioSchedTimeout;
    }
    if (schedTimeout > 1000)
        schedTimeout -= 1000;

    unsigned slotNo = 1 + schedTimeout / TIMESLOT_RESOLUTION_MICROS;

    //Always schedule a maximum amount further in time
    if (slotNo > FUTURE_LENGTH / TIMESLOT_RESOLUTION)
        slotNo = FUTURE_LENGTH / TIMESLOT_RESOLUTION;

    unsigned slotsNeeded = slotNo + 1;
    if (mFutureTimeslots.size() < slotsNeeded) {
        if (mMaxSendList < mFutureTimeslots.size()) {
            mMaxSendList = mFutureTimeslots.size();
            JLogger::jniLogTrace(mRuntimeEnv, CLASSNAME, "Maximum number of slots observed: %d", mMaxSendList);
        }
        mFutureTimeslots.insert(mFutureTimeslots.end(), slotsNeeded - mFutureTimeslots.size(),
                std::list<OutboundSession*>());
    }
    mFutureTimeslots[slotNo].push_back(&session);
}

void OutputProcessor::initial()
{
    setCancel(Thread::cancelDeferred);
    //Removed for running as non-root
    //Processor::realizeThreadPriorities(*this,true,20);
}

void OutputProcessor::process()
{
    int32 toWait;

    try {
        //Wait for next slot if it hasn't been reached yet.
        updateTimeReference();
        if ((toWait = static_cast<int32>(mNextSlot - mTimeReference)) > 0) {
            /*JLogger::jniLogTrace(env, CLASSNAME, "OK %d waiting %d", mProcessorId, toWait);*/
            Thread::sleep(toWait);
        }
        /* Somebody commented: When running with real time priorities the log call below locks out all other
         non real time threads from printing on the log causing them to hang.
         And I replied: In that case, something is severely broken in the loggger, because this log
         should almost _never_ be printed !
         else {
         JLogger::jniLogTrace(env, CLASSNAME, "Backlogged %d awoke %d ms to late.", mProcessorId, toWait);
         }
         */
        mNextSlot += TIMESLOT_RESOLUTION;

        if (!mFutureTimeslots.empty()) {
            //Iterate through all in this timeslot.
            std::list<OutboundSession*>& timeSlice = mFutureTimeslots.front();
            std::list<OutboundSession*>::iterator iter = timeSlice.begin();
            for (; iter != timeSlice.end(); iter++) {
                updateTimeReference();
                sendAndSchedule(**iter);
            }
            //Remove this timeslot
            mFutureTimeslots.pop_front();
            mFutureTimeslots.push_back(std::list<OutboundSession*>());
        }

        //Handle control data
        maybeControlTick();

        //Iterate through all unallocated candidates.
        if (!mNewSessions.empty()) {
            std::list<OutboundSession*>::iterator iter = mNewSessions.begin();
            for (; iter != mNewSessions.end(); iter++) {
                sendAndSchedule(**iter);
            }
            mNewSessions.clear();
        }

        if (!m_callbacks->isPushQueueEmpty() && m_callbacks->isPopQueueEmpty()) {
            JLogger::jniLogTrace(mRuntimeEnv, CLASSNAME, "Swapping callback queues: %d",
                    m_callbacks->getSizeOfPushQueue());
            m_callbacks->swapQueues();
        }
    } catch (ost::Socket* error) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "OutputProcessor: Caught exception on RTP Control Data Channel.");
    } catch (ost::SockException const& error) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "OutputProcessor: Caught exception on RTP Control Channel.");
    } catch (...) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "OutputProcessor: Caught unknown exception.");
    }
}

void OutputProcessor::registerSession(SessionSupport& session)
{
    OutboundSession* os((OutboundSession*) &session);
    os->setCallbackQueue(m_callbacks.get());
    mNewSessions.push_back(os);

    //insert into currentSession list to facilitate the onControlTick
    //function for outbound session
    //std::list<OutboundSession*>::iterator iter = mNewSessions.begin();
    //for(;iter != mNewSessions.end();iter++) {
    //mCurrentSessions.insert(*iter);
    mCurrentSessions.insert(os);
    //}
}

void OutputProcessor::unRegisterSession(SessionSupport& session, int requestId)
{
    session.setEvent(EVENT_DELETED, requestId);
    OutboundSession* os((OutboundSession*) &session);
    mCurrentSessions.erase(os);
}
