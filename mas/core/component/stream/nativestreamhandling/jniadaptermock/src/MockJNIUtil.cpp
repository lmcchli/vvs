#include "jniutil.h"

#include <MockJavaVM.h>
#include <MockByteBuffer.h>
#include <logger.h>

#include <map>
#include <vector>
#include <memory>

using std::vector;
using std::map;

static std::auto_ptr<Logger> ptrLogger;
static Logger* logger = (Logger*)0;

static map<jobject, int> g_objects;

JavaVM* JNIUtil::mJvm = (JavaVM*)0;

void eraseJNIUtil()
{
    /*
    for (map<jobject, int>::iterator iter = g_objects.begin();
         iter != g_objects.end(); ++iter) {
        delete ((MockObject*)iter->first);
    }
    */
}


bool JNIUtil::getJavaEnvironment(void** env, bool& alreadyAttached)
{
    alreadyAttached = true;
    *env = mJvm;
    return true;
}

jclass JNIUtil::getJavaClass(JNIEnv* env, jobject obj, const char* className, 
	bool shouldThrowJavaExceptionOnFailure)
{
	jclass clazz;
    MockJavaVM* javaVM((MockJavaVM*)env);
	
	LOGGER_DEBUG(logger, "--> getJavaClass([" << className << "])");
    assert(env != 0);
	clazz = (jclass)javaVM->getObject(className);
	LOGGER_DEBUG(logger, "<-- getJavaClass()");
	return clazz;
}

jmethodID JNIUtil::getJavaMethodID(JNIEnv* env, jclass cls, 
    const char* methodName, const char* signature,
    bool shouldThrowJavaExceptionOnFailure) 
{
	jmethodID methodId;
	LOGGER_DEBUG(logger, "--> getJavaMethodID([" << methodName << "][" << signature << "])");
	methodId = (jmethodID)MockJavaVM::instance().getMethodId((MockObject*)cls, methodName, signature);
	LOGGER_DEBUG(logger, "<-- getJavaMethodID()");
	return methodId;
}

void JNIUtil::checkException(JNIEnv* env, const char* methodName,
    bool shouldThrowJavaExceptionOnFailure) 
{
	LOGGER_DEBUG(logger, "--> checkException()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- checkException()");
}

void JNIUtil::checkException(JNIEnv* env, base::String& message,
    bool shouldThrowJavaExceptionOnFailure)
{
	LOGGER_DEBUG(logger, "--> checkException()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- checkException()");
}

void JNIUtil::DetachCurrentThread() 
{
	LOGGER_DEBUG(logger, "--> DetachCurrentThread()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- DetachCurrentThread()");
}

void JNIUtil::init(JavaVM* jvm)
{
	// Seems like a good place to initialize the logger ...
	logger = Logger::getLogger("mtest.MockJNIUtil");
	// Save local reference of JVM ...
    mJvm = jvm;
    ptrLogger.reset(logger);
}

void JNIUtil::throwStackException(const StackException& exception, 
    const char* message) 
{
	LOGGER_DEBUG(logger, "--> throwStackException()");
	LOGGER_FATAL(logger, "**** ABORT: " << message << "*****");
	LOGGER_DEBUG(logger, "<-- throwStackException()");
}

void JNIUtil::throwStackException(const StackException& exception, 
    const char* message, jobject callId)
{
	LOGGER_DEBUG(logger, "--> throwStackException()");
	LOGGER_FATAL(logger, "**** ABORT: " << message << "*****");
	LOGGER_DEBUG(logger, "<-- throwStackException()");
}

void JNIUtil::throwStackException(const StackException& exception, 
    const char* message, JNIEnv* env)
{
	LOGGER_DEBUG(logger, "--> throwStackException()");
	LOGGER_FATAL(logger, "**** ABORT: " << message << "*****");
	LOGGER_DEBUG(logger, "<-- throwStackException()");
}

void JNIUtil::deleteGlobalRef(jobject ref) 
{
    LOGGER_DEBUG(logger, "--> deleteGlobalRef()");
    /*
    if (g_objects.find(ref) == g_objects.end()) {
        LOGGER_ERROR(logger, "No such object!");
    } else {
        g_objects[ref]--;
        if (g_objects[ref] <= 0) {
            g_objects.erase(ref);
            delete ((MockObject*)ref);
        }
    }
    */
    LOGGER_DEBUG(logger, "<-- deleteGlobalRef()");
}

jobject JNIUtil::newGlobalRef(jobject lobj)
{
    LOGGER_DEBUG(logger, "--> newGlobalRef()");
    if (g_objects.find(lobj) == g_objects.end()) g_objects[lobj] = 0;
    g_objects[lobj]++;
    LOGGER_DEBUG(logger, " Returning input " << (long)lobj);
    LOGGER_DEBUG(logger, "<-- newGlobalRef()");
    return lobj;
}

void JNIUtil::callVoidMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callVoidMethod()");
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args);
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callVoidMethod()");
}

jobject JNIUtil::callObjectMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callObjectMethod(" << (long)methodID << ")");
    jobject result(0);
	vector<void*> argv;
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		result = (jobject)MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args);
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callObjectMethod() : " << (void*)result);
	return result;
}

jboolean JNIUtil::callBooleanMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callBooleanMethod()");
    jboolean result(0);
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		result = *((bool*)MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args));
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callBooleanMethod()");
	return result;
}

