#ifndef AmrwbInfo_h
#define AmrwbInfo_h

#include <vector>

#include <amrinfo.h>
#include <mediainfo.h>
#include <amrtrackinfo.h>
#include <platform.h>
#include "jniutil.h"

namespace quicktime {
class MoovAtom;
};


class MEDIALIB_CLASS_EXPORT AmrwbInfo: public AmrInfo
{
public:
    AmrwbInfo(JNIEnv* env);
    ~AmrwbInfo();
	//returns true if this represents amr-wb info false if nb.
	virtual bool isWideBand();
	virtual const unsigned get_sid(); //returns the value of sid for this type of amr.
	virtual const unsigned get_MaxFrameSize(); //returns the largest rtp framesize for this type of amr.
		/*
	 * returns the number of samples per 20ms frame used for calculating time delta in samples for rtp.
	 * 
	 * see rfc3267 4.1:
		The duration of one speech frame-block is 20 ms for both AMR and
		AMR-WB.  For AMR, the sampling frequency is 8 kHz, corresponding to
		160 encoded speech samples per frame from each channel.  For AMR-WB,
		the sampling frequency is 16 kHz, corresponding to 320 samples per
		frame from each channel.  Thus, the timestamp is increased by 160 for
		AMR and 320 for AMR-WB for each consecutive frame-block.
	*/ 
	virtual const unsigned get_samplesPer20msFrame(); 
protected:
	static const unsigned FT_SID_WB;
	static const unsigned MAX_FRAME_SIZE_WB;
	static const unsigned SAMPLES_PER_FRAME_20MS_WB;
	
private:
    static const unsigned wbModeSize[16];	
	static const char* CLASSNAME;
};

#endif
