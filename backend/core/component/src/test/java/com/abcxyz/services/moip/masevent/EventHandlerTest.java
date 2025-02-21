package com.abcxyz.services.moip.masevent;

import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.SchedulerFactory;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.AbstractEventHandler;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.masevent.SlamdownEventHandler;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.CommonOamManagerTest;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

//@TODO: fix this test!
@Ignore 
@RunWith(JMock.class)
public class EventHandlerTest {
	
	private static ILogger logger = ILoggerFactory.getILogger(EventHandlerTest.class);
	private static SlamdownEventHandler slamdownHandler;

	private Mockery context;
	private IConfiguration configuration;
	private IGroup cmAccessGroup;
	
	private volatile String threadTestResult = null;
	
	
	@BeforeClass
	public static void setUpBefore() {
		try {
		    System.setProperty("abcxyz.mrd.noAYL", "true");
		    System.setProperty("abcxyz.messaging.scheduler.memory", "true");

			CommonOamManagerTest.initOam();
			slamdownHandler = new SlamdownEventHandler();

		    //start scheduler
	        // retrieve scheduler's instance from the factory
			SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
			scheduler.init(CommonOamManager.getInstance().getMrdOam());
			scheduler.start();
			slamdownHandler.start(MoipMessageEntities.MESSAGE_SERVICE_MAS);
		} catch (Exception e) {
			Assert.fail("Unexpected exception");
		}
	}

	@Before
	public void setUp() throws Exception {
		threadTestResult = null;
		context = new JUnit4Mockery();
		configuration = context.mock(IConfiguration.class);
		cmAccessGroup = context.mock(IGroup.class);

	    // Expectations
		
		context.checking(new Expectations() {{
			try {
				allowing(configuration).getGroup("cmnaccess");
				will(returnValue(cmAccessGroup));
				
				allowing(cmAccessGroup).getGroup("local");
				allowing(cmAccessGroup).getGroup("mrd");
				allowing(cmAccessGroup).getGroup("scheduler");
				allowing(cmAccessGroup).getGroup("mfs");
				allowing(cmAccessGroup).getGroup("mcd");
				allowing(cmAccessGroup).getGroup("cdrgen");
				allowing(cmAccessGroup).getGroup("pa");
				allowing(cmAccessGroup).listParameters();
				will(returnValue(new LinkedList<String>()));
			} catch (Exception e) {
				Assert.fail("Unexpected exception");
			}
		}});

		CommonOamManager oamManager = CommonOamManager.getInstance();
		oamManager.setConfiguration(configuration);
	}
	
	@AfterClass
	static public void tearDown() {
		SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
		scheduler.stop();
	}

	@Test
	public void testSlamdownFired() {
		//scheduler
		Properties properties = new Properties();

		properties.setProperty(SlamdownEventHandler.OMSA, "omsa");
		properties.setProperty(SlamdownEventHandler.RMSA, "rmsa");
		properties.setProperty(SlamdownEventHandler.OMSGID, "omsgid");
		properties.setProperty(SlamdownEventHandler.RMSGID, "rmsgid");
		properties.setProperty(SlamdownEventHandler.RECIPIENT_ID, "rcpid");

		AppliEventInfo info = slamdownHandler.scheduleEvent("omsgidrmagid01", EventTypes.SLAM_DOWN.getName(), properties);
		//test to be activated with P1D19 baseline build
		/*assertTrue (info.getEventType().equalsIgnoreCase(MasEventTypes.SLAM_DOWN.getName()));
		assertTrue (info.getEventProperties().getProperty(MasEventHandler.OMSA).equalsIgnoreCase("omsa"));
		assertTrue (info.getEventProperties().getProperty(MasEventHandler.RMSA).equalsIgnoreCase("rmsa"));
		assertTrue (info.getEventProperties().getProperty(MasEventHandler.OMSGID).equalsIgnoreCase("omsgid"));
		assertTrue (info.getEventProperties().getProperty(MasEventHandler.RMSGID).equalsIgnoreCase("rmsgid"));
		assertTrue (info.getEventProperties().getProperty(MasEventHandler.RECIPIENT_ID).equalsIgnoreCase("rcpid"));
		*/

		//fire event
		slamdownHandler.eventFired(info);
	}


