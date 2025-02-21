/*
 *
 *  Allows JNI code to use java util logging to log to APP server logging.
 *
 *  @author lmcdasi 2012-07-09
 */

#include "jniutil.h"
#include "jlogger.h"

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>
#include <errno.h>
#include <sys/poll.h>
#include <iostream>

#define STRING_TO_LOG_LENGTH 4096

jboolean JLogger::jniLogIsEnabled(JNIEnv* const env, const char *jniLoggerName, jfieldID levelID)
{
    jboolean ret = false;
	
	if (jniLoggerName == NULL ) {
		printf("JLogger::jniLogIsEnabled, ERROR LoggerName not defined!");
		return false;
	}
	
	if (env == NULL) {
		printf("JLogger::jniLogIsEnabled, ERROR bad env from, %s", jniLoggerName );
		return false;
	} else {
		//test if env is actually set to a JNIENV
		//if it was not initialized can cause MAS to crash.
		JNIEnv* test = dynamic_cast<JNIEnv*> (env);
		if (test == NULL) {
			printf("JLogger::jniLogIsEnabled, ERROR bad env type from, %s", jniLoggerName );
			return false;
		}
	}

    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    jobject level = env->GetStaticObjectField(JNIUtil::logFJLevelClass, levelID);

    ret = env->CallBooleanMethod(loggerMethod, JNIUtil::isEnabledMID, level);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    return ret;
}

void JLogger::jniLogFatal(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 };

    if (env == NULL) {
        abort();
    }

    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::fatalFieldID)) {
        return;
    }
	//std::cout << fmt << "\n"; //FIXME
    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);

    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::fatalMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}

void JLogger::jniLogError(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 }; 

    if (env == NULL) {
        abort();
    }

    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::errorFieldID)) {
        return;
    }
	//std::cout << fmt << "\n";; //FIXME
    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);

    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::errorMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}

void JLogger::jniLogWarn(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 };

    if (env == NULL) {
        abort();
    }

    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::warnFieldID)) {
        return;
    }
	//std::cout << fmt << "\n";; //FIXME
    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);

    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::warnMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}

void JLogger::jniLogInfo(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 };

    if (env == NULL) {
        abort();
    }
    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::infoFieldID)) {
        return;
    }

    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);
	//std::cout << fmt << "\n";; //FIXME
    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::infoMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}

void JLogger::jniLogTrace(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 };

    if (env == NULL) {
        abort();
    }

    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::traceFieldID)) {
        return;
    }
	//std::cout << fmt << "\n";; //FIXME
    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);

    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::traceMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}

void JLogger::jniLogDebug(JNIEnv* const env, const char *jniLoggerName, char const* fmt, ...)
{
    va_list ap;
    char strToLog[STRING_TO_LOG_LENGTH] = { 0 };

    if (env == NULL) {
        abort();
    }

    if (!jniLogIsEnabled(env, jniLoggerName, JNIUtil::debugFieldID)) {
        return;
    }

	//std::cout << fmt << "\n";; //FIXME
    va_start(ap, fmt);
    (void) vsnprintf(strToLog, STRING_TO_LOG_LENGTH, fmt, ap);
    strToLog[sizeof(strToLog) - 1] = 0; /* (just in case) */
    va_end(ap);
    jstring loggerNameTxt = env->NewStringUTF(jniLoggerName);
    jobject loggerMethod = env->CallStaticObjectMethod(JNIUtil::logFJLogManagerClass, JNIUtil::getLoggerMID,
            loggerNameTxt);
    env->DeleteLocalRef(loggerNameTxt);

    // Convert the C++ char array to Java String
    jstring txtToLog = env->NewStringUTF(strToLog);

    env->CallVoidMethod(loggerMethod, JNIUtil::debugMID, txtToLog);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();
    }

    env->DeleteLocalRef(txtToLog);
}
