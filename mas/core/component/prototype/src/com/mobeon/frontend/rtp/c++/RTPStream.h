#ifndef RTPSTREAM_H
#define RTPSTREAM_H

#include "rtpconfig.h"

#include "rtpsession.h"
#include "rtppacket.h"
#include "rtpudpv4transmitter.h"
#include "rtpipv4address.h"
#include "rtpsessionparams.h"
#include "rtperrors.h"
#ifndef WIN32
	#include <netinet/in.h>
	#include <arpa/inet.h>
#else
	#include <winsock2.h>
#endif // WIN32
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <string>
#include <jni.h>
#include "jmutex.h"
using namespace std;


class RTPStream : public RTPSession {
 private:
  RTPUDPv4TransmissionParams transparams;
  RTPSessionParams sessparams;
  u_int16_t localport,destport;
  u_int32_t destip;
  string ipstr;
  int payloadType;
  bool interrupted;
  JNIEnv *env;
  jclass jClass;
  jobject jObject;
  int lastDTMFseqNr;
  JMutex mutex;
  JMutex scannerMutex;
  bool endScanner;

  void init(u_int16_t localport, u_int16_t destport, string destip,int payloadTyp);

  void putDTMFToken(jint token, JNIEnv* e, jobject obj);
  bool handleDTMF(RTPPacket* pack, JNIEnv* e, jobject obj);
  bool handleDTMF(RTPPacket* pack);

  void makeWavFile(ofstream &outfile, ofstream &wavfile, string filename);
  void resetInterrupted();

    
 public:
  RTPStream(u_int16_t localport, u_int16_t destport, string destip, int payloadType);
  ~RTPStream();
  
  int sendPacket(u_int8_t buf[], int bufsize, bool interruptable,JNIEnv* e, jobject obj);
  int sendFile(string filename, bool interruptable,JNIEnv* e, jobject obj);
  
  int receivePackets(ofstream *outfile, bool interruptable,JNIEnv* e, jobject obj);
  int receive(string filename, bool interruptable,JNIEnv* e, jobject obj);
  void setInterrupted(bool val);
  bool isInterrupted();
  bool scan(JNIEnv* e, jobject obj);
  void interrupt();

  void setJavaCallbackEntities(JNIEnv *e, jclass javaclass);
  void setJavaCallbackEntities(JNIEnv *e, jobject javaobject);

  void autoScanner(JNIEnv* e, jobject obj);
  void endAutoScanner();
  bool getEndAutoScanner();
};


#endif
