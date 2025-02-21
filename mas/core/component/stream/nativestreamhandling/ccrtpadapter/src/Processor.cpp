#if !defined(WIN32)
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#if defined(LINUX)
#include <pthread.h>
#include <sched.h>
#include <sys/syscall.h>
#include <sys/resource.h>
#else //defined(LINUX)
#include <sys/priocntl.h>
#include <sys/rtpriocntl.h>
#endif //!defined(LINUX)
#include <sys/procfs.h>
#include <sys/prctl.h>
#endif

#include "Processor.h"
#include "InputProcessor.h"
#include "OutputProcessor.h"
#include "streamutil.h"

#include "jniutil.h"
#include "jlogger.h"

using namespace std;

using std::auto_ptr;

static const char* CLASSNAME = "masjni.ccrtpadapter.Processor";
static const char* CLASSNAME_GRP = "masjni.ccrtpadapter.ProcessorGroup";

Processor::Processor(const char * name, int id) :
        mEnv(NULL), mRuntimeEnv(NULL), mStreamCount(0), mProcessorId(id), mCurrentSessions(), mTimeReference(0),
        mLastTick(0), mCommandMutex(), mCommands()
{
    setName(name);
    StreamUtil::getTimeOfDay(mStartTime);
    setException(ost::Thread::throwException);

    (void) JNIUtil::getJavaEnvironment((void**) &mEnv, true);

    JLogger::jniLogDebug(mEnv, CLASSNAME, "Processor - create at %#x - thread id: %d", this, threadId());
}

Processor::~Processor()
{
    JLogger::jniLogDebug(mEnv, CLASSNAME, "~Processor - delete at %#x", this);

    // No need to detach here - the boost library will take care
    // if somehow the run method missed the detach
}

void Processor::process()
{
    Thread::sleep(1000);
}

void Processor::submitCommand(std::auto_ptr<Command>& command)
{
    MutexLock lock(mCommandMutex);
    mCommands.push_back(command.release());
}

bool Processor::tryProcessCommands()
{
    TryMutexLock lock(mCommandMutex);
    if (lock.succeded()) {
        while (!mCommands.empty()) {
            std::auto_ptr<Command> command(mCommands.pop_front().release());
            command->perform(*this, command);
        }
        return true;
    }
    return false;
}

//Exception guards
void Processor::run()
{
    (void) JNIUtil::getJavaEnvironment((void**) &mRuntimeEnv, true);
    JLogger::jniLogInfo(mRuntimeEnv, CLASSNAME, "Thread [%d] with name [%s] started", threadId(), getName());

    while (!testCancel()) {
        try {
            bool commandsExecuted = false;

            //Push JNI frame - 16 frames is a min required by JVM spec
            (void) JNIUtil::PushLocalFrame(mRuntimeEnv, 16);

            //First try to execute commands
            commandsExecuted = tryProcessCommands();

            //Do main processing
            process();

            //Retry commands after processing.
            if (!commandsExecuted)
                tryProcessCommands();
        } catch (ost::Socket*) {
            JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "Socket* Processor run. Allow thread to continue.");
        } catch (ost::SockException const& error) {
            JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "SockException Processor run. Allow thread to continue.");
        } catch (...) {
            JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "Unknown exception. Allow thread to continue.");
        }

        JNIUtil::PopLocalFrame(mRuntimeEnv);
    }

    JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "Thread [%d] with name [%s] exits", threadId(), getName());

    // Detach only when thread finishes up:
    //    - here the runtime env
    //    - the remaining in the destructor
    JNIUtil::DetachCurrentThread();
    exit();
}

void Processor::maybeControlTick()
{
    if (mTimeReference - mLastTick > CONTROL_TICK_CHECK) {
        mLastTick = mTimeReference;
        std::set<SessionSupport*>::iterator iter = mCurrentSessions.begin();
        for (; iter != mCurrentSessions.end(); iter++) {
            (*iter)->onControlTick(mTimeReference);
        }
    }
}

void Processor::updateTimeReference()
{
    timeval now;
    StreamUtil::getTimeOfDay(now);
    mTimeReference = StreamUtil::timeDiff64(mStartTime, now);
}

unsigned Processor::getStreamCount()
{
    return mStreamCount;
}

void Processor::decStreamCount()
{
    mStreamCount--;
}

