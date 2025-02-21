import jakarta.mail.Flags;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Reads each .msg file and makes a SimpleStoredMessage which are put into a list.
 */
public class SimpleStore {

    private static String messageDirectoryPath = "messages/";

    static {
        if (isWindows()) {
            messageDirectoryPath = "J:\\java\\mssim\\messages\\";
        }
    }

    private static Session session;
    private static List<SimpleStoredMessage> messages = new ArrayList<SimpleStoredMessage>();
    private static long uidCounter = 0;

    public static void load() {
        Properties props = new Properties();
        session = Session.getInstance(props);

        loadMessages();
    }

    /**
     * Returns message specified with id
     *
     * @param id
     * @return the message
     */
    public static SimpleStoredMessage getMessage(int id) {
        if (id < 0 || id >= messages.size()) return null;
        return messages.get(id);
    }

    public static int getNoOfMessages() {
        return messages.size();
    }

    private static void loadMessages() {
        File dir = new File(messageDirectoryPath);
        if (dir.isDirectory()) {
            File[] filez = dir.listFiles(new MessageFileFilter());

            for (int i = 0; i < filez.length; i++) {
                loadMessageFromFile(filez[i]);
            }
        } else {
            System.out.println(messageDirectoryPath + " is not a directory");
        }
    }

    private static void loadMessageFromFile(File file) {
        long length = file.length();
        MimeMessage mimeMessage = null;
        try {
            mimeMessage = new MimeMessage(session, new FileInputStream(file));
        } catch (MessagingException e) {
            System.out.println("Exception in loadMessageFromFile" + e);
        } catch (FileNotFoundException e) {
            System.out.println("Exception in loadMessageFromFile" + e);
        }

        if (mimeMessage != null) {
            makeSimpleStoredMessage(mimeMessage, length);
        }
    }

    private static void makeSimpleStoredMessage(MimeMessage mimeMessage, long size) {
        Flags flags = new Flags(Flags.Flag.SEEN);
        SimpleStoredMessage simpleStoredMessage = new SimpleStoredMessage(mimeMessage, flags, size, uidCounter++);
        messages.add(simpleStoredMessage);
    }

    private static boolean isWindows() {
        return ("\\".equals(File.separator));
    }

    static class MessageFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".msg");
        }
    }
}