	@Test
	public void testScheduleEvent() {
		final String propertyKey = "mykey";
		final String propertyValue = "myvalue";
		final Object lock = new Object();
		
		threadTestResult = "Event Handler not called.";
		
		AbstractEventHandler handler = new AbstractEventHandler() {
			@Override
			public int eventFired(AppliEventInfo eventInfo) {
				try {
					String eventName = eventInfo.getEventType();
					EventTypes eventType = EventTypes.get(eventName);
					if (eventType == null) {
						threadTestResult = "AbstractEventHandler: Received unknown event: " + eventName;
						System.err.println(threadTestResult);
						Assert.fail(threadTestResult);
					}

					if (eventType == EventTypes.INTERNAL_TIMER) {
						Properties properties = eventInfo.getEventProperties();
						threadTestResult = "Property validation test failed";
						Assert.assertNotNull(threadTestResult, properties);
						Assert.assertEquals(threadTestResult, propertyValue, properties.get(propertyKey));
						threadTestResult = null;
					} else {
						String msg = "AbstractEventHandler: Received unexpected event: " + eventName;
						System.err.println(msg);
						threadTestResult = msg;
						Assert.fail(msg);
					}
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
				return EventHandleResult.STOP_RETRIES;
			}
			
			@Override 
			public void reportCorruptedEventFail(String eventId) {
				try {
					threadTestResult = "Unexpected call to reportCorruptedEventFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
			
			@Override 
			public void reportEventCancelFail(AppliEventInfo eventInfo) {
				try {
					threadTestResult = "Unexpected call to reportEventCancelFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
			
			@Override 
			public void reportEventScheduleFail(AppliEventInfo eventInfo) {
				try {
					threadTestResult = "Unexpected call to reportEventScheduleFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
		};
		
        RetryEventInfo info = new RetryEventInfo("MasEventHandlerTest");
        String retrySchema = "15s stop";
        info.setEventRetrySchema(retrySchema);
		handler.start(info);

		Properties properties = new Properties();
		properties.setProperty(propertyKey, propertyValue);
		
		long eventTime = System.currentTimeMillis() + 15000;
		Date eventDate = new Date(eventTime);
		logger.debug("Scheduling event at " + eventDate);
		System.out.println("Scheduling event at " + eventDate);

		handler.scheduleEvent("myevent", EventTypes.INTERNAL_TIMER.getName(), properties);
		
		try {
			synchronized (lock) {
				lock.wait(30000);
			}
			System.out.println("Main thread released");
			if (threadTestResult != null) {
				Assert.fail(threadTestResult);
			}
		} catch (InterruptedException e) {
			Assert.fail("Unexpected exception");
		}
	}
	
	@Test
	public void testCancelEvent() {
		final Object lock = new Object();

		AbstractEventHandler handler = new AbstractEventHandler() {
		
			@Override
			public int eventFired(AppliEventInfo eventInfo) {
				synchronized (lock) {
					lock.notify();
				}
				threadTestResult = "Unexpected fired event even if cancelled: " + eventInfo.getEventType();
				return EventHandleResult.OK;
			}
			
			@Override 
			public void reportCorruptedEventFail(String eventId) {
				try {
					threadTestResult = "Unexpected call to reportCorruptedEventFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
			
			@Override 
			public void reportEventCancelFail(AppliEventInfo eventInfo) {
				try {
					threadTestResult = "Unexpected call to reportEventCancelFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
			
			@Override 
			public void reportEventScheduleFail(AppliEventInfo eventInfo) {
				try {
					threadTestResult = "Unexpected call to reportEventScheduleFail";
					Assert.fail(threadTestResult);
				} finally {
					synchronized (lock) {
						System.out.println("Unlocking main thread");
						lock.notify();
					}
				}
			}
		};
		
        RetryEventInfo info = new RetryEventInfo("MasEventHandlerTest");
        String retrySchema = "15s stop";
        info.setEventRetrySchema(retrySchema);
		handler.start(info);

		long eventTime = System.currentTimeMillis() + 15000;
		Date eventDate = new Date(eventTime);
		logger.debug("Scheduling event at " + eventDate);
		System.out.println("Scheduling event at " + eventDate);

		AppliEventInfo event = handler.scheduleEvent("cancelledEvent", 
				EventTypes.INTERNAL_TIMER.getName(), null);

		try {
			Thread.sleep(5000);
			handler.cancelEvent(event);
			
			synchronized (lock) {
				lock.wait(11000);
			}

			System.out.println("Main thread released");
			if (threadTestResult != null) {
				Assert.fail(threadTestResult);
			}
		} catch (InterruptedException e) {
			Assert.fail("Unexpected exception");
		}
	}
}
