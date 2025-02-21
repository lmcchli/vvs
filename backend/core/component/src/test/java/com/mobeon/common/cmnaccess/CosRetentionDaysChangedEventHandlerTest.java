/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.common.cmnaccess;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.easymock.Capture;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.impl.StdoutLogger;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysChangedEvent;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysData;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CosRetentionDaysChangedEventHandler.MessageExpirySetter;
import com.mobeon.common.cmnaccess.CosRetentionDaysChangedEventHandler.StateFileFetcher;

/**
 * 
 *
 * @author lmcyvca
 */
//Required because of a dependency problem with LibSysUtils
@SuppressStaticInitializationFor("com.abcxyz.messaging.mfs.MFSFactory")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MFSFactory.class })
public class CosRetentionDaysChangedEventHandlerTest {

    public static final SimpleDateFormat DateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    private CosRetentionDaysChangedEventHandler eventHandler;

    @BeforeClass
    public static void initClass() {
        DateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    @Before
    public void createMOIPCosChangeMessageRetentionBR() throws Exception {
        LogAgent logger = new StdoutLogger();
        eventHandler = new CosRetentionDaysChangedEventHandler(logger, null, null);
    }

    /**
     * Happy test case where the new COS offers more days than the old COS. No COS values are missing and they are all valid values.
     * We verify that events are sent for each message, using the correct attribute value based on the message state and class.
     */
    @Test
    public void newCosOffersMoreDaysThanCurrentCos() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataLowValues();
        COSRetentionDaysData cos_B = createCosDataHighValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
        
        // Setup mocks

        StateFileData[] stateFileDataArray = new StateFileData[] { new StateFileData(1, "new", "voice"),
                new StateFileData(2, "read", "video"), new StateFileData(3, "saved", "fax") };

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileDataArray);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        for (int i = 0; i < stateFileDataArray.length; i++) {
            for (Map.Entry<String, String> keyVal : cos_A.getAttributes()) {
                String attributeName = keyVal.getKey();
                if (attributeName.toLowerCase().endsWith(stateFileDataArray[i].messageClass)
                        && attributeName.toLowerCase().contains(stateFileDataArray[i].messageState)) {
                    String currentExpiryDaysString = keyVal.getValue();
                    int currentExpiryDays = Integer.parseInt(currentExpiryDaysString);
                    String newExpiryDaysString = cos_B.getAttributeValue(attributeName);
                    int newExpiryDays = Integer.parseInt(newExpiryDaysString);
                    int expiryDaysDiff = newExpiryDays - currentExpiryDays;
                    Calendar expectedExpiryCal = Calendar.getInstance();
                    expectedExpiryCal.setTime(stateFileDataArray[i].date);
                    expectedExpiryCal.add(Calendar.DAY_OF_MONTH, expiryDaysDiff);

                    // New expiry is in the future - check the expiry date is as expected
                    msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());
                }
            }

        }

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * Happy test case where both COS have the same number of expiry days. We expect no notifications to be sent.
     */
    @Test
    public void bothCOSHaveSameExpiryDays() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataLowValues();
        COSRetentionDaysData cos_B = createCosDataLowValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        // Setup mocks

        StateFileData[] stateFileDataArray = new StateFileData[] { new StateFileData(100, "new", "voice") };

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileDataArray);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * Happy test case where the new COS offers less days than the old COS. The messages will therefore be rescheduled to be
     * expiring sooner, but in this test we arrange that the current message is set to expire far enough in the future that the new
     * expiry time is also in the future (i.e. not an immediate expiry).
     */
    @Test
    public void newCosLessPermissiveWithNewExpiryInTheFuture() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataHighValues();
        COSRetentionDaysData cos_B = createCosDataLowValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        // Setup mocks

        StateFileData stateFileData = new StateFileData(100, "new", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        String currentExpiryDaysString = cos_A.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
        int currentExpiryDays = Integer.parseInt(currentExpiryDaysString);
        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        int expiryDaysDiff = newExpiryDays - currentExpiryDays;
        Calendar expectedExpiryCal = Calendar.getInstance();
        expectedExpiryCal.setTime(stateFileData.date);
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, expiryDaysDiff);

        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * Happy test case where the new COS offers less days than the old COS. No COS values are missing and they are all valid values.
     * The messages will therefore be rescheduled to be expiring sooner, and in this test we arrange that the new expiry time ends
     * up being "now".
     */
    @Test
    public void newCosLessPermissiveWithNewExpiryNow() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataHighValues();
        COSRetentionDaysData cos_B = createCosDataLowValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        // Setup mocks

        StateFileData stateFileData = new StateFileData(1, "new", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        Capture<Date> expiryCapture = Capture.newInstance();

        // Capture the new expiry values so we can check them post-test, because the allowed values are in a range.
        msgExpirySetterMock.setMessageExpiry(capture(expiryCapture), (StateFile) anyObject());

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);

        /*
         * The new expiry times should all be any time today, or in the past. In the past is OK because the scheduler is supposed to
         * be able to handle events in the past and schedule them as soon as possible.
         */
        for (Date actualExpiry : expiryCapture.getValues()) {
            Calendar nowCal = Calendar.getInstance();
            Calendar actualExpiryCal = Calendar.getInstance();
            actualExpiryCal.setTime(actualExpiry);

            assertTrue(isToday(actualExpiryCal.getTime()) || actualExpiryCal.compareTo(nowCal) <= 0);
        }
    }

    /**
     * Verify the class can handle state files that contain no expiry time in C1. For these messages the current expiry time should
     * be considered infinite (no expiry), so the new expiry time should be scheduled relative to "now". i.e. new expiry = "now" +
     * expiry days.
     */
    @Test
    public void stateFileDoesNotHaveAnyExpiryTime() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataLowValues();
        COSRetentionDaysData cos_B = createCosDataHighValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        // Setup mocks

        StateFileData stateFileData = new StateFileData(null, "new", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        Calendar expectedExpiryCal = Calendar.getInstance();
               
        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        int expiryDaysDiff = newExpiryDays;// - currentExpiryDays;
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, expiryDaysDiff);
    
        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);

    }

    /**
     * The state file has the "Time-of-Expiry" attribute set to no value.  Expect the same behavior as if there is no
     * current expiry, that is, we expect the expiry to be set to "now" + expiry days in new cos.
     * 
     * <p>
     * This unit test is to reproduce bug 10904.
     * </p>
     */
    @Test
    public void bug10904_timeOfExpirySetToEmptyValueInStateFileShouldBehaveLikeNoValue() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataLowValues();
        COSRetentionDaysData cos_B = createCosDataHighValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        // Setup mocks
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(createStateFileMock("read", "voice"));

        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        Calendar expectedExpiryCal = Calendar.getInstance();
        
        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        int expiryDaysDiff = newExpiryDays;// - currentExpiryDays;
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, expiryDaysDiff);
    
        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    /**
     * Verify the class can handle profiles that are missing some of the retention days attributes. We expect these to be skipped,
     * but the ones that do have attributes should still be processed.
     */
    @Test
    public void profileIsMissingSomeRetentionDaysAttributes() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = createCosDataLowValues();
        COSRetentionDaysData cos_B = createCosDataHighValues();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        // Let's remove all FAX attributes from both COS profiles
        cos_A.removeAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_FAX);
        cos_A.removeAttribute(DAConstants.ATTR_MSG_RETENTION_READ_FAX);
        cos_A.removeAttribute(DAConstants.ATTR_MSG_RETENTION_SAVED_FAX);
        cos_B.removeAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_FAX);
        cos_B.removeAttribute(DAConstants.ATTR_MSG_RETENTION_READ_FAX);
        cos_B.removeAttribute(DAConstants.ATTR_MSG_RETENTION_SAVED_FAX);

        // Setup mocks

        StateFileData[] stateFileDataArray = new StateFileData[] { new StateFileData(1, "new", "voice"),
                new StateFileData(2, "read", "video"), new StateFileData(3, "saved", "fax") };

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileDataArray);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        for (int i = 0; i < stateFileDataArray.length; i++) {
            for (Map.Entry<String, String> keyVal : cos_A.getAttributes()) {
                String attributeName = keyVal.getKey();
                if (attributeName.toLowerCase().endsWith(stateFileDataArray[i].messageClass)
                        && attributeName.toLowerCase().contains(stateFileDataArray[i].messageState)) {
                    String currentExpiryDaysString = keyVal.getValue();
                    int currentExpiryDays = Integer.parseInt(currentExpiryDaysString);
                    String newExpiryDaysString = cos_B.getAttributeValue(attributeName);
                    int newExpiryDays = Integer.parseInt(newExpiryDaysString);
                    int expiryDaysDiff = newExpiryDays - currentExpiryDays;
                    Calendar expectedExpiryCal = Calendar.getInstance();
                    expectedExpiryCal.setTime(stateFileDataArray[i].date);
                    expectedExpiryCal.add(Calendar.DAY_OF_MONTH, expiryDaysDiff);

                    // New expiry is in the future - check the expiry date is as expected
                    msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());
                }
            }

        }

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * The retention days goes from some number of days to being "ignored" in the new COS as per story/task 10249. We expect the
     * messages of the corresponding type are rescheduled for deletion from "now" + number of retention days for new messages.
     */
    @Test
    public void newCosIgnoresRetentionDays() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "1");

        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "-1");
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "5");

        // Setup mocks

        StateFileData stateFileData = new StateFileData(1, "read", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        Calendar expectedExpiryCal = Calendar.getInstance();
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, newExpiryDays);

        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * The retention days goes from "ignored" to some number of days in the new COS as per story/task 10249. We expect the messages
     * of the corresponding type are rescheduled for deletion from "now" + number of retention days. The number of retention days is
     * fetched from the cos attribute corresponding to the message type and message state.
     */
    @Test
    public void oldCosIgnoresRetentionDaysNewCosDoesNot() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "-1");

        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "8");

        // Setup mocks

        StateFileData stateFileData = new StateFileData(1, "read", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        Calendar expectedExpiryCal = Calendar.getInstance();
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, newExpiryDays);

        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);
    }

    /**
     * A value of "ignored" for retention days of new messages is not supported. We expect no event to be generated in that case.
     */
    @Test
    public void ignoredRetentionDaysOfNewMessagesIsNotSupported() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);

        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "-1");
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_FAX, "1");
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO, "-1");

        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "1");
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_FAX, "-1");
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO, "-1");

        // Setup mocks

        StateFileData stateFileData = new StateFileData(1, "new", "voice");

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);

        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);

        // Execute the test

        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);

        // Verify expectations

        verify(msgExpirySetterMock);

    }

    /**
     * The retention days goes from some number of days to "infinite".  We expect the expiry to be cancelled.
     */
    @Test
    public void oldCosHasSomeRetentionDaysNewCosHasInfinite() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "5");
    
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "0");
    
        // Setup mocks
    
        StateFileData stateFileData = new StateFileData(1, "read", "voice");
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        // Expiry should be cancelled
        msgExpirySetterMock.cancelMessageExpiry((StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    /**
     * The retention days goes from "ignored" to "infinite".  We expect the expiry to be cancelled.
     */
    @Test
    public void oldCosHasIgnoredDaysNewCosHasInfinite() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "-1");
    
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "0");
    
        // Setup mocks
    
        StateFileData stateFileData = new StateFileData(1, "read", "voice");
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        // Expiry should be cancelled
        msgExpirySetterMock.cancelMessageExpiry((StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    /**
     * The retention days goes from "infinite" to some number of days.  We expect the expiry to be set to "now" + expiry days
     * in new cos.
     */
    @Test
    public void oldCosHasInfiniteDaysNewCosHasSomeNumberOfDays() throws MsgStoreException {
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "0");
    
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "5");
    
        // Setup mocks
    
        StateFileData stateFileData = new StateFileData(0, "read", "voice");
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        Calendar expectedExpiryCal = Calendar.getInstance();
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, newExpiryDays);
    
        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    /**
     * Read/saved messages in old cos had infinite retention, and in the new COS they have retention "ignored".  Retention
     * days for new messages is some number of days.  Expect read/saved messages are re-scheduled relative to "now" +
     * retention days for new messages.
     */
    @Test
    public void readSavedMsgDidNotExpireNowTheyAreIgnoredAndNewMsgRetentionSetToSomeDays() throws MsgStoreException {
        //TODO
        
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "0");
    
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "-1");
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "5");
    
        // Setup mocks
    
        StateFileData stateFileData = new StateFileData(0, "read", "voice");
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        String newExpiryDaysString = cos_B.getAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
        int newExpiryDays = Integer.parseInt(newExpiryDaysString);
        Calendar expectedExpiryCal = Calendar.getInstance();
        expectedExpiryCal.add(Calendar.DAY_OF_MONTH, newExpiryDays);
    
        // New expiry is in the future - check the expiry date is as expected
        msgExpirySetterMock.setMessageExpiry(eqDate(expectedExpiryCal.getTime()), (StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    /**
     * Read/saved messages in old cos had infinite retention, and in the new COS they have retention "ignored".  Retention
     * days for new messages is set to "infinite".  Expect expiry is cancelled.
     */
    @Test
    public void readSavedMsgDidNotExpireNowTheyAreIgnoredAndNewMsgRetentionIsInfinite() throws MsgStoreException {
        //TODO
        
        // Setup data
        COSRetentionDaysData cos_A = new COSRetentionDaysData();
        COSRetentionDaysData cos_B = new COSRetentionDaysData();
        COSRetentionDaysChangedEvent event = new COSRetentionDaysChangedEvent("msid:1234", cos_A, cos_B);
    
        cos_A.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "0");
        
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "-1");
        cos_B.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "0");
    
        // Setup mocks
    
        StateFileData stateFileData = new StateFileData(0, "read", "voice");
    
        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileData);
        MessageExpirySetter msgExpirySetterMock = createMock(MessageExpirySetter.class);
    
        eventHandler.setStateFileFetcher(stateFileFetcherMock);
        eventHandler.setMessageExipirySetter(msgExpirySetterMock);
    
        // Expiry should be cancelled
        msgExpirySetterMock.cancelMessageExpiry((StateFile) anyObject());
    
        // Execute the test
    
        replay(stateFileFetcherMock, msgExpirySetterMock);
        eventHandler.handleCosRetentionDaysChangedEvent(event);
    
        // Verify expectations
    
        verify(msgExpirySetterMock);
    }

    private COSRetentionDaysData createCosDataLowValues() {
        COSRetentionDaysData data = new COSRetentionDaysData();
        
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "1");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_VOICE, "2");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "3");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VIDEO, "4");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_VIDEO, "5");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO, "6");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_FAX, "7");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_FAX, "8");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_FAX, "9");
        
        return data;
    }

    private COSRetentionDaysData createCosDataHighValues() {
        COSRetentionDaysData data = new COSRetentionDaysData();
        
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VOICE, "10");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_VOICE, "15");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "20");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_VIDEO, "25");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_VIDEO, "30");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO, "35");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_READ_FAX, "40");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_SAVED_FAX, "45");
        data.setAttributeValue(DAConstants.ATTR_MSG_RETENTION_NEW_FAX, "50");
        
        return data;
    }
    
    /**
     * The class under test does its calculations relative to the current date/time, so in order for the test to produce reliable
     * results, we have to create test dates relative to the current date/time.
     */
    private Date getRelativeDate(Integer daysFromNow) {
        if (daysFromNow == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);

        return cal.getTime();
    }

    private StateFile createStateFileMock(String messageState, Date expiry, String messageClass) {
        StateFile stateFileMock = createNiceMock(StateFile.class);
        String formattedDate = null;

        if (expiry != null) {
            formattedDate = DateFormatter.format(expiry);
        }

        expect(stateFileMock.getC1Attribute("Time-of-expiry")).andStubReturn(formattedDate);
        expect(stateFileMock.getC1Attribute("Message-class")).andStubReturn(messageClass);
        expect(stateFileMock.getMsgState()).andStubReturn(messageState);

        return stateFileMock;
    }

    private StateFile createStateFileMock(String messageState, String messageClass) {
        StateFile stateFileMock = createNiceMock(StateFile.class);

        expect(stateFileMock.getC1Attribute("Time-of-expiry")).andStubReturn("");
        expect(stateFileMock.getC1Attribute("Message-class")).andStubReturn(messageClass);
        expect(stateFileMock.getMsgState()).andStubReturn(messageState);
        
        replay(stateFileMock);

        return stateFileMock;
    }
    
    private StateFileFetcher createStateFileFetcherMock(StateFileData... stateFileDataList) throws MsgStoreException {
        StateFile[] stateFileArray = new StateFile[stateFileDataList.length];

        for (int i = 0; i < stateFileDataList.length; i++) {
            stateFileArray[i] = createStateFileMock(stateFileDataList[i].messageState, stateFileDataList[i].date,
                    stateFileDataList[i].messageClass);
            replay(stateFileArray[i]);
        }

        StateFileFetcher stateFileFetcherMock = createStateFileFetcherMock(stateFileArray);

        return stateFileFetcherMock;
    }

    private StateFileFetcher createStateFileFetcherMock(StateFile... stateFiles) throws MsgStoreException {
        StateFileFetcher stateFileFetcherMock = createNiceMock(StateFileFetcher.class);

        expect(stateFileFetcherMock.getAllSubscriberMessages((String) anyObject())).andStubReturn(stateFiles);

        return stateFileFetcherMock;
    }

    /**
     * <p>
     * Checks if a date is today.
     * </p>
     * 
     * @param date
     *        the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException
     *         if the date is <code>null</code>
     */
    private static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    /**
     * <p>
     * Checks if two dates are on the same day ignoring time.
     * </p>
     * 
     * @param date1
     *        the first date, not altered, not null
     * @param date2
     *        the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException
     *         if either date is <code>null</code>
     */
    private static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>
     * Checks if two calendars represent the same day ignoring time.
     * </p>
     * 
     * @param cal1
     *        the first calendar, not altered, not null
     * @param cal2
     *        the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException
     *         if either calendar is <code>null</code>
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
                .get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    private class StateFileData {

        public String messageState;
        public Date date;
        public String messageClass;

        public StateFileData(Integer expiryDaysFromNow, String messageState, String messageClass) {
            this.messageState = messageState;
            this.date = getRelativeDate(expiryDaysFromNow);
            this.messageClass = messageClass;
        }
    }
    
    /**
     * In these tests, dates are considered equal if they are on the same day. 
     *
     * @author lmcyvca
     */
    private class DateEquals implements IArgumentMatcher {
        private Calendar expectedCal = null;

        public DateEquals(Date expected) {
            expectedCal = createCalendar(expected);
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof Date)) {
                return false;
            }
            
            Calendar actualCal = createCalendar((Date)actual);
            
            return actualCal.compareTo(expectedCal) == 0;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqDate(");
            buffer.append(expectedCal.getTime());
            buffer.append(")");

        }
        
        private Calendar createCalendar(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR, 0);
            
            return cal;
        }
    }    
    
    private Date eqDate(Date expected) {
        reportMatcher(new DateEquals(expected));
        return null;
    }    
}
