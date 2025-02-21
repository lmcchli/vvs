#include "MockCCRTPSession.h"
#include "MockJavaVM.h"
#include "MockMediaObject.h"

#include <ccrtpsessionproxy.h>

/*
* This is the implementation of the mocked Java class CCRTPSession.
*/
MockCCRTPSession::MockCCRTPSession()
{
    JNI_OnLoad((JavaVM*)&MockJavaVM::instance(), 0);
    logger.reset(Logger::getLogger("mtest.MockCCRTPSession"));
}

MockCCRTPSession::~MockCCRTPSession()
{
    JNI_OnUnload(0, 0);
}

void MockCCRTPSession::initConfiguration(MockStreamConfiguration* configuration)
{
    JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
    jclass dummy(0);
    jobject jconfiguration((jobject)configuration);
    Java_com_mobeon_masp_stream_CCRTPSession_initConfiguration
        (env, dummy, jconfiguration);
}

SessionSupport* MockCCRTPSession::createInboundSession(MockMediaStream* stream)
{
    LOGGER_DEBUG(logger.get(), "--> createInboundSession()");    
	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
	jobject dummy(0);
    // Should set session ID here.
	jint handle = Java_com_mobeon_masp_stream_CCRTPSession_createInboundSession
	(env, dummy, (jobject)stream);
    LOGGER_DEBUG(logger.get(), "<-- createInboundSession()");
    return reinterpret_cast<SessionSupport*>(handle);
}

SessionSupport* MockCCRTPSession::createOutboundSession(MockMediaStream* stream)
{
    LOGGER_DEBUG(logger.get(), "--> createOutboundSession()");
	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
	jobject dummy(0);
    // Should set session ID here.
	jlong handle = Java_com_mobeon_masp_stream_CCRTPSession_createOutboundSession
	(env, dummy, (jobject)stream);
    LOGGER_DEBUG(logger.get(), "<-- createOutboundSession()");
    return (SessionSupport*)handle;
}

void MockCCRTPSession::play(MockObject* mockObject,
                int requestId,
			    MockMediaObject* mockMediaObject, 
			    int playOption, int cursor,
			    SessionSupport* sessionSupport)
{
  LOGGER_DEBUG(logger.get(), "--> play()");    
  JNIEnv *env((JNIEnv*)&MockJavaVM::instance());
  jobject obj(0);
  jobject jMediaObject((jobject)mockMediaObject);
  jint jPlayOption(playOption);
  jlong jCursor(cursor);
  
  Java_com_mobeon_masp_stream_CCRTPSession_play
    (env, obj, (jint)requestId, jMediaObject, jPlayOption, jCursor, (jlong)sessionSupport);
  LOGGER_DEBUG(logger.get(), "<-- play()");    
 }

void MockCCRTPSession::record(MockObject* mockObject, 
                              MockMediaObject* mockMediaObject, 
                              MockRecordingProperties* mockRecordingProperties,
                              int sessionSupport)
{
    LOGGER_DEBUG(logger.get(), "--> record()");     
	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
	jobject obj(0);
	jobject callId((jobject)mockObject);
    jobject playMediaObject(0);
	jint outboundStreamHandle(0); 
 	jobject recordMediaObject((jobject)mockMediaObject); 
	jobject properties((jobject)mockRecordingProperties);
	jint handle((jint)sessionSupport);
	Java_com_mobeon_masp_stream_CCRTPSession_record
	(env, obj, callId, playMediaObject, outboundStreamHandle, 
	recordMediaObject, properties, handle);
}

void MockCCRTPSession::create(MockStreamContentInfo* contentInfo, 
				MockStackEventNotifier* eventNotifier,
    			int localAudioPort, int localVideoPort,
                int sessionHandle)
{
    LOGGER_DEBUG(logger.get(), "--> create(inbound) ");    
  	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
  	jobject dummy(0);
  	jobject jContentInfo((jobject)contentInfo);
  	jobject jEventNotifier((jobject)eventNotifier); 

	Java_com_mobeon_masp_stream_CCRTPSession_create__Lcom_mobeon_masp_stream_StreamContentInfo_2Lcom_mobeon_masp_stream_StackEventNotifier_2IIJ
  	(env, dummy, 
  		jContentInfo, jEventNotifier, 
  		(jint)localAudioPort, (jint)localVideoPort,
    	(jlong)sessionHandle);
    LOGGER_DEBUG(logger.get(), "<-- create(inbound) ");    
}
                	
