#include "InputProcessor.h"
#include "streamutil.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.InputProcessor";

#if defined(LINUX_EPOLL)
InputProcessor::InputProcessor(int id):
Processor("InputProcessor", id),
mNewSessions(),
mToDeleteSessions(),
mSocketCount(0),
mPortEventArray(new struct epoll_event[INPUT_FDALLOC + 1])
{
    mPort = epoll_create(INPUT_FDALLOC);
    JLogger::jniLogTrace(mEnv, CLASSNAME, "poll port=%d opened", mPort);

    JLogger::jniLogDebug(mEnv, CLASSNAME, "InputProcessor - create at %#x", this);
}

InputProcessor::~InputProcessor()
{
    if(mPort > -1) {
        JLogger::jniLogTrace(mEnv, CLASSNAME, "poll port=%d closed", mPort);
        ::close(mPort);
    }

    JLogger::jniLogDebug(mEnv, CLASSNAME, "~InputProcessor - delete at %#x", this);
}
#endif

void InputProcessor::initial()
{
    setCancel(Thread::cancelDeferred);
    //Removed for running as non-root
    //Processor::realizeThreadPriorities(*this,false,2);
}

void InputProcessor::setupProcessors(boost::ptr_vector<Processor> &processors, int nOfProcessors)
{
    JNIEnv* env = ProcessorGroup::instance().getJNIEnv();
    JLogger::jniLogTrace(env, CLASSNAME, "InputProcessor - no. of threads: %d", nOfProcessors);
    for (int i = 0; i < nOfProcessors; i++) {
        InputProcessor *proc = new InputProcessor(i);
        processors.push_back(proc);
        proc->start();
    }
}

void InputProcessor::shutdownProcessors(boost::ptr_vector<Processor> &processors)
{
}

void InputProcessor::updatePolledSet()
{
    //Associate all new sessions
    std::list<InboundSession*>::iterator iter = mNewSessions.begin();
    for (; iter != mNewSessions.end(); iter++) {
        associateSession(**iter);
        mCurrentSessions.insert(*iter);
    }
    mNewSessions.clear();
}

void InputProcessor::registerSession(SessionSupport& session)
{
    mNewSessions.push_back((InboundSession*) &session);
}

void InputProcessor::unRegisterSession(SessionSupport& session, int requestId)
{
    dissociateSession((InboundSession&) session);
}

void InputProcessor::associateSession(InboundSession &session)
{
    boost::ptr_vector<ReceptionAdapter>& adapters = session.getReceptionAdapters();
    boost::ptr_vector<ReceptionAdapter>::iterator iter = adapters.begin();
    for (; iter != adapters.end(); iter++) {
        associate(iter->getSocket(), *iter);
    }
}

void InputProcessor::process()
{
    try {
        processDeletedSessions();

        updatePolledSet();

        updateTimeReference();

        maybeControlTick();

        poll();
    } catch (ost::Socket* error) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "InputProcessor: Caught exception on RTP Control Channel.");
    } catch (ost::SockException const& error) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "InputProcessor: Caught exception on RTP Control Channel.");
    } catch (...) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "InputProcessor: Caught unknown exception.");
    }
}

/*
 * The inbound session is not deleted right away,
 * need to wait for the outbound session to be deleted first.
 * Both inbound and outbound session share the same socket
 * The inbound session is the 'owner' and will delete it.
 *
 * The inbound session to be deleted are added to a list.
 * The list will be processed in processDeletedSessions().
 */
void InputProcessor::markAsDeleted(InboundSession &session)
{
    JLogger::jniLogInfo(mRuntimeEnv, CLASSNAME,
            "Marking session %#x for deletion. Number of sessions to be deleted: %d", &session,
            mToDeleteSessions.size());
    mToDeleteSessions.push_back(&session);
}

/*
 * Goes through the list of inbound sessions to be deleted and
 * check if their corresponding outbound session has been deleted.
 * If it has, the function will delete the inbound session otherwise
 * the inbound session stays in the queue.
 */
void InputProcessor::processDeletedSessions()
{
    std::list<InboundSession*>::iterator iter = mToDeleteSessions.begin();
    InboundSession* session(NULL);

    while (iter != mToDeleteSessions.end()) {
        if ((*iter)->getOutboundSession() == NULL) {
            session = *iter;
            JLogger::jniLogInfo(mRuntimeEnv, CLASSNAME, "Deleting session %#x number of sessions left in the queue: %d",
                    &session, mToDeleteSessions.size());

            iter = mToDeleteSessions.erase(iter);
            delete session;
            session = NULL;
        } else {
            iter++;
        }
    }
}

#if defined(LINUX_EPOLL)
void InputProcessor::dissociateSession(InboundSession &session) {
    JNIEnv* env = JNIUtil::getJavaEnvironment(mRuntimeEnv);

    std::vector<SOCKET> sockets(session.getAllSockets());
    std::vector<SOCKET>::iterator iter = sockets.begin();

    for(;iter < sockets.end();iter++) {
        mSocketCount--;
        epoll_ctl(mPort,EPOLL_CTL_DEL,*iter,0);
    }
    mCurrentSessions.erase(&session);
    markAsDeleted(session);

    JLogger::jniLogTrace(env, CLASSNAME, "InputProcessor::dissociateSession mSocketCount [%d]", mSocketCount);
}

