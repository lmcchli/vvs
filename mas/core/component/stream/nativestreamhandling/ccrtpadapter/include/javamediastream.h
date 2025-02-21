/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JAVAMEDIASTREAM_H_
#define JAVAMEDIASTREAM_H_

#include "jni.h"
#include <base_std.h>
#include <base_include.h>

/**
 * Wrapper class for a Java Media Stream that hides the JNI details.
 * 
 * @author Jorgen Terner
 */
class JavaMediaStream
{
private:
    JavaMediaStream(JavaMediaStream& rhs);
    JavaMediaStream& operator=(const JavaMediaStream& rhs);

    /** Global reference to Java instance. */
    jobject mStreamInstance;

    /** true if inbound stream, false if outbound. */
    bool mIsInbound;

    JNIEnv* mEnv;

private:
    /**
     * Constructor.
     * 
     * @param streamInstance Java Media Stream instance.
     * @param env            Reference to Java environment.
     * @param isInbound      <code>true</code> if <code>streamInstance</code>
     *                       is an inobund stream, <code>false</code> if
     *                       it is an outbound stream.
     */
    JavaMediaStream(jobject streamInstance, JNIEnv* env, bool isInbound);

public:
    /**
     * Creates an instance that wraps the given inbound Java stream.
     * 
     * @param streamInstance Java Media Stream instance.
     * @param env            Reference to Java environment.
     */
    static JavaMediaStream* getInbound(jobject streamInstance, JNIEnv* env);

    /**
     * Creates an instance that wraps the given outbound Java stream.
     * 
     * @param streamInstance Java Media Stream instance.
     * @param env            Reference to Java environment.
     */
    static JavaMediaStream* getOutbound(jobject streamInstance, JNIEnv* env);

    /**
     * Destructor.
     */
    ~JavaMediaStream();

    /**
     * Cleans up JNI-related resources. Call this method if possible before
     * deleting an instance to save the destructor from the work of attaching
     * itself to Java before cleaning up.
     * 
     * @param env Reference to Java environment used to clean up resources.
     */
    void cleanUp(JNIEnv* env);

    /**
     * Requests an I-frame.
     */
    void sendPictureFastUpdateRequest(JNIEnv* env);

    /**
     * Call when local ports can be reused.
     *
     */
    void releasePorts(JNIEnv* env);

    base::String getCallSessionId(JNIEnv* env);

    /**
     * @return The wrapped Java instance.
     */
    jobject getJavaInstance();

    /*
     * updates the media stream with the JNIenv
     */
    void updateJniEnv(JNIEnv* env);
};

inline jobject JavaMediaStream::getJavaInstance()
{
    return mStreamInstance;
}

#endif /*JAVAMEDIASTREAM_H_*/
