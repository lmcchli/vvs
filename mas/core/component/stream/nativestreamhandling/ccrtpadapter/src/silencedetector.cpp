#include "jlogger.h"
#include "jniutil.h"
#include "streamutil.h"
#include <base_std.h> 

#include "silencedetector.h"

#include <limits.h>

static const int SILENCE_THRESHOLD = 1200;
static const float SILENCE_THRESHOLD_RATIO = .90f;
static const int DEBUG_LEVEL1 = 1;
static const int DEBUG_LEVEL2 = 2;
static const int DEBUG_LEVEL3 = 3;
unsigned SilenceDetector::signalDeadband = 80;
unsigned SilenceDetector::silenceDeadband = 1200;
unsigned SilenceDetector::samplesPerFrame = 80;

static const char* CLASSNAME = "masjni.ccrtpadapter.SilenceDetector";

// Class SilenceDetector 

SilenceDetector::SilenceDetector(JNIEnv* env)
{
    mEnv = env;
    initSilenceDetector();
    JLogger::jniLogDebug(env, CLASSNAME, "SilenceDetector - create at %#x", this);
}

SilenceDetector::~SilenceDetector()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~SilenceDetector - delete at %#x", this);
}

void SilenceDetector::initSilenceDetector()
{
    m_packetDuration = 0;
    m_minDuration = 0;
    m_silenceDuration = 0;
    m_TalkTime = 0;
    m_initialized = false;

    memset(&m_silenceDetectorParams, 0, sizeof(m_silenceDetectorParams));

    m_silenceThreshold = 0;
    inTalkBurst = false;			// we start with a silent state
    framesReceived = 0;
    levelThreshold = 0;			// we start with base threshold
    signalMinimum = 32676;		// signal peak value for a linear sample value
    silenceMaximum = 0;			// we start with zero level as silence
    signalFramesReceived = 0;
    silenceFramesReceived = 0;
    totalSilenceFramesReceived = 0;
    totalSignalFramesReceived = 0;
    binitThreshold = false;

    signalDeadbandFrames = (signalDeadband + samplesPerFrame - 1) / samplesPerFrame;
    silenceDeadbandFrames = (silenceDeadband + samplesPerFrame - 1) / samplesPerFrame;
}

bool SilenceDetector::IsSilenceDetectionEnabled()
{
    if (m_initialized && (m_silenceDetectorParams.m_mode == 1 || m_silenceDetectorParams.m_mode == 2)) {
        return true;
    } else {
        return false;
    }
}

void SilenceDetector::resetSilenceDuration()
{
    m_silenceDuration = 0;

    m_silenceThreshold = m_finalSilenceThreshold;
}

