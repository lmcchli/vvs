//#include "TestBench.h"
#include "MockCCRTPSession.h"
#include "MockNativeStreamHandling.h"
#include "MockMediaStream.h"
#include "MockMediaProperties.h"
#include "MockStreamContentInfo.h"
#include "MockStreamConfiguration.h"
#include "MockRTPPayload.h"
#include "MockStackEventNotifier.h"
#include "MockMediaObject.h"
#include "MockMediaLength.h"
#include "MockJavaVM.h"
#include "MockMimeType.h"
#include "MockMediaObjectNativeAccess.h"
#include "MockMediaObjectIterator.h"
#include "MockByteBuffer.h"
#include "MockLengthUnit.h"
#include "MockRecordingProperties.h"

#include <sessionsupport.h>
#include "RtpBlockHandlerTest.h"
#include "OutboundStreamTest.h"
#include "InboundStreamTest.h"
#include "NativeInterfaceTest.h"
#include "CallbackQueueTest.h"
#include "CallbackQueueHandlerTest.h"
#include "OutputProcessorTest.h"
#include "mediahandler.h"

#include <cppunit/ui/text/TestRunner.h>

#include <jniutil.h>
#include <iostream>
#include <base_include.h>
#include <logger.h>
#include <log4cxx/level.h>

#if defined(WIN32)
#include "windows.h"
#else
#include <unistd.h>
#include <time.h>
#endif
#include <string.h>
#include <string>

using std::cout;
using std::endl;

int g_localPort(4712);
std::string addParam1 = "ADDPARAM=";

void executeCppUnitTesting(const base::String& address, int port);
void testPlayAudio(const base::String& address, int port);
void testPlayVideo(const base::String& address, int port);
void testRecord(const base::String& address, int port);
void testJoinUnjoin(const base::String& address, int port);
void testOutbound();

// This is a real test case based upon TR #27990 
void testSequenceNumber(const base::String& address, int port);
// This is a real test case based upon TR #NNNNN 
void testSessionId(const base::String& sessionId);
void testPlayAudioAndInbound(const base::String& address, int port);
void testPlayVideoAndInbound(const base::String& address, int port);

bool isAlphanum (char alpha){
	switch (alpha)
	{
	case '0' :
		return true;
	case '1' :
		return true;
	case '2' :
		return true;
	case '3' :
		return true;
	case '4' :
		return true;
	case '5' :
		return true;
	case '6' :
		return true;
	case '7' :
		return true;
	case '8' :
		return true;
	case '9' :
		return true;
	}
	return false;
}

