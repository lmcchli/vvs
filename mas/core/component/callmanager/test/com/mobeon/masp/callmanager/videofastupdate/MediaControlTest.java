package com.mobeon.masp.callmanager.videofastupdate;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;

import org.apache.xmlbeans.XmlException;

import java.util.List;

/**
 * VideoFastUpdaterImpl Tester.
 *
 * @author Malin Flodin
 */
public class MediaControlTest extends TestCase {

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private MediaControlImpl mediaControl = MediaControlImpl.getInstance();

    public MediaControlTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that getGeneralErrors throw an exception is the XML document
     * is empty.
     * @throws Exception if test case fails.
     */
    public void testGetGeneralErrorsForEmptyDocument() throws Exception {
        // Verify that exception is thrown if the xml document is empty
        String xmlDoc = "";
        try {
            mediaControl.getGeneralErrors(xmlDoc);
            fail("Exception not thrown when expected");
        } catch (XmlException e) {
        }
    }

    /**
     * Verifies that getGeneralErrors throw an exception is the XML document
     * is invalid.
     * @throws Exception if test case fails.
     */
    public void testGetGeneralErrorsForInvalidDocument() throws Exception {
        // Verify that exception is thrown if the xml document is invalid
        String xmlDoc = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n";
        try {
            mediaControl.getGeneralErrors(xmlDoc);
            fail("Exception not thrown when expected");
        } catch (XmlException e) {
        }
    }

    /**
     * Verifies that getGeneralErrors returns an empty list if no errors exist
     * in the XML document.
     * @throws Exception if test case fails.
     */
    public void testGetGeneralErrorsWhenNoErrorsExist() throws Exception {
        // Verify that an empty list is returned if no general_error exists in
        // document
        String xmlDoc =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                "<media_control xmlns=\"urn:ietf:params:xml:ns:media_control\">\r\n" +
                "  <vc_primitive>\r\n" +
                "    <to_encoder>\r\n" +
                "      <picture_fast_update/>\r\n" +
                "    </to_encoder>\r\n" +
                "  </vc_primitive>\r\n" +
                "</media_control>";
        try {
            List<String> errors = mediaControl.getGeneralErrors(xmlDoc);
            assertEquals(0, errors.size());
        } catch (XmlException e) {
            fail("Exception thrown when not expected");
        }
    }

    /**
     * Verifies that getGeneralErrors returns a list with the error string
     * if the XML document contains one general_error.
     * @throws Exception if test case fails.
     */
    public void testGetGeneralErrorsWhenOneErrorExists() throws Exception {
        // Verify that true is returned if one general_error exists in document
        String xmlDoc =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                "<media_control xmlns=\"urn:ietf:params:xml:ns:media_control\">\r\n" +
                "  <general_error>\r\n" +
                "    ERROR...\r\n" +
                "  </general_error>\r\n" +
                "</media_control>";
        try {
            List<String> errors = mediaControl.getGeneralErrors(xmlDoc);
            assertEquals(1, errors.size());
            assertEquals("\n" + "    ERROR...\n" + "  ", errors.get(0));
        } catch (XmlException e) {
            fail("Exception thrown when not expected");
        }
    }

    /**
     * Verifies that getGeneralErrors returns a list with the error strings
     * if the XML document contains multiple general_error's.
     * @throws Exception if test case fails.
     */
    public void testGetGeneralErrorsWhenMultipleErrorsExists() throws Exception {
        // Verify that true is returned if one general_error exists in document
        String xmlDoc =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                "<media_control xmlns=\"urn:ietf:params:xml:ns:media_control\">\r\n" +
                "  <general_error>\r\n" +
                "    ERROR 1...\r\n" +
                "  </general_error>\r\n" +
                "  <general_error>\r\n" +
                "    ERROR 2...\r\n" +
                "  </general_error>\r\n" +
                "</media_control>";
        try {
            List<String> errors = mediaControl.getGeneralErrors(xmlDoc);
            assertEquals(2, errors.size());
            assertEquals("\n" + "    ERROR 1...\n" + "  ", errors.get(0));
            assertEquals("\n" + "    ERROR 2...\n" + "  ", errors.get(1));
        } catch (XmlException e) {
            fail("Exception thrown when not expected");
        }
    }


    /**
     * Verifies that createPictureFastUpdateRequest returns an XML document
     * containing a picture fast update request.
     * @throws Exception if test case fails.
     */
    public void testCreatePictureFastUpdateRequest() throws Exception {
        String pictureFastUpdateRequest =
                mediaControl.createPictureFastUpdateRequest();

        pictureFastUpdateRequest = pictureFastUpdateRequest.replace("\r", "");
        pictureFastUpdateRequest = pictureFastUpdateRequest.replace("\n", "");

        String expectedResult =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<media_control xmlns=\"urn:ietf:params:xml:ns:media_control\">" +
                "  <vc_primitive>" +
                "    <to_encoder>" +
                "      <picture_fast_update/>" +
                "    </to_encoder>" +
                "  </vc_primitive>" +
                "</media_control>";

        assertEquals(expectedResult, pictureFastUpdateRequest);
    }
}
