#ifndef _MockStreamConfiguration_h_
#define _MockStreamConfiguration_h_

#include <MockObject.h>

#include <base_include.h>

class MockStreamConfiguration : public MockObject
{
 public:
    MockStreamConfiguration();
    virtual ~MockStreamConfiguration();
    int getMethodId(const base::String& name, const base::String& signature);  
    virtual void* callMethod(int methodId, va_list& args);

 private:
    int m_threadPoolSize;
    int m_packetPendTimeout;
    int m_sendPacketsAhead;
    int m_expireTimeout;
    int m_maximumTransmissionUnit;
    int m_abandonedStreamDetectedTimeout;
    float m_sendersControlFraction;
    int m_audioSkip;
    int m_skew;
    int m_skewMethodIntRep;
    int m_audioReplaceWithSilence;
    int m_threadPoolMaxWaitTime;
    int m_maxWaitForIFrameTimeout;
    bool m_isDispatchDTMFOnKeyDown;
    bool m_isUsePoolForRTPSessions;
    base::String m_localHostName;
	int m_movFileVersion;
    int m_silenceDetectionMode;
    int m_silenceThreshold;
    int m_initialSilenceFrames;
    int m_detectionFrames;
    int m_silenceDeadband;
    int m_signalDeadband;
    int m_silenceDetectionDebugLevel;
};

#endif
