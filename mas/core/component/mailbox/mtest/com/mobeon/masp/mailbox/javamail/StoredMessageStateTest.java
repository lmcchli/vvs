/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import static com.mobeon.masp.mailbox.StoredMessageState.*;
import com.mobeon.masp.mediaobject.IMediaObject;


/**
 * @author qhast
 */
public class StoredMessageStateTest extends ConnectedMailboxTest {


    public StoredMessageStateTest(String name) {
        super(name);
    }


    private void scanContent(IStoredMessage message) throws MailboxException {
        for(IMessageContent content : message.getContent()) {
            IMediaObject mo = content.getMediaObject();
        }
    }



    public void testStateHandling1() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("stateTest");
        IStoredMessageList mlist = folder.getMessages();

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
            scanContent(m);
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
            m.setState(StoredMessageState.READ);
            m.saveChanges();
        }

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
            scanContent(m);
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
        }

        IStoredMessageList mlist2 = folder.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
            scanContent(m);
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
        }

        IFolder folder2 = mbox.getFolder("stateTest");
        IStoredMessageList mlist3 = folder2.getMessages();
        for (IStoredMessage m : mlist3) {
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
            scanContent(m);
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
        }

        mbox.close();


    }

    public void testStateHandling2() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("stateTest");
        IStoredMessageList mlist = folder.getMessages();

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
            scanContent(m);
            assertEquals("State should be \"READ\"", StoredMessageState.READ, m.getState());
            m.setState(StoredMessageState.SAVED);
            m.saveChanges();
        }

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
            scanContent(m);
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
        }

        IStoredMessageList mlist2 = folder.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
            scanContent(m);
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
        }

        mbox.close();


    }


    public void testStateHandling3() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("stateTest");
        IStoredMessageList mlist = folder.getMessages();

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
            scanContent(m);
            assertEquals("State should be \"SAVED\"", StoredMessageState.SAVED, m.getState());
            m.setState(StoredMessageState.NEW);
            m.saveChanges();
        }

        for (IStoredMessage m : mlist) {
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
            scanContent(m);
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
        }

        IStoredMessageList mlist2 = folder.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
            scanContent(m);
            assertEquals("State should be \"NEW\"", StoredMessageState.NEW, m.getState());
        }

        mbox.close();

    }



    public void testStateHandling() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();


        StoredMessageState state = SAVED;

        //mbox 2
        //IMailbox mbox2 = accountManager.getMailbox((IServiceInstance)serviceInstance.proxy(),mailboxProfile);
        //IFolder folder2 = mbox2.getFolder("stateTest");

        //mbox 1
        IFolder folder = mbox.getFolder("stateTest");
        IStoredMessageList mlist = folder.getMessages();
        for (IStoredMessage m : mlist) {
            assertEquals("State should be "+NEW, NEW, m.getState());
            scanContent(m);
            assertEquals("State should be "+NEW, NEW, m.getState());
            m.setState(state);
            m.saveChanges();
        }


        //mbox 2
        IMailbox mbox2 = accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);
        IFolder folder2 = mbox2.getFolder("stateTest");
        IStoredMessageList mlist2 = folder2.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be "+state, state, m.getState());
            scanContent(m);
            assertEquals("State should be "+state, state, m.getState());
        }

        //Mbox 1
        folder = mbox.getFolder("stateTest");
        mlist = folder.getMessages();
        for (IStoredMessage m : mlist) {
            assertEquals("State should be "+state, state, m.getState());
            scanContent(m);
            assertEquals("State should be "+state, state, m.getState());
            m.setState(READ);
            m.saveChanges();
        }

        //mbox2
        mlist2 = folder2.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be "+READ, READ, m.getState());
            scanContent(m);
            assertEquals("State should be "+READ, READ, m.getState());
        }
        mbox2.close();

        mbox.close();
    }


    public void testStateHandling5() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();



        //mbox 2
        IMailbox mbox2 = accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);
        IFolder folder2 = mbox2.getFolder("stateTest");
        IStoredMessageList mlist2 = folder2.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be "+NEW, NEW, m.getState());
            scanContent(m);
            assertEquals("State should be "+NEW, NEW, m.getState());
        }
        mbox2.close();

        //mbox 2
        mbox2 = accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);
        folder2 = mbox2.getFolder("stateTest");
        mlist2 = folder2.getMessages();
        for (IStoredMessage m : mlist2) {
            assertEquals("State should be "+NEW, NEW, m.getState());
            scanContent(m);
            assertEquals("State should be "+NEW, NEW, m.getState());
        }
        mbox2.close();

        mbox.close();
    }


}
