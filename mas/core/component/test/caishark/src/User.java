/**
 * Date: 2008-mar-26
 *
 * @author emahagl
 */
public class User extends Entry {
    private String telephoneNumber;

    public User(String telephoneNumber) {
        super();
        this.telephoneNumber = telephoneNumber;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return user.equals(telephoneNumber);
        } else {
            return super.equals(obj);
        }
    }
}
