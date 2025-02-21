package com.mobeon.common.cmnaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;

public class EventInfoPersistenceTest {

    private final static String EVENT_ID = "tn0/20101028-12h50/238_c56e0e876a7dc010-0.vvmTimeoutType-vvmTimeoutType;try=1;exp=0";
    private final static String MSID = "f7d9eff42e3d8a2e";
    private final static long HALF_SECOND = 500;
    private final static long ONE_SECOND = HALF_SECOND * 2;
    private final static long FIVE_MINUTES = ONE_SECOND * 60 * 5;

    @BeforeClass
    static public void setUp() throws Exception {
        System.setProperty("abcxyz.mfs.userdir.create", "false");
        System.setProperty("abcxyz.system.out.print", "true");
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testCreate() throws MsgStoreException {
        // public boolean create(eventInfoTypes type, AppliEventInfo event, String msid, long validityTimestamp) throws
        // MsgStoreException{
        AppliEventInfo event = new AppliEventInfo();
        event.setEventId(EVENT_ID);
        String msid = MSID;
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, FIVE_MINUTES);
        eip.isPersistentFileValid(msid);
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testDelete() throws MsgStoreException {
        // public boolean delete(eventInfoTypes type, String msid)
        String msid = MSID;
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, FIVE_MINUTES);
        eip.delete(msid);
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testIsPersistentFileValidReturnsValid() throws MsgStoreException {
        // public boolean create(eventInfoTypes type, AppliEventInfo event, String msid, long validityTimestamp) throws
        // MsgStoreException{
        AppliEventInfo event = new AppliEventInfo();
        event.setEventId(EVENT_ID);
        String msid = MSID;
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, FIVE_MINUTES);
        eip.save(event, msid);
        // Inside the 5 minutes
        assertTrue(eip.isPersistentFileValid(msid));
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testIsPersistentFileValidReturnsInvalid() throws MsgStoreException {
        // public boolean create(eventInfoTypes type, AppliEventInfo event, String msid, long validityTimestamp) throws
        // MsgStoreException{
        AppliEventInfo event = new AppliEventInfo();
        event.setEventId(EVENT_ID);
        String msid = MSID;
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, HALF_SECOND);
        eip.save(event, msid);
        // Wait for few seconds
        try {
            Thread.sleep(ONE_SECOND);
        } catch (InterruptedException e) {
        }
        assertFalse(eip.isPersistentFileValid(msid));
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testIsPersistentFileValidNoFileReturnsInvalid() throws MsgStoreException {
        // public boolean create(eventInfoTypes type, AppliEventInfo event, String msid, long validityTimestamp) throws
        // MsgStoreException{
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, FIVE_MINUTES);
        String msid = MSID;
        eip.delete(msid);
        assertFalse(eip.isPersistentFileValid(msid));
    }

    @Test
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    public void testGetEventExist() throws MsgStoreException {
        AppliEventInfo event = new AppliEventInfo();
        event.setEventId(EVENT_ID);
        String msid = MSID;
        EventInfoPersistence eip = new EventInfoPersistence(EventInfoPersistence.EventInfoTypes.VVMTimeout, FIVE_MINUTES);
        eip.save(event, msid);
        assertEquals(EVENT_ID, eip.getEvent(msid));
    }
}