int main(int argc, char** argv)
{

	log4cxx::LevelPtr l1 = log4cxx::Level::OFF;
	log4cxx::LevelPtr l2 = log4cxx::Level::FATAL;
	log4cxx::LevelPtr l3 = log4cxx::Level::ERROR;
	log4cxx::LevelPtr l4 = log4cxx::Level::WARN;
	log4cxx::LevelPtr l5 = log4cxx::Level::INFO;
	log4cxx::LevelPtr l6 = log4cxx::Level::DEBUG;
    log4cxx::LevelPtr l7 = log4cxx::Level::ALL;

  Logger::init("stream.log.properties");
  MockJavaVM::instance();
  int testCaseNumber(-1);
  base::String receiverAddress;
  int receiverPort(-1);
  int size_int;
  if (argc == 4) {
      testCaseNumber = atoi(argv[1]);
      receiverAddress = argv[2];
      receiverPort = atoi(argv[3]);
  } else {
      if (argc == 5) {
      	std::string addParam2 = "";
	addParam2.append(argv[4],9);
	if (addParam1.compare(addParam2) == 0) {
	
	        cout << "  ADDPARAM found" << endl;
	      
	   	std::string size_val = "";
		std::string size_val1 = "";
		size_val1.append(argv[4]);
		size_val.append(size_val1, 9, size_val1.size());
		if (size_val.size() == 0){
			cout << "Error: No ADDPARAM value has been specified" << endl;
			testCaseNumber--;
			//return 0;
		}
			
		//convert rtpsize_val (char string) into integer

		unsigned int i = 0;
		for (i=0; i< size_val.size(); i++){
			if (!isAlphanum(size_val[i])){
				cout << "Error: 'ADDPARAM' digit: '" << size_val[i] << "' is not alphanumeric\n";
				testCaseNumber--;
				//return 0;
			}
		}
		
		if (testCaseNumber == -1) {
		   size_int = atoi(size_val.c_str());   
	      
	           cout << "  ADDPARAM found, ADDPARAM=" << size_int << endl;
	      
	           testCaseNumber = atoi(argv[1]);
      		   receiverAddress = argv[2];
      		   receiverPort = atoi(argv[3]);
      		}
        } 
      }
  
  
   if (testCaseNumber<0) {
      cout << "Syntax: " << argv[0] << " <test case number> <reciever ip> <receiver port>" << endl;
      cout << "  0: executeCppUnitTesting" << endl;
      cout << "  1: testPlayAudio (wav)" << endl;
      cout << "  2: testPlayVideo (mov)" << endl
           << "  3: testRecord" << endl
           << "  4: testJoinUnjoin" << endl
           << "  5: testOutbound" << endl
           << "  6: testSequenceNumber" << endl
           << "  7: testSessionId" << endl
           << "  8: testPlayAudioAndInbound" << endl
           << "  9: testPlayVideoAndInbound" << endl
           
           
           << "or Syntax: " << argv[0] << " <test case number> <reciever ip> <receiver port> ADDPARAM=<number of test case specific parameters, n> <TC param 1> ... <TC param n>" << endl;
           
           
      }            
  }

  JNIUtil::init((JavaVM*)&(MockJavaVM::instance()));
  MockJavaVM::instance().addObject("medialibrary.MediaObject", new MockMediaObject());
  MockJavaVM::instance().addObject(new MockMediaObject());
  MockJavaVM::instance().addObject(new MockMediaLength());
  MockJavaVM::instance().addObject(new MockMediaObjectNativeAccess());
  MockJavaVM::instance().addObject(new MockMediaObjectIterator());
  MockJavaVM::instance().addObject(new MockStreamConfiguration());
  MockJavaVM::instance().addObject(new MockRecordingProperties());
  MockJavaVM::instance().addObject(new MockStreamContentInfo());
  MockJavaVM::instance().addObject(new MockStackEventNotifier());
  MockJavaVM::instance().addObject(new MockMediaStream());
  MockJavaVM::instance().addObject("IInboundMediaStream", new MockMediaStream());
  MockJavaVM::instance().addObject(new MockRTPPayload());
  MockJavaVM::instance().addObject(new MockMediaProperties());
  MockJavaVM::instance().addObject(new MockMimeType());
  MockJavaVM::instance().addObject(new MockByteBuffer());
  MockJavaVM::instance().addObject(new MockLengthUnit());
  
  switch (testCaseNumber) {
  case 0:
      executeCppUnitTesting(receiverAddress, receiverPort);
      break;

  case 1:
      testPlayAudio(receiverAddress, receiverPort);
      break;

  case 2:
      for (int i(0); i < 10; i++) {
        cout << "Starting iteration #" << i << " ..." << endl;
        testPlayVideo(receiverAddress, receiverPort);
        cout << "Ended iteration #" << i << "." << endl;
      }
      break;

  case 3:
      testRecord(receiverAddress, receiverPort);
      break;

  case 4:
      testJoinUnjoin(receiverAddress, receiverPort);
      break;

  case 5:
      testOutbound();
      break;

  case 6:
      testSequenceNumber(receiverAddress, receiverPort);
      break;

  case 7:
      testSessionId(receiverAddress);
      break;
      
  case 8:
      testPlayAudioAndInbound(receiverAddress, receiverPort);
      break;
      
  case 9:
      testPlayVideoAndInbound(receiverAddress, receiverPort);
      break;

  default:
      break;
  }

  cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
  Sleep(6000);
#else
  sleep(2);
#endif

  exit(0);
}

