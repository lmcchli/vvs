#ifndef MOCKCCRTPSESSION_H_
#define MOCKCCRTPSESSION_H_

#include <logger.h>
#include <base_include.h>

class SessionSupport;
class MockObject;
class MockMediaObject;
class MockMediaStream;
class MockMediaProperties;
class MockRecordingProperties;
class MockStreamContentInfo;
class MockStreamConfiguration;
class MockStackEventNotifier;

class MockCCRTPSession
{
public:
	MockCCRTPSession();
	virtual ~MockCCRTPSession();
	
        void initConfiguration(MockStreamConfiguration* configuration);
	SessionSupport* createInboundSession(MockMediaStream* stream);
	SessionSupport* createOutboundSession(MockMediaStream* stream);
	void create(MockStreamContentInfo* contentInfo, 
				MockStackEventNotifier* eventNotifier,
    			int localAudioPort, int localVideoPort,
                int sessionHandle);
    void create(MockStreamContentInfo* contentInfo,
                MockStackEventNotifier* eventNotifier,
                int localAudioPort, int localVideoPort,
                base::String audioHost, int remoteAudioPort,
                base::String videoHost, int remoteVideoPort,
                int mtu, int sessionHandle, int inboundSession);                
	void play(MockObject* mockObject,
          int requestId,
		  MockMediaObject* mockMediaObject, 
		  int playOption, int cursor,
		  SessionSupport* sessionSupport);
	void record(MockObject* mockObject, 
        MockMediaObject* mockMediaObject,
        MockRecordingProperties* mockMediaRecordingProperties,
        int sessionSupport);
    void stop(MockObject* mockObject, SessionSupport* sessionSupport);
    void cancel(MockObject* mockObject, SessionSupport* sessionSupport);
	void destroy(SessionSupport* handle, int requestId=-1);
	void send();
	void join(SessionSupport* outboundHandle, SessionSupport* inboundHandle);
	void unjoin(SessionSupport* outboundHandle, SessionSupport* inboundHandle);

private:
    std::auto_ptr<Logger> logger;	
};

#endif /*MOCKCCRTPSESSION_H_*/
