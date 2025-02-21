#ifndef MOCKSTACKEVENTNOTIFIER_H_
#define MOCKSTACKEVENTNOTIFIER_H_

#include <MockObject.h>

class MockStackEventNotifier : public MockObject
{
public:
	MockStackEventNotifier();
	virtual ~MockStackEventNotifier();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
};

#endif /*MOCKSTACKEVENTNOTIFIER_H_*/