jint JNIUtil::callIntMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callIntMethod(" << (void*)obj << ")");
    jint result(0);
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		result = *((int*)MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args));
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callIntMethod() : " << result);
	return result;
}

jlong JNIUtil::callLongMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callLongMethod()");
    jlong result(0);
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		result = *((long*)MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args));
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callLongMethod()");
	return result;
}

jfloat JNIUtil::callFloatMethod(jobject obj, jmethodID methodID, ...)
{
	LOGGER_DEBUG(logger, "--> callFloatMethod()");
    jfloat result(0);
    JNIEnv* env;
    bool alreadyAttached(false);
    if (getJavaEnvironment((void**)&env, alreadyAttached)) {
 	   	va_list args;
		va_start(args, methodID);
		result = *((float*)MockJavaVM::instance().callMethod((MockObject*)obj, (long)methodID, args));
		va_end(args);
    	if (!alreadyAttached) {
        	DetachCurrentThread();
    	}
    }
	LOGGER_DEBUG(logger, "<-- callFloatMethod()");
	return result;
}

jstring JNIUtil::newStringUTF(const char *utf)
{
	jstring result(0);
	LOGGER_DEBUG(logger, "--> newStringUTF()");
	int length(strlen(utf)+1);
	char* newStr(new char[length]);
	for (int i(0); i < length; i++) newStr[i] = utf[i];
	result = (jstring)newStr;
	LOGGER_DEBUG(logger, "<-- newStringUTF()");
	return result;
}

jsize JNIUtil::getStringUTFLength(jstring str)
{
	jsize result(0);
	LOGGER_DEBUG(logger, "--> getStringUTFLength()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- getStringUTFLength()");
	return result;
}

const char* JNIUtil::getStringUTFChars(jstring str, jboolean *isCopy)
{
	const char* result((char*)str);
	LOGGER_DEBUG(logger, "--> getStringUTFChars([" << (char*)str << "])");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- getStringUTFChars()");
	return result;
}

void JNIUtil::releaseStringUTFChars(jstring str, const char* chars)
{
	LOGGER_DEBUG(logger, "--> releaseStringUTFChars([" << (char*)str << "][" << chars << "])");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- releaseStringUTFChars()");
}

jfieldID JNIUtil::getStaticFieldID(jclass clazz, const char *name, const char *sig)
{
	jfieldID result(0);
	LOGGER_DEBUG(logger, "--> getStaticFieldID()");
	((MockObject*)clazz)->getStaticFieldId(name, sig);
	LOGGER_DEBUG(logger, "<-- getStaticFieldID()");
	return result;
}

jobject JNIUtil::getStaticObjectField(jclass clazz, jfieldID fieldID)
{
	jobject result(0);
	LOGGER_DEBUG(logger, "--> getStaticFieldID()");
	result = (jobject)((MockObject*)clazz)->getStaticObjectField((long)fieldID);
	LOGGER_DEBUG(logger, "<-- getStaticFieldID()");
	return result;
}

jobject JNIUtil::newObject(jclass clazz, jmethodID methodID, ...)
{
	jobject result(0);
	vector<void*> argv;
	LOGGER_DEBUG(logger, "--> newObject()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- newObject()");
	return result;
}

void* JNIUtil::getDirectBufferAddress(JNIEnv* env, jobject& buf)
{
	void* result(0);
	LOGGER_DEBUG(logger, "--> getDirectBufferAddress()");
	result = (void*)((MockByteBuffer*)buf)->m_buffer;
	LOGGER_DEBUG(logger, "<-- getDirectBufferAddress()");
	return result;
}

jclass JNIUtil::findClass(JNIEnv* env, const char *name)
{
	jclass clazz;
	LOGGER_DEBUG(logger, "--> findClass([" << name << "])");
	clazz = (jclass)MockJavaVM::instance().getObject(name);
	LOGGER_DEBUG(logger, "<-- findClass()");
	return clazz;
}


void JNIUtil::deleteLocalRef(jobject obj)
{
	LOGGER_DEBUG(logger, "--> deleteLocalRef()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- deleteLocalRef()");
}

void JNIUtil::deleteLocalRef(jclass clazz)
{
	LOGGER_DEBUG(logger, "--> deleteLocalRef()");
	LOGGER_DEBUG(logger, "  <not implemented>)");
	LOGGER_DEBUG(logger, "<-- deleteLocalRef()");
}

jobject JNIUtil::getObjectArrayElement(jobjectArray& array, jsize index)
{
    jobject element(0);
    LOGGER_DEBUG(logger, "--> getObjectArrayElement(" << (void*)array << ")");
    vector<void*>* vectorPtr((vector<void*>*)array);
    LOGGER_DEBUG(logger, "Getting " << index << " of " << vectorPtr->size());
    element = (jobject)(*vectorPtr)[index];
    LOGGER_DEBUG(logger, "<-- getObjectArrayElement()");
    return element;
}

jsize JNIUtil::getArrayLength(jarray array)
{
    jsize size;
    LOGGER_DEBUG(logger, "--> getArrayLength()");
    vector<void*>* vectorPtr((vector<void*>*)array);
    size = vectorPtr->size();
    LOGGER_DEBUG(logger, "<-- getArrayLength() : " << size);
    return size;
}

void JNIUtil::PopLocalFrame()
{
}

bool JNIUtil::PushLocalFrame(int)
{
    return true;
}
