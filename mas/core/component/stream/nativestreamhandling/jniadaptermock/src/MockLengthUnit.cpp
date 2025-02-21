#include "MockLengthUnit.h"

enum {
	MilliSeconds = 1
};

//MockLengthUnit MockLengthUnit::MILLISECONDS;

MockLengthUnit::MockLengthUnit() 
	: MockObject("com/mobeon/masp/mediaobject/MediaLength$LengthUnit")
      
{
}

MockLengthUnit::~MockLengthUnit()
{
}

int MockLengthUnit::getMethodId(const base::String& name, const base::String& signature)
{
	return 0;
}  

void* MockLengthUnit::callMethod(int methodId, va_list& args)
{
    return 0;    
}

int MockLengthUnit::getStaticFieldId(const base::String& name, const base::String& signature)
{
	if (name == "MILLISECONDS") {
		return MilliSeconds;
	}
    return 0;    
}

MockObject* MockLengthUnit::getStaticObjectField(int fieldID)
{
	switch (fieldID) {
	case MilliSeconds:
		return this;

	default:
		break;
	}
    return 0;    
}
