/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "javamediastream.h"
#include "jnimediastream.h"
#include "jlogger.h"
#include "jniutil.h"

using namespace std;

static const char* CLASSNAME = "ccrtpadapter.JavaMediaStream";

const char* REQUEST_IFRAME_METHOD = "sendPictureFastUpdateRequest";
const char* REQUEST_IFRAME_METHOD_SIGNATURE = "()V";

const char* GET_CALL_SESSION_ID_METHOD = "getCallSessionId";
const char* GET_CALL_SESSION_ID_METHOD_SIGNATURE = "()Ljava/lang/String;";

const char* MEDIA_STREAM_JAVA_CLASSNAME = "IInboundMediaStream";

JavaMediaStream* JavaMediaStream::getInbound(jobject streamInstance, JNIEnv* env)
{
    return new JavaMediaStream(streamInstance, env, true);
}

JavaMediaStream* JavaMediaStream::getOutbound(jobject streamInstance, JNIEnv* env)
{
    return new JavaMediaStream(streamInstance, env, false);
}

JavaMediaStream::JavaMediaStream(jobject streamInstance, JNIEnv* env, bool isInbound)
{
    mEnv = env;

    if (streamInstance != 0) {
        mStreamInstance = env->NewGlobalRef(streamInstance);
    }

    mIsInbound = isInbound;

    JLogger::jniLogDebug(env, CLASSNAME, "JavaMediaStream - created at %#x", this);
}

JavaMediaStream::~JavaMediaStream()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment(mEnv);

    JNIUtil::deleteGlobalRef(env, mStreamInstance);
    mStreamInstance = NULL;

    JLogger::jniLogDebug(env, CLASSNAME, "~JavaMediaStream - deleted at %#x", this);
}

void JavaMediaStream::sendPictureFastUpdateRequest(JNIEnv* env)
{
    if (!mIsInbound) {
        return;
    }

    try {
        JNIUtil::callVoidMethod(env, mStreamInstance, JNIMediaStream::getIframeMID());

        JNIUtil::checkException(env, REQUEST_IFRAME_METHOD, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }
}

void JavaMediaStream::releasePorts(JNIEnv* env)
{
    try {
        JNIUtil::callVoidMethod(env, mStreamInstance, JNIMediaStream::getReleasePortsMID());

        JNIUtil::checkException(env, JNIMediaStream::RELEASE_PORTS_METHOD, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send call relasePorts(): %s", e.what());
    }
}

base::String JavaMediaStream::getCallSessionId(JNIEnv* env)
{
    base::String callSessionId = "";

    try {
        // Call	getCallSessionId
        jstring value((jstring) JNIUtil::callObjectMethod(env, mStreamInstance, JNIMediaStream::getCallSessionIdMID()));
        JNIUtil::checkException(env, GET_CALL_SESSION_ID_METHOD, true);

        if (value != NULL) {
            const char* valueStr(env->GetStringUTFChars(value, 0));
            callSessionId = valueStr;
            env->ReleaseStringUTFChars(value, valueStr);
        }
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send call getCallSessionId(): %s", e.what());
    }

    return callSessionId;
}

void JavaMediaStream::updateJniEnv(JNIEnv* env)
{
    mEnv = env;
}
