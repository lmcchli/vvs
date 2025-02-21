#ifndef _RTP_HANDLERFACTORY_H_
#define _RTP_HANDLERFACTORY_H_

#include "jni.h"

#include <base_include.h>
//#include <base_std.h>

class SessionSupport;
class RTPAudioHandler;
class RTPVideoHandler;

class RTPHandlerFactory
{
public:
    static RTPAudioHandler *createAudioHandler(JNIEnv* env, SessionSupport& session);
    static RTPVideoHandler *createVideoHandler(JNIEnv* env, SessionSupport& session);
protected:
    static const base::String AMR;
	static const base::String AMRWB;
    static const base::String PCMU;
    static const base::String PCMA;
    static const base::String H263;

    static base::String& toLower(base::String& s);
};

#endif
