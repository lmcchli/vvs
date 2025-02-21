package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.JUnit4TestAdapter;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.data.MessageFileHandle;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.SchedulerFactory;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;


@RunWith(JMock.class)
public class MfsEventManagerTest {
	
	static class CmAccessMock extends CommonMessagingAccess {
		
		Properties ntfProperties;
		String ntfRecipient;
		EventTypes ntfEventType;
		MessageInfo ntfMessageInfo;
		
		private String testDir;

		CmAccessMock() {
			File[] roots = File.listRoots();
			if (roots.length > 0) {
				testDir = roots[0].getAbsolutePath() + "tmp" + File.separator + "CmAccessMock" +
				File.separator;

				File dir = new File(testDir);
				if (!dir.exists()) {
					Assert.assertTrue(dir.mkdirs());
				}
			} else {
				testDir = "";
			}
		}

		@Override
		public int countMessages(MSA msa, StateAttributesFilter filter)
				throws MsgStoreException {
			
			return 0;
		}

		@Override
		public void createMoipPrivateFolder(String msid)
				throws MsgStoreException {
		}

		@Override
		public void deleteMessage(StateFile state) throws MsgStoreException {
		}

		@Override
		public void deleteMoipPrivateFolder(String msid)
				throws MsgStoreException {
		}

		@Override
		public IConfiguration getConfiguration() {
			return null;
		}

		@Override
		public MessageFileHandle getMessageFileHandle(MessageInfo msgInfo)
				throws MsgStoreException {
			return null;
		}

		@Override
		public String getMoipPrivateFolder(String msid, boolean internal) {
		    String folder;
		    folder = testDir + File.separator + (internal?"internal":"external") + File.separator;
		    return folder;
		}

		@Override
		public MSA getMsid(String subscriber) {
			return null;
		}

		@Override
		public CommonOamManager getOamManager() {
			return null;
		}

		@Override
		public StateFile getStateFile(MessageInfo msgInfo)
				throws MsgStoreException {
			return null;
		}

		@Override
		public void initConfig() {
		}

		@Override
		protected void initMcd() {
		}

		@Override
		protected void initMfs() {
		}

		@Override
		public Message readMessage(MessageInfo msgInfo)
				throws MsgStoreException {
			return null;
		}

		//@Override
		public void registerRemoteNtfService() {
		}

		@Override
		public void reInitializeMfs(ConfigManager mgr) {
		}

		@Override
		public void reInitializeMrd(ConfigManager mgr) {
		}

		@Override
		public void releaseFileHandle(MessageFileHandle handle) {
		}

		@Override
		public MessageInfo[] searchMessageInfos(MSA msa,
				StateAttributesFilter filter) throws MsgStoreException {
			return null;
		}

		@Override
		public Message[] searchMessages(MSA msa, StateAttributesFilter filter)
				throws MsgStoreException {
			return null;
		}

		@Override
		public void setConfiguration(IConfiguration configuration) {
		}

		@Override
		public void setOamManager(CommonOamManager oamManager) {
		}

		@Override
		public void stop() {
		}

		@Override
		public int storeMessage(Container1 c1, Container2 c2,
				MsgBodyPart[] parts, StateAttributes attributes)
				throws Exception {
			return 0;
		}

		@Override
		public MessageInfo storeMessageTest(Container1 c1, Container2 c2,
				MsgBodyPart[] parts, StateAttributes attributes)
				throws Exception {
			return null;
		}

		@Override
		public void updateState(StateFile state) throws MsgStoreException {
		}

		@Override
	    public String denormalizeNumber(String number)
		{
			return "";
		}


		@Override
		public void notifyNtf(EventTypes eventType, MessageInfo msgInfo,
				String recipientId, Properties properties) {
			
			ntfEventType = eventType;
			ntfMessageInfo = msgInfo;
			ntfRecipient = recipientId;
			ntfProperties = properties;
		}
		
	}

	private static final String subscriberNumber = "15143457900";

	private static final String notificationNumber = "12345";

	private static final String EventName = "TestEvent";
	private static final String PhoneNumber = "9999999999";
	private static ILogger logger = ILoggerFactory.getILogger(MfsEventManagerTest.class);
	
