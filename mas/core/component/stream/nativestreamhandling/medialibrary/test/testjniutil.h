/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef TESTJNIUTIL_H_
#define TESTJNIUTIL_H_

#include <jni.h>

/**
 * Utility class for JNI-related functionality.
 * 
 * @author Jörgen Terner
 */
class TestJNIUtil {
private:
    /** Reference to the JVM. */
    static JavaVM* mJvm;
    
public:
    /** 
     * Initiates this class. Must be called before any other methods
     * are used in this class.
     * 
     * @param vmOptions Options used when starting the Java VM. Defaults
     *                  to <code>null</code>.
     * 
     * @return A value < 0 if initiation failed.
     */
    static int setUp(char* vmOptions=0);

    /** Destroys the JavaVM.*/
    static void tearDown();
};

#endif /*TESTJNIUTIL_H_*/
