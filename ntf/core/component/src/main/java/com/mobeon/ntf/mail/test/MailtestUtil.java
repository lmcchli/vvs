/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import org.eclipse.angus.mail.imap.IMAPFolder;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


/**
 * This class contains functions that helps test programs working with
 * mailboxes.
 */
public class MailtestUtil {
    private static Logger log = Logger.getLogger();
    private static Session session = null;;

    MailtestUtil() {
    }

    public static void emptyMailbox(String user, String pw) {
	try {
            IMAPFolder inbox = getInbox(Config.getImapHost(), user, pw);
            int count;
	    while ((count = inbox.getMessageCount()) > 0) {
                log.logMessage("Emptying mailbox " + user + " (" + count + " messages)");
                inbox.setFlags(1, count, new Flags("\\deleted"), true);
                    //                for (int i = 1; i < count + 1; i++) {
                    //                MimeMessage m = (MimeMessage)(inbox.getMessage(i));
                    //                if (m != null && !m.isExpunged()) {
                    //                    m.setFlag(Flags.Flag.DELETED, true);
                    //                }
                    //           }
                    inbox.expunge();
            }
            inbox.close(false);
	}
	catch (Exception e) {
	    log.logMessage("Exception when emptying mailbox " + e, log.L_ERROR);
	}
    }

    public static void createFolder(String user, String pw, String folder) throws MessagingException {
        Store store = getStore(Config.getImapHost(), user, pw);
        IMAPFolder fold = (IMAPFolder) store.getFolder(folder);
        if( !fold.exists() ) {
            log.logMessage("Creating folder " + folder);
            fold.create(Folder.HOLDS_MESSAGES);
        } else {
            fold.open(Folder.READ_WRITE);
            int count = fold.getMessageCount();
            log.logMessage("Emptying " + folder + " mailbox " + user + " (" + count + " messages)");
            for (int i = 1; i < count + 1; i++) {
                MimeMessage m = (MimeMessage)(fold.getMessage(i));
                if (m != null && !m.isExpunged()) {
                    m.setFlag(Flags.Flag.DELETED, true);
                }
            }
            fold.expunge();
            fold.close(false);
        }
    }

    public static void dropFolder(String user, String pw, String folder) {
        try {
            Store store = getStore(Config.getImapHost(), user, pw);

        IMAPFolder fold = (IMAPFolder) store.getFolder(folder);
        if( fold.exists() ) {
            fold.delete(false);
        }
        } catch (Exception e) {
            log.logMessage("Exception dropping folder, " + e.toString());
        }
    }

    public static void moveMessages(String fromFolder, String toFolder, int start, int count, String user, String pw) {
        try {
            IMAPFolder from = getFolder(Config.getImapHost(), user, pw, fromFolder);
            if( !from.exists() ) {
                log.logMessage(fromFolder + " does not exists", log.L_ERROR);
            }
            MimeMessage[] messages = new MimeMessage[count];
            for( int i=0;i<count;i++ ) {
                MimeMessage m = (MimeMessage)(from.getMessage(start+i));
                messages[i] = m;
                m.setFlag(Flags.Flag.SEEN, false);

            }
            IMAPFolder to = getFolder(Config.getImapHost(), user, pw, toFolder);
            if( !to.exists() ) {
                to.create(Folder.HOLDS_MESSAGES);
            }
            from.copyMessages(messages, to);
            int toCount = to.getMessageCount();
            if( toCount > count )
                toCount = count;
            for( int i=1;i<=toCount;i++ ) {
                MimeMessage m = (MimeMessage) to.getMessage(i);
                m.setFlag(Flags.Flag.SEEN, false);
            }


            for( int i=0;i<count;i++ ) {
                MimeMessage m = (MimeMessage)(from.getMessage(start+i));

                m.setFlag(Flags.Flag.DELETED, true);
            }
            from.expunge();
            from.close(false);
            to.close(false);
        } catch(Exception e) {
            e.printStackTrace();
            log.logMessage("Failed to move messages, " + e.toString(), log.L_ERROR );
        }
    }

    public static void flagMessages(String user, String pw, int first, int n, Flags f) {
	try {
            IMAPFolder inbox = getInbox(Config.getImapHost(), user, pw);
            log.logMessage("Flagging mailbox " + user + " as " + f + " (" + first + "-" + (first + n - 1) +")");
            inbox.setFlags(first, first + n - 1, f, true);

            //            for (int i = first; i < first + n; i++) {
            //                MimeMessage m = (MimeMessage)(inbox.getMessage(i));
            //            //                if (m != null && !m.isExpunged()) {
            //                    m.setFlags(f, true);
            //                }
            //}
            inbox.close(false);
	}
	catch (Exception e) {
	    log.logMessage("Exception when flagging messages" + e, log.L_ERROR);
	}
    }