void executeCppUnitTesting(const base::String& address, int port) {
    CppUnit::TextUi::TestRunner runner;

    runner.addTest( RtpBlockHandlerTest::suite() );
    runner.addTest( OutboundStreamTest::suite() );
    runner.addTest( InboundStreamTest::suite() );
    runner.addTest( NativeInterfaceTest::suite() );
    runner.addTest( CallbackQueueTest::suite() );
    runner.addTest( CallbackQueueHandlerTest::suite() );
    runner.addTest( OutputProcessorTest::suite() );

    // Run the test.
    bool wasSucessful = runner.run( "" );

    // Return error code 1 if the one of test failed.
    exit(wasSucessful ? 0 : 1);
}

void testPlayAudio(const base::String& address, int port)
{
  cout << "     *** Testing Play ***" << endl;
  int audioPort(port);
  int videoPort(port+2);
  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockCCRTPSession mockCCRTPSessionProxy;
  MockNativeStreamHandling mockNativeStreamHandling;

  cout << "Initializing onfiguration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
	
  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier; 
  
                                                     
  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  outboundStream.setCallSessionId("Nisse");

  cout << "Intializing outbound RTP session ..." << endl;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       2000, (long)outboundSession, 0);

  cout << "Issuing play ..." << endl;
  for (int i(0); i < 10; i++) {
      MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
      MockObject mockObject("Object");


      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);

      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(6000);
#else
      sleep(20);
#endif
  }

  cout << "Destroying the RTP session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
}

void testPlayVideo(const base::String& address, int port)
{
  cout << "     *** Testing Play ***" << endl;
  int audioPort(port);
  int videoPort(port+2);

  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockCCRTPSession mockCCRTPSessionProxy;
  MockNativeStreamHandling mockNativeStreamHandling;

  cout << "Initializing configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
	
  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier; 
  
                                                     

  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  outboundStream.setCallSessionId("Nisse");

  cout << "Intializing outbound RTP session ..." << endl;
  g_localPort = 23000;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       1500, (long)outboundSession, 0);

      cout << "Issuing play ..." << endl;
      //UM_0604.mov
      //MockMediaObject mockMediaObject(".", "rtphcarola", "mov", "video/quicktime", 500);
      MockMediaObject mockMediaObject(".", "UM_0604", "mov", "video/quicktime", 500);
      MockObject mockObject("Object");

      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);

      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(240000);
#else
      sleep(240);
#endif
      cout << "Destroying the RTP session ..." << endl;	
      mockCCRTPSessionProxy.destroy(outboundSession);
      cout << "     *** End of test  ***" << endl;
}

void testRecord(const base::String& address, int port)
{
  cout << "Starting tests ..." << endl;
  int audioPort(port);
  int videoPort(port+2);

  cout << "  Receiving on: " << audioPort << "," << videoPort << endl;
  MockNativeStreamHandling mockNativeStreamHandling;
  MockCCRTPSession mockCCRTPSessionProxy;

  MockMediaObject mockMediaObject;
  MockObject mockObject("Object");
  MockRecordingProperties mockRecordingProperties;

  cout << "Configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);

  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;

  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
	
  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream);

  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               audioPort, 
                               videoPort, 
                               (long)inboundSession);

  mockCCRTPSessionProxy.record(&mockObject, &mockMediaObject, &mockRecordingProperties, (long)inboundSession); 
  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60000);
#else
  sleep(60);
#endif

  cout << "Destroying the RTP sessions ..." << endl;	
  mockCCRTPSessionProxy.destroy(inboundSession);

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif
}

void testJoinUnjoin(const base::String& address, int port)
{
  cout << "Starting tests ..." << endl;
  int audioPort(port);
  int videoPort(port+2);

  cout << "  Receiving on: " << g_localPort << "," << (g_localPort+2) << endl;
  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockNativeStreamHandling mockNativeStreamHandling;
  MockCCRTPSession mockCCRTPSessionProxy;

  cout << "Configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);

  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;

  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
	
  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream);

  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               g_localPort, 
                               g_localPort+2, 
                               (long)inboundSession);

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
      mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  cout << "Intializing outbound RTP session ..." << endl;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       1000, (long)outboundSession, (long)inboundSession);

  cout << "Joining the RTP sessions ..." << endl;	
  mockCCRTPSessionProxy.join(outboundSession, inboundSession);

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(6000);
#else
  sleep(60);
