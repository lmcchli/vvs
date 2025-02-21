#include "AMRWBRTPHandler.h"
#include "jlogger.h"
#include "jniutil.h"
#include "audiomediadata.h"
#include "rtppayload.h"

#include "amrwbparser.h"
#include "amrwbbuilder.h"

const char* AMRWBRTPHandler::CLASSNAME = "masjni.ccrtpadapter.AMRWBRTPHandler";

/* Basically this class is the same AMRTPHandler
 * The differnnce being it uses amrwbInfo to know the size
 * and other infor used to encode the rtp packets.
 * The builder is also different so the validate and 
 * initialize become slighly different
 * 
 * Because the base class gets the info from amrInfo changing
 * the AmrInfo to an AmrwbInfo changes the behaviour to WB
 * which is for all intents and purposes a larger version of
 * amr-nb.
 * */
AMRWBRTPHandler::AMRWBRTPHandler(JNIEnv* env, StreamRTPSession& audioSession,
        java::StreamContentInfo& contentInfo, java::RTPPayload& audioPayload) :
        AMRRTPHandler(env, audioSession, contentInfo, audioPayload,new AmrwbInfo(env))
{
    JLogger::jniLogDebug(env, CLASSNAME, "AMRWBRTPHandler - create at %#x", this);
}


void AMRWBRTPHandler::validateMediaProperties(MediaParser *mediaParser)
{
    JNIEnv* env = mediaParser->getMediaObjectJniEnv();
    JLogger::jniLogTrace(env, CLASSNAME, "ValidateMediaProperties");

    if (mediaParser->getAudioCodec() == "AMR-WB") {
#ifdef WIN32
        AmrwbParser *p = (AmrParser*)mediaParser;
#else
        AmrwbParser *p = dynamic_cast<AmrwbParser*>(mediaParser);
        if (p == NULL) {
			JLogger::jniLogWarn(env, CLASSNAME, "unable to cast %s mediaParser to amrwbParser",
                            mediaParser->getAudioCodec().c_str());		
            throw ost::Exception("Wrong audio codec, expected AMR-WB!");
        }
#endif
    } else {
		JLogger::jniLogWarn(env, CLASSNAME, "Wrong codec, expected AMR-WB but got %s",
                            mediaParser->getAudioCodec().c_str());
        throw ost::Exception("Wrong audio codec, expected AMR-WB!");
    }
}

void AMRWBRTPHandler::initializeBuilderProperties(MediaBuilder* mediaBuilder)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME,
            "initializeBuilderProperties, recorded mode-set: %d",
            mRecordedModeSet);

#ifdef WIN32
    AmrwbBuilder *builder = (AmrwbBuilder*)mediaBuilder;
#else
    AmrwbBuilder *builder = dynamic_cast<AmrwbBuilder*>(mediaBuilder);
    if (builder == 0) {
        throw ost::Exception("Wrong audio builder, expected AmrwbBuilder!");
    }
#endif
    builder->setModeSet(mRecordedModeSet);
}

AMRWBRTPHandler::~AMRWBRTPHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME,
            "~AMRWBRTPHandler - delete at %#x", this);
}
