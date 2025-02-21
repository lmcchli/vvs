#include "MockMimeType.h"

#include <base_include.h>

using base::String;

enum {
	ToString = 1
};

MockMimeType::MockMimeType() :
	MockObject("MimeType")
{
}

MockMimeType::MockMimeType(const base::String& type) :
	MockObject("MimeType"),
	m_mimeType(type)
{
}

MockMimeType::~MockMimeType()
{
}

int MockMimeType::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "toString") {
		return ToString;
	}
    return 0;    
}

void* MockMimeType::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
	case ToString:
		return (void*)m_mimeType.c_str();

	default:
		break;
	}
    return 0;    
}
