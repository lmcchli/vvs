#ifndef MockRecordingProperties_H_
#define MockRecordingProperties_H_

#include <MockObject.h>

#include <string>
#include <vector>

class MockRecordingProperties : public MockObject
{
public:
	MockRecordingProperties();
	virtual ~MockRecordingProperties();
	int getMethodId(const std::string& name, const std::string& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
public:
	bool isSilenceDetectionForStart;
    bool isSilenceDetectionForStop;
    int getMaxWaitBeforeRecord;
    int getMaxRecordingDuration;
	int getMinRecordingDuration;
	bool isWaitForRecordToFinish;
	int getMaxSilence;
	int getRecordingType;
    
};

#endif /*MockRecordingProperties_H_*/
