#include "MockMediaObjectIterator.h"

enum {
	HasNext = 1,
	GetNext
};

MockMediaObjectIterator::MockMediaObjectIterator() 
	: MockObject("IMediaObjectIterator"),
	  m_currentBuffer(0)
{
}

MockMediaObjectIterator::~MockMediaObjectIterator()
{
}

int MockMediaObjectIterator::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "hasNext") {
		return HasNext;
	} else if (name == "next") {
		return GetNext;
	}
	return 0;
}  

void* MockMediaObjectIterator::callMethod(int methodId, va_list& args)
{
	static bool hasNext(false);
	int nOfBuffers((int)m_mockByteBuffers.size());

	switch (methodId) {
	case HasNext:
		hasNext = nOfBuffers > m_currentBuffer;
		return (void*)&hasNext;
		break;
	
	case GetNext:
		return (void*)m_mockByteBuffers[m_currentBuffer++];
		break;
		
	default:
		break;
	}
    return 0;    
}
