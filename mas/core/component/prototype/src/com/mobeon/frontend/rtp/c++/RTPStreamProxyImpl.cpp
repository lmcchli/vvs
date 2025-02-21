
#include "com_mobeon_frontend_rtp_RTPStream.h"
#include "RTPStream.h"
#include <iostream>

using namespace std;

/*
 * Class:     RTPStream
 * Method:    nativeRTPStream
 * Signature: (IILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeRTPStream
(JNIEnv *env, jclass jClass, jint localPort, jint remotePort, jstring destIpAddress, jint payloadType) {
  const char *destIp = env->GetStringUTFChars(destIpAddress, 0);
  
  jint handle =  (jint) (new RTPStream((int) localPort, (int) remotePort, destIp, (int) payloadType));
  ((RTPStream*) handle)->setJavaCallbackEntities(env, jClass);

  env->ReleaseStringUTFChars(destIpAddress, destIp);
  return handle;

} 

/*
 * Class:     RTPStream
 * Method:    nativeSendFile
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeSendFile
(JNIEnv *env , jobject jobj, jint handle, jstring filename, jboolean interruptable) {
  const char *fname = env->GetStringUTFChars(filename, NULL);
  jint ret = (jint) ((RTPStream*) handle)->sendFile(string(fname), interruptable, env, jobj);
  env->ReleaseStringUTFChars(filename, fname);
  return ret;
}

/*
 * Class:     RTPStream
 * Method:    nativeReceive
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeReceive
(JNIEnv *env, jobject jObj, jint handle, jstring filename, jboolean interruptable) {
  const char *fname = env->GetStringUTFChars(filename, 0);
 ((RTPStream*) handle)->setJavaCallbackEntities(env, jObj);
 jint ret = (jint) ((RTPStream*) handle)->receive(fname, interruptable, env, jObj);
  env->ReleaseStringUTFChars(filename, fname);
  return ret;
}



/*
 * Class:     com_mobeon_frontend_rtp_RTPStream
 * Method:    nativeScanStream
 * Signature: (I)V
 */
JNIEXPORT jboolean JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeScanStream
(JNIEnv *env, jobject jobj, jint handle) {
  ((RTPStream*) handle)->setJavaCallbackEntities(env, jobj);
  return ((RTPStream*) handle)->scan(env, jobj);
}


/*
 * Class:     com_mobeon_frontend_rtp_RTPStream
 * Method:    nativeInterrupt
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeInterrupt
(JNIEnv *env, jobject jobj, jint handle) {
  ((RTPStream*) handle)->interrupt();
}


/*
 * Class:     com_mobeon_frontend_rtp_RTPStream
 * Method:    nativeAutoScanner
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeAutoScanner
(JNIEnv *env, jobject jobj, jint handle) {
  ((RTPStream*) handle)->autoScanner(env, jobj);

}

/*
 * Class:     com_mobeon_frontend_rtp_RTPStream
 * Method:    nativeEndAutoScanner
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_frontend_rtp_RTPStream_nativeEndAutoScanner
(JNIEnv *env, jobject jobj, jint handle) {
  ((RTPStream*) handle)->endAutoScanner();

}
