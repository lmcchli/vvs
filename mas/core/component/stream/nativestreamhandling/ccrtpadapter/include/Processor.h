#ifndef PROCESSOR_H_
#define PROCESSOR_H_

#include "jni.h"
#include "sessionsupport.h"
#include "inboundsession.h"
#include "outboundsession.h"

#include <base_include.h>
#include <cc++/thread.h>
#include <boost/ptr_container/ptr_vector.hpp>
#include <boost/ptr_container/ptr_list.hpp>
#include <base_std.h>

#define CONTROL_TICK_CHECK 200

class Command
{
public:
    Command(SessionSupport &session);
    virtual ~Command();

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
    virtual std::auto_ptr<Command> waitForCompletion();
    virtual void completed(std::auto_ptr<Command>& cmd);
    virtual bool isBlocking();

    virtual SessionSupport& getSession();
protected:
    SessionSupport& mSession;
private:
    Command(const Command& rhs);
    Command& operator=(const Command& rhs);
};

class Blocking
{
public:
    Blocking();
    virtual ~Blocking();

    virtual std::auto_ptr<Command> doWaitForCompletion();
    virtual void doCompleted(std::auto_ptr<Command>& cmd);
private:
    bool mCompleted;
    std::auto_ptr<Command> mCommand;
    ost::Mutex mCompletedMutex;
    ost::Semaphore mCompletedSemaphore;
};

class InputCommand: public Command
{
public:
    InputCommand(InboundSession &session);
    virtual ~InputCommand();

    virtual InboundSession& getSession();

};

class InputBlockingCommand: public InputCommand,
        protected Blocking
{
public:
    InputBlockingCommand(InboundSession &session);
    virtual ~InputBlockingCommand();
    virtual std::auto_ptr<Command> waitForCompletion();
    virtual void completed(std::auto_ptr<Command>& cmd);
    virtual bool isBlocking();
};

class OutputCommand: public Command
{
public:
    OutputCommand(OutboundSession &session);
    virtual ~OutputCommand();

    virtual OutboundSession& getSession();

};

class BlockingCommand: public Command,
        protected Blocking
{
public:
    BlockingCommand(SessionSupport &session);
    virtual ~BlockingCommand();
    virtual std::auto_ptr<Command> waitForCompletion();
    virtual void completed(std::auto_ptr<Command>& cmd);
    virtual bool isBlocking();
};

class OutputBlockingCommand: public OutputCommand,
        protected Blocking
{
public:
    OutputBlockingCommand(OutboundSession &session);
    virtual ~OutputBlockingCommand();
    virtual std::auto_ptr<Command> waitForCompletion();
    virtual void completed(std::auto_ptr<Command>& cmd);
    virtual bool isBlocking();
};

class PlayCommand: public OutputCommand
{
public:
    PlayCommand(OutboundSession &session, std::auto_ptr<PlayJob>& job);
    virtual ~PlayCommand();

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);

protected:
    std::auto_ptr<PlayJob> mPlayJob;
};

class RecordCommand: public InputCommand
{
public:
    RecordCommand(InboundSession &session, std::auto_ptr<RecordJob>& job);
    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);

    virtual ~RecordCommand();
protected:
    std::auto_ptr<RecordJob> mRecordJob;
};

class StopRecordCommand: public InputBlockingCommand
{
public:
    StopRecordCommand(InboundSession &session);
    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
    long getResult();
    virtual ~StopRecordCommand();

protected:
    long mStopResult;

};

class StopPlayCommand: public OutputBlockingCommand
{
public:
    StopPlayCommand(OutboundSession &session);
    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
    long getResult();
    virtual ~StopPlayCommand();

protected:
    long mStopResult;

};

class RegisterCommand: public Command
{
public:
    RegisterCommand(SessionSupport &session);
    virtual ~RegisterCommand();

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
};

class UnRegisterCommand: public Command
{
public:
    UnRegisterCommand(SessionSupport &session, int requestId = -1);
    virtual ~UnRegisterCommand();

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
private:
    int m_requestId;
};

class JoinToInputCommand: public OutputCommand
{
public:
    JoinToInputCommand(OutboundSession &session, InboundSession& joinedSession, bool handleDtmfAtInbound,
            bool forwardDtmfToOutbound);

    virtual ~JoinToInputCommand();

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);
protected:
    InboundSession& mJoinedSession;
    bool mHandleDtmfAtInbound;
    bool mForwardDtmfToOutbound;
};

class IncomingJoinCommand: public InputCommand
{
public:
    IncomingJoinCommand(InboundSession &session);

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);

    virtual ~IncomingJoinCommand();
};

class UnJoinCommand: public InputCommand
{
public:
    UnJoinCommand(InboundSession &session, OutboundSession& outboundSession);

    virtual void perform(Processor& proc, std::auto_ptr<Command> &cmd);

    virtual ~UnJoinCommand();
protected:
    OutboundSession& mOutboundSession;
};

class Processor: public Thread
{
public:
    Processor(const char * name, int id);
    virtual ~Processor();

    JNIEnv* mEnv;
    JNIEnv* mRuntimeEnv;

    virtual void submitCommand(std::auto_ptr<Command>& ptr);
    virtual void process();

    virtual void registerSession(SessionSupport& session) = 0;
    virtual void unRegisterSession(SessionSupport& session, int requestId) = 0;

    void decStreamCount();
    void incStreamCount();
    unsigned getStreamCount();

    void updateTimeReference();
    void maybeControlTick();

    static int cpuCount();
    int threadId();
    void realizeThreadPriorities(Thread& thread, bool realtime, int boost);
protected:
    void precisionWait(unsigned deltaMs);
    bool tryProcessCommands();
    virtual void run();

    unsigned mStreamCount;
    unsigned mProcessorId;

    std::set<SessionSupport*> mCurrentSessions;

    uint64 mTimeReference;
    timeval mStartTime;
    uint64 mLastTick;

    ost::Mutex mCommandMutex;
    boost::ptr_list<Command> mCommands;
    ost::Semaphore mPrecisionWaitSemaphore;
};

class ProcessorGroup
{
public:
    virtual ~ProcessorGroup();

    void initialize(int nOfOutputs, int nOfInputs);
    int getOutputCount();
    int getInputCount();

    std::auto_ptr<Command> dispatchCommand(JNIEnv* env, std::auto_ptr<Command>& command);
    void removeSession(SessionSupport& session);
    void addSession(JNIEnv* env, SessionSupport& session);

    static ProcessorGroup& instance();
    inline JNIEnv* getJNIEnv()
    {
        return mEnv;
    };
    static void shutdown();
protected:
    ProcessorGroup();

    ost::Mutex mProcessorMutex;stdext::hash_map<long, Processor*> mProcessorById;
    boost::ptr_vector<Processor> mInputProcessors;
    boost::ptr_vector<Processor> mOutputProcessors;

    static ProcessorGroup* sInstance;
    static ost::Mutex sGroupMutex;

    JNIEnv* mEnv;
};

class TryMutexLock
{
private:
    bool mSucceded;
    Mutex& mutex;
public:
    /**
     * Acquire the mutex
     */
    TryMutexLock(Mutex& _mutex) :
            mutex(_mutex)
    {
        mSucceded = mutex.tryEnterMutex();
    }
    /**
     * Release the mutex automatically
     */
    // this should be not-virtual
    ~TryMutexLock()
    {
        if (mSucceded)
            mutex.leaveMutex();
    }

    bool succeded()
    {
        return mSucceded;
    }
};
#endif /*PROCESSOR_H_*/
