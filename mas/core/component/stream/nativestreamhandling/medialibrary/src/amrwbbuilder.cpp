#include "amrwbbuilder.h"

#include "AtomName.h"
#include "jlogger.h"
#include "jniutil.h"

const char* AmrwbBuilder::WB_CLASSNAME = "masjni.medialibrary.AmrwbBuilder";

AmrwbBuilder::AmrwbBuilder(JNIEnv* env) : 
	AmrBuilder(	env,
				quicktime::SAWB, //amr-wb codec
				AMRWB_AUDIO_TIME_SCALE, //sample rate
				AmrBuilder::H2623_TIME_SCALE, 
				SAMPLES_PER_FRAME_20MS_WB,
				quicktime::AmrSpecificAtom::modeSetWB //default wide band mode (all).
			)
{
    JLogger::jniLogDebug(m_env, WB_CLASSNAME, "AmrwbBuilder - create at %#x", this);
}

AmrwbBuilder::~AmrwbBuilder()
{
    JLogger::jniLogDebug(m_env, WB_CLASSNAME, "~AmrwbBuilder - delete at %#x", this);
}

void AmrwbBuilder::setAudioCodec(const base::String& codecName)
{
    // The codec is hard coded.
    //       for each new codec we need to have a Sample Description Atom
    if (codecName != "AMR-WB" && codecName != "amr-wb") {
        JLogger::jniLogWarn(m_env, WB_CLASSNAME, "Attempting to set wrong audio codec: %s", codecName.c_str());
        throw base::String("Bad audio codec name [" + codecName + "]");
    }
}