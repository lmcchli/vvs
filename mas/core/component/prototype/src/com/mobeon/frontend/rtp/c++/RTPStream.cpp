#include "RTPStream.h"

#include <iostream>
#include <fstream>
#include <string.h>
#include <sys/stat.h>
#include "wavehelper.h"
#include <assert.h>

//#define BUFFSIZE (1024*8)/10
#define TIMESTAMPINC 10
#define BUFFSIZE 8000/TIMESTAMPINC
#define DELAY 1.0/TIMESTAMPINC

RTPStream::RTPStream(u_int16_t localport, u_int16_t destport, string destip, int payloadType) : RTPSession()
{
  #ifdef WIN32
	WSADATA dat;
	WSAStartup(MAKEWORD(2,2),&dat);
  #endif // WIN32
  init(localport, destport, destip, payloadType);
}


RTPStream::~RTPStream() {
  BYEDestroy(RTPTime(10,0),0,0);
  endAutoScanner();
#ifdef WIN32
	WSACleanup();
#endif //WIN32
	cerr << "Native RTP stream released" << endl;
}

void 
RTPStream::init(u_int16_t _localport, u_int16_t _destport, string _destip, 
		int _payloadType) 
{
  int status;
  localport = _localport;
  destport = _destport;
  destip = inet_addr(_destip.c_str());
  destip =  ntohl(destip);
  // One sample each second
  sessparams.SetOwnTimestampUnit(TIMESTAMPINC);		 
  sessparams.SetAcceptOwnPackets(true);
  transparams.SetPortbase(localport);
  status = Create(sessparams,&transparams);
  if (status < 0) {
    cerr << "Failed to create session" << endl;
  }
  RTPIPv4Address addr(destip,destport);	
  status = AddDestination(addr);
  if (status < 0)
    throw "Failed to create RTP session";
  AddToAcceptList(addr);
  SetDefaultPayloadType(_payloadType);
  payloadType = _payloadType;
  SetDefaultMark(false);
  SetDefaultTimestampIncrement(TIMESTAMPINC);
  interrupted = false;
  lastDTMFseqNr = 0;
  
  if (mutex.Init() < 0)
    cerr << "Cant initialize mutex in RTPStream!" << endl;
  if (scannerMutex.Init() < 0)
    cerr << "Cant initialize scannerMutex in RTPStream!" << endl;
  endScanner = false;
}

void 
RTPStream::setJavaCallbackEntities(JNIEnv *e, jclass javaclass) {
  env = e;
  jClass = javaclass;
}

void 
RTPStream::setJavaCallbackEntities(JNIEnv *e, jobject javaobject) {
  env = e;
  jObject = javaobject;
}

void 
RTPStream::putDTMFToken(jint token, JNIEnv* jenv, jobject jobj) {
  if (jenv) {
    printf("Sending DTMF %d to java side\n", token);
    cout.flush();
    if (jobj) {

      jclass cls1 = jenv->GetObjectClass(jobj);
      if (cls1 == 0) {
	cerr << "Failed to retrieve java class reference!" << endl;
	return; 
      }
      jclass cls = (jclass) jenv->NewGlobalRef(cls1);
      if (cls == 0) {
	cerr << "Failed to retrieve java class reference!" << endl;
	return; 
      }

      jmethodID mid = jenv->GetMethodID(cls, "addControlToken", "(I)V");
      if (mid == 0) {
	return;
      }
      cout.flush();
      jenv->CallVoidMethod(jobj, mid, token);
      jenv->DeleteGlobalRef(cls);
    }
    else {
      cerr << "Error: jObj is null, can't call back to javaspace!" << endl;
    }
  }
  else {
    cerr << "ENV is NULL!" << endl;
  }
}


bool
RTPStream::handleDTMF(RTPPacket* pack, JNIEnv* jenv, jobject jobj) {
  char *data = (char*) pack->GetPayloadData();
  
  if (data != NULL) {
      int endMarker = (data[1] >> 4);
    int seqNr = pack->GetSequenceNumber();
    if (!pack->HasMarker() && seqNr != lastDTMFseqNr && endMarker != 0) {
	lastDTMFseqNr = seqNr;
	int len = pack->GetPayloadLength();
	if (len > 1) {
	    putDTMFToken((jint) data[0], jenv, jobj);
	    return true;
	}
    }
    else {
	// Discard the package since it does not have the end marker
	// cout << "Discarding DTMF message (end not found yet or same sequence nr)" << endl;
	// cout << "SeqNr = " << seqNr << " endMarker = " << endMarker << " hasMarker = " << pack->HasMarker() << endl;
    }
    return false;
  }
  else {
    cerr<< "Got DTMF, but the data length was NULL!" << endl;
    return false;
  }
}

