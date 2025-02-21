#ifndef RECORDER_H_
#define RECORDER_H_

#include <cc++/config.h>
#include <base_std.h>

#include "recordjob.h"
#include "jni.h"

class Recorder
{
public:
    Recorder(JNIEnv* env);
    virtual ~Recorder();
    void record(std::auto_ptr<RecordJob> job, JNIEnv* env);
    bool isRecording();
    long stop();
    void abandon();
    void handlePacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool isVideoPacket);
    virtual void onTimerTick(uint64 timeref);
protected:
    std::auto_ptr<RecordJob> mJob;

};

#endif /*RECORDER_H_*/
