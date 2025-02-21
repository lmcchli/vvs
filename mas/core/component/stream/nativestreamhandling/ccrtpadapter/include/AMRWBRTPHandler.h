#ifndef _AMRWBRTPHANDLER_H_
#define _AMRWBRTPHANDLER_H_

#include "AMRRTPHandler.h"
#include "jni.h"
#include "mediabuilder.h"
#include "RTPAudioHandler.h"
#include "amrinfo.h"
#include "amrwbinfo.h"



class AMRWBRTPHandler: public AMRRTPHandler
{
public:
    AMRWBRTPHandler(JNIEnv* env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& audioPayload);
    virtual ~AMRWBRTPHandler();

    virtual void validateMediaProperties(MediaParser* mediaParser);
    virtual void initializeBuilderProperties(MediaBuilder* mediaBuilder);

private:
	static const char* CLASSNAME;
};

#endif