#endif

      cout << "Issuing play ..." << endl;
      MockMediaObject mockMediaObject(".", "UM_0604", "mov", "video/quicktime", 500);
      MockObject mockObject("Object");

      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);

      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(12000);
#else
      sleep(10);
#endif

  cout << "Unjoining the RTP sessions ..." << endl;	
  mockCCRTPSessionProxy.unjoin(outboundSession, inboundSession);

  cout << "Destroying the RTP sessions ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  mockCCRTPSessionProxy.destroy(inboundSession);

  cout << "Deleting the sessions ..." << endl;

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif
}

void testOutbound()
{
  cout << "     *** Testing Outbund ***" << endl;
  MockCCRTPSession mockCCRTPSessionProxy;
  MockNativeStreamHandling mockNativeStreamHandling;

  cout << "Initializing onfiguration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
	
  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;
  
  MockMediaObject mockMediaObject(".", "beep", "wav", "audio/wav", 500);
  MockObject mockObject("Object");


  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  cout << "Intializing outbound RTP session ..." << endl;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       23000, 23002,
			       "127.0.0.1", 23004, 
			       "127.0.0.1", 23006, 
			       1500, (long)outboundSession, 0);

  cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif

  cout << "Destroying the RTP session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
}

void testSequenceNumber(const base::String& address, int port)
{
  cout << "     *** Testing Sequence Number ***" << endl;
  int audioPort(port);
  int videoPort(port+2);

  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockCCRTPSession mockCCRTPSessionProxy;
  MockNativeStreamHandling mockNativeStreamHandling;

  cout << "Initializing onfiguration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
	
  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;
  
  MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
  MockObject mockObject("Object");


  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  cout << "Intializing outbound RTP session ..." << endl;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       2000, (long)outboundSession, 0);

  cout << "Issuing play ..." << endl;
  mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);
#if defined(WIN32)
  Sleep(250);
#else
  sleep(1);
#endif
  cout << "Issuing stop ..." << endl;
  mockCCRTPSessionProxy.stop(&mockObject, outboundSession);
#if defined(WIN32)
  Sleep(250);
#else
  sleep(1);
#endif
  cout << "Issuing play ..." << endl;
  mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);

  cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
  Sleep(6000);
#else
  sleep(2);
#endif

  cout << "Destroying the RTP session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
}

void testSessionId(const base::String& sessionId)
{
  cout << "     *** Testing Session Id ***" << endl;
  MockCCRTPSession mockCCRTPSessionProxy;
  MockNativeStreamHandling mockNativeStreamHandling;

  cout << "Creating outbound stream ..." << endl;
  mockNativeStreamHandling.initialize();
  MockMediaStream outboundStream(sessionId);

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  cout << "Session ID: " << outboundSession->getCallSessionId() << endl;
  if (sessionId != outboundSession->getCallSessionId()) {
      cout << "ERROR: Test failed!" << endl;
  }

  cout << "     *** End of test  ***" << endl;
}

void testPlayAudioAndInbound(const base::String& address, int port)
{
  cout << "     *** Testing Play ***" << endl;
  int audioPort(port);
    //both using same port
  int videoPort(port+2);
  //both using same port
  
  cout << "  Receiving on: " << audioPort << "," << videoPort << endl;
  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockNativeStreamHandling mockNativeStreamHandling;
//2x
  MockCCRTPSession mockCCRTPSessionProxy;
           //2x

//  MockMediaObject mockMediaObject;
//  MockObject mockObject("Object");
//  MockRecordingProperties mockRecordingProperties;
  
  cout << "Initializing configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);

  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;
  
  	

			       
//============================================
//Setting up inbound stream
 
  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
	
  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream); 
  
  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               audioPort, 
                               videoPort, 
                               (long)inboundSession);

