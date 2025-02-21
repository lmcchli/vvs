package com.mobeon.masp.execution_engine.platformaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.jmock.Mock;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.mediaobject.FileMediaObject;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.profilemanager.BaseContext;
import com.mobeon.masp.profilemanager.IProfile;
import com.mobeon.masp.profilemanager.McdSubscriber;

/**
 * Tests greetings all the way from platformaccess down to MFS. MCD access is
 * mocked and/or stubbed.
 * 
 * @author estberg
 * 
 */
public class PlatformAccessImplGreetingTest extends PlatformAccessTest {
    private static final String METHOD_NAME = "getSubscriberIdentity";
	static private final Collection<String> configFilenames = new LinkedList<String>();
    static private String configFilename = "../../ipms_sys2/backend/cfg/backend.conf";
    static private CommonMessagingAccess commonMessagingAccess = null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    /**
     * The files that are loaded for the greetings
     */
    private static final String FILE_NAME_ALLCALLS_VOICE = "../profilemanager/allcalls.wav";
    private static final String FILE_NAME_CDG_VOICE = "../profilemanager/cdg12345.wav";
    private static final String FILE_NAME_SPOKENNAME_VOICE = "../profilemanager/spokenname.wav";
    private static final String FILE_NAME_ALLCALLS_VIDEO = "../profilemanager/allcalls.mov";
    private static final String FILE_NAME_CDG_VIDEO = "../profilemanager/cdg12345.mov";
    private static final String FILE_NAME_SPOKENNAME_VIDEO = "../profilemanager/spokenname.mov";
    /**
     * The size of each <code>ByteBuffer</code>, used to map file into memory.
     */
    private static final long BUFFER_SIZE = 8 * 1024;
    private PlatformAccess platformAccess1;
    private BaseContext baseContext;
    private Mock jmockDirectoryAccess;
    private Mock jmockDirectoryAccessSubscriber1;
    private Mock jmockDirectoryAccessSubscriber2;
    private String msid1 = "aaaa1111";
    private String msid2 = "bbbb2222";
    private String telephoneNumber1 = "11111111";
    private String telephoneNumber2 = "22222222";

    /**
     * Wrapper class for McdSubscriber, to set the directoryaccess object
     * through the constructor
     * 
     * @author estberg
     * 
     */
    public class GreetingSubscriber extends McdSubscriber {

        public GreetingSubscriber(String phoneNumber, BaseContext context,
                IDirectoryAccess directoryAccess) {
            super(phoneNumber, context, directoryAccess);

        }
    }

    public PlatformAccessImplGreetingTest(String name) {
        super(name);
    }

    static public void initOam() throws ConfigurationException, ConfigurationDataException {

        configFilenames.add(configFilename);
        IConfiguration configuration = new ConfigurationImpl(null,
                configFilenames, false);
        CommonOamManager.getInstance().setConfiguration(configuration);

        // deleteDirectory(new File(strDirectoy + "\\internal"));
        // deleteDirectory(new File(strDirectoy + "\\external"));
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        System.setProperty("abcxyz.mrd.noAYL", "true");

        CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();

        ConfigManager mfsConfig = MfsConfiguration.getInstance();
        new File(strDirectoy).mkdir();
        mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        commonMessagingAccess.reInitializeMfs(mfsConfig);
    }
    
