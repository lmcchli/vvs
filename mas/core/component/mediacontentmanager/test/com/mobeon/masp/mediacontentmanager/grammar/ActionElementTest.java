package com.mobeon.masp.mediacontentmanager.grammar;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.IActionElementFactory;
import com.mobeon.masp.mediacontentmanager.IActionElement;

/**
 * @author mmawi
 */
public class ActionElementTest extends TestCase {

    private final ILogger LOGGER = ILoggerFactory.getILogger(getClass());

    private IActionElement mediaFileActionElement;
    private IActionElement selectActionElement;
    private IActionElement skipActionElement;
    private IActionElement swapActionElement;

    public void setUp() throws Exception {
        super.setUp();

        IActionElementFactory actionElementFactory = new ActionElementFactory();

        IActionElement.ActionType type;
        type = IActionElement.ActionType.mediafile;
        mediaFileActionElement = actionElementFactory.create(type);
        type = IActionElement.ActionType.select;
        selectActionElement = actionElementFactory.create(type);
        type = IActionElement.ActionType.skip;
        skipActionElement = actionElementFactory.create(type);
        type = IActionElement.ActionType.swap;
        swapActionElement = actionElementFactory.create(type);
    }

    public void testMediaFile() throws Exception {
        // test getType
        assertTrue("Class is not MediaFileActionElement",
                mediaFileActionElement instanceof MediaFileActionElement);
        assertEquals("Wrong type from getType",
                mediaFileActionElement.getType(), IActionElement.ActionType.mediafile);

        // test setSwapValue
        try {
            mediaFileActionElement.setSwapValue(1);
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        // test getSwapValue
        try {
            mediaFileActionElement.getSwapValue();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        // test setMediaFileName
        mediaFileActionElement.setMediaFileName("mediafile.wav");

        // test getMediaFileName
        assertEquals("Wrong filename. ",
                "mediafile.wav", mediaFileActionElement.getMediaFileName());
    }

    public void testSelect() throws Exception {

        assertTrue("Class is not SelectActionElement",
                selectActionElement instanceof SelectActionElement);
        assertEquals("Wrong type from getType",
                selectActionElement.getType(), IActionElement.ActionType.select);

        try {
            selectActionElement.setSwapValue(1);
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            selectActionElement.getSwapValue();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            selectActionElement.setMediaFileName("invalid");
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            selectActionElement.getMediaFileName();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    public void testSkip() throws Exception {
        assertTrue("Class is not SkipActionElement",
                skipActionElement instanceof SkipActionElement);
        assertEquals("Wrong type from getType",
                skipActionElement.getType(), IActionElement.ActionType.skip);

        try {
            skipActionElement.setSwapValue(1);
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            skipActionElement.getSwapValue();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            skipActionElement.setMediaFileName("invalid");
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            skipActionElement.getMediaFileName();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

    }

    public void testSwap() throws Exception {
        assertTrue("Class is not SwapActionElement",
                swapActionElement instanceof SwapActionElement);
        assertEquals("Wrong type from getType",
                swapActionElement.getType(), IActionElement.ActionType.swap);

        swapActionElement.setSwapValue(1);

        assertEquals("Unexpected value", 1, swapActionElement.getSwapValue());

        try {
            swapActionElement.setMediaFileName("invalid");
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }

        try {
            swapActionElement.getMediaFileName();
            fail("UnsupportedOperationException expected.");
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }
}
