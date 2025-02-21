#include "AutoGlobal.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.AutoGlobal";

AutoGlobal::AutoGlobal(jobject localref, JNIEnv* env)
{
    mGlobalRef = env->NewGlobalRef(localref);
    mEnv = env;
    JLogger::jniLogDebug(env, CLASSNAME, "AutoGlobal - create mGlobalRef at %#x", mGlobalRef);
}

AutoGlobal::~AutoGlobal(void)
{
    if (mGlobalRef != NULL) {
        JNIEnv* env = JNIUtil::getJavaEnvironment(mEnv);
        JLogger::jniLogDebug(env, CLASSNAME, "~AutoGlobal - delete mGlobalRef at %#x", mGlobalRef);
        JNIUtil::deleteGlobalRef(env, mGlobalRef);
    }
}

jobject& AutoGlobal::operator *()
{
    return mGlobalRef;
}
