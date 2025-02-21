/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Unit tests for class {@link ContentTypeMapperImpl}.
 *
 * @author Mats Egland
 */
public class ContentTypeMapperImplTest extends MultiThreadedTeztCase {

    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(ContentTypeMapperImplTest.class);

    /**
     * The content type mapper tested.
     */
    private ContentTypeMapperImpl contentTypeMapper;

    private IConfiguration configuration;

    /**
     * Creates the tested ContentTypeMapper from
     * spring factory.
     *
     * @throws Exception If error occurrs.
     */
    protected void setUp() throws Exception {
        super.setUp();

    	File dir = new File(".");
    	String masSpecificConfig = dir.getAbsolutePath() + "\\cfg\\/" + CommonOamManager.MAS_SPECIFIC_CONF;
    	System.out.println(masSpecificConfig);

        configuration = getConfiguration(masSpecificConfig);
        contentTypeMapper = new ContentTypeMapperImpl();
        contentTypeMapper.setConfiguration(configuration);
        contentTypeMapper.init();

    }

    private IConfiguration getConfiguration(String... files) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(files);
        return configurationManager.getConfiguration();
    }

    /**
     * Tests for the method
     * {@link ContentTypeMapperImpl#mapToContentType(com.mobeon.masp.mediaobject.MediaMimeTypes)}.
     *
     * <pre>
     * 1. Test for codec "audio/pcmu".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "audio/wav". This content type has the codec "audio/pcmu".
     *  Action:
     *      Call mapToContentType with codec "audio/pcmu".
     *  Result:
     *      The content type "audio/wav" is returned.
     *
     * 2. Test for codecs "audio/pcmu" "video/h263".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "video/quicktime". This content type has the codecs "audio/pcmu"
     *      and "video/h263"
     *  Action:
     *      Call mapToContentType with codecs "audio/pcmu" "video/h263".
     *  Result:
     *      The content type "video/quicktime" is returned.
     * </pre>
     */
    public void testMapToContentType_from_codecs() {
        // Conditions
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        MediaMimeTypes codecs;
        MimeType contentType;

        // 1
        codecs = new MediaMimeTypes(mimeType1);
        contentType = contentTypeMapper.mapToContentType(codecs);
        try {
            assertTrue("The contentType should be audio/wav",
                    contentType.match("audio/wav"));
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        // 2
        codecs = new MediaMimeTypes(mimeType1, mimeType2);
        contentType = contentTypeMapper.mapToContentType(codecs);
        try {
            assertTrue("The contentType should be video/quicktime",
                    contentType.match("video/quicktime"));
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
    }

    /**
     * Tests for the method
     * {@link ContentTypeMapperImpl#mapToContentType(String)}.
     *
     * <pre>
     * 1. Test for file extension "wav".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "audio/wav". This content type has the file extension "wav".
     *  Action:
     *      Call mapToContentType with the file extension "wav".
     *  Result:
     *      The content type "audio/wav" is returned.
     *
     * 2. Test for file extension "mov".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "video/quicktime". This content type has the file extension "mov".
     *  Action:
     *      Map the file extension "mov".
     *  Result:
     *      The content type "video/quicktime" is returned.
     *
     * 3. Test for file extension "txt".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "text/plain". This content type has the file extension "txt".
     *  Action:
     *      Map the file extension "txt".
     *  Result:
     *      The content type "text/plain" is returned.
     * </pre>
     */
    public void testMapToContentType_from_fileextension() {
        MimeType mimeType;
        String fileExt;
        String contentType;

        //1 wav
        fileExt = "wav";
        contentType = "audio/wav";
        mimeType = contentTypeMapper.mapToContentType(fileExt);
        try {
            assertTrue("MimeType should be \"" + contentType + "\"",
                    mimeType.match(contentType));
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        //2 mov
        fileExt = "mov";
        contentType = "video/quicktime";
        mimeType = contentTypeMapper.mapToContentType(fileExt);
        try {
            assertTrue("MimeType should be \"" + contentType + "\"",
                    mimeType.match(contentType));
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        //3 txt
        fileExt = "txt";
        contentType = "text/plain";
        mimeType = contentTypeMapper.mapToContentType(fileExt);
        try {
            assertTrue("MimeType should be \"" + contentType + "\"",
                    mimeType.match(contentType));
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
    }

    /**
     * Tests for method
     * {@link ContentTypeMapperImpl#mapToFileExtension(MediaMimeTypes)}.
     *
     * <pre>
     * 1. Test for codec "audio/pcmu".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "audio/wav". This content type has the file extension "wav" and
     *      the codec "audio/pcmu".
     *  Action:
     *      Call mapToFileExtension with the codec "audio/wav".
     *  Result:
     *      The file extension "wav" is returned.
     *
     * 2. Test for codec "audio/pcmu video/h263".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "video/quicktime". This content type has the file extension "mov"
     *      and the codecs "audio/pcmu" and "video/h263".
     *  Action:
     *      Call mapToFileExtension with the codecs "audio/wav" "video/h263".
     *  Result:
     *      The file extension "mov" is returned.
     *
     * 3. Test for codec "text/plain".
     *  Condition:
     *      The ContentTypeMapper is configured with the content type
     *      "text/plain". This content type has the file extension "txt"
     *      and the codec "text/plain".
     *  Action:
     *      Call mapToFileExtension with the codec "text/plain".
     *  Result:
     *      The file extension "txt" is returned.
     *
     * </pre>
     */
    public void testMapToFileExtension() {
        // Conditions
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        MimeType mimeType3 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "h263");
            mimeType3 = new MimeType("text", "plain");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        MediaMimeTypes codecs;
        String fileExt;

        // 1
        codecs = new MediaMimeTypes(mimeType1);
        fileExt = contentTypeMapper.mapToFileExtension(codecs);
        assertEquals("The file extension should be wav",
                "wav", fileExt);

        // 2
        codecs = new MediaMimeTypes(mimeType1, mimeType2);
        fileExt = contentTypeMapper.mapToFileExtension(codecs);
        assertEquals("The file extension should be mov",
                "mov", fileExt);

        // 3
        codecs = new MediaMimeTypes(mimeType3);
        fileExt = contentTypeMapper.mapToFileExtension(codecs);
        assertEquals("The file extension should be txt",
                "txt", fileExt);
    }

    /**
     * Test multiple threads accessing the content type mapper.
     *
     * <pre>
     *
     * </pre>
     */
    public void testConcurrent() {
        MimeType codec1 = null;
        MimeType codec2 = null;
        MimeType contentAudio = null;
        MimeType contentVideo = null;
        String fileExtWav = "wav";
        String fileExtMov = "mov";
        try {
            codec1 = new MimeType("audio/pcmu");
            codec2 = new MimeType("video/h263");
            contentAudio = new MimeType("audio/wav");
            contentVideo = new MimeType("video/quicktime");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes codecsAudio = new MediaMimeTypes(codec1);
        MediaMimeTypes codecsVideo = new MediaMimeTypes(codec1, codec2);

        final ContentTypeMapperClient[] clients = new ContentTypeMapperClient[2];

        clients[0] =
                new ContentTypeMapperClient(contentTypeMapper,
                        contentAudio,
                        codecsAudio,
                        fileExtWav);
        clients[1] =
                new ContentTypeMapperClient(contentTypeMapper,
                        contentVideo,
                        codecsVideo,
                        fileExtMov);

        runTestCaseRunnables(clients);
        // Let the clients run for two seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                for (ContentTypeMapperClient client : clients) {
                    client.setDone();
                }
            }
        }, 2000);
        joinTestCaseRunnables(clients);
    }

    private class ContentTypeMapperClient extends TestCaseRunnable {
        private boolean done = false;
        private MimeType contentType;
        private MediaMimeTypes codecs;
        private String fileExt;
        private ContentTypeMapperImpl contentTypeMapper;

        public ContentTypeMapperClient(ContentTypeMapperImpl contentTypeMapper,
                                       MimeType contentType,
                                       MediaMimeTypes codecs,
                                       String fileExt) {
            this.contentTypeMapper = contentTypeMapper;
            this.contentType = contentType;
            this.codecs = codecs;
            this.fileExt = fileExt;
        }

        public void setDone() {
            done = true;
        }

        public void runTestCase() throws Throwable {
            while(!done) {
                String mappedFileExt = contentTypeMapper.mapToFileExtension(codecs);
                assertEquals("File extension should be " + fileExt,
                        fileExt, mappedFileExt);

                MimeType mappedContentType = contentTypeMapper.mapToContentType(fileExt);
                assertTrue("Content type should be " + contentType.toString(),
                        mappedContentType.match(contentType));

                mappedContentType = contentTypeMapper.mapToContentType(codecs);
                assertTrue("Content type should be " + contentType.toString(),
                        mappedContentType.match(contentType));
            }
        }
    }
}
