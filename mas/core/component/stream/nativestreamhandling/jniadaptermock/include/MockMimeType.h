#ifndef MOCKMIMETYPE_H_
#define MOCKMIMETYPE_H_

#include <MockObject.h>

#include <base_include.h>

class MockMimeType : public MockObject
{
public:
	MockMimeType();
	MockMimeType(const base::String& type);
	virtual ~MockMimeType();
	int getMethodId(const base::String& name, const base::String& signature);
	void* callMethod(int methodId, va_list& args);

public:
	base::String m_mimeType;
};

#endif /*MOCKMIMETYPE_H_*/
