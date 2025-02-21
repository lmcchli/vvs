package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.IMediaContentResource;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.MockObjectTestCase;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.io.File;

/**
 * JUnit test for the {@link MediaContentResourceMapper} class.
 */
public class MediaContentResourceMapperTest extends MockObjectTestCase {
    /**
     * The logger used.
     */
    private static ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContentResourceMapperTest.class);
    /**
     * The MediaContentPackage.xml file to parse.
     */
    private static String voicePackageFileName =
            "applications/mediacontentpackages/en_audio_1/MediaContentPackage.xml";
    /**
     * A well-formed file but with illegal content.
     */
    private static String illegalContentFile =
            "test/IllegalMediaContentPackage.xml";
    /**
     * A not-well-formed file.
     */
    private static String notWellFormedContentFile =
            "test/NotWellFormedMediaContentPackage.xml";
    /**
     * The mapper object tested.
     */
    private MediaContentResourceMapper mediaContentResourceMapper;
    /**
     * URL to a well-formed XML with valid content.
     */
    private URL validPackageXML = null;
    /**
     * URL to a well-formed XML, but that has illegal content.
     */
    private URL illegalContentPackageXML = null;
    /**
     * URL to a non-well-formed XML
     */
    private URL notWellFormedXML = null;

    /**
     * Creates the mediaContentResourceMapper object to test.
     * Al
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        mediaContentResourceMapper =
                new MediaContentResourceMapper();

        try {
            File file = new File(voicePackageFileName);
            validPackageXML = file.toURL();
            //LOGGER.debug("URL="+file.toURL());
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+voicePackageFileName);
        }
        try {
            File file = new File(illegalContentFile);
            illegalContentPackageXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+illegalContentFile);
        }
        try {
            File file = new File(notWellFormedContentFile);
            notWellFormedXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+notWellFormedContentFile);
        }
    }

    // Cleans up after each test
    public void tearDown() throws Exception {
        super.tearDown();
        mediaContentResourceMapper = null;
    }

    /**
     * Todo test should also parse a video package.
     * Tests for the {@link MediaContentResourceMapper#fromXML(java.net.URL)}
     * method.
     * <p/>
     * <pre>
     * 1. Illegal Files
     *  Condition:
     * <p/>
     *  Action:
     *      1. file is Well-formed but illegal content.
     *      2. file is not Well-formed.
     *  Result:
     *      1-2. SaxMapperException
     * <p/>
     * 2. Parse file MediaContentPackage file.
     *  Condition:
     *      A FileReader is created to the MediaContentPackage file.
     *      The MediaContentPackage file is well formed.
     *  Action:
     *  Result:
     *      A non-null IMediaContentResource object is returned.
     * <p/>
     * 3. Validate content in returned object from a voice package xml.
     * Condition:
     *      A non-null IMediaContentResource is returned from
     *      a well formed MediaContentPackage xml.
     *  Action:
     *  Result:
     *      1) Id is "en_audio_1".
     *      2) Language is "en".
     *      3) Type is "prompt".
     *      4) Priority is 2.
     *      5) Number of codecs should be 1 and and the
     *         MIME-type of the codec should be "audio/pcmu"
     * </pre>
     */
    public void testFromXML() {
        MimeType audioPcmu = null;
        try {
            audioPcmu = new MimeType("audio/pcmu");
        } catch (MimeTypeParseException e1) {
            fail("Failed to create MimeType objects");
        }

        // 1
        try {
            IMediaContentResource mediaContentResource =
                    mediaContentResourceMapper.fromXML(illegalContentPackageXML);
            fail("SaxMapperException should be thrown if illegal content xml");
        } catch (SaxMapperException e) {
            /*ok*/
        }

        try {
            mediaContentResourceMapper = new MediaContentResourceMapper();
            IMediaContentResource mediaContentResource =
                mediaContentResourceMapper.fromXML(notWellFormedXML);
            fail("SaxMapperException should be thrown if not well formed xml");
        } catch (SaxMapperException e) {
            /*ok*/
        }
        // 2
        mediaContentResourceMapper = new MediaContentResourceMapper();
        IMediaContentResource mediaContentResource =
                mediaContentResourceMapper.fromXML(validPackageXML);
        assertNotNull(mediaContentResource);

        // 3
        assertEquals("Id does not match XML",
                "en_audio_1", mediaContentResource.getID());
        assertEquals("Language does not match XML",
                "en", mediaContentResource.
                getMediaContentResourceProperties().getLanguage());
        assertEquals("Type does not match XML",
                "prompt", mediaContentResource.
                getMediaContentResourceProperties().getType());
        assertEquals("Priority does not match XML",
                1, mediaContentResource.getPriority());
        assertEquals("Number of codecs does not match XML",
                1, mediaContentResource.getMediaContentResourceProperties().
                getMediaCodecs().size());
        assertTrue("Codec does not match XML",
                mediaContentResource.getMediaContentResourceProperties().
                        hasMatchingCodec(audioPcmu));

        assertNotNull(mediaContentResource.getMediaContentResourceProperties());
        assertNotNull(mediaContentResource.getMediaContentResourceProperties().
                getLanguage());
        assertNotNull(mediaContentResource.getMediaContentResourceProperties().
                getType());
        assertNotNull(mediaContentResource.getMediaContentResourceProperties().
                getVoiceVariant());

        assertNotNull(mediaContentResource.getMediaContentResourceProperties().
                getMediaCodecs());


        List<MimeType> mimeTypeList =
                mediaContentResource.getMediaContentResourceProperties().
                        getMediaCodecs();
        assertTrue(mimeTypeList.size() > 0);
        try {
            assertTrue(mediaContentResource.getMediaContentResourceProperties().
                    hasMatchingCodec(
                    new MimeType("audio", "pcmu")));
        } catch (MimeTypeParseException e) {
            //todo
            fail("");
        }
        assertTrue(mediaContentResource.getMediaContentResourceProperties()
                .getMediaCodecs().size() == 1);

    }
}
