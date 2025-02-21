#ifndef MockMediaProperties_H_
#define MockMediaProperties_H_

#include <MockObject.h>
#include <MockMimeType.h>

#include <base_include.h>
#include <vector>

class MockMediaProperties : public MockObject
{
public:
	MockMediaProperties();
	MockMediaProperties(const base::String& extension, const base::String& type);
	virtual ~MockMediaProperties();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
public:
	base::String m_fileExtension;
	MockMimeType m_mockMimeType;

};

#endif /*MockMediaProperties_H_*/
