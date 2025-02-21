#include "MockMediaLength.h"

enum {
	constructor = 1
};

MockMediaLength::MockMediaLength() 
	: MockObject("com/mobeon/masp/mediaobject/MediaLength")
{
}

MockMediaLength::~MockMediaLength()
{
}

int MockMediaLength::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "<init>") { // MediaLength::MediaLength(LengthUnit&)
		return constructor;
	} 
	return 0;
}  

void* MockMediaLength::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
		case constructor:
			return (void*)new MockMediaLength();
			break;
		
		default:
			break;
	}
    return (void*)0;    
}