    private void setupMocks() {
        // Setup directory access mock for subscriber1, telephoneNumber1, msid1
        jmockDirectoryAccessSubscriber1 = mock(IDirectoryAccessSubscriber.class);
        jmockDirectoryAccessSubscriber2 = mock(IDirectoryAccessSubscriber.class);

        jmockDirectoryAccessSubscriber1.stubs().method(METHOD_NAME).will(
                returnValue(msid1));
        jmockDirectoryAccessSubscriber2.stubs().method(METHOD_NAME).will(
                returnValue(msid2));

        jmockDirectoryAccess = mock(IDirectoryAccess.class);
        jmockDirectoryAccess
                .stubs()
                .method("lookupSubscriber")
                .with(eq(telephoneNumber1))
                .will(
                        returnValue((IDirectoryAccessSubscriber) jmockDirectoryAccessSubscriber1
                                .proxy()));
        jmockDirectoryAccess
        .stubs()
        .method("lookupSubscriber")
        .with(eq(telephoneNumber2))
        .will(
                returnValue((IDirectoryAccessSubscriber) jmockDirectoryAccessSubscriber2
                        .proxy()));

        baseContext = new BaseContext();
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
        baseContext.setMediaObjectFactory(mediaObjectFactory);
        IProfile greetingSubscriber1 = new GreetingSubscriber(telephoneNumber1,
                baseContext, (IDirectoryAccess) jmockDirectoryAccess.proxy());
        IProfile greetingSubscriber2 = new GreetingSubscriber(telephoneNumber2,
                baseContext, (IDirectoryAccess) jmockDirectoryAccess.proxy());

        jmockProfileManager.stubs().method("getProfile").with(
                eq(telephoneNumber1)).will(returnValue(greetingSubscriber1));
        jmockProfileManager.stubs().method("getProfile").with(
                eq(telephoneNumber2)).will(returnValue(greetingSubscriber2));

        platformAccess1 = createPlatformAccess();        
    }

