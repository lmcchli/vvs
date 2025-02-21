#ifndef MockLengthUnit_H_
#define MockLengthUnit_H_

#include <MockObject.h>

#include <base_include.h>
#include <vector>

class MockLengthUnit : public MockObject
{
public:
	MockLengthUnit();
	virtual ~MockLengthUnit();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	virtual int getStaticFieldId(const base::String& name, const base::String& signature);
	virtual MockObject* getStaticObjectField(int fieldID);
	
public:
//	MockLengthUnit* m_MILLISECONDS;
};

#endif /*MockLengthUnit_H_*/