void InputProcessor::reassociate(SOCKET sock,ReceptionAdapter& adapter) {
}

void InputProcessor::associate(SOCKET sock,ReceptionAdapter& adapter) {
    JNIEnv* env = JNIUtil::getJavaEnvironment(mRuntimeEnv);

    int oldopts;
    struct epoll_event event_definition;
    oldopts = fcntl(sock, F_GETFL, 0);
    fcntl(sock, F_SETFL, oldopts | O_NONBLOCK);

    event_definition.events = EPOLLIN | EPOLLHUP | EPOLLERR;
    event_definition.data.ptr = (void *)&adapter;
    if((epoll_ctl(mPort,EPOLL_CTL_ADD, sock,&event_definition) == -1)) {
        switch (errno)
        {
        case EBADF :
            JLogger::jniLogError(env, CLASSNAME, "EBADF - epfd or fd is not a valid file descriptor - epoll_ctl");
            break;
        case ENOMEM :
            JLogger::jniLogError(env, CLASSNAME, "ENOMEM - There was insufficient memory to handle the requested op control operation. - epoll_ctl");
            break;
        case EPERM:
            JLogger::jniLogError(env, CLASSNAME, "EPERM - The target file fd does not support epoll. - epoll-ctl");
            break;
        default:
            JLogger::jniLogError(env, CLASSNAME, "Other error [%d] - epoll_ctl", errno);
            break;
        }

        // Since we associate only if it's a new session then we call on abandoned to shut off the call.
        adapter.getSession().onUnableToReceive(adapter.isVideo());
    } else {
        mSocketCount++;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "InputProcessor::associate mSocketCount [%d]", mSocketCount);
}

void InputProcessor::poll()
{
    // < 5 ==> Wait for 1 event;
    // 5 -> 10 ==> Wait for 2 events
    // ...

    unsigned int nToReturn = 1+mSocketCount/5;

    int result = epoll_wait(mPort, mPortEventArray.get(),nToReturn, INPUT_POLL_YIELD_MICROS/1000);
    int error = errno;

    if(mSocketCount == 0) {
        sleep(INPUT_POLL_YIELD_MICROS/1000);
        return;
    }
    if(result >= 0) {
        JLogger::jniLogTrace(mRuntimeEnv, CLASSNAME, "No of FD's available after epoll [%d]", result);

        for(int i=0;i<result;i++) {
            struct epoll_event event = mPortEventArray[i];

            ReceptionAdapter& adapter = *(ReceptionAdapter*)event.data.ptr;
            int eventFlags = event.events;

            if((eventFlags & (EPOLLIN)) != 0) {
                adapter.handleReception(mTimeReference);
            } else if((eventFlags & (EPOLLERR)) != 0)  {
              JLogger::jniLogWarn(mRuntimeEnv, CLASSNAME, "Waked up from epoll_wait due to error [%d]", errno);
            }
        }
    } else {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "port_getn failed with error %d", error);
        sleep(INPUT_POLL_YIELD_MICROS/1000);
    }
}
#endif

ReceptionAdapter::ReceptionAdapter(InboundSession &session, SOCKET s, bool isVideo) :
        mIsVideo(isVideo), mSession(session), mSock(s)
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "ReceptionAdapter - create at %#x", this);
}

InboundSession & ReceptionAdapter::getSession()
{
    return mSession;
}

SOCKET ReceptionAdapter::getSocket()
{
    return mSock;
}

bool ReceptionAdapter::isVideo()
{
    return mIsVideo;
}

ReceptionAdapter::~ReceptionAdapter()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~ReceptionAdapter - delete at %#x", this);
}

void ReceptionAdapter::handleReception(uint64 timeRef)
{
}

ControlReceptionAdapter::ControlReceptionAdapter(InboundSession &session, bool isVideo) :
        ReceptionAdapter(session,
                isVideo ?
                        session.getVideoSession().getControlRecvSocket() :
                        session.getAudioSession().getControlRecvSocket(), isVideo)
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "ControlReceptionAdapter - create at %#x", this);
}

ControlReceptionAdapter::~ControlReceptionAdapter()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~ControlReceptionAdapter - delete at %#x", this);
}

void ControlReceptionAdapter::handleReception(uint64 timeRef)
{
    mSession.onControlDataAvailable(mIsVideo, timeRef);
    if (mIsVideo) {
        JLogger::jniLogTrace(JNIUtil::getJavaEnvironment(), CLASSNAME, "Control data (Video)");
    } else {
        JLogger::jniLogTrace(JNIUtil::getJavaEnvironment(), CLASSNAME, "Control data (Audio)");
    }
}

DataReceptionAdapter::DataReceptionAdapter(InboundSession &session, bool isVideo) :
        ReceptionAdapter(session,
                isVideo ? session.getVideoSession().getDataRecvSocket() : session.getAudioSession().getDataRecvSocket(),
                isVideo)
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "DataReceptionAdapter - create at %#x", this);
}

DataReceptionAdapter::~DataReceptionAdapter()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~DataReceptionAdapter - delete at %#x", this);
}

void DataReceptionAdapter::handleReception(uint64 timeref)
{
    mSession.onDataAvailable(mIsVideo, timeref);
}
