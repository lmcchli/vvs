#include "Callback.h"
#include "jlogger.h"
#include "jniutil.h"

// Command types ...
const long Callback::PLAY_COMMAND = 1;
const long Callback::RECORD_COMMAND = 2;
const long Callback::JOIN_COMMAND = 3;
const long Callback::UNJOIN_COMMAND = 4;
const long Callback::CREATE_COMMAND = 5;
const long Callback::DELETE_COMMAND = 6;
const long Callback::DTMF_COMMAND = 7;
// Status types
// Success - 2xx
const long Callback::OK = 200;
const long Callback::OK_STOPPED = 201;
const long Callback::OK_CANCELLED = 202;
const long Callback::OK_JOINED = 203;
const long Callback::OK_DELETED = 204;
const long Callback::OK_MAX_DURATION = 205;
const long Callback::OK_ABANDONED = 206;
// Failed - 4xx
const long Callback::FAILED = 400;
const long Callback::FAILED_EXCEPTION = 401;
const long Callback::FAILED_MIN_DURATION = 402;

static const char* CLASSNAME = "masjni.ccrtpadapter.Callback";

Callback::Callback(JNIEnv* env, unsigned rid, unsigned cmd, unsigned stat, long dta) :
        requestId(rid), command(cmd), status(stat), data(dta), mEnv(env)
{
    JLogger::jniLogDebug(env, CLASSNAME, "Callback - create at %#x", this);
}

Callback::~Callback()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~Callback - delete at %#x", this);
}

void Callback::getAsLong(long* array)
{
    array[0] = requestId << 16 | command << 12 | status;
    array[1] = data;
}
