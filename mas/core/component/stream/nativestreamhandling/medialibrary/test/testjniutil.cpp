/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
 
#include <iostream>
#include <base_std.h>
#include "testjniutil.h"
#include "jniutil.h"

using namespace std;

// Declare a reference to the global jvm instance so the compiler knows
// it is in use, otherwise it might be "optimized away"...
JavaVM* TestJNIUtil::mJvm = NULL;

int TestJNIUtil::setUp(char* vmOptions) {
    int result(0);
    if (mJvm == NULL) {
        JNIEnv *env;
        JavaVMInitArgs vm_args;
        JavaVMOption *options = new JavaVMOption[3];
	int nOptions = 2;
	if(vmOptions != 0) {
        	options[0].optionString = vmOptions;
        	options[1].optionString = "-Djava.compiler=NONE";
        	options[2].optionString = (char*)(new base::String(getenv("DEFAULT_VMOPTIONS")))->c_str();
	} else {
        	options[0].optionString = "-Djava.compiler=NONE";
        	options[1].optionString = (char*)(new base::String(getenv("DEFAULT_VMOPTIONS")))->c_str();
	}
	printf("Options used: %s\n",options[1].optionString);
        vm_args.version = 0x00010002;
        vm_args.options = options;
        vm_args.nOptions = nOptions;
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
