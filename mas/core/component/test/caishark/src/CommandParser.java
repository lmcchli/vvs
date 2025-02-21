import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2008-mar-20
 * 
 * @author emahagl
 */
public class CommandParser {
	private static final String SEMICOLON = ";";
	private static final String COLON = ":";
	private static final String COMMA = ",";

	private String requestLine;
	private String commandTokens[];
	private String moName;
	private String telephoneNumberCommand;

	/**
	 * Constructor, parses the request string
	 * 
	 * @param requestLine
	 * @throws CAIException
	 */
	public CommandParser(String requestLine) throws CAIException {
		this.requestLine = requestLine;
		parse(requestLine);
	}

	private void parse(String requestLine) throws CAIException {
		int semicolonPos = requestLine.indexOf(SEMICOLON);
		if (semicolonPos == -1) {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Invalid command sequence", 5004);
			else
				throw new CAIException("Invalid command sequence", 3008);
		}

		requestLine = requestLine.substring(0, semicolonPos);
		commandTokens = requestLine.split(COLON);
		if (commandTokens.length == 0) {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Invalid command sequence", 5004);
			else
				throw new CAIException("Invalid command sequence", 3008);
		}

		if (commandTokens.length > 1) {
			moName = commandTokens[1];
		}
	}

	public String getCommand() {
		return getCommandToken(0);
	}

	public String getTelephoneNumber() throws CAIException {
		if (moName != null) {
			telephoneNumberCommand = commandTokens[2];
		} else {
			telephoneNumberCommand = commandTokens[1];
		}
		String[] arr = telephoneNumberCommand.split(COMMA);
		if (arr.length == 2)
			return arr[1];
		else {
			if (CAIShark.MOIP_SYSTEM)
				throw new CAIException("Invalid command sequence", 5004);
			else
				throw new CAIException("Invalid command sequence", 3008);
		}
	}

	public String getCommandToken(int index) {
		if (index >= commandTokens.length)
			return "";
		return commandTokens[index];
	}

	public String getRequestLine() {
		return requestLine;
	}

	/**
	 * Retrieves list of attributes that may come after the commands
	 * 
	 * @return the list
	 */
	public List<Attribute> getAttributes() {
		ArrayList<String> tokens = new ArrayList<String>();
		int index = 2;
		if (moName != null)
			index = 3;

		for (; index < commandTokens.length; index++) {
			tokens.add(commandTokens[index]);
		}
		if (tokens.isEmpty())
			return null;

		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		for (int i = 0; i < tokens.size(); i++) {
			Attribute a = getAttribute(tokens.get(i));
			if (a != null)
				attributeList.add(a);
		}
		if (attributeList.isEmpty())
			return null;

		return attributeList;
	}

	private Attribute getAttribute(String token) {
		int commaPos = token.indexOf(COMMA);
		if (commaPos == -1)
			return null;

		String name = token.substring(0, commaPos);
		String value = token.substring(commaPos + 1, token.length());

		return new Attribute(name, new String[] { value });
	}

}
