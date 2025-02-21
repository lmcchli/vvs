#include <amrwbparser.h>
#include <amrinfo.h>
#include <amrwbinfo.h>

#include "jlogger.h"
#include "jniutil.h"

const char* AmrwbParser::AMRWB_CLASSNAME = "masjni.medialibrary.AmrwbParser";

AmrwbParser::AmrwbParser(java::MediaObject* mediaObject) :
       AmrParser(mediaObject,(AmrInfo *)new AmrwbInfo(mediaObject->getJniEnv()))
{
	JLogger::jniLogDebug(m_env, AMRWB_CLASSNAME, "AmrwbParser::AmrwbParser() m_movInfo(AmrwbInfo) - create at %#x", m_movInfo);

    JLogger::jniLogDebug(m_env, AMRWB_CLASSNAME, "AmrwbParser - create at %#x", this);
}

AmrwbParser::~AmrwbParser()
{
    JLogger::jniLogDebug(m_env, AMRWB_CLASSNAME, "~AmrwbParser - delete at %#x", this);
}






