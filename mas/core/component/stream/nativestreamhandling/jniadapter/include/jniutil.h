/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIUTIL_H_
#define JNIUTIL_H_

#include "jni.h"
#include "platform.h"
#include "stackexception.h"
#include "jnienvproxy.h"

#include <base_include.h>
#include <boost/thread/tss.hpp>

static boost::thread_specific_ptr<JNIEnvProxy> mEnv;

extern "C" {
extern void JNI_LoggerOnLoad(void*);
extern void JNI_LoggerOnUnload(void*);
}

/**
 * Utility class for JNI-related functionality.
 */
class MEDIALIB_CLASS_EXPORT JNIUtil
{
public:
    static jclass logFJLogManagerClass;
    static jclass logFJLoggerClass;
    static jclass logFJLevelClass;

    static jmethodID getLoggerMID;
    static jmethodID fatalMID;
    static jmethodID errorMID;
    static jmethodID warnMID;
    static jmethodID infoMID;
    static jmethodID debugMID;
    static jmethodID traceMID;
    static jmethodID isEnabledMID;

    static jfieldID offFieldID;
    static jfieldID fatalFieldID;
    static jfieldID errorFieldID;
    static jfieldID warnFieldID;
    static jfieldID infoFieldID;
    static jfieldID debugFieldID;
    static jfieldID traceFieldID;
    static jfieldID allFieldID;

    static void LoggerOnLoad(void *reserved);
    static void LoggerOnUnload(void *reserved);

    /**
     * Gets a reference to Java environment, either by asking
     * for it from the JavaVM reference if the current thread is
     * already attached to the jvm, or by attaching the current
     * thread to the jvm.
     * <p>
     * Note that this method cannot throw new exceptions
     * back to the Java-space. If getting the environment fails,
     * there is no way back...
     * 
     * @param env             Storage place for environment reference.
     * @param toAbort         By default false. But when in constructours/destructors
     *                        toAbort shall be true to abort the swft.
     * 
     * @return <code>true</code> if the operation succeeded, <code>false</code>
     *         if getting the environment failed.
     */
    static bool getJavaEnvironment(void** env, bool toAbort = false);
    static JNIEnv* getJavaEnvironment();
    static JNIEnv* getJavaEnvironment(JNIEnv* const env);
    static void DetachCurrentThread();

    static void PopLocalFrame(JNIEnv* env);
    static jint PushLocalFrame(JNIEnv* env, int capacity);

    /**
     * Gets a reference to the Java-class for the given jobject.
     * 
     * @param env       Reference to Java environment.
     * @param obj       Java object.
     * @param className Name of the objects class. Used in error messages.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a Java
     *                  exception is thrown on failure as well as a 
     *                  runtime_error. If <code>false</code>, only a 
     *                  runtime_error is thrown. May only be <code>true</code>
     *                  for a synchronous call from Java, otherwise there is
     *                  no one there to receive the exception.
     * 
     * @return The class of the given jobject.
     * 
     * @throws runtime_error if the class could not be retrieved.
     */
    static jclass getJavaClass(JNIEnv* env, jobject obj, const char* className, bool shouldThrowJavaExceptionOnFailure);

    /**
     * Gets a reference to the Java method specified by <code>methodName</code>
     * and <code>signature</code>.
     * 
     * @param env        Reference to Java environment.
     * @param cls        Class that contains the method.
     * @param methodName Name of method.
     * @param signature  Method signature.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a Java
     *                  exception is thrown on failure as well as a 
     *                  runtime_error. If <code>false</code>, only a 
     *                  runtime_error is thrown. May only be <code>true</code>
     *                  for a synchronous call from Java, otherwise there is
     *                  no one there to receive the exception.
     * 
     * @return Reference to the Java method.
     * 
     * @throws exception If the method reference could not be retrieved.
     */
    //static jmethodID JNIUtil::getJavaMethodID(JNIEnv* env, jclass cls, 
    static jmethodID getJavaMethodID(JNIEnv* env, jclass cls, const char* methodName, const char* signature,
            bool shouldThrowJavaExceptionOnFailure);

    /**
     * Checks if an exception is pending in the Java environment.
     * 
     * @param env        Reference to Java environment.
     * @param methodName Name of the last called method. Used as information
     *                   in exception message.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a Java
     *                  exception is thrown on failure as well as a 
     *                  runtime_error. If <code>false</code>, only a 
     *                  runtime_error is thrown. May only be <code>true</code>
     *                  for a synchronous call from Java, otherwise there is
     *                  no one there to receive the exception.
     * 
     * @throws exception If an exception was pending in the Java environment.
     */
    static void checkException(JNIEnv* env, const char* methodName, bool shouldThrowJavaExceptionOnFailure);

    /**
     * Checks if an exception is pending in the Java environment.
     * 
     * @param env     Reference to Java environment.
     * @param message Exception message.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a Java
     *                  exception is thrown on failure as well as a 
     *                  runtime_error. If <code>false</code>, only a 
     *                  runtime_error is thrown. May only be <code>true</code>
     *                  for a synchronous call from Java, otherwise there is
     *                  no one there to receive the exception.
     * 
     * @throws exception If an exception was pending in the Java environment.
     */
    static void checkException(JNIEnv* env, base::String& message, bool shouldThrowJavaExceptionOnFailure);

    /**
     * Throws a new Java-exception back to Java-space.
     * 
     * @param exception Type of exception that shall be thrown.
     * @param message   The exception message.
     * @param env       Valid reference to Java environment.
     */
    static void throwStackException(const StackException& exception, const char* message, JNIEnv* env);

    /**
     * Throws a new Java-exception back to Java-space and deletes the global
     * reference to <code>callId</code>.
     *
     * @param exception Type of exception that shall be thrown.
     * @param message   The exception message.
     * @param callId    Global reference to a callId-object.
     */
    static void throwStackException(const StackException& exception, const char* message, jobject callId, JNIEnv* env);

    /**
     * Attaches the current thread to the jvm if necessary and deletes the
     * given global reference.
     * 
     * @param ref Global reference to Java object.
     */
    static void deleteGlobalRef(JNIEnv* env, jobject ref);
    static void callVoidMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jobject callObjectMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jboolean callBooleanMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jint callIntMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jlong callLongMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jfloat callFloatMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...);
    static jobject newObject(JNIEnv* env, jclass clazz, jmethodID methodID, ...);
    static void deleteLocalRef(JNIEnv* env, jobject obj);
    static void deleteLocalRef(JNIEnv* env, jclass clazz);
};

#endif /*JNIUTI_H_*/
