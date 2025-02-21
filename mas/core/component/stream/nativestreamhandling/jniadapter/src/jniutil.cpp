/* * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <stdexcept>
#include <iostream>

#if !defined(WIN32)
#include <sys/mman.h>
#endif

#include <base_include.h>
#include <cc++/exception.h>

#include "jniutil.h"
#include "backtrace.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIUtil";

jclass JNIUtil::logFJLogManagerClass;
jclass JNIUtil::logFJLoggerClass;
jclass JNIUtil::logFJLevelClass;

jmethodID JNIUtil::getLoggerMID;
jmethodID JNIUtil::fatalMID;
jmethodID JNIUtil::errorMID;
jmethodID JNIUtil::warnMID;
jmethodID JNIUtil::infoMID;
jmethodID JNIUtil::debugMID;
jmethodID JNIUtil::traceMID;
jmethodID JNIUtil::isEnabledMID;

jfieldID JNIUtil::offFieldID;
jfieldID JNIUtil::fatalFieldID;
jfieldID JNIUtil::errorFieldID;
jfieldID JNIUtil::warnFieldID;
jfieldID JNIUtil::infoFieldID;
jfieldID JNIUtil::debugFieldID;
jfieldID JNIUtil::traceFieldID;
jfieldID JNIUtil::allFieldID;

void JNI_LoggerOnLoad(void *reserved)
{
    JNIUtil::LoggerOnLoad(reserved);
}

void JNI_LoggerOnUnload(void *reserved)
{
    JNIUtil::LoggerOnUnload(reserved);
}

void JNIUtil::LoggerOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    logFJLoggerClass = (jclass) env->NewGlobalRef(env->FindClass("org/apache/log4j/Logger"));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    logFJLogManagerClass = (jclass) env->NewGlobalRef(env->FindClass("org/apache/log4j/LogManager"));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    getLoggerMID = env->GetStaticMethodID(logFJLogManagerClass, "getLogger",
            "(Ljava/lang/String;)Lorg/apache/log4j/Logger;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    logFJLevelClass = (jclass) env->NewGlobalRef(env->FindClass("org/apache/log4j/Level"));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    offFieldID = env->GetStaticFieldID(logFJLevelClass, "OFF", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    fatalFieldID = env->GetStaticFieldID(logFJLevelClass, "FATAL", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    errorFieldID = env->GetStaticFieldID(logFJLevelClass, "ERROR", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    warnFieldID = env->GetStaticFieldID(logFJLevelClass, "WARN", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    infoFieldID = env->GetStaticFieldID(logFJLevelClass, "INFO", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    debugFieldID = env->GetStaticFieldID(logFJLevelClass, "DEBUG", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    traceFieldID = env->GetStaticFieldID(logFJLevelClass, "TRACE", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    allFieldID = env->GetStaticFieldID(logFJLevelClass, "ALL", "Lorg/apache/log4j/Level;");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    fatalMID = env->GetMethodID(logFJLoggerClass, "fatal", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    errorMID = env->GetMethodID(logFJLoggerClass, "error", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    warnMID = env->GetMethodID(logFJLoggerClass, "warn", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    infoMID = env->GetMethodID(logFJLoggerClass, "info", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    debugMID = env->GetMethodID(logFJLoggerClass, "debug", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    traceMID = env->GetMethodID(logFJLoggerClass, "trace", "(Ljava/lang/Object;)V");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    isEnabledMID = env->GetMethodID(logFJLoggerClass, "isEnabledFor", "(Lorg/apache/log4j/Priority;)Z");
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }
}

void JNIUtil::LoggerOnUnload(void *reserved)
{
    JNIEnv* env = NULL;
    if (!JNIUtil::getJavaEnvironment((void**) &env)) {
        cout << "Cannot unload JNI in CCRTP lib" << endl << flush;
    } else {
        env->DeleteGlobalRef(logFJLoggerClass);
        env->DeleteGlobalRef(logFJLevelClass);
    }

    cout << "JNI unloading - LoggerOnUnload" << endl << flush;

    DetachCurrentThread();
}

// used in the Processor run method or at creation
bool JNIUtil::getJavaEnvironment(void** env, bool toAbort)
{
    if (mEnv.get() == NULL) {
        mEnv.reset(new JNIEnvProxy((JNIEnv *) *env, toAbort));
    }

    *env = mEnv.get()->ptr;

    return *env != NULL;
}

// used in the methods that are running under the Input/Output Processors
JNIEnv* JNIUtil::getJavaEnvironment()
{
    JNIEnvProxy* envProxy = mEnv.get();
    if (envProxy == NULL) {
        cout << "Boost fault - no JNI env found when one should be" << endl << flush;
        BackTrace::dump();
        abort();
    } else if (envProxy->ptr == NULL) {
        cout << "JNIEnv - missing env pointer even though JNIEnvProxy present" << endl << flush;
        BackTrace::dump();
        abort();
    }

    return envProxy->ptr;
}

//used in the mediaobject
JNIEnv* JNIUtil::getJavaEnvironment(JNIEnv* const env)
{
    JNIEnv* retEnv = NULL;
    JNIEnvProxy* envProxy = mEnv.get();
    if ((envProxy == NULL) && (env == NULL)) {
        cout << "Boost fault - no JNI env found when one should be" << endl << flush;
        BackTrace::dump();
        abort();
    } else if ((envProxy != NULL) && (envProxy->ptr == NULL) && (env == NULL)) {
        cout << "JNIEnv - missing env pointer even though JNIEnvProxy present" << endl << flush;
        BackTrace::dump();
        abort();
    } else if (envProxy == NULL) {
        retEnv = env;
    } else {
        retEnv = envProxy->ptr;
    }
    return retEnv;
}

void JNIUtil::DetachCurrentThread()
{
    cout << "~mEnv: " << (void *) mEnv.get() << endl;

    JNIEnvProxy *env = mEnv.release();
    if (env != NULL) {
        delete env;
        env = NULL;
    }
}

jint JNIUtil::PushLocalFrame(JNIEnv* env, int capacity)
{
    jint ret = env->PushLocalFrame(capacity);

    if(ret < 0) {
        cout << CLASSNAME << "PushLocalFrame return " << ret << endl << flush;
    }

    return ret;
}

void JNIUtil::PopLocalFrame(JNIEnv* env)
{
    env->ExceptionClear();
    env->PopLocalFrame(0);
}

jclass JNIUtil::getJavaClass(JNIEnv* env, jobject obj, const char* className, bool shouldThrowJavaExceptionOnFailure)
{
    jclass cls = env->GetObjectClass(obj);
    if (cls == NULL) {
        base::String message("JNIUtil::getJavaMethodID: Failed to lookup class ");
        message += className;
        checkException(env, message, shouldThrowJavaExceptionOnFailure);
    }
    return cls;
}

jmethodID JNIUtil::getJavaMethodID(JNIEnv* env, jclass cls, const char* methodName, const char* signature,
        bool shouldThrowJavaExceptionOnFailure)
{
    jmethodID mid = env->GetMethodID(cls, methodName, signature);
    if (mid == NULL) {
        base::String message("JNIUtil::getJavaMethodID: Failed to find methodid for ");
        message += methodName;
        message += ", with signature ";
        message += signature;
        checkException(env, message, shouldThrowJavaExceptionOnFailure);
    }
    return mid;
}

void JNIUtil::checkException(JNIEnv* env, const char* methodName, bool shouldThrowJavaExceptionOnFailure)
{
    base::String message("JNIUtil::checkException: Failed to call Java method ");
    message += methodName;
    checkException(env, message, shouldThrowJavaExceptionOnFailure);
}

void JNIUtil::checkException(JNIEnv* env, base::String& message, bool shouldThrowJavaExceptionOnFailure)
{
    jthrowable exc = env->ExceptionOccurred();
    if (exc) {
        env->ExceptionDescribe(); // print debug message
        env->ExceptionClear();
        if (shouldThrowJavaExceptionOnFailure) {
            throwStackException(StackException::STACK_EXCEPTION, message.c_str(), env);
        }
        throw ost::Exception(message);
    }
}

void JNIUtil::throwStackException(const StackException& exception, const char* message, JNIEnv* env)
{
    jclass cls = env->FindClass(exception.getName());

    // if cls is NULL, an exception has already been thrown by the jvm.
    if (cls != NULL) {
        env->ThrowNew(cls, message);
        env->DeleteLocalRef(cls);
    }
}

void JNIUtil::throwStackException(const StackException& exception, const char* message, jobject callId, JNIEnv* env)
{
    throwStackException(exception, message, env);

    if (callId != NULL) {
        env->DeleteGlobalRef(callId);
    }
}

void JNIUtil::deleteGlobalRef(JNIEnv* env, jobject ref)
{
    if (ref != NULL) {
        env->DeleteGlobalRef(ref);
    }
}

void JNIUtil::callVoidMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    va_list args;
    va_start(args, methodID);
    env->CallVoidMethodV(obj, methodID, args);
    va_end(args);
}

jobject JNIUtil::callObjectMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    jobject result(0);
    va_list args;
    va_start(args, methodID);
    result = env->CallObjectMethodV(obj, methodID, args);
    va_end(args);
    return result;
}

jboolean JNIUtil::callBooleanMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    jboolean result(0);
    va_list args;
    va_start(args, methodID);
    result = env->CallBooleanMethodV(obj, methodID, args);
    va_end(args);
    return result;
}

jint JNIUtil::callIntMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    jint result(0);
    va_list args;
    va_start(args, methodID);
    result = env->CallIntMethodV(obj, methodID, args);
    va_end(args);
    return result;
}

jlong JNIUtil::callLongMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    jlong result(0);
    va_list args;
    va_start(args, methodID);
    result = env->CallLongMethodV(obj, methodID, args);
    va_end(args);
    return result;
}

jfloat JNIUtil::callFloatMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...)
{
    jfloat result(0);
    va_list args;
    va_start(args, methodID);
    result = env->CallFloatMethodV(obj, methodID, args);
    va_end(args);
    return result;
}

jobject JNIUtil::newObject(JNIEnv* env, jclass clazz, jmethodID methodID, ...)
{
    jobject result(0);
    va_list args;
    va_start(args, methodID);
    result = env->NewObjectV(clazz, methodID, args);
    va_end(args);
    return result;
}

void JNIUtil::deleteLocalRef(JNIEnv* env, jobject obj)
{
    if (obj != NULL) {
        env->DeleteLocalRef(obj);
    }
}

void JNIUtil::deleteLocalRef(JNIEnv* env, jclass obj)
{
    if (obj != NULL) {
        env->DeleteLocalRef(obj);
    }
}