void Processor::incStreamCount()
{
    mStreamCount++;
}

int Processor::cpuCount()
{
#if !defined(WIN32)
    return static_cast<int>(sysconf(_SC_NPROCESSORS_ONLN));
#else
    return 1;
#endif
}

pid_t Processor::threadId()
{
    pid_t ret = syscall(SYS_gettid);
    if (ret == -1) {
        JLogger::jniLogError(mRuntimeEnv, CLASSNAME,
                "Stream is running under old style linux threads, this is not supported.");
        return getpid();
    } else {
        return (pid_t) ret;
    }
}

void Processor::realizeThreadPriorities(Thread &thread, bool realtime, int boost)
{
#if !defined(WIN32)
    if (realtime) {
        do {
#if defined(LINUX)
            int policy;
            struct sched_param param;
            pthread_attr_t attr;

            sched_getparam(threadId(), &param);
            param.sched_priority=50;

            policy=SCHED_FIFO;
            int result = sched_setscheduler (threadId(), policy, &param);
            if (result)
            JLogger::jniLogError(mRuntimeEnv, CLASSNAME , "Set FIFO scheduler failed: %s(%d)", strerror(errno), errno);

            pthread_attr_init (&attr);
            result = pthread_attr_setscope (&attr,PTHREAD_SCOPE_SYSTEM);
            if (result)
            JLogger::jniLogError(mRuntimeEnv, CLASSNAME, "Set schduling scope to SYSTEM failed: %s(%d)", strerror(errno), errno);
#else //defined(LINUX)
            pcinfo_t pcinfo;
            pcparms_t pcparms;
            rtinfo_t *rtinfop;
            int i;

            // get the class ID for the real-time class
            strcpy(pcinfo.pc_clname, "RT");
            if (priocntl((idtype) 0, 0, PC_GETCID, (caddr_t) &pcinfo) == -1) {
                realtime = false;
                JLogger::jniLogWarn(mRuntimeEnv, CLASSNAME,
                        "Unable to find realtime class id, maybe it's not supported ?");
                break;
            }
            int maxrtpri;
            rtinfop = (rtinfo_t *) pcinfo.pc_clinfo;
            maxrtpri = rtinfop->rt_maxpri;

            // set up the real-time parameters
            pcparms.pc_cid = pcinfo.pc_cid;
            ((rtparms_t *) pcparms.pc_clparms)->rt_pri = maxrtpri - 1;
            // Ignored when tqnsecs is RT_TQINF
            ((rtparms_t *) pcparms.pc_clparms)->rt_tqsecs = 0;

            // set an infinite time quantum
            ((rtparms_t *) pcparms.pc_clparms)->rt_tqnsecs = RT_TQINF;

            // move this thread to the real-time scheduling class
            if (priocntl(P_LWPID, P_MYID, PC_SETPARMS, (caddr_t) &pcparms) == -1) {
                realtime = false;
                int eno = errno;
                JLogger::jniLogWarn(mRuntimeEnv,
                        "Error, %d unable to move thread into the realtime class , message was %s", eno, strerror(eno));
                break;
            }
#endif //!defined(LINUX)

        } while (false);
    }
#else // !defined(WIN32)
    realtime = false;
#endif // defined(WIN32)

    if (!realtime) {
#if defined(LINUX)
        setpriority(PRIO_PROCESS,threadId(),-boost);
#else //defined(LINUX)
        sched_param param;
        pthread_t id = thread.getId();
        int policy;
        int prio;

        int ret = pthread_getschedparam(id, &policy, &param);
        if (ret == 0) {
            int max = sched_get_priority_max(policy);
            int min = sched_get_priority_min(policy);

            param.sched_priority = boost;
            JLogger::jniLogFine(mRuntimeEnv, CLASSNAME, "Setting priority to %d from a range of %d to %d",
                    param.sched_priority, min, max);

            if (pthread_setschedparam(id, policy, &param) != 0) {
                JLogger::jniLogWarn(mRuntimeEnv, CLASSNAME, "pthread_setschedparam failed %d", errno);
            }
        }
#endif //!defined(LINUX)
    }
}

/*
 * ProcessorGroup
 */

ProcessorGroup* ProcessorGroup::sInstance = 0;
Mutex ProcessorGroup::sGroupMutex = Mutex();

