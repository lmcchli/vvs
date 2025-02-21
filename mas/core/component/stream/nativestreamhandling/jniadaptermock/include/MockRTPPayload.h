#ifndef MOCKRTPPAYLOAD_H_
#define MOCKRTPPAYLOAD_H_

#include <MockObject.h>
#include <base_include.h>

class MockRTPPayload : public MockObject
{
public:
	MockRTPPayload(int payloadType=-1, int clockRate=8000, const base::String& encoding = "PCMU", const base::String& mediaFormatParameters = "");
	virtual ~MockRTPPayload();
	int getMethodId(const base::String& name, const base::String& signature);
	void* callMethod(int methodId, va_list& args);
private:
	int m_payloadType;
	int m_channels;
	int m_clockRate;
	int m_bwSender;
	int m_bwReceiver;
	int m_minSender;
	int m_maxSender;
	int m_minReceiver;
	int m_maxReceiver;

	base::String m_encoding;
	base::String m_mediaFormatParameters;
};

#endif /*MOCKRTPPAYLOAD_H_*/
