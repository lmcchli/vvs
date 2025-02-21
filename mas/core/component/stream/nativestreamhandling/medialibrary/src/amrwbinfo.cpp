#include "amrwbinfo.h"

#include "MoovAtom.h"
#include "TrakAtom.h"
#include "jlogger.h"
#include "jniutil.h"

using namespace quicktime;

const char* AmrwbInfo::CLASSNAME = "masjni.medialibrary.AmrwbInfo";

/* TS 126 201 - section 4.2.2 table 2
 *  * values are devived from the number of bits
 * rounded up to the nearest byte as the
 * info is stored as bytes on disk and in mem.
*/
const unsigned AmrwbInfo::wbModeSize[] = { 
			17, //  #0: 132 bits/8 (bytes)=16.5  6.60
            23, //  #1:  177/8=22.125 8.85
            32, //  #2:  253/8=31.625 12.65
            36, //  #3:  285/8=35.625 14.25
            40, //  #4:  317/8=39.625 15.85
            46, //  #5:  365/8=45.625 18.25
            50, //  #6:  397/8=49.626 19.85
            58, //  #7:  461/8=57.625 23.05
			60, //  #8:  477/8=59.625 23.85
            5,  //  #9:  40/8=5 GSM-EFR SID (confort noise)
			0, 0, 0, // #10-13 future use
			0, // #14 speach lost
			0 // #15 no-data
        };
		
const unsigned AmrwbInfo::FT_SID_WB = 0x9;
const unsigned AmrwbInfo::MAX_FRAME_SIZE_WB=60; //mode 8 as above table
const unsigned AmrwbInfo::SAMPLES_PER_FRAME_20MS_WB=320;
		
AmrwbInfo::AmrwbInfo(JNIEnv* env) :
	AmrInfo(env)
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "AmrwbInfo() create at %x", this);
	modeSize=wbModeSize;
}

AmrwbInfo::~AmrwbInfo()
{
}

//returns true if this represents amr-wb info false if nb.
bool AmrwbInfo::isWideBand() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "AmrwbInfo::isWideBand() true");
	return true;
}

const unsigned AmrwbInfo::get_sid() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "AmrwbInfo::get_sid() %d", FT_SID_WB);
	return FT_SID_WB;
}

const unsigned AmrwbInfo::get_MaxFrameSize() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "AmrwbInfo::get_MaxFrameSize() %d", MAX_FRAME_SIZE_WB);
	return MAX_FRAME_SIZE_WB;
}

//note by changing this value you can speed up and slow down the play speed of the audio.
//this is the number of samples in a 20 ms period for 16khz samples.
const unsigned AmrwbInfo::get_samplesPer20msFrame() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "AmrwbInfo::get_samplesPer20msFrame() %d",SAMPLES_PER_FRAME_20MS_WB);
	return SAMPLES_PER_FRAME_20MS_WB; 
}


