#ifndef MOCKMEDIAOBEJCT_H_
#define MOCKMEDIAOBEJCT_H_

#include <MockObject.h>

#include <MockMediaObjectNativeAccess.h>
#include <MockInputStream.h>
#include <MockMediaProperties.h>

#include <base_include.h>
#include <vector>

class MockMediaObject : public MockObject
{
public:
	MockMediaObject();
	MockMediaObject(const base::String& path, const base::String& name, const base::String& extension,
		const base::String& type, int chunkSize);
	virtual ~MockMediaObject();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);

public:
	MockMediaObjectNativeAccess m_mockMediaObjectNativeAccess;
	MockInputStream m_mockInputStream;
	MockMediaProperties m_mockMediaProperties;
	bool m_isImmutable;
	long m_size;
};

#endif /*MOCKMEDIAOBEJCT_H_*/
