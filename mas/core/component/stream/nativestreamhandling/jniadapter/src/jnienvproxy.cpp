/* * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include "jnienvproxy.h"

#include <stdexcept>
#include <iostream>

#if !defined(WIN32)
#include <sys/mman.h>
#endif

#include <base_include.h>
#include <cc++/exception.h>
#include <execinfo.h>
#include <stdlib.h>

#include "backtrace.h"

using namespace std;
// Declare a reference to the global jvm instance so the compiler knows
// it is in use, otherwise it might be "optimized away"...
JavaVM* JNIEnvProxy::mJvm = NULL;

// Used for debugging - check no of env attachments.
ost::AtomicCounter JNIEnvProxy::countNoJniEnvAttach;
ost::AtomicCounter JNIEnvProxy::countNoJniEnvAlreadyAttached;

JNIEnvProxy::JNIEnvProxy(JNIEnv *env, bool toAbort = false)
{
    if (env != NULL) {
        //cout << "Create JNIEnvProxy - env null " << pthread_self() << endl << flush;
        ptr = env;
        alreadyAttached = true;
    } else {
        //cout << "Create JNIEnvProxy - env non null " << pthread_self() << endl << flush;
        alreadyAttached = false;
        ptr = AttachCurrentThread(toAbort);
    }
}

JNIEnvProxy::~JNIEnvProxy()
{
    //cout << "Delete JNIEnvProxy " << pthread_self() << endl << flush;
    DetachCurrentThread();
}

void JNIEnvProxy::init(JavaVM* jvm)
{
    mJvm = jvm;
}

JNIEnv* JNIEnvProxy::AttachCurrentThread(bool toAbort = false)
{
    JNIEnv* environment(NULL);
    jint result(mJvm->GetEnv((void**) &environment, JNI_VERSION_1_6));

    switch (result)
    {
    case JNI_OK:
        alreadyAttached = true;
        //cout << "JNI_OK" << endl << flush;
        countNoJniEnvAlreadyAttached = countNoJniEnvAlreadyAttached + 1;
        break;

    case JNI_EDETACHED:
        //BackTrace::dump();
        //cout << "JNI_EDETACHED" << endl << flush;
        if ((result = mJvm->AttachCurrentThread((void**) &environment, NULL)) != JNI_OK) {
            cout << "Warning: JNIEnvProxy::AttachCurrentThread() AttachCurrentThread(): " << getJNIError(result) << endl
                    << flush;
        }
        countNoJniEnvAttach = countNoJniEnvAttach + 1;
        break;

    default:
        cout << "Warning: JNIEnvProxy::AttachCurrentThread() GetEnv(): " << getJNIError(result) << endl << flush;
        break;
    }

    if (environment == NULL) {
        // we cannot really continue if the VM environment is no longer accessible!
        cout << "ERROR: JNIEnvProxy::AttachCurrentThread() JNIEnv is NULL!" << endl << flush;
        BackTrace::dump();
        if (toAbort)
            abort();
    }

    return environment;
}

void JNIEnvProxy::DetachCurrentThread()
{
    if (!alreadyAttached) {
        //cout << "Detaching " << pthread_self() << endl << flush;
        mJvm->DetachCurrentThread();

        countNoJniEnvAttach = countNoJniEnvAttach - 1;
    } else {
        countNoJniEnvAlreadyAttached = countNoJniEnvAlreadyAttached - 1;
    }
}

const char* getJNIError(jint error)
{
    static const char jniOk[] = "JNI_OK";
    static const char jniErr[] = "JNI_ERR";
    static const char jniEDetached[] = "JNI_EDETACHED";
    static const char jniEVersion[] = "JNI_EVERSION";
    static const char jniENomem[] = "JNI_ENOMEM";
    static const char jniEExist[] = "JNI_EXIST";
    static const char jniEInval[] = "JNI_EINVAL";
    static const char unknown[] = "UNKNOWN";

    switch (error)
    {
    case JNI_OK:
        return jniOk;

    case JNI_ERR:
        return jniErr;

    case JNI_EDETACHED:
        return jniEDetached;

    case JNI_EVERSION:
        return jniEVersion;

    case JNI_ENOMEM:
        return jniENomem;

    case JNI_EEXIST:
        return jniEExist;

    case JNI_EINVAL:
        return jniEInval;

    default:
        break;
    }
    return unknown;
}
