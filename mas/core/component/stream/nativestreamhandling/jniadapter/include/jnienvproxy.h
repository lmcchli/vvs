/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIENVPROXY_H_
#define JNIENVPROXY_H_

#include "jni.h"
#include "platform.h"
#include "stackexception.h"

#include <base_include.h>
#include <cc++/thread.h>

extern "C" {
const char* getJNIError(jint);
}

class JNIEnvProxy
{
public:
    JNIEnvProxy(JNIEnv*, bool);
    ~JNIEnvProxy();
    /**
     * Initiates this class.
     *
     * @param jvm Reference to the jvm.
     */
    static void init(JavaVM*);

    /**
     * Ataches the current thread from the Java VM.
     */
    JNIEnv* AttachCurrentThread(bool);

    /**
     * Detaches the current thread from the Java VM.
     */
    void DetachCurrentThread();

    /*
     * Get the no of jni env attached
     */
    inline static ost::AtomicCounter getNoJniEnvAttach() {return countNoJniEnvAttach;};
    inline static ost::AtomicCounter getNoJniEnvAlreadyAttached() {return countNoJniEnvAlreadyAttached;};

public:
    JNIEnv* ptr;

private:
    bool alreadyAttached;

    /** Atomic counter - for debugging purposes */
    static ost::AtomicCounter countNoJniEnvAttach;
    static ost::AtomicCounter countNoJniEnvAlreadyAttached;

    /** Reference to the JVM. */
    static JavaVM* mJvm;
};

#endif /*JNIENVPROXY_H_*/
