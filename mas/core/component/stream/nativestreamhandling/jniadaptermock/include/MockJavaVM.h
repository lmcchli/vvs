#ifndef MockJavaVM_h
#define MockJavaVM_h


#include <MockStreamContentInfo.h>

#include <vector>
#include <map>

class Logger;
class MockObject;

#include <jniutil.h>

#include <base_include.h>

//jint JNI_CreateJavaVM(JavaVM **pvm, void **penv, void *args);

class /*MEDIALIB_CLASS_EXPORT*/ MockJavaVM {
	public:
		static MockJavaVM& instance();
		void addObject(const base::String& className, MockObject* object);
		void addObject(MockObject* object);
		MockObject* getObject(const base::String& className);
		long getMethodId(MockObject* object, const base::String& methodName, const base::String& methodSignature);
		void* callMethod(MockObject* object, int methodId, va_list& argv);
		
	private:
		MockJavaVM();
		~MockJavaVM();
	
	private:
		static MockJavaVM* s_mockJavaVMSingleton;
    	static Logger* logger;
    	std::map<base::String, MockObject*> objectList;	
};

#endif