	Properties properties;
	TrafficEvent[] events;
	MfsEventManager eventManager;
	static CmAccessMock mfs;
	static McdStub directory;
	Mockery context;
	IGroup groupMock;


	@BeforeClass
	public static void setUpBefore() {
		try {
			CommonTestingSetup.setup();

			mfs = new CmAccessMock();
			directory = getMcdStub(notificationNumber);

			MfsEventManager.setCommonMessagingAccess(mfs);
			MfsEventManager.setDirectoryAccess(directory);

		    //start scheduler
	        // retrieve scheduler's instance from the factory
			SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
			scheduler.init(CommonOamManager.getInstance().getMrdOam());
			scheduler.start();

			CommonMessagingAccess.setMcd(directory);
		} catch (Exception e) {
			Assert.fail("Unexpected exception");
		}
	}

	@AfterClass
	static public void tearDownAfter() {
		SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
		scheduler.stop();
	}

	@Before
	public void setUp() throws Exception {
		final int nbLines = 2;
		final int nbProperties = 3;
		context = new JUnit4Mockery();
		properties = new Properties();
		for (int i = 1; i <= nbLines; ++i) {
			for (int j = 1; j <= nbProperties; ++j) {
				properties.setProperty("key" + i + j, "value" + j);
			}
		}
		events = new TrafficEvent[nbLines];
		for (int i = 0; i < nbLines; ++i) {
			events[i] = new TrafficEvent(EventName);
			for (int j = 0; j < nbProperties; ++j) {
				String key = "key" + (i + 1) +(j + 1);
				events[i].setProperty(key, properties.getProperty(key));
			}
		}
		eventManager = new MfsEventManager();
	}
	
	@After
	public void tearDown() throws Exception {
	    String path = getPrivateFolder().toString();
	    deleteTree(path);

	    path = mfs.getMoipPrivateFolder(PhoneNumber, false) + File.separator + PhoneNumber;
	    deleteTree(path);
	}

