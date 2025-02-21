#include "MockObject.h"

MockObject::MockObject(const base::String& name) 
	: m_className(name)
{
    logger.reset(Logger::getLogger("mtest.MockObject"));
}

MockObject::~MockObject()
{
}

int MockObject::getMethodId(const base::String& name, const base::String& signature)
{
    LOGGER_DEBUG(logger.get(), "--> " << m_className << "::getMethodId(" << name << ")");
    LOGGER_DEBUG(logger.get(), " virtual <not implemented>");        
    LOGGER_DEBUG(logger.get(), "<-- getMethodId()");
    return 0;    
}

void* MockObject::callMethod(int methodId, va_list& args)
{
    LOGGER_DEBUG(logger.get(), "--> " << m_className << "::callMethod(" << methodId << ")");
    LOGGER_DEBUG(logger.get(), " virtual <not implemented>");        
    LOGGER_DEBUG(logger.get(), "<-- callMethod()");
    return 0;    
}

int MockObject::getStaticFieldId(const base::String& name, const base::String& signature)
{
    LOGGER_DEBUG(logger.get(), "--> " << m_className << "::getStaticFieldId(" << name << ")");
    LOGGER_DEBUG(logger.get(), " virtual <not implemented>");        
    LOGGER_DEBUG(logger.get(), "<-- getStaticFieldId()");
    return 0;    
}

MockObject* MockObject::getStaticObjectField(int fieldID)
{
    LOGGER_DEBUG(logger.get(), "--> " << m_className << "::getStaticObjectField(" << fieldID << ")");
    LOGGER_DEBUG(logger.get(), " virtual <not implemented>");        
    LOGGER_DEBUG(logger.get(), "<-- getStaticObjectField()");
    return 0;    
}

const base::String& MockObject::className()
{
	return m_className;
}
