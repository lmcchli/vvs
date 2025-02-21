#include "MockMediaProperties.h"

#include <base_include.h>

using base::String;

enum {
	SetContentType = 1,
	GetContentType,
	SetFileExtension,
	GetFileExtension,
	AddLength
};

MockMediaProperties::MockMediaProperties() 
	: MockObject("MediaProperties"),
	  m_fileExtension("wav")
{
}

MockMediaProperties::MockMediaProperties(const base::String& extension, 
										 const base::String& type)
	: MockObject("MediaProperties"),
	  m_fileExtension(extension),
	  m_mockMimeType(type)
{
}

MockMediaProperties::~MockMediaProperties()
{
}

int MockMediaProperties::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "setContentType") {
		return SetContentType;
	} else if (name == "getContentType") {
		return GetContentType;
	} else if (name == "setFileExtension") {
		return SetFileExtension;
	} else if (name == "getFileExtension") {
		return GetFileExtension;
	} else if (name == "addLength") {
		return AddLength;
	}
	return 0;
}  

void* MockMediaProperties::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
	case SetContentType:
		m_mockMimeType = *((MockMimeType*)va_arg(args, MockMimeType*));
		break;
		
	case GetContentType:
		return (void*)&m_mockMimeType;
		break;
		
	case SetFileExtension:
		m_fileExtension = (char*)va_arg(args, char*);
		break;
		
	case GetFileExtension:
		return (void*)m_fileExtension.c_str();
		break;
		
	case AddLength:
		return (void*)0;
		break;
		
	default:
		break;
	}
    return 0;    
}
