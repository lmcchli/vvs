package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.MediaObjectSource;
import com.mobeon.masp.mediaobject.MediaLength;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.MockObjectTestCase;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * JUnit test for the {@link MediaContentResourceMapper} class.
 */
public class MediaObjectSourceMapperTest extends MockObjectTestCase {
    /**
     * The logger used.
     */
    private static ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContentResourceMapperTest.class);
    /**
     * The MediaContentPackage.xml file to parse.
     */
    private static final String MEDIA_OBJECTS_FILE =
            "applications/mediacontentpackages/en_audio_1/MediaObjects.xml";
    /**
     * Number of objects in file above
     */
    private static final int NR_OF_OBJECTS = 198;
    /**
     * A well-formed file but with illegal content.
     */
    private static String ILLEGALMEDIAOBJECTSFILE =
            "test/IllegalMediaObjects.xml";
    /**
     * A well-formed file but with illegal content (the type attr is missing).
     */
    private static String ILLEGALMEDIAOBJECTSFILE2 =
            "test/IllegalMediaObjects2.xml";
    /**
     * A not-well-formed file.
     */
    private static String NOTWELLFORMEDFILE =
            "test/NotWellFormedMediaObjects.xml";
    /**
     * The mapper object tested.
     */
    private MediaObjectSourceMapper mediaObjectSourceMapper;

    /**
     * URL to a well-formed XML with valid content.
     */
    private URL validXML = null;
    /**
     * URL to a well-formed XML, but that has illegal content.
     */
    private URL illegalXML = null;
    /**
     * URL to a well-formed XML, but that has illegal content.
     */
    private URL illegalXML2 = null;
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
        mediaObjectSourceMapper =
                new MediaObjectSourceMapper();

        try {
            File file = new File(MEDIA_OBJECTS_FILE);
            validXML = file.toURL();
            //LOGGER.debug("URL="+file.toURL());
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+MEDIA_OBJECTS_FILE);
        }
        try {
            File file = new File(ILLEGALMEDIAOBJECTSFILE);
            if (!file.exists()) {
                fail("File does not exist " + ILLEGALMEDIAOBJECTSFILE);
            }
            illegalXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+ILLEGALMEDIAOBJECTSFILE);
        }
        try {
            File file = new File(ILLEGALMEDIAOBJECTSFILE2);
            if (!file.exists()) {
                fail("File does not exist " + ILLEGALMEDIAOBJECTSFILE2);
            }
            illegalXML2 = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+ILLEGALMEDIAOBJECTSFILE2);
        }

        try {
            File file = new File(NOTWELLFORMEDFILE);
            if (!file.exists()) {
                fail("File does not exist " + NOTWELLFORMEDFILE);
            }
            notWellFormedXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from:"+NOTWELLFORMEDFILE);
        }
    }

    // Cleans up after each test
    public void tearDown() throws Exception {
        super.tearDown();
        mediaObjectSourceMapper = null;
    }

    /**
     *
     * Tests for the {@link MediaObjectSourceMapper#fromXML(java.net.URL)}.
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
     *      SaxMapperException is thrown
     * <p/>
     * 2. Parse file MediaObjects.xml file.
     *  Condition:
     *
     *      The MediaObjects.xml file is well formed.
     *  Action:
     *  Result:
     *      A non-null list of MediaObjectSource objects is returned with
     *      the size of NR_OF_OBJECTS.
     *
     * <p/>
     * 3. Validate content in returned objects.
     * Condition:
     *      A non-null list of MediaObjectSource objects is returned.
     *  Action:
     *      Iterate over each MediaObjectSource and assert content
     *      is corrent.
     *
     *  Result:
     *      - General: All objects has length 5000 MILLISECONDS (except the below)
     *      - The object with src="beep.wav" has the following:
     *           - length = {6000 Milliseconds}
     *           - sourcetext = "Previous message."
     *      - The object with src="subject" has the following:
     *           - length = 1 PAGES
     *           - sourcetext = "Voice message from Nisse"
     *
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
            List<MediaObjectSource> mediaObjectSourceList =
                    mediaObjectSourceMapper.fromXML(illegalXML);
            fail("SaxMapperException should have been thrown");
        } catch (SaxMapperException e) {/*ok*/}

        try {
            List<MediaObjectSource> mediaObjectSourceList =
                    mediaObjectSourceMapper.fromXML(notWellFormedXML);
            fail("SaxMapperException should have been thrown");
        } catch (SaxMapperException e) {/*ok*/}

        // Reset the mediaObjectSourceMapper
        mediaObjectSourceMapper = new MediaObjectSourceMapper();
        try {
            List<MediaObjectSource> mediaObjectSourceList =
                    mediaObjectSourceMapper.fromXML(illegalXML2);
            fail("SaxMapperException should have been thrown");
        } catch (SaxMapperException e) {/*ok*/}

        // 2
        mediaObjectSourceMapper = new MediaObjectSourceMapper();
        List<MediaObjectSource> mediaObjectSourceList =
                mediaObjectSourceMapper.fromXML(validXML);
        assertNotNull(mediaObjectSourceList);
        assertEquals("Number of objects should be " + NR_OF_OBJECTS,
                NR_OF_OBJECTS, mediaObjectSourceList.size());
        // 3
        boolean foundBeep = false;
        boolean foundSubject = false;
        for (MediaObjectSource mediaObjectSource : mediaObjectSourceList) {

            if (mediaObjectSource.getSrc().equals("beep.wav")) {
                foundBeep = true;
                List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
                assertEquals("beep.wav should have 1 length",
                        1, lengths.size());
                MediaLength length = lengths.get(0);
                assertEquals("beep.wav should have length in unit MILLISECONDS",
                        MediaLength.LengthUnit.MILLISECONDS, length.getUnit());
                assertEquals("Length of beep.wav should be 6000 millisecs",
                        6000, length.getValue());
            } else if (mediaObjectSource.getSrc().equals("subject")) {
                foundSubject = true;
                List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
                assertEquals("subject should have 1 length",
                        1, lengths.size());
                MediaLength length = lengths.get(0);
                assertEquals("subject should have length in unit PAGES",
                        MediaLength.LengthUnit.PAGES, length.getUnit());
                assertEquals("Length of subject should be 1 pages",
                        1, length.getValue());
                String subject = mediaObjectSource.getSourceText();
                assertEquals("Text in subject should be Voice message from Nisse",
                        "Voice message from Nisse", subject);
            } else if (mediaObjectSource.getSrc().equals("subject2")) {
                foundSubject = true;
                List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
                assertEquals("subject should have 1 length",
                        1, lengths.size());
                MediaLength length = lengths.get(0);
                assertEquals("subject should have length in unit PAGES",
                        MediaLength.LengthUnit.PAGES, length.getUnit());
                assertEquals("Length of subject should be 5 pages",
                        5, length.getValue());
                String subject = mediaObjectSource.getSourceText();
                assertEquals("Text in subject should be Voice message from Kalle",
                        "Voice message from Kalle", subject);
            } else if (mediaObjectSource.getSrc().equals("test.wav")) {

                List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
                assertEquals("test.wav should have 1 length",
                        1, lengths.size());
                MediaLength length = lengths.get(0);
                assertEquals("test.wav should have length in unit MILLISECONDS",
                        MediaLength.LengthUnit.MILLISECONDS, length.getUnit());
                assertEquals("Length of test.wav should be 10000 millisecs",
                        10000, length.getValue());
            } else {
                List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
                assertEquals("should have 1 length",
                        1, lengths.size());
                MediaLength length = lengths.get(0);
                assertEquals("wav should have length in unit MILLISECONDS",
                        MediaLength.LengthUnit.MILLISECONDS, length.getUnit());
                assertEquals("Length should be 5000 millisecs",
                        5000, length.getValue());
            }
        }
        assertTrue("Did not find object beep", foundBeep);
        assertTrue("Did not find object subject", foundSubject);

    }
}
