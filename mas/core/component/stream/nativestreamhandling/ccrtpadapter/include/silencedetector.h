#ifndef _SILENCEDETECTOR_H_
#define _SILENCEDETECTOR_H_

#include "jni.h"
#include <base_std.h>

static const int MAX_PCM_DATA = 1024;

typedef struct SilenceDetection_t
{
    int m_mode;
    int m_threshold;
    int m_initialSilenceFrames;
    int m_detectionFrames;
    int m_silenceDeadband;
    int m_signalDeadband;
    int m_debugLevel;
} SilenceDetection_t;

//"etpmsdefs.h"
enum RTPCodec_t
{
    Eric_G711_ALaw, Eric_G711_ULaw, Eric_G729A, Eric_GSMFR, Eric_H263
};

class SilenceDetector
{

public:
    SilenceDetector(JNIEnv*);

    ~SilenceDetector();

    bool IsSilenceDetectionEnabled();
    uint32 CheckSilence(char *buffer, unsigned int length, bool &abort);

    uint32 AddToSilenceDuration(unsigned long addedComfortNoiseDuration);

    bool IsSilence(char *buffer, unsigned int length);

    void initialize(JNIEnv* env, unsigned long packetdur, unsigned long mindur, base::String codec,
            unsigned long initialsilencedur, unsigned long silencedur, int mode, int threshold,
            int initialSilenceFrames, int detectionFrames, int signalDeadband, int silenceDeadband, int debugLevel);

private:
    // Reset internal state of the silence detector
    void initSilenceDetector();
    void resetSilenceDuration();

    unsigned long m_packetDuration;
    unsigned long m_minDuration;
    base::String m_codec;
    unsigned long m_silenceThreshold;
    unsigned long m_silenceDuration;
    unsigned long m_TalkTime;
    short m_linearBuffer[MAX_PCM_DATA];
    bool m_initialized;

    SilenceDetection_t m_silenceDetectorParams;
    static unsigned signalDeadband;	// 10 milliseconds of signal will be used to determine a speech transition
    static unsigned silenceDeadband;	// 125 milliseconds of silence will be used to determine the silence transition
    static unsigned samplesPerFrame;	// 10 milliseconds of signal frame will be used to determine the energy.

    unsigned signalDeadbandFrames;	// Frames of signal before talk burst starts
    unsigned silenceDeadbandFrames;	// Frames of silence before talk burst ends
    bool inTalkBurst;				// A flag to determine if we are in talk state or silence state.
    unsigned framesReceived;	// Number of consecutive signal or silence frames received before declaring a transition
    unsigned levelThreshold;			// Adaptive threshold value to determine talk or silence
    unsigned signalMinimum;			// Minimum of signal frames above threshold
    unsigned silenceMaximum;			// Maximum of silence frames below threshold
    unsigned signalFramesReceived;	// Frames of signal received after we transit to talk state
    unsigned silenceFramesReceived;	// Frames of silence received after we transit to silence state
    unsigned totalSilenceFramesReceived;
    unsigned totalSignalFramesReceived;
    bool binitThreshold;
    unsigned long m_initialSilenceThreshold;
    unsigned long m_finalSilenceThreshold;

    JNIEnv* mEnv;
};

#endif
