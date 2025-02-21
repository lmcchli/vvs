#ifndef _MockNativeStreamHandling_h_
#define _MockNativeStreamHandling_h_

#include <logger.h>
#include <base_include.h>

class Callback;

class MockNativeStreamHandling
{
public:
	MockNativeStreamHandling();
	virtual ~MockNativeStreamHandling();
	
    void initialize();
    Callback* getCallback(int queueId);

private:
    std::auto_ptr<Logger> logger;	
};

#endif /*_MockNativeStreamHandling_h_*/
