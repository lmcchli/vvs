#ifndef MOCKSTREAMCONTENTINFO_H_
#define MOCKSTREAMCONTENTINFO_H_

#include <MockObject.h>
#include <MockRTPPayload.h>

#include <base_include.h>
#include <vector>

class MockStreamContentInfo : public MockObject
{
public:
	MockStreamContentInfo();
	virtual ~MockStreamContentInfo();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
private:
	MockRTPPayload m_mockRTPPayloadAudio;
	MockRTPPayload m_mockRTPPayloadVideo;
	MockRTPPayload m_mockRTPPayloadDTMF;
        std::vector<void*> m_mockRTPPayloads;
	MockObject m_mockMimeType;
	base::String m_fileExtension;
	base::String m_cname;
	int m_pTime;
	bool m_isVideo;
	int m_maxPTime;
};

#endif /*MOCKSTREAMCONTENTINFO_H_*/
