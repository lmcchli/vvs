import java.util.ArrayList;

/**
 * Date: 2008-mar-04
 *
 * @author emahagl
 */
public class UserFactory {
    static User getUser(String uid, String pwd) {
        ArrayList<Integer> msgIds = new ArrayList<Integer>();

        int no = SimpleStore.getNoOfMessages();
        int count = MsSimUtil.randomize(no + 1);
        if (count == 0) count = 1;
        for (int i = 0; i < count; i++) {
            msgIds.add(i + 1);
        }
        return new User(uid, msgIds, uid.startsWith("GrtAdm_"));
    }
}
