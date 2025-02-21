#include "MockStackEventNotifier.h"

enum {
    PlayFinished = 1,
    PlayFailed,
    RecordFinished,
    RecordFailed
};

MockStackEventNotifier::MockStackEventNotifier() 
	: MockObject("StackEventNotifier")
{
}

MockStackEventNotifier::~MockStackEventNotifier()
{
}

int MockStackEventNotifier::getMethodId(const base::String& name, const base::String& signature)
{
    if (name == "playFinished") { // void
        return PlayFinished;
    } else if (name == "playFailed") { // void
        return PlayFailed;
    } else if (name == "recordFinished") { // void
        return RecordFinished;
    } else if (name == "recordFailed") { // void
        return RecordFailed;
    } 

    return 0;
}  

void* MockStackEventNotifier::callMethod(int methodId, va_list& args)
{
    switch (methodId) {

    case PlayFinished:
        return (void*)0;
        break;
        
    case PlayFailed:
        return (void*)0;
        break;
        
    case RecordFinished:
        return (void*)0;
        break;
        
    case RecordFailed:
        return (void*)0;
        break;
 
               
    default:
        break;
    }

    return (void*)0;    
}

