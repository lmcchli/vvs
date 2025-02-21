/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
 
#include <iostream>

#include "TestJNIUtil.h"
#include "jniutil.h"

using namespace std;

// Declare a reference to the global jvm instance so the compiler knows
// it is in use, otherwise it might be "optimized away"...
JavaVM* TestJNIUtil::mJvm = NULL;

char* TestJNIUtil::DEFAULT_VMOPTIONS="-Djava.class.path=../../classes:/vobs/ipms/mas/lib/activation.jar:/vobs/ipms/mas/lib/log4j-1.2.9.jar";

int TestJNIUtil::setUp(char* vmOptions) {
    return 1;
    int result(0);
    if (mJvm == NULL) {
        JNIEnv *env;
        JavaVMInitArgs vm_args;
        JavaVMOption options[2];
        options[0].optionString = vmOptions;
        options[1].optionString = "-Djava.compiler=NONE";
        vm_args.version = 0x00010002;
        vm_args.options = options;
        vm_args.nOptions = 2;
        vm_args.ignoreUnrecognized = JNI_FALSE;
        /* Create the Java VM */
        result = JNI_CreateJavaVM(&mJvm, (void**)&env, &vm_args);
        if (result >= 0) {
            JNIUtil::init(mJvm);
        }
    }
    return result;
}

void TestJNIUtil::tearDown() {
    //if (mJvm != NULL) {
    //    mJvm->DestroyJavaVM();
    //    mJvm = NULL;
    //}
}