bool
RTPStream::handleDTMF(RTPPacket* pack) {
  return handleDTMF(pack, env, jObject);
}

int 
RTPStream::sendPacket(u_int8_t buf[], int bufsize, bool interruptable,JNIEnv* env, jobject jobj)
{
  int ret;
  bool handled = false;
  // ret = SendPacket(buf, bufsize,payloadType,false,TIMESTAMPINC);
  ret = SendPacket(buf, bufsize);
  if (ret < 0) {
      return ret;
  }

  BeginDataAccess();
  
  // check incoming packets
  if (GotoFirstSourceWithData())

    {
      do
	{
	  RTPPacket *pack;
	  
	  while (!isInterrupted() && (pack = GetNextPacket()) != NULL)
	    {
	      if (pack->GetPayloadType() != payloadType) {
		char pt[5];
		sprintf(pt,"%d", pack->GetPayloadType());
		//		cout << "Got packet with payloadtype (in sendPacket)" << pt << "." << endl;
		handled = handleDTMF(pack, env, jobj);
		if (handled && interruptable) {
		  ret = 1;
		}
	      }
	      delete pack;
	      if (ret == 1) {
		EndDataAccess();
		return ret;
	      }
	    }
	} while ( !isInterrupted() && GotoNextSourceWithData());
    }
  
  EndDataAccess();
  return 0;
}

int
RTPStream::sendFile(string filename, bool interruptable,JNIEnv* env, jobject jobj) 
{
  u_int8_t buffer[BUFFSIZE];
  scannerMutex.Lock(); 
  //  cout << "opening file " << filename << endl;
  ifstream fin(filename.c_str(),ios::in | ios::binary);
 

  if (!fin.is_open()) {
    cerr << "Failed to open file " << filename << endl;
      resetInterrupted();
      scannerMutex.Unlock();
      return -1;

  }
  int ret;
  setInterrupted(false);

  // Skip header of the media file
  // int headerLen =  sizeof(riffBlock) + sizeof(formatBlock) + sizeof(dataBlock);
  int headerLen =  0x40; // Packets MUST be octet alligned for the G711 encoder to work
  assert(headerLen <= BUFFSIZE);
  if (!fin.eof()) {
    fin.read((char*) &buffer,headerLen);
  }
  while(!isInterrupted() && !fin.eof()) {
      int numread;
      memset(buffer, 128 ,sizeof(buffer)); // Fill buffer with silence
      fin.read((char *)&buffer,sizeof(buffer));
      numread = fin.gcount();
      // Skip packets, if not successfully read a full buffer
      if (numread == sizeof(buffer)) {
	  ret = sendPacket(buffer, sizeof(buffer), interruptable,env, jobj);
	  if (ret < 0) {
	      cerr << "Failed to send packet! Ret = " << ret << endl;
	      resetInterrupted();
	      scannerMutex.Unlock();
	      return -1;
	  }
	  RTPTime::Wait(RTPTime(DELAY));
      }
  }
  resetInterrupted();
  scannerMutex.Unlock();
  return 0;
}

int
RTPStream::receivePackets(ofstream *outfile, bool interruptable,JNIEnv* env, jobject jobj) 
{
  int ret = -1;
  bool handled = false;
  BeginDataAccess();
  if (GotoFirstSourceWithData()) { 
    ret = 0;
    do
      {
	RTPPacket *pack;
	
	while (!isInterrupted() && (pack = GetNextPacket()) != NULL) {
	  // You can examine the data here
	  char pt[5];
	  if (pack->GetPayloadType() != payloadType) {
	    sprintf(pt,"%d", pack->GetPayloadType());
	    //	    cout << "Got packet with payloadtype " << pt << "." << endl;
	    handled = handleDTMF(pack,env, jobj);
	    if (handled && interruptable) {
	      ret = 1;
	    }
	  }
	  else {
	    outfile->write((char*) pack->GetPayloadData(), pack->GetPayloadLength());
	  }
	  // we don't longer need the packet, so
	  // we'll delete it
	  delete pack;
	  if (ret == 1) {
	    EndDataAccess();
	    return ret;
	  }
	}
      } while (!isInterrupted() && GotoNextSourceWithData());
  }
  
  EndDataAccess();
  return ret;
}

