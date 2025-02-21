#include "MockMediaObject.h"

enum {
	getMediaProperties = 1,
	getInputStream,
	getNativeAccess,
	setImmutable,
	isImmutable,
	getSize
};

MockMediaObject::MockMediaObject() 
	: MockObject("IMediaObject"),
	  m_isImmutable(false),
	  m_size(0L)
{
}

MockMediaObject::MockMediaObject(const base::String& path, const base::String& name, const base::String& extension,
								 const base::String& type, int chunkSize) :
	MockObject("IMediaObject"),
	m_mockMediaObjectNativeAccess(path, name, extension, chunkSize),
	m_mockMediaProperties(extension, type),
	m_isImmutable(true)
{
	m_size = m_mockMediaObjectNativeAccess.m_fileSize;
}

MockMediaObject::~MockMediaObject()
{
}

int MockMediaObject::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "getMediaProperties") { // MockMediaProperties
		return getMediaProperties;
	} else if (name == "getInputStream") { // MockInputStream
		return getInputStream;
	} else if (name == "getNativeAccess") { // MockMediaObjectNativeAccess
		return getNativeAccess;
	} else if (name == "setImmutable") { // void
		return setImmutable;
	} else if (name == "isImmutable") { // bool
		return isImmutable;
	} else if (name == "getSize") { // int
		return getSize;
	}
	return 0;
}  

void* MockMediaObject::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
		case getMediaProperties:
			return (void*)&m_mockMediaProperties;
			break;
		
		case getInputStream:
			return (void*)&m_mockInputStream;
			break;
		
		case getNativeAccess:
			return (void*)&m_mockMediaObjectNativeAccess;
			break;
		
		case setImmutable:
			return (void*)0;
			break;

		case isImmutable:
			return (void*)&m_isImmutable;
			break;
		
		case getSize:
			return (void*)&m_size;
			break;
		
		default:
			break;
	}
    return (void*)0;    
}