    public void setUp() throws Exception {
        super.setUp();
        initOam();

        setupMocks();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test get spoken name voice greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSubscriberGetSpokenNameVoice()
            throws MimeTypeParseException, MediaObjectException {
        // First save a spoken name greeting
        testSubscriberSetSpokenNameVoice();

        String mediaType = "voice";

        // Get the spoken name greeting
        IMediaObject spokenName = platformAccess1.subscriberGetSpokenName(
                telephoneNumber1, mediaType);

        assertNotNull(spokenName);

        // Compare with the original file
        InputStream spokenStream = spokenName.getInputStream();
        InputStream origStream = null;
        try {
            origStream = new FileInputStream(FILE_NAME_SPOKENNAME_VOICE);
        } catch (FileNotFoundException e) {
            fail();
        }

        int c1 = 0;
        int c2 = 0;
        int f1, f2;
        try {
            do {
                f1 = spokenStream.read();
                c1++;
                f2 = origStream.read();
                c2++;
                // Verify that each piece of data is equal
                assertEquals(f1, f2);
            } while (f1 != -1 && f2 != -1);
        } catch (IOException e) {
            // Check that equal amount of data was read, and more than one loop
            assertEquals(c1, c2);
            assertTrue(c1 > 1);
        }
        try {
            origStream.close();
            spokenStream.close();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test get allcalls voice greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSubscriberGetGreetingAllCallsVoice()
            throws MimeTypeParseException, MediaObjectException {
        // First save a spoken name greeting
        testSubscriberSetGreetingAllCallsVoice();

        String greetingType = "allcalls";
        String mediaType = "voice";
        String cdgNumber = null;

        // Get the spoken name greeting
        IMediaObject greeting = platformAccess1.subscriberGetGreeting(
                telephoneNumber1, greetingType, mediaType, cdgNumber);

        assertNotNull(greeting);

        // Compare with the original file
        InputStream greetingStream = greeting.getInputStream();
        InputStream origStream = null;
        try {
            origStream = new FileInputStream(FILE_NAME_ALLCALLS_VOICE);
        } catch (FileNotFoundException e) {
            fail();
        }

        int c1 = 0;
        int c2 = 0;
        int f1, f2;
        try {
            do {
                f1 = greetingStream.read();
                c1++;
                f2 = origStream.read();
                c2++;
                // Verify that each piece of data is equal
                assertEquals(f1, f2);
            } while (f1 != -1 && f2 != -1);
        } catch (IOException e) {
            // Check that equal amount of data was read, and more than one loop
            assertEquals(c1, c2);
            assertTrue(c1 > 1);
        }
        try {
            origStream.close();
            greetingStream.close();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test get spoken name video greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSubscriberGetSpokenNameVideo()
    throws MimeTypeParseException, MediaObjectException {
        // First save a spoken name greeting
        testSubscriberSetSpokenNameVideo();
        
        String mediaType = "video";
        
        // Get the spoken name greeting
        IMediaObject spokenName = platformAccess1.subscriberGetSpokenName(
                telephoneNumber1, mediaType);
        
        assertNotNull(spokenName);
        
        // Compare with the original file
        InputStream spokenStream = spokenName.getInputStream();
        InputStream origStream = null;
        try {
            origStream = new FileInputStream(FILE_NAME_SPOKENNAME_VIDEO);
        } catch (FileNotFoundException e) {
            fail();
        }
        
        int c1 = 0;
        int c2 = 0;
        int f1, f2;
        try {
            do {
                f1 = spokenStream.read();
                c1++;
                f2 = origStream.read();
                c2++;
                // Verify that each piece of data is equal
                assertEquals(f1, f2);
            } while (f1 != -1 && f2 != -1);
        } catch (IOException e) {
            // Check that equal amount of data was read, and more than one loop
            assertEquals(c1, c2);
            assertTrue(c1 > 1);
        }
        try {
            origStream.close();
            spokenStream.close();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test set allcalls voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberSetGreetingAllCallsVoice()
            throws MediaObjectException, MimeTypeParseException {
        String greetingType = "allcalls";
        String mediaType = "voice";
        String cdgNumber = null;

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("audio", "wav"));
        IMediaObject greeting = new FileMediaObject(new File(
                FILE_NAME_ALLCALLS_VOICE), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, greeting);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + "_voice.wav";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test set caller dependent voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberSetGreetingCDGVoice()
            throws MediaObjectException, MimeTypeParseException {
        String greetingType = "cdg";
        String mediaType = "voice";
        String cdgNumber = telephoneNumber2;

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("audio", "wav"));
        IMediaObject greeting = new FileMediaObject(new File(
                FILE_NAME_CDG_VOICE), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, greeting);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + cdgNumber + "_voice.wav";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test set spoken name voice greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSubscriberSetSpokenNameVoice()
            throws MimeTypeParseException, MediaObjectException {
        String greetingType = "spokenname";
        String mediaType = "voice";

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("audio", "wav"));
        IMediaObject spokenName = new FileMediaObject(new File(
                FILE_NAME_SPOKENNAME_VOICE), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                spokenName);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + "_voice.wav";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test set allcalls video greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberSetGreetingAllCallsVideo()
            throws MediaObjectException, MimeTypeParseException {
        String greetingType = "allcalls";
        String mediaType = "video";
        String cdgNumber = null;

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("video", "mov"));
        IMediaObject greeting = new FileMediaObject(new File(
                FILE_NAME_ALLCALLS_VIDEO), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, greeting);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + "_video.mov";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test set caller dependent video greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberSetGreetingCDGVideo()
            throws MediaObjectException, MimeTypeParseException {
        String greetingType = "cdg";
        String mediaType = "video";
        String cdgNumber = telephoneNumber2;

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("video", "mov"));
        IMediaObject greeting = new FileMediaObject(new File(
                FILE_NAME_CDG_VIDEO), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, greeting);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + cdgNumber + "_video.mov";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test set spoken name video greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSubscriberSetSpokenNameVideo()
            throws MimeTypeParseException, MediaObjectException {
        String greetingType = "spokenname";
        String mediaType = "video";

        MediaProperties mediaProps = new MediaProperties();
        mediaProps.setContentType(new MimeType("video", "mov"));
        IMediaObject spokenName = new FileMediaObject(new File(
                FILE_NAME_SPOKENNAME_VIDEO), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                spokenName);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + "_video.mov";
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }

    /**
     * Test delete the allcalls video greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingAllCallsVideo()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for allcalls
        testSubscriberSetGreetingAllCallsVideo();

        String greetingType = "allcalls";
        String mediaType = "video";
        String cdgNumber = null;

        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + ".mov";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);
    }

    /**
     * Test delete the caller dependent video greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingCDGVideo()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for cdg
        testSubscriberSetGreetingCDGVideo();

        String greetingType = "cdg";
        String mediaType = "video";
        String cdgNumber = telephoneNumber2;

        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + cdgNumber + ".mov";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);
    }

    /**
     * Test delete the spoken name video greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingSpokenNameVideo()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for allcalls
        testSubscriberSetSpokenNameVideo();

        String greetingType = "spokenname";
        String mediaType = "video";

        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + ".mov";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                null);
    }
    
    /**
     * Test delete the allcalls voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingAllCallsVoice()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for allcalls
        testSubscriberSetGreetingAllCallsVoice();

        String greetingType = "allcalls";
        String mediaType = "voice";
        String cdgNumber = null;

        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + ".wav";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);
    }

    /**
     * Test delete the caller dependent voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingCDGVoice()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for cdg
        testSubscriberSetGreetingCDGVoice();

        String greetingType = "cdg";
        String mediaType = "voice";
        String cdgNumber = telephoneNumber2;

        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + cdgNumber + ".wav";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetGreeting(telephoneNumber1, greetingType,
                mediaType, cdgNumber, null);
    }

    /**
     * Test delete the spoken name voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    public void testSubscriberDeleteGreetingSpokenNameVoice()
            throws MediaObjectException, MimeTypeParseException {

        // First save a greeting for allcalls
        testSubscriberSetSpokenNameVoice();

        String greetingType = "spokenname";
        String mediaType = "voice";

        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                null);

        // Verify that the file is deleted
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid1, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber1
                + "/Greeting";
        String greetingFilePath = greetingFolderPath + "/" + greetingType
                + ".wav";
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetSpokenName(telephoneNumber1, mediaType,
                null);
    }
    
    /**
     * Helper method to set a greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    private void genericSetGreeting(String telephoneNumber, String msid, String greetingType, String mediaType, 
            String cdgNumber, String filename)
            throws MediaObjectException, MimeTypeParseException {

        MediaProperties mediaProps = new MediaProperties();
        if (mediaType.equals("voice")) {
            mediaProps.setContentType(new MimeType("audio", "wav"));
        }
        else {
            mediaProps.setContentType(new MimeType("video", "mov"));
            
        }
        IMediaObject greeting = new FileMediaObject(new File(
                filename), mediaProps, BUFFER_SIZE);

        // Save the greeting
        platformAccess1.subscriberSetGreeting(telephoneNumber, greetingType,
                mediaType, cdgNumber, greeting);

        // Verify that the folder is created
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber
                + "/Greeting";
        File greetingFolder = new File(greetingFolderPath);
        assertTrue(greetingFolder.exists());

        // Verify that the file is there
        String basename = greetingType;
        if (basename.equals("extended_absence")) {
            // fix for extended absence
            basename = "extendedabsence";
        }
        else if (basename.equals("cdg")) {
            // fix for caller dependent greeting
            basename += cdgNumber;
        }
        String greetingFilePath = greetingFolderPath + "/" + basename
                + "_" + mediaType + (mediaType.equals("voice") ? ".wav" : ".mov");
        File greetingFile = new File(greetingFilePath);
        assertTrue(greetingFile.exists());
    }
    
    /**
     * Helper method to get a greeting
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    private void genericGetGreeting(String telephoneNumber, String greetingType, String mediaType, 
            String cdgNumber, String filename)
            throws MimeTypeParseException, MediaObjectException {

        // Get the spoken name greeting
        IMediaObject greeting = platformAccess1.subscriberGetGreeting(
                telephoneNumber, greetingType, mediaType, cdgNumber);

        assertNotNull(greeting);

        // Compare with the original file
        InputStream greetingStream = greeting.getInputStream();
        InputStream origStream = null;
        try {
            origStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            fail();
        }

        int c1 = 0;
        int c2 = 0;
        int f1, f2;
        try {
            do {
                f1 = greetingStream.read();
                c1++;
                f2 = origStream.read();
                c2++;
                // Verify that each piece of data is equal
                assertEquals(f1, f2);
            } while (f1 != -1 && f2 != -1);
        } catch (IOException e) {
            // Check that equal amount of data was read, and more than one loop
            assertEquals(c1, c2);
            assertTrue(c1 > 1);
        }
        try {
            origStream.close();
            greetingStream.close();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test delete the allcalls voice greeting
     * 
     * @throws MediaObjectException
     * @throws MimeTypeParseException
     */
    private void genericDeleteGreeting(String telephoneNumber, String msid, String greetingType, String mediaType, 
            String cdgNumber)
            throws MediaObjectException, MimeTypeParseException {

        platformAccess1.subscriberSetGreeting(telephoneNumber, greetingType,
                mediaType, cdgNumber, null);

        // Verify that the file is there
        String privateFolderPath = commonMessagingAccess
                .getMoipPrivateFolder(msid, false);
        String greetingFolderPath = privateFolderPath + "/" + telephoneNumber
                + "/Greeting";

        String basename = greetingType;
        if (basename.equals("extended_absence")) {
            // fix for extended absence
            basename = "extendedabsence";
        }
        else if (basename.equals("cdg")) {
            // fix for caller dependent greeting
            basename += cdgNumber;
        }
        String greetingFilePath = greetingFolderPath + "/" + basename
                + "_" + mediaType + (mediaType.equals("voice") ? ".wav" : ".mov");
        File greetingFile = new File(greetingFilePath);
        assertFalse(greetingFile.exists());

        // Delete again, to be sure it works when already deleted
        platformAccess1.subscriberSetGreeting(telephoneNumber, greetingType,
                mediaType, cdgNumber, null);
    }
    
    /**
     * Test setting, getting and deleting all greetings, except spoken name
     * 
     * @throws MimeTypeParseException
     * @throws MediaObjectException
     */
    public void testSetGetDeleteAllGreetings()
            throws MimeTypeParseException, MediaObjectException {
        String greetingTypes[] = {"allcalls",
                "noanswer",
                "busy",
                "outofhours",
                "extended_absence",
                "cdg",
                "temporary",
                "ownrecorded"};
        String filenames[] = {"allcalls",
                "noanswer",
                "busy",
                "outofhours",
                "extendedabsence",
                "cdg12345",
                "temporary",
                "ownrecorded"};

        // First, set the greetings
        String cdgNumber = null;
        // Loop for both voice and video
        for (String mediaType : new String[] {"voice", "video"}) {
            // Loop for the greeting types
            for (int i=0; i<greetingTypes.length; i++) {
                String filename = "../profilemanager/" + filenames[i] + (mediaType.equals("voice") ? ".wav" : ".mov");                

                if (greetingTypes[i].equals("cdg")) {
                    cdgNumber = telephoneNumber1;
                }
                else {
                    cdgNumber = null;
                }
                genericSetGreeting(telephoneNumber2, msid2, greetingTypes[i], mediaType, cdgNumber, filename);
            }
        }

        // Then, get them again 
        for (String mediaType : new String[] {"voice", "video"}) {
            for (int i=0; i<greetingTypes.length; i++) {
                String filename = "profilemanager/" + filenames[i] + (mediaType.equals("voice") ? ".wav" : ".mov");                

                if (greetingTypes[i].equals("cdg")) {
                    cdgNumber = telephoneNumber1;
                }
                else {
                    cdgNumber = null;
                }
                genericGetGreeting(telephoneNumber2, greetingTypes[i], mediaType, cdgNumber, filename);
            }
        }

        // And delete them 
        for (String mediaType : new String[] {"voice", "video"}) {
            for (int i=0; i<greetingTypes.length; i++) {
                if (greetingTypes[i].equals("cdg")) {
                    cdgNumber = telephoneNumber1;
                }
                else {
                    cdgNumber = null;
                }
                genericDeleteGreeting(telephoneNumber2, msid2, greetingTypes[i], mediaType, cdgNumber);
            }
        }        
    }
    
}
