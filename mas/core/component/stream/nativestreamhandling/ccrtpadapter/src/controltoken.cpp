/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h>
#include "controltoken.h"
#include "jniutil.h"
#include "jlogger.h"
#include "jnicontroltoken.h"

#include <stdexcept>

using namespace std;
using namespace ost;

const int ControlToken::SILENCE_BETWEEN_TOKENS = -1;
static const char* CLASSNAME = "masjni.ccrtpadapter.ControlToken";

ControlToken::ControlToken(jobject controlToken, JNIEnv* env) :
                mDigit(-1), mDuration(-1), mVolume(-1)
{
    try {
        // getTokenDigit
        mDigit = (int) JNIUtil::callIntMethod(env, controlToken, JNIControlToken::getGetTokenDigitMID());
        JNIUtil::checkException(env, JNIControlToken::GET_TOKEN_DIGIT_METHOD, true);

        // getVolume
        mVolume = (int) JNIUtil::callIntMethod(env, controlToken, JNIControlToken::getGetVolumeMID());
        JNIUtil::checkException(env, JNIControlToken::GET_VOLUME_METHOD, true);

        // getDuration
        mDuration = (int) JNIUtil::callIntMethod(env, controlToken, JNIControlToken::getGetDurationMID());
        JNIUtil::checkException(env, JNIControlToken::GET_DURATION_METHOD, true);

        JLogger::jniLogTrace(env, CLASSNAME, "Created token: %d, Volume=%d, Duration=%d", mDigit, mVolume, mDuration);

        // XXX If volume or duration is negativ, read values
        // from configuration!
        JLogger::jniLogDebug(env, CLASSNAME, "ControlToken - create at %#x", this);
    } catch (exception& e) {
        JLogger::jniLogError(env, "%s", e.what());
        throw;
    }
}

ControlToken::ControlToken(int digit, int volume, int duration, JNIEnv* env) :
                mDigit(digit), mDuration(duration), mVolume(volume)
{
    JLogger::jniLogDebug(env, CLASSNAME, "ControlToken - create at %#x", this);
}

ControlToken::~ControlToken()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~ControlToken - delete at %#x", this);
}

int ControlToken::getVolume() const
{
    return mVolume;
}

int ControlToken::getDuration() const
{
    return mDuration;
}

void ControlToken::setDuration(int duration)
{
    mDuration = duration;
}

int ControlToken::getDigit() const
{
    return mDigit;
}
