package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediaobject.MultiThreadedTeztCase;
import com.mobeon.masp.mediaobject.MediaObject;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * Unit test class for {@link MediaQualifierFactory}
 *
 * @author mmawi
 */
public class MediaQualifierFactoryTest extends MultiThreadedTeztCase {
    /**
     * The factory tested.
     */
    private MediaQualifierFactory mediaQualifierFactory;

    public void setUp() {
        mediaQualifierFactory = new MediaQualifierFactory();
    }

    public void testCreateNumberQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.Number;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "10",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof NumberQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Integer.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof NumberQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Integer.class, mediaQualifier.getValueType());
    }

    public void testCreateStringQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.String;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "stringvalue",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof StringQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                String.class, mediaQualifier.getValueType());

        // 2 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof StringQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                String.class, mediaQualifier.getValueType());
    }

    public void testCreateDateDMQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.DateDM;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "2006-01-05",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof DateDMQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof DateDMQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());
    }

    public void testCreateCompleteDateQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.CompleteDate;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "2006-01-05 22:05:42 CET",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof CompleteDateQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof CompleteDateQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());
    }

    public void testCreateWeekdayQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.WeekDay;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "2006-02-21",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof WeekdayQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof WeekdayQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());
    }

    public void testCreateTime12Qualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.Time12;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "23:30:00",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof Time12Qualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof Time12Qualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());
    }

    public void testCreateTime24Qualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.Time24;

        // 1 test OK input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                "23:30:00",
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof Time24Qualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());

        // 2 test invalid input
        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("MediaQualifierException was excpected when " +
                    "creating a qualifier with invalid value.");
        } catch (MediaQualifierException e) {
            //OK
        }

        // 3 test null input
        mediaQualifier = mediaQualifierFactory.create(
                type,
                "testQualifier",
                null,
                IMediaQualifier.Gender.None);
        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof Time24Qualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                Date.class, mediaQualifier.getValueType());
    }

    public void testCreateIMediaObjectQualifier() throws Exception {
        IMediaQualifier mediaQualifier;
        IMediaQualifier.QualiferType type = IMediaQualifier.QualiferType.MediaObject;

        try {
            mediaQualifier = mediaQualifierFactory.create(
                    type,
                    "testQualifier",
                    "invalid",
                    IMediaQualifier.Gender.None);
            fail("UnsupportedOperationException was expected when " +
                    "creating a media object qualifier with this method.");
        } catch (UnsupportedOperationException e) {
            //OK
        }

        IMediaObject mediaObject = new MediaObject();

        mediaQualifier = mediaQualifierFactory.create(
                "testQualifier",
                mediaObject,
                IMediaQualifier.Gender.None);

        assertTrue("Media qualifier is not correct class.",
                mediaQualifier instanceof IMediaObjectQualifier);
        assertEquals("getType returns incorrect type.",
                type, mediaQualifier.getType());
        assertEquals("Incorrect value type.",
                IMediaObject.class, mediaQualifier.getValueType());
    }

    /**
     * Test the thread safety of <code>MediaQualifierFactory<code>.
     *
     * <pre>
     * Create two threads, one that uses the factory to create WeekDay
     * qualifiers and one that creates DateDM qualifiers.
     * The client threads run for 2000 ms.
     * </pre>
     */
    public void testConcurrent() {
        final MediaQualifierFactoryClient[] clients =
                new MediaQualifierFactoryClient[2];

        clients[0] = new MediaQualifierFactoryClient(
                        IMediaObjectQualifier.QualiferType.WeekDay,
                        "2006-02-27");

        clients[1] = new MediaQualifierFactoryClient(
                        IMediaObjectQualifier.QualiferType.DateDM,
                        "2006-02-28");

        runTestCaseRunnables(clients);

        // run for 2000 ms.
        new Timer().schedule(new TimerTask() {
            public void run() {
                for (int i = 0; i < clients.length; i++) {
                    clients[i].setDone();
                }
            }

        }, 2000);

        joinTestCaseRunnables(clients);
    }

    private class MediaQualifierFactoryClient extends TestCaseRunnable {
        private IMediaQualifier.QualiferType type;
        private String value;
        private boolean done = false;

        public MediaQualifierFactoryClient(IMediaQualifier.QualiferType type,
                                           String value) {
            this.type = type;
            this.value = value;
        }

        public void setDone() {
            done = true;
        }

        public void runTestCase() throws Throwable {
            while (!done) {
                IMediaQualifier qualifier =
                        mediaQualifierFactory.create(type, null, value, IMediaQualifier.Gender.None);
                assertEquals("Qualifier type should be " + type,
                        type, qualifier.getType());

                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                assertEquals("Qualifier value should be " + value,
                        value, sdf.format(qualifier.getValue()));
            }
        }
    }
}
