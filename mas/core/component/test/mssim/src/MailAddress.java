import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * Date: 2008-feb-27
 *
 * @author emahagl
 */
public class MailAddress {
    private String host;
    private String user;
    private String email;
    private String name;

    public MailAddress(String str) throws AddressException {
        InternetAddress address = new InternetAddress(str);
        email = address.getAddress();
        name = address.getPersonal();

        String[] strs = email.split("@");
        user = strs[0];
        if (strs.length > 1) {
            host = strs[1];
        } else {
            host = "localhost";
        }
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String toString() {
        return email;
    }

    public String getEmail() {
        return email;
    }

}