uint32 SilenceDetector::CheckSilence(char *buffer, unsigned int length, bool &abort)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (m_silenceDetectorParams.m_mode == 0 || m_silenceDetectorParams.m_mode == 1) {
        if (m_silenceThreshold > 0 && IsSilence(buffer, length)) {
            if (m_silenceDuration >= m_silenceThreshold) {
                abort = true;
            } else {
                m_silenceDuration += m_packetDuration;
                JLogger::jniLogTrace(env, CLASSNAME, "Silence Detected:Duration=%d:Silence Threshold=%d (ms)",
                        m_silenceDuration, m_silenceThreshold);
                if (m_silenceDuration >= m_silenceThreshold)
                    abort = true;
            }
        } else {
            m_TalkTime += m_silenceDuration + m_packetDuration;
            resetSilenceDuration();
            //m_silenceDuration = 0;
        }
    } else if (m_silenceDetectorParams.m_mode == 2) {
        if (m_silenceDuration >= m_silenceThreshold) {
            abort = true;
        } else {
            if (binitThreshold) {
                levelThreshold = 0;
                extern void eric_alaw2linear(unsigned char *buffer, short *linear, unsigned int len);
                extern void eric_ulaw2linear(unsigned char *buffer, short *linear, unsigned int len);

                if ((m_codec == "PCMA") || (m_codec == "pcma")) {
                    eric_alaw2linear((unsigned char*) buffer, m_linearBuffer, length);
                } else if ((m_codec == "PCMU") || (m_codec == "pcmu")) {
                    eric_ulaw2linear((unsigned char*) buffer, m_linearBuffer, length);
                } else {
                    return m_silenceDuration;
                }

                int sum = 0;

                const short * pcm = m_linearBuffer;
                const short * end = pcm + length;
                while (pcm != end) {
                    if (*pcm < 0)
                        sum -= *pcm++;
                    else
                        sum += *pcm++;
                }
                int level = sum / length;
                extern unsigned char g_linear2ulaw[];
                // Convert to a logarithmic scale - use uLaw which is complemented
                levelThreshold = g_linear2ulaw[level] ^ 0xff;
                binitThreshold = false;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL1) {
                    JLogger::jniLogTrace(env, CLASSNAME, "Initial threshold for silence detector set to: %d",
                            levelThreshold);
                }

                m_TalkTime += length / 8;
                return m_silenceDuration;
            }
            int bytes = samplesPerFrame;
            while (bytes <= (int) length) {
                if (m_silenceThreshold > 0 && IsSilence(&buffer[bytes - samplesPerFrame], samplesPerFrame)) {
                    m_silenceDuration += silenceFramesReceived * m_packetDuration;
                    if (m_silenceDuration >= m_silenceThreshold) {
                        if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL1) {
                            JLogger::jniLogTrace(env, CLASSNAME,
                                    "silence duration:%d caused abort;threshold=%d;Total Silence Frames=",
                                    m_silenceDuration, levelThreshold, silenceFramesReceived);
                        }

                        abort = true;
                        break;
                    }
                } else {
                    m_TalkTime += m_silenceDuration + m_packetDuration;
                    resetSilenceDuration();
                    //m_silenceDuration = 0;
                }
                bytes += samplesPerFrame;
            }
        }
    }
    return m_silenceDuration;
}

uint32 SilenceDetector::AddToSilenceDuration(unsigned long addedComfortNoiseDuration)
{
    m_silenceDuration += addedComfortNoiseDuration;
    return (uint32) m_silenceDuration;
}

