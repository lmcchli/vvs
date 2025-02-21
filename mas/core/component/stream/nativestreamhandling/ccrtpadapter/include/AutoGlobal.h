#ifndef AUTOGLOBAL_H_
#define AUTOGLOBAL_H_

#include "jniutil.h"

class AutoGlobal
{
public:
    AutoGlobal(jobject localref, JNIEnv* env);
    ~AutoGlobal();

    jobject& operator*();

private:
    const AutoGlobal& operator=(const AutoGlobal& rhs);
    AutoGlobal(AutoGlobal &rhs);

    jobject mGlobalRef;

    JNIEnv* mEnv;
};

#endif /*AUTOGLOBAL_H_*/
