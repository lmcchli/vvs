#ifndef MOCKMEDIASTREAM_H_
#define MOCKMEDIASTREAM_H_

#include <MockObject.h>

#include <base_include.h>

class MockMediaStream : public MockObject
{
public:
	MockMediaStream();
    MockMediaStream(const base::String& callSessionId);
	virtual ~MockMediaStream();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
    void setCallSessionId(const base::String& callSessionId);

public:
    int releasePortsCounter;

protected:
    base::String m_callSessionId;
};

#endif /*MOCKMEDIASTREAM_H_*/
