package com.mobeon.masp.operateandmaintainmanager;

import junit.framework.TestCase;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import java.util.Vector;
import java.util.Hashtable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

/**
 * This cklass test some help classes
 */
public class MiscTest extends TestCase {
    ILogger log;
    OMManager omm = new OMManager();
    private int num = 0;
    private Throwable unexpectedThrowable = null;
    private StackTraceElement[] unexpectedStrackTrace;

    private class UCHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            unexpectedThrowable = e;
            unexpectedStrackTrace = e.getStackTrace();
        }
    }

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("MAS_INSTALL_PATH","/");
        System.setProperty("MAS_VERSION","TEST_VER");
        System.setProperty("MAS_HOST","localhost");
        System.setProperty("MAS_LOGICALZONE","undefined");


        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("logmanager.xml");
        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(eventDispatcher);
        omm.init();

    }

    public void tearDown() throws Exception {
        super.tearDown();
        try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void testNull() {
//        ILoggerFactory.configureAndWatch("logmanager.xml");
//        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

//        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
//        OMManager omm = new OMManager();
//        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
//        configuration.setConfigFile("mas.xml");
//        IConfiguration config = configuration.getConfiguration();

        try {
            omm.setConfigurationManager(null); // set config manager to be able to reload config
            assertTrue(false);
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        omm.setConfiguration(null);               // set configuration for om

        try {
            omm.setEventDispatcher(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        try {
            omm.init();
            assertTrue(false);
        } catch (NullPointerException e) {
            assertTrue(true);
        }


      /*  try {
            omm.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            eprintStackTrace();
        }*/

    }

    public void testSessionInfo_Information() {
/*        ILoggerFactory.configureAndWatch("logmanager.xml");
        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
        OMManager omm = new OMManager();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(eventDispatcher);
        omm.init();
  */

        SessionInfoFactory sessionFactory;
        SessionInfo session;

/*        OMManager omm = new OMManager();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("operateandmaintainmanager.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfiguration(config);
        omm.init();
  */
        sessionFactory = omm.getSessionInfoFactory();
        //ConnectionMonitorImpl connectionMonitor = new ConnectionMonitorImpl();

        // register a ConnectionMonitor and retreive a sessionFactory.
        //sessionFactory = omm.registerConnectionMonitor(connectionMonitor);

        // get sessionObject from factory
        session = sessionFactory.getSessionInstance("1","1");
        try {
            session.setANI("ANI");
            session.setConnetionState(CallState.CONNECTED);
            session.setConnetionType(CallType.VOICE);
            session.setDirection(CallDirection.INBOUND);
            session.setDNIS("DNIS");
            session.setFarEndConProp("FAREND");
            session.setInboundActivity(CallActivity.IDLE);
            session.setOutboundActivity(CallActivity.IDLE );
            session.setRDNIS("RDNIS");
            session.setService("SERVICE");
            session.setSessionInitiator("SESS INIT");
        } catch (IlegalSessionInstanceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Read session information from factory
        SessionInfoFactoryImpl factoryImpl = (SessionInfoFactoryImpl)sessionFactory;

        //Get the read session object and validate parameters
        SessionInfoRead sessionRead = factoryImpl.getChangedSessions().get("11");
        assertEquals(sessionRead.getSessionId(),"1"); // validate id
        assertEquals(sessionRead.getANI(),"ANI");
        assertEquals(sessionRead.getConnetionState(),CallState.CONNECTED ); // validate id
        assertEquals(sessionRead.getConnetionType(),CallType.VOICE );
        assertEquals(sessionRead.getDirection(),CallDirection.INBOUND );
        assertEquals(sessionRead.getDNIS(),"DNIS");
        assertEquals(sessionRead.getFarEndConProp(),("FAREND"));
        assertEquals(sessionRead.getOutboundActivity(),CallActivity.IDLE );
        assertEquals(sessionRead.gettInboundActivity(),CallActivity.IDLE);
        assertEquals(sessionRead.getRDNIS(),"RDNIS");
        assertEquals(sessionRead.getService(),"SERVICE");
        assertEquals(sessionRead.getSessionInitiator(),"SESS INIT");

        // this shuld have pos 1
        assertEquals(sessionRead.getPos().toString(),"1");

/*        try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
  */
    }


    public void testSessionInfo_Multiple() {
//        ILoggerFactory.configureAndWatch("logmanager.xml");
//        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        SessionInfoFactory sessionFactory;
        SessionInfo session1;
        SessionInfo session2;
        SessionInfo session3;
        SessionInfo session4;
        //ConnectionMonitorImpl connectionMonitor = new ConnectionMonitorImpl();

 /*       MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
        OMManager omm = new OMManager();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(eventDispatcher);
        omm.init();
   */

/*        OMManager omm = new OMManager();
      ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
      configuration.setConfigFile("operateandmaintainmanager.xml");
      IConfiguration config = configuration.getConfiguration();
      omm.setConfiguration(config);
      omm.init();
*/
        // register a ConnectionMonitor and retreive a sessionFactory.
        sessionFactory = omm.getSessionInfoFactory();

        // get sessionObject from factory
        session1 = sessionFactory.getSessionInstance("1","1");
        session2 = sessionFactory.getSessionInstance("2","1");
        session3 = sessionFactory.getSessionInstance("3","1");
        session4 = sessionFactory.getSessionInstance("4","1");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Read session information from factory
        SessionInfoFactoryImpl factoryImpl = (SessionInfoFactoryImpl)sessionFactory;

        // Check no of sessins
        assertEquals(4,factoryImpl.getChangedSessions().size());

        // Return 2 sessions to factory for closure.
        sessionFactory.returnSessionInstance(session1);
        sessionFactory.returnSessionInstance(session3);

        // Check size. Shuld be the same..
        assertEquals(2,factoryImpl.getChangedSessions().size());

        // After read there should be 0.
        assertEquals(0,factoryImpl.getChangedSessions().size());


        // Create two new sessions
        session1 = sessionFactory.getSessionInstance("5","1");
        session3 = sessionFactory.getSessionInstance("6","1");

        try {
            session2.setANI("ANI");
        } catch (IlegalSessionInstanceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // 2 new sessions added and 1 changed
        assertEquals(factoryImpl.getChangedSessions().size(),3);

  /*      try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    */
    }


    private synchronized int getNum() {
        num++;
        return num;
    }


    /**
     * This testcase was written when trying to recreate TR 30802.
     *
     * I failed to recreate the scenario but found another bug which was
     * related to a race condition in SessionInfoFactoryImpl. This test method recreates "the other bug".
     * This bug was caused by "getChangedSessions" not being thread-safe.
     *
     * The test case creates several threads which invoke the method concurrently,
     * and if a thread encounters an exception, the test case fails.
     * @throws Exception
     */
    public void testThreadSafety() throws Exception {
        final SessionInfoFactory sessionFactory;
        sessionFactory = omm.getSessionInfoFactory();

        unexpectedThrowable = null;
        unexpectedStrackTrace = null;

        UCHandler ucHandler = new UCHandler();
        Thread.setDefaultUncaughtExceptionHandler(ucHandler);

        final int numThreads = 100;

        Thread allThreads[] = new Thread[numThreads];

        for(int i=0;i<numThreads;i++){
            Thread t = new Thread() {
                public void run() {

                    // Do some valid operations in a SessionInfo and the SessionInfoFactoryImpl.

                    SessionInfo session;
                    final int yy = getNum();
                    session = sessionFactory.getSessionInstance( ""+yy,""+""+yy);
                    sessionFactory.returnSessionInstance(session);
                    SessionInfoFactoryImpl factoryImpl2 = (SessionInfoFactoryImpl)sessionFactory;

                    factoryImpl2.getChangedSessions();

                }

            };
            allThreads[i] = t;
        }

        for(int i=0;i<numThreads;i++){
            allThreads[i].start();
        }
        for(int i=0;i<numThreads;i++){
            allThreads[i].join();
        }

        String stackTrace = "";
        if(unexpectedThrowable != null){
            for (StackTraceElement stackTraceElement : unexpectedStrackTrace) {
                stackTrace += stackTraceElement.toString() +"\n";
            }
        }
        assertTrue("Unexpected exception: "+ unexpectedThrowable + ":" + stackTrace, unexpectedThrowable == null);
    }

    public void testCounterData() {
//        ILoggerFactory.configureAndWatch("mobeon_log.xml");
//        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        CounterData counterData = new CounterData(CallType.VOICE,CallResult.CONNECTED ,CallDirection.INBOUND);

        counterData.incrementCounter();
        counterData.incrementCounter();
        assertEquals((long)2,(long)counterData.getCounter());

        counterData.decrementCounter();
        counterData.decrementCounter();
        assertEquals((long)0,(long)counterData.getCounter());

        counterData.setCounter((long)10);
        assertEquals((long)10,(long)counterData.getCounter());

        assertEquals(counterData.getDirection(),CallDirection.INBOUND);
        assertEquals(counterData.getType(),CallType.VOICE);
        assertEquals(counterData.getResult(),CallResult.CONNECTED);

    }


    public void testDataSet() {

        DataSet dataSet_1 = new DataSet();
        DataSet dataSet_2 = new DataSet();
        DataSet dataSet_3 = new DataSet();
        DataSet dataSet_4 = new DataSet();

        dataSet_1.decrementCounter(CallType.VOICE,CallDirection.INBOUND);
        dataSet_2.decrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );

        // create 4 values
        dataSet_1.incrementCounter(CallType.VOICE,CallDirection.INBOUND);
        dataSet_1.incrementCounter(CallType.VOICE,CallDirection.INBOUND);
        dataSet_1.incrementCounter(CallType.VOICE,CallDirection.INBOUND);
        dataSet_1.incrementCounter(CallType.VOICE,CallDirection.INBOUND);

        // create 8 values
        dataSet_2.incrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VIDEO,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VIDEO,CallResult.ERROR,CallDirection.INBOUND );
        dataSet_2.incrementCounter(CallType.VIDEO,CallResult.CONNECTED,CallDirection.OUTBOUND );
        dataSet_2.incrementCounter(CallType.VIDEO,CallResult.ERROR,CallDirection.OUTBOUND );

        assertEquals(8,(long)dataSet_2.sumCounters());
        assertEquals(4,(long)dataSet_1.sumCounters());

        // decrement 2
        dataSet_1.decrementCounter(CallType.VOICE,CallDirection.INBOUND);
        dataSet_1.decrementCounter(CallType.VOICE,CallDirection.INBOUND);

        // decrement 2
        dataSet_2.decrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );
        dataSet_2.decrementCounter(CallType.VOICE,CallResult.CONNECTED,CallDirection.INBOUND );

        assertEquals(2,(long)dataSet_1.sumCounters());
        assertEquals(6,(long)dataSet_2.sumCounters());

        dataSet_1.decrementCounter(CallType.VOICE,CallDirection.INBOUND);
        assertEquals(1,(long)dataSet_1.sumCounters());

        assertEquals(4,(long)dataSet_2.filterDirection(CallDirection.INBOUND).sumCounters());
        assertEquals(1,(long)dataSet_2.filterDirection(CallDirection.INBOUND).filterResult(CallResult.ERROR).sumCounters());
        assertEquals(2,(long)dataSet_2.filterDirection(CallDirection.OUTBOUND).sumCounters());
        assertEquals(2,(long)dataSet_2.filterDirection(CallDirection.INBOUND).filterType(CallType.VIDEO).sumCounters());

        assertEquals(6,(long)dataSet_2.sumCounters());

        dataSet_2.parseDirection(CallDirection.INBOUND);
        assertEquals(4,(long)dataSet_2.sumCounters());

        dataSet_2.parseResult(CallResult.CONNECTED);
        assertEquals(3,(long)dataSet_2.sumCounters());

        dataSet_2.parseType(CallType.VIDEO);
        assertEquals(1,(long)dataSet_2.sumCounters());


        dataSet_3.setCounter(CallType.VOICE,CallDirection.INBOUND,(long)15);
        dataSet_3.setCounter(CallType.VOICE,CallDirection.INBOUND,(long)20);
        dataSet_3.setCounter(CallType.VOICE,CallDirection.INBOUND,(long)20);
        dataSet_3.setCounter(CallType.VIDEO,CallDirection.OUTBOUND,(long)20);
        assertEquals(40,(long)dataSet_3.getPeakValue());


        dataSet_4.setCounter(CallType.VIDEO,CallResult.FAILED,CallDirection.UNKNOWN,(long)25 );
        dataSet_4.setCounter(CallType.VIDEO,CallResult.FAILED,CallDirection.UNKNOWN,(long)20 );
        assertEquals(25,(long)dataSet_4.getPeakValue());

        assertEquals(20,(long)dataSet_4.getCounter(CallType.VIDEO ,CallResult.FAILED,CallDirection.UNKNOWN));


        dataSet_3.getPeakTime();



    }

    public void testServiceInstance() {
        ServiceInstance si = new ServiceInstance();
        si.setHostName("test");
        si.setPort(1);

        assertEquals("test",si.getHostName());
        assertEquals(1,si.getPort());

    }


    public void testStatisticMonitorInfo() {
        StatisticMonitorInfo stat = new StatisticMonitorInfo();
        Hashtable<String, Vector> lstStatisticInfo;
        Vector data;

        stat.put("1","serviceEnablerName","123");

        lstStatisticInfo = stat.getInfo();

        //pos 0 time.toString();
        //pos 1 pos            //ServiceEnabler/*/*/*
        //pos 2 serviceEnablerName; // Name of service enabler
        //pos 3 counterValue;       // Counter value

        data = lstStatisticInfo.get("1");
        assertEquals("1",data.get(1).toString());
        assertEquals("serviceEnablerName",data.get(2).toString());
        assertEquals("123",data.get(3).toString());

    }


    public void testgetMib_Information() {

        OMManager mas = (OMManager)omm;
        mas.getOperateMAS().getMibAttributes();
        String s = mas.getOperateMAS().getMibAttributes().getClass().toString(); 
        assertTrue(s.equals("class com.mobeon.masp.rpcclient.MasMibAttributes"));
    }

    public void testEnum() {
        CallActivity act = CallActivity.IDLE ;
        CallDirection dir = CallDirection.INBOUND;
        CallResult result = CallResult.CONNECTED;
        CallState state = CallState.ALERTING;
        CallType type = CallType.VIDEO;

        assertTrue(act.getInfo().equals("idle"));
        assertTrue(act.getShortInfo().equals("i"));

        assertTrue(dir.getInfo().equals("incoming"));
        assertTrue(dir.getShortInfo().equals("in"));

        assertTrue(result.getInfo().equals("connected"));
        assertTrue(result.getShortInfo().equals("conn"));

        assertTrue(state.getInfo().equals("alerting"));
        assertTrue(state.getShortInfo().equals("a"));

        assertTrue(type.getInfo().equals("video"));
        assertTrue(type.getShortInfo().equals("video"));




    }

    public void testSessionInfo_Information_Neg() {

        SessionInfoFactory sessionFactory;
        SessionInfo session;
        sessionFactory = omm.getSessionInfoFactory();

        // get sessionObject from factory
        // and setup values
        session = sessionFactory.getSessionInstance("1","1");
        try {
            session.setANI("ANI");
            session.setConnetionState(CallState.CONNECTED);
            session.setConnetionType(CallType.VOICE);
            session.setDirection(CallDirection.INBOUND);
            session.setDNIS("DNIS");
            session.setFarEndConProp("FAREND");
            session.setInboundActivity(CallActivity.IDLE);
            session.setOutboundActivity(CallActivity.IDLE );
            session.setRDNIS("RDNIS");
            session.setService("SERVICE");
            session.setSessionInitiator("SESS INIT");
        } catch (IlegalSessionInstanceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // return session to factory for disposal.
        sessionFactory.returnSessionInstance(session);

        // try to write to a disposed session, an exceprtion shuld occure..
        try {
            session.setANI("ANI");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }

        try {
            session.setConnetionState(CallState.DISCONNECTED);
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setConnetionType(CallType.VOICE);
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setDirection(CallDirection.UNKNOWN);
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setDNIS("DNIS");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setFarEndConProp("prop");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setInboundActivity(CallActivity.RECORD);
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setOutboundActivity(CallActivity.PLAY);
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setRDNIS("RDNIS");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setService("Service");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }
        try {
            session.setSessionInitiator("Session init");
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(true);
        }


        // Read session information from factory
        SessionInfoFactoryImpl factoryImpl = (SessionInfoFactoryImpl)sessionFactory;

        //Get the read session object and validate parameters
        SessionInfoRead sessionRead = factoryImpl.getChangedSessions().get("11");
        assertEquals(sessionRead.getSessionId(),"1"); // validate id
        assertEquals(sessionRead.getANI(),"ANI");
        assertEquals(sessionRead.getConnetionState(),CallState.CONNECTED ); // validate id
        assertEquals(sessionRead.getConnetionType(),CallType.VOICE );
        assertEquals(sessionRead.getDirection(),CallDirection.INBOUND );
        assertEquals(sessionRead.getDNIS(),"DNIS");
        assertEquals(sessionRead.getFarEndConProp(),("FAREND"));
        assertEquals(sessionRead.getOutboundActivity(),CallActivity.IDLE );
        assertEquals(sessionRead.gettInboundActivity(),CallActivity.IDLE);
        assertEquals(sessionRead.getRDNIS(),"RDNIS");
        assertEquals(sessionRead.getService(),"SERVICE");
        assertEquals(sessionRead.getSessionInitiator(),"SESS INIT");

        // this shuld have pos 1
        assertEquals(sessionRead.getPos().toString(),"1");

/*        try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
  */
    }

}