//============================================
//Setting up outbound stream
                                          
  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  outboundStream.setCallSessionId("Nisse");
  
  cout << "Intializing outbound RTP session ..." << endl;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       2000, (long)outboundSession, (long)inboundSession);
			       
			       
//============================================			       
			       

  cout << "Issuing play ..." << endl;
  for (int i(0); i < 20; i++) {
      MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
      MockObject mockObject("Object");

      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, -1, outboundSession);


      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(6000);
#else
      sleep(4);
#endif
  }
  cout << "Destroying the RTP outbound session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
  
//=============================================

//  mockCCRTPSessionProxy.record(&mockObject, &mockMediaObject, &mockRecordingProperties, (long)inboundSession); 
//  cout << "Sleep ..." << endl;
//#if defined(WIN32)
//  Sleep(60000);
//#else
//  sleep(60);
//#endif

  cout << "Destroying the RTP inbound sessions ..." << endl;	
  mockCCRTPSessionProxy.destroy(inboundSession);

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif
}

void testPlayVideoAndInbound(const base::String& address, int port)
{


// Additional parameters are indicated by ADDPARAM=<number of parameters>

  cout << "     *** Testing Play ***" << endl;
  int audioPort(port);
    //both using same port
  int videoPort(port+2);
  //both using same port
  
  cout << "  Receiving on: " << audioPort << "," << videoPort << endl;
  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockNativeStreamHandling mockNativeStreamHandling;
//2x
  MockCCRTPSession mockCCRTPSessionProxy;
           //2x

//  MockMediaObject mockMediaObject;
//  MockObject mockObject("Object");
//  MockRecordingProperties mockRecordingProperties;
  
  cout << "Initializing configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);

  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;
 
//Setting up inbound stream
 
  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
  
  inboundStream.setCallSessionId("Nisse2");
	
  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream); 
  
  g_localPort = 23000;
  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               g_localPort,         //audioPort, 
                               g_localPort+2,       //videoPort, 
                               (long)inboundSession);
 
  
//Setting up outbound stream

                
  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream("Nisse");

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  outboundStream.setCallSessionId("Nisse");
  
  cout << "Intializing outbound RTP session ..." << endl;
  
  g_localPort = 40000;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       1500, (long)outboundSession, (long)inboundSession);
			       		       
/*
//Setting up inbound stream
 
  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
  
  inboundStream.setCallSessionId("Nisse2");
	
  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream); 
  
  g_localPort = 41800;
  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               g_localPort,         //audioPort, 
                               g_localPort+2,       //videoPort, 
                               (long)inboundSession);
                               
*/
                               
  MockMediaObject mockMediaObjectIn;
  MockObject mockObjectIn("Object");
  MockRecordingProperties mockRecordingProperties;
  
  mockCCRTPSessionProxy.record(&mockObjectIn, &mockMediaObjectIn, &mockRecordingProperties, (long)inboundSession);
  
//============================================

#if defined(WIN32)
      Sleep(6000);
#else
      cout << "Sleep for a while ..." << endl;
      sleep(20);
#endif

  cout << "Issuing play ..." << endl;
  for (int i(0); i < 2; i++) {
      MockMediaObject mockMediaObject(".", "UM_0604", "mov", "video/quicktime", 500);
      MockObject mockObject("Object");

      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);

      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(6000);
#else
      sleep(20);
#endif
  }
  cout << "Destroying the RTP outbound session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
  
//=============================================

//  mockCCRTPSessionProxy.record(&mockObject, &mockMediaObject, &mockRecordingProperties, (long)inboundSession); 
  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60000);
#else
  sleep(60);
#endif

  cout << "Destroying the RTP inbound sessions ..." << endl;	
  mockCCRTPSessionProxy.destroy(inboundSession);

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif
}


//===================================================================================
//===================================================================================
//===================================================================================




