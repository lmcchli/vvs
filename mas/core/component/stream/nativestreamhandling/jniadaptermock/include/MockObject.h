#ifndef MOCKOBJECT_H_
#define MOCKOBJECT_H_

#include <logger.h>

#include <base_include.h>
#include <stdarg.h>

class MockObject
{
public:
	MockObject(const base::String& name);
	virtual ~MockObject();
	virtual int getMethodId(const base::String& name, const base::String& signature);
	virtual void* callMethod(int methodId, va_list& args);
	virtual int getStaticFieldId(const base::String& name, const base::String& signature);
	virtual MockObject* getStaticObjectField(int fieldID);
	const base::String& className();
private:
    std::auto_ptr<Logger> logger;
    base::String m_className;	
};
#endif /*MOCKOBJECT_H_*/
