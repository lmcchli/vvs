#include "MockRTPPayload.h"

MockRTPPayload::MockRTPPayload(int payloadType, int clockRate, const base::String& encoding, 
                               const base::String& mediaFormatParameters)
	: MockObject("RTPPayload"),
	  m_payloadType(payloadType),
	  m_channels(0),
	  m_clockRate(clockRate),
	  m_bwSender(64000),
	  m_bwReceiver(64000),
	  m_minSender(0),
	  m_maxSender(64000),
	  m_minReceiver(0),
	  m_maxReceiver(64000),
	  m_encoding(encoding),
	  m_mediaFormatParameters(mediaFormatParameters)
{
}

MockRTPPayload::~MockRTPPayload()
{
}

int MockRTPPayload::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "getPayloadType") {
		return 1;
	} else if (name == "getChannels") {
		return 2;
	} else if (name == "getClockRate") {
		return 3;
	} else if (name == "getEncoding") {
		return 4;
	} else if (name == "getMediaFormatParameters") {
		return 5;
	} else if (name == "getBwSender") {
		return 6;
	} else if (name == "getBwReceiver") {
		return 7;
	} else if (name == "getMinSender") {
		return 8;
	} else if (name == "getMaxSender") {
		return 9;
	} else if (name == "getMinReceiver") {
		return 10;
	} else if (name == "getMaxReceiver") {
		return 11;
	}
	return 0;
}  

void* MockRTPPayload::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
		case 1:
			return &m_payloadType;
		case 2:
			return &m_channels;
		case 3:
			return &m_clockRate;
		case 4:
			return (void*)m_encoding.c_str();
		case 5:
			if(m_mediaFormatParameters.size() == 0) {
				return 0;
			} else {
				return (void*)m_mediaFormatParameters.c_str();
			}
		case 6:
			return &m_bwSender;
		case 7:
			return &m_bwReceiver;
		case 8:
			return &m_minSender;
		case 9:
			return &m_maxSender;
		case 10:
			return &m_minReceiver;
		case 11:
			return &m_maxReceiver;
	}
    return 0;    
}