//Original Code
/*
void testPlayVideoAndInbound(const base::String& address, int port)
{

  //One additional parameter will be used to indicate test case variation,
  //could be one parameter telling the length of the remaining command and then
  //follows the actual parameters.
  cout << "     *** Testing Play ***" << endl;
  int audioPort(port);
    //both using same port
  int videoPort(port+2);
  //both using same port
  
  cout << "  Receiving on: " << audioPort << "," << videoPort << endl;
  cout << "  Sending to: " << address << ":" << audioPort << "," << videoPort << endl;
  MockNativeStreamHandling mockNativeStreamHandling;
//2x
  MockCCRTPSession mockCCRTPSessionProxy;
           //2x

//  MockMediaObject mockMediaObject;
//  MockObject mockObject("Object");
//  MockRecordingProperties mockRecordingProperties;
  
  cout << "Initializing configuration .... " << endl;
  MockStreamConfiguration mockStreamConfiguration;
  mockNativeStreamHandling.initialize();
  mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);

  MockStreamContentInfo mockStreamContentInfo;
  MockStackEventNotifier mockStackEventNotifier;
	


                                          
  cout << "Creating outbound stream ..." << endl;
  MockMediaStream outboundStream;
  
  cout << "Creating inbound stream ..." << endl;
  MockMediaStream inboundStream;
  
//Setting up outbound stream

  cout << "Creating outbound RTP session instance ..." << endl;
  SessionSupport* outboundSession = 
    mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

  outboundStream.setCallSessionId("Nisse");
  
  cout << "Intializing outbound RTP session ..." << endl;
  
  g_localPort = 23000;
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
			       &mockStackEventNotifier, 
			       g_localPort, g_localPort+2,
			       address.c_str(), audioPort, 
			       address.c_str(), videoPort, 
			       1500, (long)outboundSession, (long)inboundSession);			       		       
  
//Setting up inbound stream
	
  cout << "Creating inbound RTP session instance ..." << endl;
  SessionSupport* inboundSession = 
      mockCCRTPSessionProxy.createInboundSession(&inboundStream); 
      
  inboundStream.setCallSessionId("Nisse2");
  
  cout << "Intializing inbound RTP session ..." << endl;	
  mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
                               &mockStackEventNotifier, 
                               audioPort, 
                               videoPort, 
                               (long)inboundSession);
                               
  MockMediaObject mockMediaObject;
  MockObject mockObject("Object");
  MockRecordingProperties mockRecordingProperties;
  
  mockCCRTPSessionProxy.record(&mockObject, &mockMediaObject, &mockRecordingProperties, (long)inboundSession);
  
//============================================
#if defined(WIN32)
      Sleep(6000);
#else
      cout << "Sleep for a while ..." << endl;
      sleep(20);
#endif
  

  cout << "Issuing play ..." << endl;
  for (int i(0); i < 2; i++) {
      MockMediaObject mockMediaObject(".", "UM_0604", "mov", "video/quicktime", 500);
      MockObject mockObject("Object");

      mockCCRTPSessionProxy.play(&mockObject, 0, &mockMediaObject, 0, 0, outboundSession);


      cout << "Sleep for a while ..." << endl;
#if defined(WIN32)
      Sleep(6000);
#else
      sleep(20);
#endif
  }
  cout << "Destroying the RTP outbound session ..." << endl;	
  mockCCRTPSessionProxy.destroy(outboundSession);
  cout << "     *** End of test  ***" << endl;
  
//=============================================

//  mockCCRTPSessionProxy.record(&mockObject, &mockMediaObject, &mockRecordingProperties, (long)inboundSession); 
//  cout << "Sleep ..." << endl;
//#if defined(WIN32)
//  Sleep(60000);
//#else
//  sleep(60);
//#endif

  cout << "Destroying the RTP inbound sessions ..." << endl;	
  mockCCRTPSessionProxy.destroy(inboundSession);

  cout << "Sleep ..." << endl;
#if defined(WIN32)
  Sleep(60);
#else
  sleep(10);
#endif
}
*/
