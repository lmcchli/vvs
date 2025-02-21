#ifndef MockByteBuffer_H_
#define MockByteBuffer_H_

#include <MockObject.h>

#include <base_include.h>
#include <vector>

class MockByteBuffer : public MockObject
{
public:
	MockByteBuffer(const MockByteBuffer& source);
	MockByteBuffer(int limit=0, char* buffer=0);
	virtual ~MockByteBuffer();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
public:
	int m_limit;
	char* m_buffer;
};

#endif /*MockByteBuffer_H_*/