void MockCCRTPSession::create(MockStreamContentInfo* contentInfo,
                MockStackEventNotifier* eventNotifier,
                int localAudioPort, int localVideoPort,
                base::String audioHost, int remoteAudioPort,
                base::String videoHost, int remoteVideoPort,
                int mtu, int sessionHandle, int inboundSession)
{
    LOGGER_DEBUG(logger.get(), "--> create(outbound) ");    
    JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
    jobject dummy(0);
    jobject jContentInfo((jobject)contentInfo);
    jobject jEventNotifier((jobject)eventNotifier); 
    jstring jAudioHost((jstring)audioHost.c_str());
    jstring jVideoHost((jstring)videoHost.c_str());
    
    Java_com_mobeon_masp_stream_CCRTPSession_create__Lcom_mobeon_masp_stream_StreamContentInfo_2Lcom_mobeon_masp_stream_StackEventNotifier_2IILjava_lang_String_2ILjava_lang_String_2IIJJ
  	(env, dummy, jContentInfo, jEventNotifier, 
         (jint)localAudioPort, (jint)localVideoPort, 
         jAudioHost, (jint)remoteAudioPort, 
         jVideoHost, (jint)remoteVideoPort, 
         (jint)mtu, (jlong)sessionHandle, (jlong)inboundSession);
    LOGGER_DEBUG(logger.get(), "<-- create(outbound) ");    
}



void MockCCRTPSession::destroy(SessionSupport* handle, int requestId)
{
    LOGGER_DEBUG(logger.get(), "--> destroy()");    
	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
	jobject jobj(0);
	Java_com_mobeon_masp_stream_CCRTPSession_delete
  	(env, jobj, (jlong)handle, (jint)requestId);
    LOGGER_DEBUG(logger.get(), "<-- destroy()");    
}

void MockCCRTPSession::send()
{
  	JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
  	jobject dummy(0);
  	jobjectArray tokens(0);
  	jint nrOfTokens(0);
  	jint handle(0);
	Java_com_mobeon_masp_stream_CCRTPSession_send
  	(env, dummy, tokens, nrOfTokens, handle);
}

void MockCCRTPSession::stop(MockObject* mockObject, SessionSupport* sessionSupport)
{
    JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
    jobject dummy(0);
    jobject callInfo((jobject)mockObject); 
    jlong handle((jlong)sessionSupport);

    Java_com_mobeon_masp_stream_CCRTPSession_stop
    (env, dummy, callInfo, handle);
}

void MockCCRTPSession::cancel(MockObject* mockObject, SessionSupport* sessionSupport)
{
    JNIEnv* env((JNIEnv*)&MockJavaVM::instance());
    jobject callInfo((jobject)mockObject); 
    jlong handle((jlong)sessionSupport);

    Java_com_mobeon_masp_stream_CCRTPSession_cancel
    (env, callInfo, handle);
}


  void MockCCRTPSession::join(SessionSupport* outboundHandle, SessionSupport* inboundHandle)
{
    LOGGER_DEBUG(logger.get(), "--> join()");    
	JNIEnv *env((JNIEnv*)&MockJavaVM::instance()); 
	jobject dummy(0);
	Java_com_mobeon_masp_stream_CCRTPSession_join
	(env, dummy, (jlong)outboundHandle, (jlong)inboundHandle, (jboolean)true, (jboolean)true);
    LOGGER_DEBUG(logger.get(), "<-- join()");    
}

void MockCCRTPSession::unjoin(SessionSupport* outboundHandle, SessionSupport* inboundHandle)
{
    LOGGER_DEBUG(logger.get(), "--> unjoin()");    
	JNIEnv *env((JNIEnv*)&MockJavaVM::instance()); 
	jobject dummy(0);
	Java_com_mobeon_masp_stream_CCRTPSession_unjoin
	(env, dummy, (jlong)outboundHandle, (jlong)inboundHandle);
    LOGGER_DEBUG(logger.get(), "<-- unjoin()");    
}
