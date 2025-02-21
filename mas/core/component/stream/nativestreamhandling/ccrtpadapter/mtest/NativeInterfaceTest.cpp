#include "NativeInterfaceTest.h"

#include "nativestreamhandling.h"

#include "Processor.h"

void 
NativeInterfaceTest::setUp()
{
    ProcessorGroup::shutdown();
}

void 
NativeInterfaceTest::tearDown()
{
}

void
NativeInterfaceTest::testInitialize()
{
    int nOfOutputs(4);
    int nOfInputs(1);
    CPPUNIT_ASSERT_EQUAL(0, ProcessorGroup::instance().getOutputCount());
    CPPUNIT_ASSERT_EQUAL(0, ProcessorGroup::instance().getInputCount());
    Java_com_mobeon_masp_stream_jni_NativeStreamHandling_initialize((JNIEnv*)0, (jclass)0, 
        (jint)nOfOutputs, (jint)nOfInputs);
    CPPUNIT_ASSERT_EQUAL(nOfOutputs, ProcessorGroup::instance().getOutputCount());
    CPPUNIT_ASSERT_EQUAL(nOfInputs, ProcessorGroup::instance().getInputCount());
}