void ProcessorGroup::initialize(int nOfOutputs, int nOfInputs)
{
    InputProcessor::setupProcessors(mInputProcessors, nOfInputs);
    OutputProcessor::setupProcessors(mOutputProcessors, nOfOutputs);
}

int ProcessorGroup::getOutputCount()
{
    return mOutputProcessors.size();
}

int ProcessorGroup::getInputCount()
{
    return mInputProcessors.size();
}

void ProcessorGroup::removeSession(SessionSupport& session)
{
    MutexLock lock(mProcessorMutex);
    long key = reinterpret_cast<long>(&session);
    stdext::hash_map<long, Processor*>::iterator iter = mProcessorById.find(key);
    if (iter != mProcessorById.end()) {
        Processor* proc = iter->second;

        JLogger::jniLogTrace(JNIUtil::getJavaEnvironment(), CLASSNAME_GRP,
                "De-Assigned processor %#x from session %#x - stream count %d", proc, &session, proc->getStreamCount());

        mProcessorById.erase(key);
        proc->decStreamCount();
    }
}

void ProcessorGroup::addSession(JNIEnv* env, SessionSupport& session)
{
    Processor* proc = NULL;
    { //Start of mutex-locked block
        MutexLock lock(mProcessorMutex);
        boost::ptr_vector<Processor>& processors(session.isOutbound() ? mOutputProcessors : mInputProcessors);
        for (unsigned i = 0; i < processors.size(); i++) {
            Processor &tmp = processors[i];
            if (proc == NULL || tmp.getStreamCount() < proc->getStreamCount()) {
                proc = &tmp;
            }
        }

        mProcessorById[reinterpret_cast<long>(&session)] = proc;
        proc->incStreamCount();
    } //End of mutex-locked block

    JLogger::jniLogTrace(env, CLASSNAME_GRP, "Assigned processor %#x to session %#x - stream count %d", proc, &session, proc->getStreamCount());

    std::auto_ptr<Command> reg(new RegisterCommand(session));
    proc->submitCommand(reg);
}

std::auto_ptr<Command> ProcessorGroup::dispatchCommand(JNIEnv* env, std::auto_ptr<Command>& cmd)
{
    SessionSupport& session = cmd->getSession();
    long key = reinterpret_cast<long>(&session);
    stdext::hash_map<long, Processor*>::iterator iter;
    {
        MutexLock lock(mProcessorMutex);
        iter = mProcessorById.find(key);
    }
    if (iter == mProcessorById.end()) {
        addSession(env, session);
        MutexLock lock(mProcessorMutex);
        iter = mProcessorById.find(key);
    }
    //TODO: cmtPtr is necessary here since we loose ownership. This
    //could be changed into a shared ptr in the future
    Command* cmdPtr = &(*cmd);

    bool blocking = cmd->isBlocking();

    iter->second->submitCommand(cmd);

    if (blocking)
        return cmdPtr->waitForCompletion();
    else
        return std::auto_ptr<Command>(0);
}

ProcessorGroup& ProcessorGroup::instance()
{
    //Double locking works in C++, most of the time :)
    if (sInstance == 0) {
        MutexLock lock(sGroupMutex);
        if (sInstance == 0) {
            sInstance = new ProcessorGroup();
        }
    }
    return *sInstance;
}

void ProcessorGroup::shutdown()
{
    MutexLock lock(sGroupMutex);
    if (sInstance != NULL) {
        delete sInstance;
        sInstance = NULL;
    }
}

ProcessorGroup::ProcessorGroup() :
        mProcessorMutex(), mProcessorById(), mInputProcessors(), mOutputProcessors()
{
    JNIUtil::getJavaEnvironment((void**) &mEnv, true);
    JLogger::jniLogDebug(mEnv, CLASSNAME_GRP, "ProcessorGroup - create at %#x", this);
}

ProcessorGroup::~ProcessorGroup()
{
    MutexLock lock(sGroupMutex);
    InputProcessor::shutdownProcessors(mInputProcessors);
    OutputProcessor::shutdownProcessors(mOutputProcessors);

    JLogger::jniLogDebug(mEnv, CLASSNAME_GRP, "~ProcessorGroup - delete at %#x", this);
    JNIUtil::DetachCurrentThread();
}
