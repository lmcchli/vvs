#include "OutputProcessorTest.h"

#include "OutputProcessor.h"
#include "CallbackQueueHandler.h"

void 
OutputProcessorTest::setUp()
{
    CallbackQueueHandler::clean();
}

void 
OutputProcessorTest::tearDown()
{
    CallbackQueueHandler::clean();
}

void
OutputProcessorTest::testInitialize()
{
    boost::ptr_vector<Processor> processors;
    OutputProcessor::setupProcessors(processors, 7);
    CPPUNIT_ASSERT_EQUAL(unsigned(7), unsigned(processors.size()));
    CPPUNIT_ASSERT_EQUAL(unsigned(7), unsigned(CallbackQueueHandler::instance().getQueueCount()));
}
