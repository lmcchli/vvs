#ifndef MockMediaObjectIterator_H_
#define MockMediaObjectIterator_H_

#include <MockObject.h>

class MockByteBuffer;

#include <base_include.h>
#include <vector>

class MockMediaObjectIterator : public MockObject
{
public:
	MockMediaObjectIterator();
	virtual ~MockMediaObjectIterator();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
public:
	int m_currentBuffer;
	std::vector<MockByteBuffer*> m_mockByteBuffers;
};

#endif /*MockMediaObjectIterator_H_*/
