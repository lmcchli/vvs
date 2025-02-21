#include "MockStreamConfiguration.h"

MockStreamConfiguration::MockStreamConfiguration()
    : MockObject("StreamConfiguration"),
      m_threadPoolSize(5),
      m_packetPendTimeout(240),
      m_sendPacketsAhead(40),
      m_expireTimeout(100000000),
      m_maximumTransmissionUnit(1500),
      m_abandonedStreamDetectedTimeout(800000),
      m_sendersControlFraction(0.4f),
      m_audioSkip(0),
      m_skew(0),
      m_skewMethodIntRep(0),
      m_audioReplaceWithSilence(10),
      m_threadPoolMaxWaitTime(5000),
      m_maxWaitForIFrameTimeout(2000),
      m_isDispatchDTMFOnKeyDown(true),
      m_isUsePoolForRTPSessions(true),
      m_localHostName("127.0.0.1"),
	  m_movFileVersion(1),
      m_silenceDetectionMode(0),
      m_silenceThreshold(0),
      m_initialSilenceFrames(0),
      m_detectionFrames(0),
      m_silenceDeadband(20),
      m_signalDeadband(20),
      m_silenceDetectionDebugLevel(0)
{
	char buffer[256];
	int len(256);
	base::String maybeFqdn;
	if(!gethostname(buffer,len)) {
		maybeFqdn += buffer;
		if(!getdomainname(buffer,len)) {
			maybeFqdn += ".";
			maybeFqdn += buffer;
			m_localHostName = maybeFqdn;
		}	
	}
}

MockStreamConfiguration::~MockStreamConfiguration()
{
}

int MockStreamConfiguration::getMethodId(const base::String& name, const base::String& signature)
{
    if (name == "getThreadPoolSize") { // int
        return 1;
    } else if (name == "getPacketPendTimeout") { // int
        return 2; 
    } else if (name == "getSendPacketsAhead") { // int
        return 3; 
    } else if (name == "getExpireTimeout") { // int
        return 4;
    } else if (name == "getMaximumTransmissionUnit") { // int
        return 5;
    } else if (name == "getAbandonedStreamDetectedTimeout") { // int
        return 6;
    } else if (name == "getSendersControlFraction") { // float
        return 7;
    } else if (name == "getAudioSkip") { // int
        return 8;
    } else if (name == "getSkew") { // int
        return 9;
    } else if (name == "getSkewMethodIntRep") { // int
        return 10;
    } else if (name == "getAudioReplaceWithSilence") { // int
        return 11;
    } else if (name == "getThreadPoolMaxWaitTime") { // int
        return 12;
    } else if (name == "getMaxWaitForIFrameTimeout") { // int
        return 13;
    } else if (name == "isDispatchDTMFOnKeyDown") { // bool
        return 14;
    } else if (name == "isUsePoolForRTPSessions") { // bool
        return 15;
    } else if (name == "getLocalHostName") { // string
        return 16;
    } else if (name == "getMovFileVersion") { // string
        return 17;
    } else if (name == "getSilenceDetectionMode") { // int
        return 18;
    } else if (name == "getSilenceThreshold") { // int
        return 19;
    } else if (name == "getInitialSilenceFrames") { // int
        return 20;
    } else if (name == "getDetectionFrames") { // int
        return 21;
    } else if (name == "getSilenceDeadband") { // int
        return 22;
    } else if (name == "getSignalDeadband") { // int
        return 23;
    } else if (name == "getSilenceDetectionDebugLevel") { // int
        return 24;
    }
    return 0;
}

void* MockStreamConfiguration::callMethod(int methodId, va_list& args)
{
    void* result(0);

    switch (methodId) {
    case 1:
        result = &m_threadPoolSize;
        break;

    case 2:
        result = &m_packetPendTimeout;
        break;

    case 3:
        result = &m_sendPacketsAhead;
        break;

    case 4:
        result = &m_expireTimeout;
        break;

    case 5:
        result = &m_maximumTransmissionUnit;
        break;

    case 6:
        result = &m_abandonedStreamDetectedTimeout;
        break;

    case 7:
        result = &m_sendersControlFraction;
        break;

    case 8:
        result = &m_audioSkip;
        break;

    case 9:
        result = &m_skew;
        break;

    case 10:
        result = &m_skewMethodIntRep;
        break;

    case 11:
        result = &m_audioReplaceWithSilence;
        break;
    
    case 12:
        result = &m_threadPoolMaxWaitTime;
        break;

    case 13:
        result = &m_maxWaitForIFrameTimeout;
        break;

    case 14:
        result = &m_isDispatchDTMFOnKeyDown;
        break;

    case 15:
        result = &m_isUsePoolForRTPSessions;
        break;

    case 16:
        result = (void*)m_localHostName.c_str();
        break;

    case 17:
        result = &m_movFileVersion;
        break;
        
    case 18:
        result = &m_silenceDetectionMode;
        break;

    case 19:
        result = &m_silenceThreshold;
        break;

    case 20:
        result = &m_initialSilenceFrames;
        break;

    case 21:
        result = &m_detectionFrames;
        break;

    case 22:
        result = &m_silenceDeadband;
        break;
	
    case 23:
        result = &m_signalDeadband;
        break;
    case 24:
        result = &m_silenceDetectionDebugLevel;
        break;
    default:
        break;
    }

    return result;
}
