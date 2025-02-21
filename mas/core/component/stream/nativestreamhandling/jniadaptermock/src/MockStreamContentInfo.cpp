#include "MockStreamContentInfo.h"

MockStreamContentInfo::MockStreamContentInfo() 
	: MockObject("StreamContentInfo"),
	  m_mockRTPPayloadAudio(0,8000,"PCMU"),
	  m_mockRTPPayloadVideo(34, 90000, "H263"),
	  m_mockRTPPayloadDTMF(101),
	  m_mockMimeType("MimeType"),
	  m_fileExtension("wav"),
          m_cname(""),
	  m_pTime(40),
	  m_isVideo(true),
	  m_maxPTime(120)
{
    m_mockRTPPayloads.push_back(&m_mockRTPPayloadAudio);
    m_mockRTPPayloads.push_back(&m_mockRTPPayloadVideo);
}

MockStreamContentInfo::~MockStreamContentInfo()
{
}

int MockStreamContentInfo::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "getAudioPayload") { // RTPPayload
		return 1;
	} else if (name == "getVideoPayload") { // RTPPayload
		return 2;
	} else if (name == "getDTMFPayload") { // RTPPayload
		return 3;
	} else if (name == "getCNPayload") { // RTPPayload
		return 4;
	} else if (name == "getVideoPayload") { // RTPPayload
		return 5;
	} else if (name == "getContentType") { // MimeType
		return 6;
	} else if (name == "getFileExtension") { // String
		return 7;
	} else if (name == "getCNAME") { // String
		return 8;
	} else if (name == "getPayloads") { // RTPPayload[]
		return 9;
	} else if (name == "getPTime") { // Integer
		return 10;
	} else if (name == "isVideo") { // Boolean
		return 11;
	}
else if (name == "getMaxPTime") {
		return 12;
	}
	
	return 0;
}  

void* MockStreamContentInfo::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
		case 1:
			return &m_mockRTPPayloadAudio;
			break;

		case 2:
			return &m_mockRTPPayloadVideo;
			break;

		case 3:
			return &m_mockRTPPayloadDTMF;
			break;

		case 4:
			return 0;
			break;

		case 5:
			return 0;
			break;

		case 6:
			return &m_mockMimeType;
			break;

		case 7:
			return (void*)m_fileExtension.c_str();
			break;

		case 8:
			return (void*)m_cname.c_str();
			break;

		case 9:
			return &m_mockRTPPayloads;
			break;

		case 10:
			return &m_pTime;
			break;

		case 11:
			return &m_isVideo;
			break;
			
		case 12:
			return &m_maxPTime;
			break;

		default:
			;
	}
    return 0;    
}