int
RTPStream::receive(string filename, bool interruptable,JNIEnv* env, jobject jobj) {
  string datafile =  filename + "_data";
  ofstream outfile(datafile.c_str(),ios::out | ios::binary);
  ofstream wavfile(filename.c_str(),ios::out | ios::binary);

  scannerMutex.Lock();  
  setInterrupted(false);
  int tries = 0;
  int intr = 0;
  //  cout << "Waiting for data..." << endl;
  while(!isInterrupted() && (intr=receivePackets(&outfile, interruptable,env,jobj)) < 0) {
    RTPTime::Wait(RTPTime(0,100000));
  }
  if (intr == 1) { // We got interrupted
    //    cout << "Interrupted!" << endl;
    outfile.close();
    makeWavFile(outfile, wavfile, datafile);
    resetInterrupted();
    scannerMutex.Unlock();
    return 1;
  }
  //  cout << "OK, packets are arriving, store them until stream closes!" << endl;
  int status;
  while(!isInterrupted() && (status =receivePackets(&outfile,interruptable, env, jobj)) != 1 && 
	(!isInterrupted() &&  tries < 20)) {
    RTPTime::Wait(RTPTime(0,100000));
    if (status == -1)
      tries++;
  }
  outfile.close();
  makeWavFile(outfile, wavfile, datafile);
  resetInterrupted();
  scannerMutex.Unlock();
  return 0;
}

void 
RTPStream::makeWavFile(ofstream &outfile, ofstream &wavfile, string filename) {
  // Write header
  wavehelper wh;
  int sampleRate = 8000;
  short bytesPerSample = 1;
  // find the size in seconds of the data
  struct stat result;
  stat(filename.c_str(),&result); 
  
  wh.makeHeader(wavfile ,sampleRate,
		(long) result.st_size,
		bytesPerSample);
  ifstream samplefile(filename.c_str(), ios::in | ios::binary);
  char buffer[BUFFSIZE];
  while (!samplefile.eof()) {
    samplefile.read(buffer, sizeof(buffer));
    wavfile.write(buffer, sizeof(buffer));
  }
  samplefile.close();
  wavfile.close();
}




bool
RTPStream::scan(JNIEnv* env, jobject jobj) {
  scannerMutex.Lock();
  bool ret = false;
  while ( !isInterrupted() && !ret) {
    BeginDataAccess();
    if (GotoFirstSourceWithData()) { 
      do
	{
	  ret = false;
	  RTPPacket *pack;
	  
	  while (!isInterrupted() && (pack = GetNextPacket()) != NULL) {
	    // You can examine the data here
	    char pt[5];
	    if (pack->GetPayloadType() != payloadType) {
	      sprintf(pt,"%d", pack->GetPayloadType());
	      //	      cout << "Got packet with payloadtype " << pt << "." << endl;
	      ret = handleDTMF(pack, env,jobj);
	    }
	    delete pack;
	    if (ret) {
	      EndDataAccess();
	      resetInterrupted();
	      scannerMutex.Unlock();
	      return ret;
	    }
	    
	  }
	} while (!isInterrupted() && GotoNextSourceWithData());
    }
    
    EndDataAccess();
    RTPTime::Wait(RTPTime(0,100000));
  }
  resetInterrupted();
  scannerMutex.Unlock();
  return ret;
}


void
RTPStream::setInterrupted(bool val) {
  mutex.Lock();
  interrupted=val;
  mutex.Unlock();
}

bool
RTPStream::isInterrupted() {
  bool ret;
  mutex.Lock();
  ret = interrupted;
  mutex.Unlock();
  return ret;

}

void
RTPStream::interrupt() {
  setInterrupted(true);
}

void
RTPStream::resetInterrupted() {
  if (isInterrupted())
    setInterrupted(false);
}


void
RTPStream::autoScanner(JNIEnv* e, jobject jobj) {
  bool hasDTMF = false;
  while(!getEndAutoScanner()) {
    scannerMutex.Lock();
    BeginDataAccess();
    if (GotoFirstSourceWithData()) { 
      do
	{
	  hasDTMF = false;
	  RTPPacket *pack;
	  
	  while ((pack = GetNextPacket()) != NULL) {
	    // You can examine the data here
	    char pt[5];
	    if (pack->GetPayloadType() != payloadType) {
	      sprintf(pt,"%d", pack->GetPayloadType());
	      //	      cout << "Got packet with payloadtype: " << pt << "." << endl;
	      cout.flush();

	      hasDTMF = handleDTMF(pack, e, jobj);
	    }
	    delete pack;
	    if (hasDTMF) {
	      EndDataAccess();
	      // break;
	    }	    
	  }
	} while (GotoNextSourceWithData());
    }
    
    EndDataAccess();
    scannerMutex.Unlock();
    RTPTime::Wait(RTPTime(0,100000));
  }
  cout << "Leaving autoscanner... Strange!" << endl;
}

void
RTPStream::endAutoScanner() {
  scannerMutex.Lock();
  endScanner = true;;
  scannerMutex.Unlock();
}

bool
RTPStream::getEndAutoScanner() {
  bool ret;
  scannerMutex.Lock();
  ret = endScanner;
  scannerMutex.Unlock();
  return ret;
}