bool SilenceDetector::IsSilence(char *buffer, unsigned int length)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (m_silenceDetectorParams.m_mode == 0)
        return false;

    extern void eric_alaw2linear(unsigned char *buffer, short *linear, unsigned int len);
    extern void eric_ulaw2linear(unsigned char *buffer, short *linear, unsigned int len);

    if ((m_codec == "PCMA") || (m_codec == "pcma")) {
        eric_alaw2linear((unsigned char*) buffer, m_linearBuffer, length);
    } else if ((m_codec == "PCMU") || (m_codec == "pcmu")) {
        eric_ulaw2linear((unsigned char*) buffer, m_linearBuffer, length);
    } else {
        return false;
    }

    if (m_silenceDetectorParams.m_mode == 1) {
        float count = 0;
        unsigned short linearval = 0;
        short val;

        for (int i = 0; i < (int) length; i++) {
            val = m_linearBuffer[i];
            if (val < 0)
                linearval = -val;
            else
                linearval = val;
            if (linearval < levelThreshold)
                count++;
            if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL3) {
                JLogger::jniLogTrace(env, CLASSNAME, "Mode 1:linearval=%d", linearval);
            }
        }

        if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
            JLogger::jniLogTrace(env, CLASSNAME, "Mode 1:count=%d:length=%d:threshold=%d", linearval, length,
                    levelThreshold);
        }

        count = count / length;
        if (count >= SILENCE_THRESHOLD_RATIO)
            return true;

        return false;
    }
    //mode must be 2
    else {
        int sum = 0;

        const short * pcm = m_linearBuffer;
        const short * end = pcm + samplesPerFrame;
        while (pcm != end) {
            if (*pcm < 0)
                sum -= *pcm++;
            else
                sum += *pcm++;
        }

        int level = sum / samplesPerFrame;

        extern unsigned char g_linear2ulaw[];
        // Convert to a logarithmic scale - use uLaw which is complemented
        level = g_linear2ulaw[level] ^ 0xff;

        // Now if signal level above threshold we are "talking"
        bool haveSignal = level > (int) levelThreshold;

        bool transition = false;
        // If no change ie still talking or still silent, resent frame counter
        if (inTalkBurst == haveSignal) {
            signalFramesReceived = 0;
            silenceFramesReceived = 0;
            framesReceived = 0;
        } else {
            framesReceived++;
            // If have had enough consecutive frames talking/silent, swap modes.
            if (framesReceived >= (inTalkBurst ? silenceDeadbandFrames : signalDeadbandFrames)) {
                inTalkBurst = !inTalkBurst;
                transition = true;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
                    if (inTalkBurst) {
                        JLogger::jniLogTrace(env, CLASSNAME,
                                "[Talk]:energy level=%a:threshold=d:Total Silence Frames=%d:Maximum silence level=%d",
                                level, levelThreshold, totalSilenceFramesReceived, silenceMaximum);
                    } else {
                        JLogger::jniLogTrace(env, CLASSNAME,
                                "[Silent]:energy level=%d:threshold=%d:Total Signal Frames=%d:Minimum Signal level=%d",
                                level, levelThreshold, totalSignalFramesReceived, signalMinimum);
                    }
                }

                // If we had talk/silence transition restart adaptive threshold measurements
                signalMinimum = 32676;
                silenceMaximum = 0;
                if (inTalkBurst) {
                    signalFramesReceived = framesReceived;
                    silenceFramesReceived = 0;
                } else {
                    silenceFramesReceived = framesReceived;
                    signalFramesReceived = 0;
                }
                totalSignalFramesReceived = 0;
                totalSilenceFramesReceived = 0;
            }
        }

        if (levelThreshold == 0) {
            if (level > 1) {
                // Bootstrap condition, use first frame level as silence level
                levelThreshold = level / 2;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL1) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Silence Detection threshold initialised to:%d : energy level=%d", levelThreshold, level);
                }
            }
            return true; // inTalkBurst always FALSE here, so return silent
        }

        // Count the number of silent and signal frames and calculate min/max
        if (haveSignal) {
            if (level < (int) signalMinimum)
                signalMinimum = level;
            if (!transition) {
                signalFramesReceived++;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "speech continued:::::::::%d Energy level=%d:Minimum Signal=%d", signalFramesReceived,
                            level, signalMinimum);
                }
                totalSignalFramesReceived++;
            }
        } else {
            if (level > (int) silenceMaximum)
                silenceMaximum = level;
            if (!transition) {
                silenceFramesReceived++;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "silence continued:::::::::%d Energy level=%d:Maximum Silence=%d", silenceFramesReceived,
                            level, silenceMaximum);
                }
                totalSilenceFramesReceived++;
            }
        }
        if (transition && !inTalkBurst) {
            unsigned newThreshold = (levelThreshold + silenceMaximum) / 2 + 1;
            if (levelThreshold != newThreshold) {
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "[silence]:threshold changed to %d from %d:Maximum Silence Level=%d", newThreshold,
                            levelThreshold, silenceMaximum);
                }
                levelThreshold = newThreshold;
            }
            transition = false;
        } else if (transition && inTalkBurst) {
            int prevThreshold = levelThreshold;
            int delta = (signalMinimum - levelThreshold) / 4;
            if (delta != 0) {
                levelThreshold += delta;
                if (m_silenceDetectorParams.m_debugLevel >= DEBUG_LEVEL2) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "[talk]:threshold changed to %d from %d:Minimum Signal Level=%d", levelThreshold,
                            prevThreshold, signalMinimum);
                }
            }
            transition = false;
        }
        return !inTalkBurst;
    }
}

