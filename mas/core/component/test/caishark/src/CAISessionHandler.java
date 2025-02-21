import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.List;

/**
 * Date: 2008-mar-20
 *
 * @author emahagl
 */
public class CAISessionHandler {

	/**
	 * Handles a request string and writes a response to the outputstream.
	 * 
	 * @param requestLine
	 * @param output
	 * @return
	 * @throws ProtocolException
	 */
	public boolean handleRequest(String requestLine, PrintWriter output)
			throws ProtocolException {
		System.out.println(requestLine);

		CAIResponse response = new CAIResponse(output);

		CommandParser commandParser = null;
		try {
			commandParser = new CommandParser(requestLine);
			if (commandParser.getCommand().equals("LOGOUT")) {
				response.success(null);
				return false;
			}
			processCommand(commandParser, response);

		} catch (CAIException e) {
			response.bad(e.getErrorCode(), e.getMessage());
		}

		return true;
	}

	private void processCommand(CommandParser commandParser, CAIResponse response) {
		if (commandParser.getCommand().equals("LOGIN")) {
			login(commandParser, response);
		} else if (commandParser.getCommand().equals("CREATE")) {
			create(commandParser, response);
		} else if (commandParser.getCommand().equals("GET")) {
			get(commandParser, response);
		} else if (commandParser.getCommand().equals("DELETE")) {
			delete(commandParser, response);
		} else {
			if (CAIShark.MOIP_SYSTEM)
				response.bad(5005, "Unsupported command");
			else
				response.bad(3001, "Invalid command");
		}
	}

	private void login(CommandParser commandParser, CAIResponse response) {
		String uid = commandParser.getCommandToken(1);
		String pwd = commandParser.getCommandToken(2);
		try {
			UserRegister.getInstance().bind(uid, pwd);
			response.success(null);
		} catch (CAIException e) {
			response.bad(e.getErrorCode(), e.getMessage());
		}
	}

	private void get(CommandParser commandParser, CAIResponse response) {
		try {
			String telephoneNumber = commandParser.getTelephoneNumber();
			User user = UserRegister.getInstance().getUser(telephoneNumber);

			List<Attribute> attrs = user.getAttributes();
			if (attrs != null && !attrs.isEmpty()) {
				String respStr = "";
				for (int i = 0; i < attrs.size(); i++) {
					Attribute a = attrs.get(i);
					respStr += a.getName();
					respStr += ",";
					respStr += a.getValues()[0];
					if (i + 1 < attrs.size()) respStr += ":";
				}
				response.success(respStr);
			} else {
				response.success(null);
			}
		} catch (CAIException e) {
			response.bad(e.getErrorCode(), e.getMessage());
		}
	}

	private void create(CommandParser commandParser, CAIResponse response) {
		String telephoneNumber = null;
		try {
			telephoneNumber = commandParser.getTelephoneNumber();
			UserRegister.getInstance().getUser(telephoneNumber);
			if (CAIShark.MOIP_SYSTEM)
				response.bad(5006, "Subscription already exists/Data uniqueness violation");
			else
				response.bad(-3011, "Subscriber number already exists");
			return;
		} catch (CAIException e) {
			// Check if the error comes from the parsing when telephonenumber is extracted.
			if (e.getErrorCode() == 5004 || e.getErrorCode() == 3008) {
				response.bad(e.getErrorCode(), e.getMessage());
			}
		}

		User user = new User(telephoneNumber);
		List<Attribute> attrs = commandParser.getAttributes();

		if (attrs != null) user.addAttributes(attrs);

		try {
			UserRegister.getInstance().add(user);
		} catch (CAIException e) {
			e.printStackTrace();
		}

		response.success(null);
	}

	private void delete(CommandParser commandParser, CAIResponse response) {
		try {
			String telephoneNumber = commandParser.getTelephoneNumber();
			UserRegister.getInstance().delete(telephoneNumber);
			response.success(null);
		} catch (CAIException e) {
			response.bad(e.getErrorCode(), e.getMessage());
		}
	}
}
