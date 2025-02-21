#include "Recorder.h"
#include "recordjob.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.Recorder";

Recorder::Recorder(JNIEnv* env) :
        mJob(0)
{
    JLogger::jniLogDebug(env, CLASSNAME, "Recorder - create at %#x", this);
}

Recorder::~Recorder()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~Recorder - delete at %#x", this);
}

void Recorder::record(std::auto_ptr<RecordJob> job, JNIEnv* env)
{
    if (isRecording()) {
        job->sendRecordFailed(StackEventDispatcher::EXCEPTION, "A recording is already in progress.", env);
    } else {
        mJob = job;
        mJob->init();
    }
}

bool Recorder::isRecording()
{
    if (mJob.get() != 0) {
        return mJob->isRecording();
    }
    return false;
}

long Recorder::stop()
{
    if (mJob.get() != 0) {
        mJob->stop();
    }
    return 0;
}

void Recorder::handlePacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool isVideoPacket)
{
    if (mJob.get() != 0) {
        mJob->handlePacket(adu, isVideoPacket);
    }
}

void Recorder::abandon()
{
    if (mJob.get() != 0) {
        mJob->abandoned();
    }
}

void Recorder::onTimerTick(uint64 timeref)
{
    if (mJob.get() != 0) {
        mJob->onTimerTick(timeref);
    }
}
