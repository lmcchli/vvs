#include "MockJavaVM.h"
#include "MockObject.h"

#include <iostream>
using std::cout;
using std::endl;

#include <logger.h>

#include <base_include.h>
#include <assert.h>

using base::String;

#if defined(WIN32)
#pragma warning(push)
#pragma warning(disable:4273)
#endif
JNIEXPORT jint JNICALL JNI_CreateJavaVM(JavaVM **pvm, void **penv, void *args)
{
	return 0;
}
#if defined(WIN32)
#pragma warning(pop)
#endif
Logger* MockJavaVM::logger = Logger::getLogger("mtest.MockJavaVM");
MockJavaVM* MockJavaVM::s_mockJavaVMSingleton = (MockJavaVM*)0;

MockJavaVM& MockJavaVM::instance()
{
	if (s_mockJavaVMSingleton == 0) {
		s_mockJavaVMSingleton = new MockJavaVM();
	}
	return *s_mockJavaVMSingleton;	
}

void MockJavaVM::addObject(MockObject* object)
{
	objectList[object->className()] = object;
}

void MockJavaVM::addObject(const base::String& className, MockObject* object)
{
	objectList[className] = object;
}

MockObject* MockJavaVM::getObject(const base::String& className)
{
	MockObject* result(0);
	LOGGER_DEBUG(logger, "--> getObject([" << className << "])");
    try {
	    result = objectList[className];
    } catch (...)  {
        cout << "Caught exception:  when " << className << endl;
    }
	LOGGER_DEBUG(logger, "<-- getObject() : " << result);
        if (result == 0) {
            LOGGER_FATAL(logger, "No such class: " << className);
        }
	assert(result > 0);
	return result;
}

long MockJavaVM::getMethodId(MockObject* object, const base::String& methodName, const base::String& methodSignature)
{
	long result(17);
	LOGGER_DEBUG(logger, "--> getMethodId([" << object << "::" << methodName << "])");
	result = object->getMethodId(methodName, methodSignature);
	LOGGER_DEBUG(logger, "<-- getMethodId() : " << result);
        if (result <= 0) {
            base::String className = object != 0 ? object->className() : "<NIL>";
            LOGGER_FATAL(logger, 
                         "No such method: " 
                         << className << "::" << methodName);
        }
	assert(result > 0);
	return result;
}

void* MockJavaVM::callMethod(MockObject* object, int methodId, va_list& args)
{
	LOGGER_DEBUG(logger, "--> callMethod(" << object << "[" << methodId << "])");
	void* result(0);
	result = object->callMethod(methodId, args);
	LOGGER_DEBUG(logger, "<-- callMethod() : " << (MockObject*)result);
	return result;
}

MockJavaVM::MockJavaVM()
{
	LOGGER_DEBUG(logger, "MockJavaVM created)");
}

MockJavaVM::~MockJavaVM()
{
    for (std::map<base::String, MockObject*>::iterator iter(objectList.begin());
         iter != objectList.end(); ++iter) {
        delete iter->second;
        iter->second = (MockObject*)0;
    }
}

