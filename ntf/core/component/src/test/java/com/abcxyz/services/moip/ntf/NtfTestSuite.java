package com.abcxyz.services.moip.ntf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    NotificationBasicTest.class,
    NotificationGroupTest.class,
    //CmnEventTestSuite.class,
    NotificationMWIOffTest.class,
    NotificationMWIOnTest.class,
    NotificationHoldbackTest.class,
    NotificationSlamdownTest.class,
    //NotificationMcnTest.class,
    NotificationVvmTest.class,
    NotificationQuotaTest.class,
    NotificationEmailOutTest.class
    //NotificationMMSTest.class,
    //NotificationSipMwiTest.class
})

public class NtfTestSuite {
}
