#include "RTPHandlerFactory.h"
#include <cc++/exception.h>

#include "PCMRTPHandler.h"
#include "AMRRTPHandler.h"
#include "AMRWBRTPHandler.h"
#include "H263RTPHandler.h"

#include "sessionsupport.h"
#include "streamcontentinfo.h"
#include "rtppayload.h"
#include "jlogger.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.RTPAudioHandler";

const base::String RTPHandlerFactory::AMR = "amr";
const base::String RTPHandlerFactory::AMRWB = "amr-wb";
const base::String RTPHandlerFactory::PCMU = "pcmu";
const base::String RTPHandlerFactory::PCMA = "pcma";
const base::String RTPHandlerFactory::H263 = "h263";

base::String& RTPHandlerFactory::toLower(base::String& s)
{
    std::transform(s.begin(), s.end(), s.begin(), std::ptr_fun((int (*)(int))std::tolower));

    return s;
}

RTPAudioHandler *RTPHandlerFactory::createAudioHandler(JNIEnv* env, SessionSupport& session)
{
    java::RTPPayload *audioPayload = session.getContentInfo().getAudioPayload();
    if (audioPayload != 0) {
        base::String codec = audioPayload->getCodec();
        toLower(codec);
		JLogger::jniLogDebug(env, CLASSNAME, "createAudioHandler() audio codec: %s", codec.c_str());
        if (codec == PCMU)
            return new PCMRTPHandler(env, session.getAudioSession(), session.getContentInfo(), *audioPayload, false);
        else if (codec == PCMA)
            return new PCMRTPHandler(env, session.getAudioSession(), session.getContentInfo(), *audioPayload, true);
        else if (codec == AMR)
            return new AMRRTPHandler(env, session.getAudioSession(), session.getContentInfo(), *audioPayload);
		else if (codec == AMRWB)
            return new AMRWBRTPHandler(env, session.getAudioSession(), session.getContentInfo(), *audioPayload);
        else {
            JLogger::jniLogError(env, CLASSNAME, "createAudioHandler() Invalid audio codec: %s", codec.c_str());
            throw ost::Exception("Invalid audio codec: " + codec);
        }
    }

    return 0;
}

RTPVideoHandler *RTPHandlerFactory::createVideoHandler(JNIEnv* env, SessionSupport& session)
{
    java::RTPPayload *videoPayload = session.getContentInfo().getVideoPayload();
    if (videoPayload != 0) {
        base::String codec = videoPayload->getCodec();
        toLower(codec);
        if (codec == H263)
            return new H263RTPHandler(env, session.getVideoSession(), session.getContentInfo(), *videoPayload);
        else {
            JLogger::jniLogError(env, CLASSNAME, "Invalid audio codec: %s", codec.c_str());
            throw ost::Exception("Invalid video codec: " + codec);
        }
    }

    return 0;
}
