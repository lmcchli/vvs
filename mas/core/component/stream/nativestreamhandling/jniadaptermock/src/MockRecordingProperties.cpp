#include "MockRecordingProperties.h"

#include <string>

using std::string;

enum {
	IsSilenceDetectionForStart = 1,
    IsSilenceDetectionForStop,
    GetMaxWaitBeforeRecord,
    GetMaxRecordingDuration,
	GetMinRecordingDuration,
	IsWaitForRecordToFinish,
	GetMaxSilence,
        GetTimeout,
	GetRecordingType
};

MockRecordingProperties::MockRecordingProperties() 
	: MockObject("RecordingProperties"),
      isSilenceDetectionForStart(false),
      isSilenceDetectionForStop(false),
      getMaxWaitBeforeRecord(0),
      getMaxRecordingDuration(6000),
	  getMinRecordingDuration(0),
	  isWaitForRecordToFinish(false),
	  getMaxSilence(0),
          getTimeout(0),
	  getRecordingType(1)

{
}

MockRecordingProperties::~MockRecordingProperties()
{
}

int MockRecordingProperties::getMethodId(const std::string& name, const std::string& signature)
{
	if (name == "isSilenceDetectionForStart") {
		return IsSilenceDetectionForStart;
	} else if (name == "isSilenceDetectionForStop") {
		return IsSilenceDetectionForStop;
	} else if (name == "getMaxWaitBeforeRecord") {
		return GetMaxWaitBeforeRecord;
	} else if (name == "getMaxRecordingDuration") {
		return GetMaxRecordingDuration;
	} else if (name == "getMinRecordingDuration") {
		return GetMinRecordingDuration;
	} else if (name == "isWaitForRecordToFinish") {
		return IsWaitForRecordToFinish;
	} else if (name == "getMaxSilence") {
		return GetMaxSilence;
        } else if (name == "getTimeout") {
                return GetTimeout;
	} else if (name == "getRecordingType") {
		return GetRecordingType;
	}
	return 0;
}  

void* MockRecordingProperties::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
    case IsSilenceDetectionForStart:
		return (void*)&isSilenceDetectionForStart;
        break;

    case IsSilenceDetectionForStop:
        return (void*)&isSilenceDetectionForStop;
        break;

    case GetMaxWaitBeforeRecord:
        return (void*)&getMaxWaitBeforeRecord;
        break;

    case GetMaxRecordingDuration:
        return (void*)&getMaxRecordingDuration;
        break;

    case GetMinRecordingDuration:
        return (void*)&getMinRecordingDuration;
        break;

    case IsWaitForRecordToFinish:
        return (void*)&isWaitForRecordToFinish;
        break;

    case GetMaxSilence:
        return (void*)&getMaxSilence;
        break;

    case GetTimeout:
        return (void*)&getTimeout;
        break;

    case GetRecordingType:
        return (void*)&getRecordingType;
        break;

	default:
		break;
	}
    return 0;    
}
