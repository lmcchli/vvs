#ifndef MockMediaLength_H_
#define MockMediaLength_H_

#include <MockObject.h>

#include <base_include.h>
#include <vector>

class MockMediaLength : public MockObject
{
public:
	MockMediaLength();
	virtual ~MockMediaLength();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
private:
};

#endif /*MockMediaLength_H_*/
