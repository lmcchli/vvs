/**
 * Date: 2008-mar-26
 *
 * @author emahagl
 */
public class Administrator extends Entry {

    private String userName;
    private String password;

    public Administrator(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
