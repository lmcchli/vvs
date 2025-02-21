import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Date: 2008-feb-27
 *
 * @author emahagl
 */
public class TestMsSim {

    final static String MAIL_STORE_SOCKET_CONNECTION_TIMEOUT = "15000"; // 15 sec
    final static String MAIL_STORE_SOCKET_IO_TIMEOUT = "15000"; // 15 sec
    static String _host = "localhost";
    //static String _host = "alouette.lab.mobeon.com";
    //static String _host = "brage.mobeon.com";
    //static String _host = "10.8.5.20";
    static String _port = "1143";
    static String _user = "116202035";
    static String _pwd = "abcd";
    static String foldername = "INBOX";
    static boolean debug = true;

    public TestMsSim() {
    }

    public void runMailSession() {
        FetchProfile fetchProfile = new FetchProfile();
        //fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
        fetchProfile.add(FetchProfile.Item.FLAGS);
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        fetchProfile.add("X-Priority");

        try {
            Properties prop = new Properties();
            prop.put("mail.imap.host", _host);
            prop.put("mail.imap.port", _port);
            prop.put("mail.imap.connectiontimeout", MAIL_STORE_SOCKET_CONNECTION_TIMEOUT);
            prop.put("mail.imap.timeout", MAIL_STORE_SOCKET_IO_TIMEOUT);
            prop.put("mail.imap.auth.login.disable", "true");
            prop.put("mail.imap.auth.plain.disable", "true");
            prop.put("mail.imap.partialfetch", "false");
            prop.put("mail.imap.fetchsize", "3000");

            Session session = Session.getInstance(prop);
            session.setDebug(debug);
            Store store = session.getStore("imap");

            if (debug) System.out.println("****** CONNECTING *******\n\n");
            store.connect(_host, _user, _pwd);

            Folder folder = store.getFolder(foldername);
            if (debug) System.out.println("****** OPENING FOLDER *******\n\n");
            folder.open(Folder.READ_WRITE);

            //Message messages[] = folder.getMessages(1, 1);//startswith message no 1
            Message messages[] = folder.getMessages();
            folder.fetch(messages, fetchProfile);

            if (messages == null) return;
            for (int i = 0; i < messages.length; i++) {
                //System.out.println("\n\n************  DUMPING ****************\n");
                //messages[i].writeTo(System.out);
                //System.out.println("\n\n************  END DUMPING ****************\n");
                System.out.println("\n\n************  Message " + i + "  ****************\n");
                System.out.println("SUBJECT: " + messages[i].getSubject());

                MimeMultipart mimeMultipart = (MimeMultipart) messages[i].getContent();
                BodyPart bodyPart = mimeMultipart.getBodyPart(0);
                Enumeration headers = bodyPart.getAllHeaders();
                bodyPart.writeTo(System.out);
            }
            if (debug) System.out.println("\n\n****** CLOSING FOLDER *******\n\n");
            folder.close(false);
            if (debug) System.out.println("****** CLOSING STORE *******\n\n");
            store.close();
        }
        catch (NoSuchProviderException e) {
            System.out.println("Error: " + e);
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        TestMsSim testMsSim = new TestMsSim();

        if (argv.length > 0) {
            TestMsSim._user = argv[0];
        }
        if (argv.length > 1) {
            _port = argv[1];
        }
        if (argv.length > 2) {
            _pwd = argv[2];
        }
        if (argv.length > 3) {
            if (argv[3].equals("-v")) debug = true;
        }

        int tries = 1;

        while (tries-- > 0)
        //while (true)
            testMsSim.runMailSession();
    }

    private static void writeToFile(BodyPart part) {
        try {
            FileOutputStream out = new FileOutputStream("TEMP.wav");
            Object o = part.getContent();
            if (o instanceof InputStream) {
                InputStream in = (InputStream) o;
                byte[] buf = new byte[1024];
                while (in.read(buf) > -1) {
                    out.write(buf);
                }
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Exception in writeToFile " + e);
        } catch (MessagingException e) {
            System.out.println("Exception in writeToFile " + e);
        } catch (IOException e) {
            System.out.println("Exception in writeToFile " + e);
        }
    }

    private static void dumpClass(Object obj) {
        System.out.println("The class of " + obj + " is " + obj.getClass().getName());
    }

}
