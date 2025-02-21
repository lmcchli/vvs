import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains a Map of <code>User</code> and another Map of
 * <code>Adminstrator</code>
 * 
 * @author emahagl
 */
public class UserRegister {
	private static UserRegister instance = new UserRegister();

	/**
	 * keyed by telephoneNumber
	 */
	private Map<String, User> userMap = new HashMap<String, User>();

	/**
	 * keyed by username
	 */
	private Map<String, Administrator> adminMap = new HashMap<String, Administrator>();

	private UserRegister() {
		load();
	}

	public static UserRegister getInstance() {
		return instance;
	}

	/**
	 * Retrieves a User from the register.
	 * 
	 * @param telephoneNumber
	 * @return
	 * @throws CAIException
	 */
	public User getUser(String telephoneNumber) throws CAIException {
		User user = userMap.get(telephoneNumber);
		if (user == null) {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Subscriber does not exist", 5008);
			else
				throw new CAIException("The subscriber does not exist", 6001);
		}
		return user;
	}

	/**
	 * Adds a User to the register.
	 * 
	 * @param telephoneNumber
	 * @throws CAIException
	 */
	public void add(User user) throws CAIException {
		if (userMap.containsValue(user))
			throw new CAIException(
					"Subscription already exists/Data uniqueness violation",
					5006);

		userMap.put(user.getTelephoneNumber(), user);
	}

	/**
	 * Removes a User from the register.
	 * 
	 * @param user
	 * @throws CAIException
	 */
	public void delete(User user) throws CAIException {
		if (!userMap.containsValue(user)) {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Subscriber does not exist", 5008);
			else
				throw new CAIException("The subscriber does not exist", 6001);
		}
		userMap.remove(user.getTelephoneNumber());
	}

	/**
	 * Removes a User from the register.
	 * 
	 * @param telephoneNumber
	 * @throws CAIException
	 */
	public void delete(String telephoneNumber) throws CAIException {
		if (!userMap.containsKey(telephoneNumber)) {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Subscriber does not exist", 5008);
			else
				throw new CAIException("The subscriber does not exist", 6001);
		}
		userMap.remove(telephoneNumber);
	}

	/**
	 * Checks credentials
	 * 
	 * @param username
	 * @param password
	 * @throws CAIException
	 */
	public void bind(String username, String password) throws CAIException {
		Iterator<String> it = adminMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Administrator administrator = adminMap.get(key);
			if (administrator.getUserName().equals(username)) {
				if (administrator.getPassword().equals(password)) {
					return;
				}
			}
		}

		if (CAIShark.MOIP_SYSTEM)
			throw new CAIException("Invalid credentials", 5002);
		else
			throw new CAIException("Operator ID/password invalid", 3006);
	}

	void addAdministrator(Administrator administrator) {
		adminMap.put(administrator.getUserName(), administrator);
	}

	private void load() {
		addAdministrator(new Administrator("admin", "admin"));
		addAdministrator(new Administrator("provadm", "provadm123"));

		// MoIP user
		User u = new User("171000");
		u.addAttribute(new Attribute("TELEPHONENUMBER",
				new String[] { "161000" }));
		u.addAttribute(new Attribute("MAILHOST", new String[] { "torin.lab" }));
		userMap.put(u.getTelephoneNumber(), u);

		// Charging user
		User u2 = new User("161074");
		u2.addAttribute(new Attribute("SubscriberNumber",
				new String[] { "161074" }));
		u2.addAttribute(new Attribute("MasterSubscriberNumber",
				new String[] { "161000" }));
		u2.addAttribute(new Attribute("Language", new String[] { "2" }));
		userMap.put(u2.getTelephoneNumber(), u2);
	}
}
