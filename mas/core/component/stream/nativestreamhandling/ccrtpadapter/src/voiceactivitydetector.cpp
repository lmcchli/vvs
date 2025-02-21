/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <cc++/config.h> // F�r att __EXPORT ska vara definierad (Pointer.h)

#include "voiceactivitydetector.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.VoiceActivityDetector";

VoiceActivityDetector::VoiceActivityDetector()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "VoiceActivityDetector - create at %#x", this);
}

VoiceActivityDetector::~VoiceActivityDetector()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~VoiceActivityDetector - delete at %#x", this);
}

void VoiceActivityDetector::newCNPacket(const ost::AppDataUnit* adu)
{
}

void VoiceActivityDetector::newPacket(const ost::AppDataUnit* adu)
{
}

bool VoiceActivityDetector::isSilence(const ost::AppDataUnit* adu)
{
    return false;
}
