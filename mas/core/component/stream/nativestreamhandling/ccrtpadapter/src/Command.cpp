#include "Processor.h"

#include "jlogger.h"
#include "jniutil.h"

// TODO: static const char* CLASSNAME = "masjni.ccrtpadapter.Command";

Command::Command(SessionSupport &session):
	mSession(session)
{
    //TODO: JLogger::jniLogDebug(ProcessorGroup::instance().getProcessJNIEnv(mSession), CLASSNAME, "Command - create at %#x", this);
}

Command::~Command()
{
    //TODO: JLogger::jniLogDebug(ProcessorGroup::instance().getProcessJNIEnv(mSession), CLASSNAME, "~Command - delete at %#x", this);
}

std::auto_ptr<Command> Command::waitForCompletion() {
	return std::auto_ptr<Command>();
}

bool Command::isBlocking()
{
	return false;
}

void Command::completed(std::auto_ptr<Command>& cmd) {
}

SessionSupport& Command::getSession() {
	return mSession;
}
void Command::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
}

Blocking::Blocking():
	mCompleted(false),
	mCompletedMutex(),
	mCompletedSemaphore(0)
{
}

Blocking::~Blocking()
{
}

std::auto_ptr<Command> Blocking::doWaitForCompletion() {
	MutexLock lock(mCompletedMutex);
	if(!mCompleted) {
		mCompletedSemaphore.wait();
		mCompleted = true;
	}
	return mCommand;
}

void Blocking::doCompleted(std::auto_ptr<Command>& cmd) {
	mCommand = cmd;
	mCompletedSemaphore.post();
}

InputCommand::InputCommand(InboundSession &session):
	Command(session) 
{
}

InputCommand::~InputCommand()
{
}

InboundSession& InputCommand::getSession() {
	return static_cast<InboundSession&>(Command::getSession());
}

OutputCommand::OutputCommand(OutboundSession &session):
	Command(session) 
{
}

OutputCommand::~OutputCommand()
{
}

OutboundSession& OutputCommand::getSession() {
	return static_cast<OutboundSession&>(Command::getSession());
}

/**********************************************************
 *  OuputBlockingCommand
 **********************************************************/
BlockingCommand::BlockingCommand(SessionSupport &session):
	Command(session),
	Blocking()
{
}

BlockingCommand::~BlockingCommand()
{
}

std::auto_ptr<Command> BlockingCommand::waitForCompletion()
{
	return doWaitForCompletion();
}

void BlockingCommand::completed(std::auto_ptr<Command>& cmd)
{
	doCompleted(cmd);
}


bool BlockingCommand::isBlocking()
{
	return true;
}

/**********************************************************
 *  OuputBlockingCommand
 **********************************************************/
OutputBlockingCommand::OutputBlockingCommand(OutboundSession &session):
	OutputCommand(session),
	Blocking()
{
}

OutputBlockingCommand::~OutputBlockingCommand() {
}

std::auto_ptr<Command> OutputBlockingCommand::waitForCompletion()
{
	return doWaitForCompletion();
}

void OutputBlockingCommand::completed(std::auto_ptr<Command>& cmd)
{
	doCompleted(cmd);
}


bool OutputBlockingCommand::isBlocking()
{
	return true;
}

/**********************************************************
 *  InputBlockingCommand
 **********************************************************/
InputBlockingCommand::InputBlockingCommand(InboundSession &session):
	InputCommand(session)
{
}

InputBlockingCommand::~InputBlockingCommand()
{
}

std::auto_ptr<Command> InputBlockingCommand::waitForCompletion()
{
	return doWaitForCompletion();
}
void InputBlockingCommand::completed(std::auto_ptr<Command>& cmd)
{
	doCompleted(cmd);
}

bool InputBlockingCommand::isBlocking()
{
	return true;
}


/**********************************************************
 *  PlayCommand
 **********************************************************/
PlayCommand::PlayCommand(OutboundSession &session,std::auto_ptr<PlayJob>& job)
	:OutputCommand(session),
	mPlayJob(job)
{
}

PlayCommand::~PlayCommand()
{
}

void PlayCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
    getSession().getPlayer().play(mPlayJob);
}

/**********************************************************
 *  RecordCommand
 **********************************************************/
RecordCommand::RecordCommand(InboundSession &session,std::auto_ptr<RecordJob>& job)
	:InputCommand(session),
	mRecordJob(job)  {
}

void RecordCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
    getSession().getRecorder().record(mRecordJob, JNIUtil::getJavaEnvironment());
}

RecordCommand::~RecordCommand()
{
}

/**********************************************************
 *  StopRecordCommand
 **********************************************************/

StopRecordCommand::StopRecordCommand(InboundSession &session):
	InputBlockingCommand(session),
	mStopResult(0)
{
}

StopRecordCommand::~StopRecordCommand()
{
}

long StopRecordCommand::getResult()
{
	return mStopResult;
}

void StopRecordCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	try {
		mStopResult = getSession().getRecorder().stop();
		completed(cmd);
	}
	catch (...) {
		completed(cmd);
	}
}

/**********************************************************
 *  StopPlayCommand
 **********************************************************/

StopPlayCommand::StopPlayCommand(OutboundSession &session):
	OutputBlockingCommand(session),
	mStopResult(0)
{
}

StopPlayCommand::~StopPlayCommand()
{
}

void StopPlayCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	try {
		getSession().getPlayer().stop();
		completed(cmd);
	}
	catch(...) {
		completed(cmd);
	}
}

long StopPlayCommand::getResult()
{
	return mStopResult;
}

/**********************************************************
 *  RegisterCommand
 **********************************************************/
RegisterCommand::RegisterCommand(SessionSupport &session):
	Command(session)
{
}

RegisterCommand::~RegisterCommand()
{
}

void RegisterCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	proc.registerSession(getSession());
}

/**********************************************************
 *  UnRegisterCommand
 **********************************************************/
UnRegisterCommand::UnRegisterCommand(SessionSupport &session, 
                                     int requestId):
	Command(session),
    m_requestId(requestId)
{
}

UnRegisterCommand::~UnRegisterCommand()
{
}

void UnRegisterCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	proc.unRegisterSession(getSession(), m_requestId);
}


JoinToInputCommand::JoinToInputCommand(OutboundSession &session,InboundSession& joinedSession,
                                       bool handleDtmfAtInbound, bool forwardDtmfToOutbound):
	OutputCommand(session),
	mJoinedSession(joinedSession),
    mHandleDtmfAtInbound(handleDtmfAtInbound),
    mForwardDtmfToOutbound(forwardDtmfToOutbound)
{
}

JoinToInputCommand::~JoinToInputCommand()
{
}

void JoinToInputCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	getSession().setEvent(EVENT_JOINED);
	getSession().prepareJoin(mJoinedSession, mHandleDtmfAtInbound, mForwardDtmfToOutbound);
}


IncomingJoinCommand::IncomingJoinCommand(InboundSession &session):
	InputCommand(session)
{
}

void IncomingJoinCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	getSession().getStreamMixer().incomingConnection();
}

IncomingJoinCommand::~IncomingJoinCommand()
{
}

UnJoinCommand::UnJoinCommand(InboundSession &session,OutboundSession& outboundSession):
	InputCommand(session),
	mOutboundSession(outboundSession)
{
}

void UnJoinCommand::perform(Processor& proc,std::auto_ptr<Command> &cmd)
{
	getSession().getStreamMixer().removeConnection(mOutboundSession.getJoinHandle());
	getSession().joinUnlock();
}

UnJoinCommand::~UnJoinCommand()
{
}