	static private McdStub getMcdStub(String notifNumber) {
	    McdStub directoryAccess = new McdStub();

	    directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS,MWI;s,c;1;;;;;default;;");
	    directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");

	    directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
	    directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
	    directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notificationNumber);
	    directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE,
	            "NotifType=SMS,MWI;MobileNumber=" + notifNumber + ";Email=test@abc.com");
	    return directoryAccess;
	}
	
	@Test
	public void testStoreEvent() {

	    try {
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 1, 100, 10, 100, 1, 100, 10);
			eventManager.updateConfiguration(mfsConfig);
			createEventFile(subscriberNumber);
			Thread.sleep(500);
			validateEventFile();
			Thread.sleep(5000);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void testStoreMwiOff()
	{
		try {
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 10000, 1000, 10, 100, 10000, 1000, 10);
			eventManager.updateConfiguration(mfsConfig);

			TrafficEvent trafficEvent = new TrafficEvent(MfsClient.EVENT_MWIOFF);
			trafficEvent.setProperty("testPropertie", "value1");
			eventManager.storeEvent(PhoneNumber, trafficEvent);
			Assert.assertEquals(EventTypes.MWI_OFF, mfs.ntfEventType);
			Assert.assertEquals(mfs.ntfRecipient, PhoneNumber);
			Assert.assertEquals("value1", mfs.ntfProperties.getProperty("testPropertie"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testRemoveFile() {
		File eventFile = getEventFile();
		try {
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 10000, 1000, 10, 100, 10000, 1000, 10);
			eventManager.updateConfiguration(mfsConfig);
			createEventFile(PhoneNumber);
			Thread.sleep(500);
			eventManager.removeFile(PhoneNumber, EventName, true);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
		
		Assert.assertFalse(eventFile.exists());
	}

	@Test
	public void testRetrieveEvents() {
		try {
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 10000, 1000, 10, 100, 10000, 1000, 10);
			eventManager.updateConfiguration(mfsConfig);

			directory = getMcdStub(PhoneNumber);
			MfsEventManager.setDirectoryAccess(directory);
			CommonMessagingAccess.setMcd(directory);

			createEventFile(PhoneNumber);
			Thread.sleep(500);
			
			TrafficEvent[] readEvents = eventManager.retrieveEvents(PhoneNumber, EventName, true);

			// readEvents must contain the events + the header
			Assert.assertEquals(events.length+1, readEvents.length);
			
			// Start the validatation at line 2 (skipping the header line)
			for (int i = 0; i < events.length; ++i) {
				Assert.assertEquals(events[i].getName(), readEvents[i+1].getName());
				
				HashMap<String, String> exProps = events[i].getProperties();
				HashMap<String, String> readProps = readEvents[i+1].getProperties();
				for (String key : exProps.keySet()) {
					Assert.assertEquals(exProps.get(key), readProps.get(key));
				}
			}
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testHeavyLoadOnEventManager() {
		final int nbThreads = 5000;

		directory = getMcdStub(PhoneNumber);
		MfsEventManager.setDirectoryAccess(directory);
		CommonMessagingAccess.setMcd(directory);

		try {
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 10000, 1000, 10, 100, 10000, 1000, 10);
			eventManager.updateConfiguration(mfsConfig);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}

		ExecutorService pool = Executors.newFixedThreadPool(nbThreads);
		Vector<Callable<Integer>> tasks = new Vector<Callable<Integer>>(nbThreads);

		for (int i = 0; i < nbThreads; ++i) {
			tasks.add(new Callable<Integer>() {
				public Integer call() {
					int retries = 100;
					
					while (retries > 0) {
						try {
							eventManager.storeEvent(PhoneNumber, events[0]);
							return 0;
						} catch (TrafficEventSenderException e) {
							--retries;
							try {
								Thread.sleep((int)(Math.random() * 1000) + 500);
							} catch (Exception ex) {
								System.err.println("Caught exception: " + e.getMessage());
								return -1;
							}
						} catch (Exception e) {
							System.err.println("Cannot store event: " + e.getMessage());
							e.printStackTrace();
							return -1;
						}
					}
					return -1;
				}
			});
		}
		File eventFile = getEventFile();
		
		BufferedReader reader = null;
		try {
			List<Future<Integer>> results = pool.invokeAll(tasks);
			
			Thread.sleep(1000);
			
			int counter = 0;
			for (Future<Integer> value : results) {
			    Assert.assertEquals("Thread " + (counter++) + " has failed storing event", 0, value.get().intValue());
			}
			
			reader = new BufferedReader(new FileReader(eventFile));
			String line = null;
			counter = 0;
			//  Skip first line (containing subscriber number)
			line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				validatePropertyLine(line, events[0].getProperties());
				++counter;
			}
			Assert.assertEquals("Wrong number of events;", nbThreads, counter);
			
		} catch (Exception e) {
		    Assert.fail("Unexpected exception: " + e.getStackTrace());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {}
			}
		}
	}
	
	@Test
	public void testNtfNotificationThreshold() {
		try {
			final int threshold = 5;
			MfsEventManager.clearWorker();
			MfsConfiguration mfsConfig = createMfsConfiguration(100, threshold, 1000, 10, 100, threshold, 1000, 10);
			eventManager.updateConfiguration(mfsConfig);
			
			directory = getMcdStub(PhoneNumber);
			MfsEventManager.setDirectoryAccess(directory);
			CommonMessagingAccess.setMcd(directory);

			File eventFile = getEventFile();
			
			for (int nbEvents = 0; nbEvents < threshold; ++nbEvents) {
				eventManager.storeEvent(PhoneNumber, events[0]);
				Thread.sleep(100);
				if (nbEvents < threshold - 1) {
					Assert.assertTrue("File " + eventFile + " should have been created.", 
							eventFile.exists());
				} else {
					Assert.assertFalse("File " + eventFile + " should have been deleted.", 
							eventFile.exists());
				}
			}
			
			validateNtfNotification();
		} catch (Exception e) {
			Assert.fail("Caught unexpected exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testNtfNotificationTimeout() {
		try {
			final int timeout = 10;
			MfsEventManager.clearWorker();
			MfsConfiguration mfsConfig = createMfsConfiguration(100, 100, timeout, 10, 100, 100, timeout, 10);
			eventManager.updateConfiguration(mfsConfig);

			directory = getMcdStub(PhoneNumber);
			MfsEventManager.setDirectoryAccess(directory);
			CommonMessagingAccess.setMcd(directory);
			
			eventManager.storeEvent(PhoneNumber, events[0]);
			Thread.sleep(timeout * 2000);
			
			validateNtfNotification();
		} catch (Exception e) {
			Assert.fail("Caught unexpected exception: " + e.getMessage());
		}
	}
	
	@Test
    	public void testGetEventFiles() {
    		String eventName = "GetEventFiles";
    		final int numFiles = 10;
    		
    		File eventFolder = getEventDirectory();
    		eventFolder.mkdirs();
    		File[] fileList = new File[numFiles];
    		for (int i = 0; i < numFiles; ++i) {
    			String name = eventName + "_20090318_120" + i;
    			fileList[i] = new File(eventFolder, name);
    			try {
    				fileList[i].createNewFile();
    			} catch (IOException e) {
    				Assert.fail("Cannot create file: " + e.getMessage());
    			}
    		}
    		
    		try {
    			String[] eventFileList = eventManager.getEventFiles(PhoneNumber, eventName);
    			
    			for (File file : fileList) {
    				Assert.assertTrue("getEventFiles did not find file: " + file.getName(), 
    						Arrays.binarySearch(eventFileList, file.getName()) >= 0);
    			}
    		} catch (Exception e) {
    			Assert.fail("Received unexpected exception: " + e.getMessage());
    		}
    	}
	
	@Test
    	public void testGetEventFileNames2() {
    	    final String eventName = "GetEventFiles2";
    	    final String otherEventName = "NotToBeSelected";
    	    final int numFiles = 10;
    	    
    	    File eventFolder = getEventDirectory();
    	    eventFolder.mkdirs();
    	    File[] fileList = new File[numFiles];
    	    final int middle = numFiles / 2;
    	    final long pastTime = new Date().getTime() - 3600000L;
    	    for (int i = 0; i < numFiles; ++i) {
    	        int visible = i < middle ? 0 : 1;
    	        String name = eventName + "_" + visible + "_" + i;
                fileList[i] = new File(eventFolder, name);
                File fileNotSelectable = new File(eventFolder, otherEventName + "." + i);
                try {
                    fileNotSelectable.createNewFile();
                    fileNotSelectable.setLastModified(pastTime);
                    
                    fileList[i].createNewFile();
                    if (i < middle) {
                        boolean status = fileList[i].setLastModified(pastTime);
                        Assert.assertTrue("Cannot set last modified time for " + fileList[i].getPath(), status);
                    }
                } catch (IOException e) {
                    Assert.fail("Cannot create file: " + e.getMessage());
                }
    	    }
    	    
    	    FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().startsWith(eventName);
                }
    	    };
    	    
    	    String[] fileNames = eventManager.getEventFileNames(PhoneNumber, filter, false);
    	    Assert.assertNotNull(fileNames);
    	    Assert.assertEquals("Incorrect number of files retrieved with name filter.", fileList.length, fileNames.length);
    	    
    	    filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.lastModified() > pastTime;
                }
    	    };
    	    
    	    fileNames = eventManager.getEventFileNames(PhoneNumber, filter, false);
            Assert.assertNotNull(fileNames);
            Assert.assertEquals("Incorrect number of files retrieved with date filter.", middle, fileNames.length);
    	}
	
	@Test
	public void testGetFilePathsNameStartingWith() {
        String eventName = "GetEventFiles";
        final int numFiles = 10;
        
        File eventFolder = getEventDirectory();
        eventFolder.mkdirs();
        File[] fileList = new File[numFiles];
        for (int i = 0; i < numFiles; ++i) {
            String name = eventName + "_20090318_120" + i;
            fileList[i] = new File(eventFolder, name);
            try {
                fileList[i].createNewFile();
            } catch (IOException e) {
                Assert.fail("Cannot create file: " + e.getMessage());
            }
        }

        String[] fileNames = eventManager.getFilesNameStartingWith(PhoneNumber, eventName);
        Assert.assertNotNull(fileNames);
        
        try {
            for (File file : fileList) {
                Assert.assertTrue("getEventFiles did not find file: " + file.getName(), 
                        Arrays.binarySearch(fileNames, file.getName()) >= 0);
            }
        } catch (Exception e) {
            Assert.fail("Received unexpected exception: " + e.getMessage());
        }

        fileNames = eventManager.getFilePathsNameStartingWith(PhoneNumber, eventName);
        Assert.assertNotNull(fileNames);
        
        try {
            for (File file : fileList) {
                Assert.assertTrue("getEventFiles did not find file: " + file.getName(), 
                        Arrays.binarySearch(fileNames, file.getPath()) >= 0);
            }
        } catch (Exception e) {
            Assert.fail("Received unexpected exception: " + e.getMessage());
        }
	}

    @Test
    public void testMcnNotification() {
        try {
            MfsEventManager.clearWorker();
            MfsConfiguration mfsConfig = createMfsConfiguration(100, 100, 100, 10, 100, 100, 100, 10);
            eventManager.updateConfiguration(mfsConfig);
            
            TrafficEvent[] events = createMcnEvents();
            for (TrafficEvent event : events) {
                eventManager.storeEvent(PhoneNumber, event);
            }
            Thread.sleep(2000);
            
            TrafficEvent[] readEvents = eventManager.retrieveEvents(PhoneNumber, MfsClient.EVENT_MISSEDCALLNOTIFICATION, false);
            // must add 1 to the events because of the subscriberNumber header on the first line
            Assert.assertEquals(events.length+1, readEvents.length);
            
            for (int i = 0; i < events.length; ++i) {
                Assert.assertEquals(events[i].getName(), readEvents[i+1].getName());
                HashMap<String, String> exProps = events[i].getProperties();
                HashMap<String, String> readProps = readEvents[i+1].getProperties();
                for (String key : exProps.keySet()) {
                    Assert.assertEquals(exProps.get(key), readProps.get(key));
                }
            }
            
        } catch (Exception e) {
            Assert.fail("Caught unexpected exception: " + e.getMessage());
        }
    }
	
    private TrafficEvent[] createMcnEvents() throws Exception {
        final int nbLines = 2;
        final int nbProperties = 3;
        
        properties = new Properties();
        
        for (int i = 1; i <= nbLines; ++i) {
            for (int j = 1; j <= nbProperties; ++j) {
                properties.setProperty("key" + i + j, "value" + j);
            }
        }
        
        TrafficEvent[] events = new TrafficEvent[nbLines];
        
        for (int i = 0; i < nbLines; ++i) {
            events[i] = new TrafficEvent(MfsClient.EVENT_MISSEDCALLNOTIFICATION);
            for (int j = 0; j < nbProperties; ++j) {
                String key = "key" + (i + 1) +(j + 1);
                events[i].setProperty(key, properties.getProperty(key));
            }
        }
        
        return events;
    }
	
	private void validateEventFile() {
		BufferedReader reader = null;
		try {
		    String filePath = MfsEventManager.generateFilePath(notificationNumber, EventName, true);
		    File path = new File(filePath); 

		    System.out.println("*********************** " + filePath);
		    try{
		    	Thread.sleep(5000);
		    }catch(Exception e){}

			Assert.assertTrue(path.exists());

			reader = new BufferedReader(new FileReader(path));
			String line = null;
			int lineCount = 0;
			
			// Skip first line (containing subscriber number)
			line = reader.readLine();
			Assert.assertTrue(line.startsWith(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY));
			
			// Skip second line (containing eventId)
			line = reader.readLine();
			Assert.assertTrue(line.startsWith(MoipMessageEntities.SLAMDOWN_EVENT_ID));

			// Loop through the file
			while ((line = reader.readLine()) != null && !line.isEmpty()) {
				validatePropertyLine(line, events[lineCount].getProperties());
				++lineCount;
			}
			reader.close();
			Assert.assertEquals(events.length, lineCount);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {}
		}
	}
	
	private void deleteTree(String dir) {
		File path = new File(dir);
		
		if (path.isDirectory()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteTree(file.getPath());
				} else {
					file.delete();
				}
			}
			path.delete();
		}
	}
	
	private File getPrivateFolder() {
		String msid = directory.lookupSubscriber(PhoneNumber).getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);
		String privateFolder = mfs.getMoipPrivateFolder(msid, true);
		return new File(privateFolder);
	}
	
	private File getEventDirectory() {
		File path = new File(
				getPrivateFolder(), 
				PhoneNumber + 
				File.separator + 
				MfsEventManager.EVENTS_DIRECTORY);
		
		return path;
	}
	
	private File getEventFile() {
		return new File(getEventDirectory(), EventName);
	}
	
	private void createEventFile(String number) throws TrafficEventSenderException {
		for (TrafficEvent event : events) {
			eventManager.storeEvent(number, event);
		}
	}
	
	private void validatePropertyLine(String line, Map<String, String> properties) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String property = entry.getKey() + "=" + entry.getValue();
			Assert.assertTrue(line.contains(property));
		}
	}
	
	private MfsConfiguration createMfsConfiguration(final int slamdownEventQueueSize, final int slamdownEventThreshold, final int slamdownEventTimeout, final int slamdownWorkers,
													final int mcnEventQueueSize, final int mcnEventThreshold, final int mcnEventTimeout, final int mcnWorkers)
	throws ConfigurationException {
		MfsConfiguration mfsConfig = new MfsConfiguration();
		
		groupMock = context.mock(IGroup.class);
		context.checking(new Expectations() {{
			allowing(groupMock).getInteger(MfsConfiguration.SLAMDOWN_QUEUE_SIZE);
			will(returnValue(slamdownEventQueueSize));

			allowing(groupMock).getInteger(MfsConfiguration.SLAMDOWN_THRESHOLD);
			will(returnValue(slamdownEventThreshold));

            allowing(groupMock).getInteger(MfsConfiguration.SLAMDOWN_TIMEOUT);
            will(returnValue(slamdownEventTimeout));

            allowing(groupMock).getInteger(MfsConfiguration.SLAMDOWN_WORKERS);
            will(returnValue(slamdownWorkers));

			allowing(groupMock).getInteger(MfsConfiguration.MCN_QUEUE_SIZE);
			will(returnValue(mcnEventQueueSize));

			allowing(groupMock).getInteger(MfsConfiguration.MCN_THRESHOLD);
			will(returnValue(mcnEventThreshold));

            allowing(groupMock).getInteger(MfsConfiguration.MCN_TIMEOUT);
            will(returnValue(mcnEventTimeout));

            allowing(groupMock).getInteger(MfsConfiguration.MCN_WORKERS);
            will(returnValue(mcnWorkers));
		}});
		
		mfsConfig.readConfiguration(groupMock);
		
		return mfsConfig;
	}

	private void validateNtfNotification() {
		
		File eventFile = getEventFile();
		File directory = eventFile.getParentFile();
		File[] fileList = directory.listFiles();
		Assert.assertEquals("Number of file in directory", 1, fileList.length);
		
		String fileName = fileList[0].getName();
		logger.debug("Validating event file: " + fileName);
		System.out.println("asdfasdfsdf "+fileName);
		Assert.assertTrue("Event file not found:" + fileName, fileName.matches(EventName + "_\\d{8}_\\d{2}_\\d{2}_\\d{2}_\\d{2,3}"));
		
		Assert.assertEquals(EventTypes.SLAM_DOWN, mfs.ntfEventType);
		Assert.assertEquals("Recipient error", PhoneNumber, mfs.ntfRecipient);
		Assert.assertNotNull("RMSA cannot be null in NTF request", mfs.ntfMessageInfo.rmsa);
		Assert.assertEquals("Wrong event file name", 
				fileName, 
				mfs.ntfProperties.getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY));
	}
	
	/**
	 * Tests reading of out-dial events from MFS.
	 * 
	 * @throws IOException
	 * @throws TrafficEventSenderException
	 */
	@Test
	public void testGetOutDialEvent() throws IOException, TrafficEventSenderException {
		//
		// Test Setup 
		//
		final String eventKey = "GODE";
		final int nbProps = 10;
		Properties props = new Properties();
		for (int i = 0; i < nbProps; ++i) {
			props.setProperty("key" + i, "value" + i);
		}
		createOdlPropertyFile(eventKey, props);
		
		//
		// Perform test
		//
		Properties readProps = eventManager.getProperties(PhoneNumber, "odl-" + eventKey);
		
		//
		// Validation
		//
		Assert.assertEquals(props, readProps);
	}
	
	/**
	 * Tests reading of a non-existing event file.
	 */
	@Test
	public void testGetOutDialEvent_NoEvent() {
		Assert.assertNull(eventManager.getProperties(PhoneNumber, "odl-" + "notexist"));
	}
	
	/**
	 * Tests MfsEventManager.getOutDialEvents() in the case events exists.
	 * @throws IOException
	 * @throws TrafficEventSenderException
	 */
	@Test
	public void testGetOutDialEvents() throws IOException, TrafficEventSenderException {
		final String eventKey = "GODE";
		final int nbProps = 3;
		final int nbEvents = 10;
		Properties props = new Properties();
		for (int i = 0; i < nbProps; ++i) {
			props.setProperty("key" + i, "value" + i);
		}
		for (int i = 0; i < nbEvents; ++i) {
			createOdlPropertyFile(eventKey + i, props);
		}
		
		String[] keys = eventManager.getOutdialEvents(PhoneNumber);
		
		Assert.assertEquals(nbEvents, keys.length);
		List<String> list = Arrays.asList(keys);
		for (int i = 0; i < nbEvents; ++i) {
			String key = eventKey + i;
			Assert.assertTrue(list.contains(key));
		}
	}
	
	/**
	 * Test MfsEventManager.getOutDialEvents() when there is no event available.
	 */
	@Test
	public void testGetOutDialEvents_noEvents() {
		String[] eventKeys = eventManager.getOutdialEvents(PhoneNumber);
		Assert.assertNull(eventKeys);
	}
	
	/**
	 * Tests MfsEventManager.removeOutidalEvent().
	 * @throws IOException
	 * @throws TrafficEventSenderException
	 */
	@Test
	public void testRemoveOutdialEvent() throws IOException, TrafficEventSenderException {
		final String eventKey = "RODE";
		Properties props = new Properties();
		props.setProperty("key", "value");
		String pathFile = createOdlPropertyFile(eventKey, props);
		
		eventManager.removeFile(PhoneNumber, "odl-" + eventKey);
		
		File file = new File(pathFile);
		Assert.assertFalse(file.exists());
	}
	
	@Test
	public void testStoreOutdialEvent() throws TrafficEventSenderException, IOException {
		final String eventKey = "SODE";
		final int nbProps = 6;
		Properties props = new Properties();
		for (int i = 0; i < nbProps; ++i) {
			props.setProperty("key" + i, "value" + i);
		}
		
		eventManager.storeProperties(PhoneNumber, "odl-" + eventKey, props);
		
		File odlFile = new File(getEventDirectory(), "odl-" + eventKey);
		Reader reader = new FileReader(odlFile);
		Properties eventProps = new Properties();
		eventProps.load(reader);
		reader.close();
		Assert.assertEquals(props, eventProps);
		
	}
	
	/**
	 * Creates out-dial event property files.
	 * 
	 * @param name Event key
	 * @param props Event's properties
	 * @return Full path of the created ODL event file.
	 * @throws IOException
	 * @throws TrafficEventSenderException
	 */
	private String createOdlPropertyFile(String name, Properties props) throws IOException, TrafficEventSenderException {
		File eventFolder = getEventDirectory();
		eventFolder.mkdirs();
		File odlFile = new File(eventFolder, "odl-" + name);
		Writer writer = new FileWriter(odlFile);
		props.store(writer, null);
		writer.close();
		
		return odlFile.getPath();
	}

	public static void main(String[] arg) {
		JUnit4TestAdapter testAdapter = new JUnit4TestAdapter(MfsEventManagerTest.class);
		junit.textui.TestRunner.run(testAdapter);
		System.exit(0);
	}
}
