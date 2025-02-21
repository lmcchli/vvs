package com.abcxyz.services.moip.ntf.coremgmt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;



@RunWith(Suite.class)

@Suite.SuiteClasses({
    NtfRetryEventHandlerTest.class,
    NtfCmnManagerTest.class,
    NtfEventTest.class,
    ConfigTest.class,
    NtfMessageServiceTest.class
})


public class CmnEventTestSuite
{

}
