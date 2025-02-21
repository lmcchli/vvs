import jakarta.mail.Part;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Utility class.
 *
 * @author emahagl
 */
public class MsSimUtil {

    public static int randomize(int max) {
        int r = new Random().nextInt();
        if (r < 0) r = 0 - r;
        return r % max;
    }

    public static byte[] getHeaderAsBytes(Part part) {
        return getHeaders(part).getBytes();
    }

    /**
     * @return The headers of an email (or a Part)
     */
    public static String getHeaders(Part msg) {
        String all = getPartAsString(msg);
        int i = all.indexOf("\r\n\r\n");
        return all.substring(0, i + 4);
    }

    /**
     * @return The both header and body for an email (or a Part)
     */
    public static String getPartAsString(Part msg) {
        try {
            ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
            msg.writeTo(bodyOut);
            return bodyOut.toString(); //"US-ASCII" //.trim()
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getBodyAsBytes(Part msg) {
        return getBody(msg).getBytes();
    }

    /**
     * @return The content of an email (or a Part)
     */
    public static String getBody(Part msg) {
        String all = getPartAsString(msg);
        int i = all.indexOf("\r\n\r\n");
        return all.substring(i + 4, all.length());
    }
}