    public static int countMessages(String user, String pw, Flags.Flag f) {
        int n = -1;
	try {
            IMAPFolder inbox = getInbox(Config.getImapHost(), user, pw);
            if (f == null) {
                n = inbox.getMessageCount();
            } else {
                n= 0;
                for (int i = 0; i < inbox.getMessageCount(); i++) {
                    if (inbox.getMessage(i).isSet(f)) {
                        n++;
                    }
                }
            }
            inbox.close(false);
	}
	catch (Exception e) {
	    log.logMessage("Exception when emptying mailbox " + e, log.L_ERROR);
	}
        return n;
    }

    public static int countMessages(String user, String pw) {
        return countMessages(user, pw, null);
    }

    public static void sendMail(String user, String file) {
        try {
            //            log.logMessage("Sending " + file + " to " + user);
            Properties props = System.getProperties();
            props.setProperty("mail.smtp.host", Config.getImapHost());
            props.setProperty("mail.imap.host", Config.getImapHost());
            session = Session.getInstance(props);
            MimeMessage msg = new MimeMessage(session, new FileInputStream(file));
            msg.setHeader("To", user + "@lab.mobeon.com");
            Transport.send(msg);
        } catch (Exception e) {
            log.logMessage("Failed to send mail: " + e, log.L_ERROR);
        }
    }

    public static void sendMailFromString(String user, String mail) {
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.smtp.host", Config.getImapHost());
            props.setProperty("mail.imap.host", Config.getImapHost());
            session = Session.getInstance(props);
            MimeMessage msg = new MimeMessage(session, new ByteArrayInputStream(mail.getBytes()));
            msg.setHeader("To", user + "@lab.mobeon.com");
            Transport.send(msg);
        } catch (Exception e) {
            log.logMessage("Failed to send mail: " + e, log.L_ERROR);
        }
    }


    static Store getStore(String host, String user, String pass) throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", Config.getImapHost());
        props.setProperty("mail.imap.host", Config.getImapHost());
        session = Session.getInstance(props);
        Store store;
        store = session.getStore("imap");
        store.connect(host, 143, user, pass);

        return store;
    }

    static IMAPFolder getInbox(String host, String user, String pass) throws MessagingException{
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", Config.getImapHost());
        props.setProperty("mail.imap.host", Config.getImapHost());
        session = Session.getInstance(props);
        Store store;
        store = session.getStore("imap");
        store.connect(host, 143, user, pass);

        IMAPFolder folder;
        folder = (IMAPFolder)(store.getDefaultFolder());
        folder = (IMAPFolder)(folder.getFolder("INBOX"));
        folder.open(Folder.READ_WRITE);
        return folder;
    }

    static IMAPFolder getFolder(String host, String user, String pass, String folderName) throws MessagingException {
        Store store = getStore(host, user, pass);

        IMAPFolder folder;
        folder = (IMAPFolder)(store.getFolder(folderName));
        folder.open(Folder.READ_WRITE);
        return folder;
    }

    static String[] par = null;

    public static void doempty() {
        emptyMailbox(par[1], par[2]);
    }

    public static void docount() {
        System.out.println(par[1] + ": " + countMessages(par[1], par[2], null));
    }

    public static void doseen() {
        System.out.println(par[1] + ": " + countMessages(par[1], par[2], Flags.Flag.SEEN));
    }

    public static void dodeleted() {
        System.out.println(par[1] + ": " + countMessages(par[1], par[2], Flags.Flag.DELETED));
    }

    public static void dosend() {
        sendMail(par[1], par[2]);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Known commands");
            System.err.println("\tcount <user> <password>");
            System.err.println("\tempty <user> <password>");
            System.err.println("\tseen <user> <password>");
            System.err.println("\tdeleted <user> <password>");

            System.err.println("\n<user> <password> may be replaced by the word \"gnot\" which makes the command repeat for all gnotification mailboxes");
        } else {
            par = args;
            try {
                Class c = Class.forName("com.mobeon.ntf.test.MailtestUtil");
                Method m = c.getMethod("do" + args[0] );
                if (args[1].equals("gnot")) {
                    par = new String[args.length + 1];
                    par[0] = args[0];
                    par[2] = Config.getImapPassword();
                    for (int i = 2; i < args.length; i++) {
                        par[i + 1] = args[i];
                    }
                    for (int i = 0; i < Config.getImapThreads(); i++) {
                        par[1] = Config.getImapUserName() + "_" + i;
                        m.invoke(null);
                    }
                } else {
                    m.invoke(null);
                }
            } catch (NoSuchMethodException e) {
                System.err.println(args[0] + ": unknown command");
            } catch (Exception e) {
                System.err.println("Exception: " + e + com.mobeon.ntf.util.NtfUtil.stackTrace(e));
            }
        }
    }
}