void SilenceDetector::initialize(JNIEnv* env, unsigned long packetdur, unsigned long mindur, base::String codec,
        unsigned long initialsilencedur, unsigned long finalsilencedur, int mode, int threshold,
        int initialSilenceFrames, int detectionFrames, int signalDeadband, int silenceDeadband, int debugLevel)
{
    // Reset all variables to their start values
    initSilenceDetector();

    m_silenceDetectorParams.m_mode = mode;
    m_silenceDetectorParams.m_threshold = threshold;                      //1000
    m_silenceDetectorParams.m_initialSilenceFrames = initialSilenceFrames;  //40
    m_silenceDetectorParams.m_detectionFrames = detectionFrames;	        //10
    m_silenceDetectorParams.m_signalDeadband = signalDeadband;              //10
    m_silenceDetectorParams.m_silenceDeadband = silenceDeadband;           //150
    m_silenceDetectorParams.m_debugLevel = debugLevel;                       //0

    JLogger::jniLogTrace(env, CLASSNAME,
            "mode=%d;threshold=%d;initialSilenceFrames=%d;detectionFrames=%d;signal deadband=%d;silence deadband=%d;Initial Silence Duration=%d;Final Max Silence Duration=%d;Debug Level=%d",
            mode, threshold, initialSilenceFrames, detectionFrames, signalDeadband, silenceDeadband, initialsilencedur,
            finalsilencedur, debugLevel);

    if (m_silenceDetectorParams.m_mode == 1) {
        m_packetDuration = packetdur;
        m_minDuration = mindur;
        m_codec = codec;
        m_initialSilenceThreshold = finalsilencedur;
        m_finalSilenceThreshold = finalsilencedur;
        m_silenceThreshold = finalsilencedur;
        m_initialized = true;
        levelThreshold = m_silenceDetectorParams.m_threshold;
        if (levelThreshold == 0)
            levelThreshold = SILENCE_THRESHOLD;
    } else if (m_silenceDetectorParams.m_mode == 2) {
        signalDeadband = m_silenceDetectorParams.m_signalDeadband * 8;
        silenceDeadband = m_silenceDetectorParams.m_silenceDeadband * 8;
        samplesPerFrame = m_silenceDetectorParams.m_detectionFrames * 8;
        m_packetDuration = m_silenceDetectorParams.m_detectionFrames;
        m_minDuration = mindur;
        m_codec = codec;
        m_initialSilenceThreshold = finalsilencedur;
        m_finalSilenceThreshold = finalsilencedur;
        m_silenceThreshold = finalsilencedur;
        m_initialized = true;

        inTalkBurst = false;			// we start with a silent state
        framesReceived = 0;
        levelThreshold = 0;				// we start with base threshold
        signalMinimum = 32676;	// signal peak value for a linear sample value
        silenceMaximum = 0;				// we start with zero level as silence
        signalFramesReceived = 0;
        silenceFramesReceived = 0;
        totalSilenceFramesReceived = 0;
        totalSignalFramesReceived = 0;

        signalDeadbandFrames = (signalDeadband + samplesPerFrame - 1) / samplesPerFrame;
        silenceDeadbandFrames = (silenceDeadband + samplesPerFrame - 1) / samplesPerFrame;
        binitThreshold = true;
        JLogger::jniLogTrace(env, CLASSNAME,
                "signal deadband=%d;silence deadband=%d;samples per frame=%d;packet duration=%d;minimum duration=%d;silence threshold=%d;signal deadband frames=%d;silence deadband frames=%d",
                signalDeadband, silenceDeadband, samplesPerFrame, m_packetDuration, m_minDuration, m_silenceThreshold,
                signalDeadbandFrames, silenceDeadbandFrames);
    }
}
