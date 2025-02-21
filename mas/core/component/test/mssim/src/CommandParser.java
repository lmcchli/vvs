/**
 * Date: 2008-feb-27
 *
 * @author emahagl
 */
public class CommandParser {
    private String requestLine;
    private String[] commandTokens;

    CommandParser(String requestLine) {
        this.requestLine = requestLine;
        parse(requestLine);
    }

    private void parse(String requestLine) {
        commandTokens = requestLine.split(" ");
    }

    public String getTag() {
        return getCommandToken(0);
    }

    public String getCommand() {
        return getCommandToken(1);
    }

    public String getCommandToken(int index) {
        if (index >= commandTokens.length) return "";
        return commandTokens[index];
    }

    public String getRequestLine() {
        return requestLine;
    }
}
