#include "MockMediaStream.h"

enum {
	releasePorts = 1,
    getCallSessionId = 2
};

MockMediaStream::MockMediaStream()
	: MockObject("MediaStream"),
      releasePortsCounter(0),
      m_callSessionId("undefined")
{
}

MockMediaStream::MockMediaStream(const base::String& callSessionId)
	: MockObject("MediaStream"),
      releasePortsCounter(0),
      m_callSessionId(callSessionId)
{
}

MockMediaStream::~MockMediaStream()
{
}

int MockMediaStream::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "releasePorts") { // void
		return releasePorts;
    } else if (name == "getCallSessionId") { // string
        return getCallSessionId;
    }
	return 0;
}  

void* MockMediaStream::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
		case releasePorts:
            releasePortsCounter++;
			return (void*)0;
			break;

		case getCallSessionId:
			return (void*)m_callSessionId.c_str();
			break;

		default:
			break;
	}
    return (void*)0;    
}

void MockMediaStream::setCallSessionId(const base::String& callSessionId)
{
    m_callSessionId = callSessionId;
}


