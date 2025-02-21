#ifndef _AmrwbBuilder_h_
#define _AmrwbBuilder_h_

#include <amrbuilder.h> //inherits
#include <mediabuilder.h>

#include <platform.h>
#include <memory>

#include "jni.h" //logging env

/**
 * AmrwbBuilder holds the information needed for 3gp file assembly and
 * functions to encode the file..
 * This class is used when creating a 3gp for wb file. Usage:
 * 1) Video frames are added as RTP packages (one call for each frame). 
 * 2) The audio is added once as one list of audio chunks. 
 * 3) Once all frames and audio is added the 3gpp data is stored.
 * 
 * Essentially the data is different from the base class such
 * as timing etc.  Most of the work is done by AmrBuilder.
 */
class MEDIALIB_CLASS_EXPORT AmrwbBuilder: public AmrBuilder
{
public:
    /**
     * Default constructor.
     */
    AmrwbBuilder(JNIEnv* env);

    /* Destructor */
    virtual ~AmrwbBuilder();

    /* Doc in baseclass. */
    virtual void setAudioCodec(const base::String& codecName);
	
	static const unsigned AMRWB_AUDIO_TIME_SCALE=16000; //the sample time for amr-wb - 16khz in hz.
	static const char* WB_CLASSNAME;
	static const unsigned SAMPLES_PER_FRAME_20MS_WB=320; //number of rtp samples that make up 20ms time frame.
};
#endif
