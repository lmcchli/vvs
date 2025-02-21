import java.util.List;

/**
 * Date: 2008-mar-04
 *
 * @author emahagl
 */
public class User {
    private String uid;
    private boolean isGreetingAdmin = false;
    private List<Integer> msgIds;
    private int noOfMessages;

    public User(String uid, List<Integer> msgIds, boolean isGreetingAdmin) {
        this.uid = uid;
        this.msgIds = msgIds;
        this.isGreetingAdmin = isGreetingAdmin;
        noOfMessages = msgIds.size();
    }

    public String getUid() {
        return uid;
    }

    public SimpleStoredMessage getMessage(int id) {
        // Get the first if an invalid id is specified.
        if (id == 0 || id > msgIds.size()) id = 1;

        id = id - 1;
        int i = msgIds.get(id);
        i = i - 1;
        return SimpleStore.getMessage(i);
    }

    public List<Integer> getMsgIds() {
        return msgIds;
    }

    public int getNoOfMessages() {
        return noOfMessages;
    }

    public int getNoOfRecentMessages() {
        return 0;
    }

    public void setGreetingAdmin(boolean greetingAdmin) {
        isGreetingAdmin = greetingAdmin;
    }

    public boolean isGreetingAdmin() {
        return isGreetingAdmin;
    }
}
