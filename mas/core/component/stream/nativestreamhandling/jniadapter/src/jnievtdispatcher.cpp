#include <stdexcept>
#include <iostream>

#include "jnievtdispatcher.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIEvtDispatcher";

const char* JNIEvtDispatcher::EVENT_PLAY_FINISHED_METHOD = "playFinished";
const char* JNIEvtDispatcher::EVENT_PLAY_FINISHED_METHOD_SIGNATURE = "(Ljava/lang/Object;IJ)V";
jmethodID JNIEvtDispatcher::playFinishedMID;

const char* JNIEvtDispatcher::EVENT_RECORD_FINISHED_METHOD = "recordFinished";
const char* JNIEvtDispatcher::EVENT_RECORD_FINISHED_METHOD_SIGNATURE = "(Ljava/lang/Object;ILjava/lang/String;)V";
jmethodID JNIEvtDispatcher::recordFinishedMID;

const char* JNIEvtDispatcher::EVENT_STREAM_ABANDONED_METHOD = "streamAbandoned";
const char* JNIEvtDispatcher::EVENT_STREAM_ABANDONED_METHOD_SIGNATURE = "(Lcom/mobeon/masp/stream/IMediaStream;)V";
jmethodID JNIEvtDispatcher::streamAbandonedMID;

const char* JNIEvtDispatcher::EVENT_PLAY_FAILED_METHOD = "playFailed";
const char* JNIEvtDispatcher::EVENT_PLAY_FAILED_METHOD_SIGNATURE = "(Ljava/lang/Object;ILjava/lang/String;)V";
jmethodID JNIEvtDispatcher::playFailedMID;

const char* JNIEvtDispatcher::EVENT_RECORD_FAILED_METHOD = "recordFailed";
const char* JNIEvtDispatcher::EVENT_RECORD_FAILED_METHOD_SIGNATURE = "(Ljava/lang/Object;ILjava/lang/String;)V";
jmethodID JNIEvtDispatcher::recordFailedMID;

const char* JNIEvtDispatcher::EVENT_RETURN_FROM_CALL_METHOD = "returnFromCall";
const char* JNIEvtDispatcher::EVENT_RETURN_FROM_CALL_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";
jmethodID JNIEvtDispatcher::retFromCallMID;

const char* JNIEvtDispatcher::EVENT_SEND_TOKEN_METHOD = "control";
const char* JNIEvtDispatcher::EVENT_SEND_TOKEN_METHOD_SIGNATURE = "(III)V";
jmethodID JNIEvtDispatcher::sendTokenMID;

const char* JNIEvtDispatcher::STACK_EVENT_NOTIFIER_CLASSNAME = "com/mobeon/masp/stream/StackEventNotifier";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_EvtDispatcherOnLoad(void *reserved)
{
    JNIEvtDispatcher::EvtDispatcherOnLoad(reserved);
}

void JNIEvtDispatcher::EvtDispatcherOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // MediaStream - MID's
    jclass stEvNotClass = (jclass) env->NewGlobalRef(env->FindClass(STACK_EVENT_NOTIFIER_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    playFinishedMID = env->GetMethodID(stEvNotClass, EVENT_PLAY_FINISHED_METHOD, EVENT_PLAY_FINISHED_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", EVENT_PLAY_FINISHED_METHOD,
                EVENT_PLAY_FINISHED_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    recordFinishedMID = env->GetMethodID(stEvNotClass, EVENT_RECORD_FINISHED_METHOD,
            EVENT_RECORD_FINISHED_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", EVENT_RECORD_FINISHED_METHOD,
                EVENT_RECORD_FINISHED_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    streamAbandonedMID = env->GetMethodID(stEvNotClass, EVENT_STREAM_ABANDONED_METHOD,
            EVENT_STREAM_ABANDONED_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                EVENT_STREAM_ABANDONED_METHOD, EVENT_STREAM_ABANDONED_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    playFailedMID = env->GetMethodID(stEvNotClass, EVENT_PLAY_FAILED_METHOD, EVENT_PLAY_FAILED_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", EVENT_PLAY_FAILED_METHOD,
                EVENT_PLAY_FAILED_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    recordFailedMID = env->GetMethodID(stEvNotClass, EVENT_RECORD_FAILED_METHOD, EVENT_RECORD_FAILED_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", EVENT_RECORD_FAILED_METHOD,
                EVENT_RECORD_FAILED_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    retFromCallMID = env->GetMethodID(stEvNotClass, EVENT_RETURN_FROM_CALL_METHOD,
            EVENT_RETURN_FROM_CALL_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                EVENT_RETURN_FROM_CALL_METHOD, EVENT_RETURN_FROM_CALL_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    sendTokenMID = env->GetMethodID(stEvNotClass, EVENT_SEND_TOKEN_METHOD, EVENT_SEND_TOKEN_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", EVENT_SEND_TOKEN_METHOD,
                EVENT_SEND_TOKEN_METHOD_SIGNATURE, STACK_EVENT_NOTIFIER_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(stEvNotClass);
}
