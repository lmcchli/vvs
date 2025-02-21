#ifndef _Callback_h_
#define _Callback_h_

#include "jni.h"

// Please note that this class i "morrored" in the Java domain
// so you must ensure consistency.

class Callback
{
public:
    // TODO: make enum instead ...
    // Command types ...
    static const long PLAY_COMMAND;
    static const long RECORD_COMMAND;
    static const long JOIN_COMMAND;
    static const long UNJOIN_COMMAND;
    static const long CREATE_COMMAND;
    static const long DELETE_COMMAND;
    static const long DTMF_COMMAND;
    // Status types
    // Success - 2xx
    static const long OK;
    static const long OK_STOPPED;
    static const long OK_CANCELLED;
    static const long OK_JOINED;
    static const long OK_DELETED;
    static const long OK_MAX_DURATION;
    static const long OK_ABANDONED;
    // Failed - 4xx
    static const long FAILED;
    static const long FAILED_EXCEPTION;
    static const long FAILED_MIN_DURATION;

public:
    Callback(JNIEnv* env, unsigned requestId, unsigned command, unsigned status, long data = 0L);
    ~Callback();

    void getAsLong(long* array);
    inline void resetJniEnv(JNIEnv* env) {mEnv = env; };

public:
    long requestId;
    long command;
    long status;
    long data;

private:
    JNIEnv* mEnv;
};

#endif
