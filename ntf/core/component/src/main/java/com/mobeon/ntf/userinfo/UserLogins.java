package com.mobeon.ntf.userinfo;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.NtfCompletedListener;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.time.NtfTime;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-jun-01
 * Time: 13:44:26
 */
public class UserLogins  {
    private static Map users;
    private static LogAgent log =  NtfCmnLogger.getLogAgent(LoginThread.class);
    private static LoginThread loginThread;
    private static NtfCompletedListener ntfCompletedListener;

    private static final int maxLoginTime = 60 * 60;  //seconds

    /**
     * Constructor.
     *
     * @param name - the name of the thread.
     */

    static {
        users = new HashMap();
        loginThread = new LoginThread("UserLoginThread");
        loginThread.start();
    }

    public static void setNotifCompletedListener(NtfCompletedListener listener) {
        ntfCompletedListener = listener;
    }

    private static class LoginThread extends NtfThread {
        /**
         * Constructor.
         *
         * @param name - the name of the thread.
         */
        public LoginThread(String name) {
            super(name);
        }

        /**
         * This method does the normal work for an NtfThread. It is called
         * repeatedly as long as the administrative state is unlocked.
         * When implementing this function, try to use the loop in the run-method of
         * NtfThread instead of making a loop in ntfRun, so the administrative state
         * is checked frequently.
         *@return false until the thread wants to stop, then true.
         */
        public boolean ntfRun() {
            ArrayList oldSessions = new ArrayList();

            try {
                int timeNow = NtfTime.now;
                Iterator userEntries;
                synchronized (users) {
                    userEntries = users.entrySet().iterator();

                    while( userEntries.hasNext() ) {
                        Map.Entry next = (Map.Entry) userEntries.next();
                        LoginEntry entry = (LoginEntry) next.getValue();
                        if( entry.isTooOld(timeNow)) {
                            //The user is not logged out immediately, you can
                            //not remove the user other than through the
                            //iterator or you will get a
                            //ConcurrentModificationException. By collecting old
                            //telephone numbers in a list and deleting them
                            //outside the iteration, the problem is avoided.
                            oldSessions.add(entry.getTelephoneNumber());
                        }
                    }
                }
            } catch(Exception e) {
                log.error("Unexpected exception when finding old sessions ",e);
            } finally {
                logoutUsers(oldSessions);
                try { sleep(2000); } catch (InterruptedException ignore) { return false; }
            }
            return false;
        }

        @Override
        public boolean shutdown() {
            return true;
        }
    }

    /**
     * Logout some telephonenumbers.
     *@param l a list with the telephone numbers to log out.
     */
    private static void logoutUsers(ArrayList l) {
        //It is not important that this method succeeds with its work. If
        //something fails, the remaining old sessions will be found again and
        //retried the next time ntfRun runs.
        if (l == null || l.isEmpty()) { return; }

        String telephoneNumber;
        try {
            for (Iterator it = l.iterator(); it.hasNext();) {
                telephoneNumber = (String) it.next();
                log.info("Forced end of holdback for " + telephoneNumber + ", simulating logout");
                logoutUser(telephoneNumber);
            }
        } catch(Exception e) {
                log.error("Unexpected exception when ending old sessions ",e);
        }
    }

    /**
     * Logs in a user and store the user.
     * @param telephoneNumber The users telephonenumber
     */
    public static void loginUser(String telephoneNumber) {
        synchronized (users) {
            LoginEntry entry = (LoginEntry) users.get(telephoneNumber);

            if (entry == null) {
                users.put(telephoneNumber, new LoginEntry(telephoneNumber) );
                log.debug("User " + telephoneNumber + " is logged in.");
            } else {
                log.debug("User " + telephoneNumber + " is already logged in.");
            }
        }
    }

    /**
     * logs out a user and removes the users from the list of logged in users.
     * Any messages that the user has is sent to a random gnotification box and will be processed
     * in a regular way when they are read by a mailboxpoller.
     *
     * @param telephoneNumber the telephonenumber of the user
     */
    public static void logoutUser(String telephoneNumber) {
        LoginEntry entry = null;
        synchronized (users) {
            entry = (LoginEntry) users.remove(telephoneNumber);
        }
        if (entry != null) {
            ArrayList<NtfEvent> uids = entry.getUids();
            if (uids != null) {
                log.debug("User " + telephoneNumber + " has stored mails. Marking them as unseen");
                for (int i = 0; i < uids.size(); i++) {
                    NtfEvent ntfEvent = uids.get(i);
                    log.debug("Marking mail " + ntfEvent.getMsgInfo().getMsgId() + " as unseen");
                    //Not invoked anymore (UserLogins is not used anymore)
                    //ntfCompletedListener.notifRenew(ntfEvent);
                }
            }
        }
    }

    /**
     * Checks if a user is logged in.
     * @param telephoneNumber the users telephonenumber
     * @return true if the user is logged in, false otherwise
     */
    public static boolean isUserLoggedIn(String telephoneNumber) {
        synchronized (users) {
            return users.containsKey(telephoneNumber);
        }
    }

    public static void storeMail(String telephoneNumber, NotificationEmail email) {
        log.debug("Storing mailinfo for mail " + email.getMessageId());
        LoginEntry entry;
        synchronized (users) {
            entry = (LoginEntry) users.get(telephoneNumber);
        }
        if( entry != null ) {
            entry.addMessage(email);
        }
    }

    /**
     * return how many mails a user has.
     * @param telephoneNumber the users telephonenumber
     * @return the number of mails stored.
     */
    public static int getStoredEmailCount(String telephoneNumber) {
        LoginEntry entry = (LoginEntry) users.remove(telephoneNumber);
        if (entry != null) {
            ArrayList messages = entry.getUids();

            if (messages != null) {
                return messages.size();
            }
        }
        return 0;
    }

    private static class LoginEntry {
        private int expiryTime;
        private String telephoneNumber;
        private ArrayList<NtfEvent> uids;

        public LoginEntry(String telephoneNumber) {
            this.expiryTime = NtfTime.now + maxLoginTime;
            this.telephoneNumber = telephoneNumber;
        }

        public void addMessage(NotificationEmail email) {
            if(uids == null) {
                uids = new ArrayList<NtfEvent>();
            }
            uids.add(email.getNtfEvent());
        }

        public ArrayList<NtfEvent> getUids() {
            return uids;
        }

        public boolean isTooOld(int timeNow) {
            return expiryTime < timeNow;
        }

        public String getTelephoneNumber() {
            return telephoneNumber;
        }
    }
}
