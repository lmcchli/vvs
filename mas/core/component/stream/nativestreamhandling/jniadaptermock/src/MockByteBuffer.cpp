#include "MockByteBuffer.h"

enum {
	Limit = 1
};

MockByteBuffer::MockByteBuffer(int limit, char* buffer) 
	: MockObject("Buffer"),
	  m_limit(limit),
	  m_buffer(0)
{
	if (limit > 0) {
		m_buffer = new char[limit];
		if (buffer != 0) memcpy(m_buffer, buffer, limit);
	}
}

MockByteBuffer::MockByteBuffer(const MockByteBuffer& source) 
	: MockObject("Buffer"),
	  m_limit(source.m_limit),
	  m_buffer(0)
{
	if (m_limit > 0) {
		m_buffer = new char[m_limit];
		if (source.m_buffer != 0) memcpy(m_buffer, source.m_buffer, m_limit);
	}
}

MockByteBuffer::~MockByteBuffer()
{
	if (m_buffer != 0) delete [] m_buffer;
}

int MockByteBuffer::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "limit") {
		return Limit;
	}
	return 0;
}  

void* MockByteBuffer::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
	case Limit:
		return (void*)&m_limit;

	default:
		break;
	}
	return 0;    
}
