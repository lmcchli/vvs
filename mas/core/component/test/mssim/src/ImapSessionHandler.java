import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.List;

/**
 * Date: 2008-feb-26
 *
 * @author emahagl
 */
public class ImapSessionHandler {

    private User user;

    public boolean handleRequest(String requestLine, PrintWriter output) throws ProtocolException {
        ImapResponse response = new ImapResponse(output);
        System.out.println(requestLine);

        CommandParser commandParser = new CommandParser(requestLine);
        response.setTag(commandParser.getTag());

        if (commandParser.getCommand().equalsIgnoreCase("logout")) {
            response.untaggedResponse("BYE LOGOUT received");
            response.taggedResponse("OK Completed");
            return false;
        }

        processCommand(commandParser, response);

        return true;
    }

    private void processCommand(CommandParser commandParser, ImapResponse response) {
        if (commandParser.getCommand().equalsIgnoreCase("login")) {
            login(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("capability")) {
            capabilities(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("fetch")) {
            fetch(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("list")) {
            list(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("select")) {
            select(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("examine")) {
            examine(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("search")) {
            search(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("getquotaroot")) {
            getQuotaRoot(commandParser, response);
        } else if (commandParser.getCommand().equalsIgnoreCase("store")) {
            store(commandParser, response);
        } else {
            // NOOP/CLOSE
            response.taggedResponse("OK Completed");
        }
    }

    private void login(CommandParser commandParser, ImapResponse response) {
        String uid = commandParser.getCommandToken(2);
        String pwd = commandParser.getCommandToken(3);
        user = UserFactory.getUser(uid, pwd);
        response.taggedResponse("OK User logged in");
    }

    private void fetch(CommandParser commandParser, ImapResponse response) {
        if (user != null) {
            FetchHandler fetchHandler = new FetchHandler(commandParser, response, user);
            fetchHandler.handleCommand();
        } else {
            response.taggedResponse("OK Completed");
        }
    }

    private void list(CommandParser commandParser, ImapResponse response) {
        response.untaggedResponse("LIST (\\NoInferiors) \"/\" INBOX");
        response.taggedResponse("OK Completed");
    }

    private void select(CommandParser commandParser, ImapResponse response) {
        selectOrExamine(response, true);
    }

    private void examine(CommandParser commandParser, ImapResponse response) {
        selectOrExamine(response, false);
    }

    private void selectOrExamine(ImapResponse response, boolean select) {
        int noOfMessages = 0;
        int noOfRecentMessages = 0;
        int firstUnseenMessage = 1;
        if (user != null) {
            noOfMessages = user.getNoOfMessages();
            noOfRecentMessages = user.getNoOfRecentMessages();
        }

        response.untaggedResponse("FLAGS (\\Answered \\Flagged \\Draft \\Deleted \\Seen)");
        if (select) {
            response.untaggedResponse("OK [PERMANENTFLAGS (\\Answered \\Flagged \\Draft \\Deleted \\Seen \\*)]");
        } else {
            response.untaggedResponse("OK [PERMANENTFLAGS ()]");
        }
        response.untaggedResponse(noOfMessages + " EXISTS");
        response.untaggedResponse(noOfRecentMessages + " RECENT");
        response.untaggedResponse("OK [UNSEEN " + firstUnseenMessage + "]");
        response.untaggedResponse("OK [UIDVALIDITY 1204022510]");
        response.untaggedResponse("OK [UIDNEXT 4]");
        if (select) {
            response.taggedResponse("OK [READ-WRITE] Completed");
        } else {
            response.taggedResponse("OK [READ-ONLY] Completed");
        }
    }

    private void search(CommandParser commandParser, ImapResponse response) {
        if (user != null) {
            StringBuffer buf = new StringBuffer();
            buf.append("SEARCH ");
            List<Integer> msgIds = user.getMsgIds();
            for (int i = 0; i < msgIds.size(); i++) {
                Integer integer = msgIds.get(i);
                buf.append(integer.toString());
                if (i + 1 < msgIds.size()) buf.append(" ");
            }
            response.untaggedResponse(buf.toString());
        } else {
            response.untaggedResponse("SEARCH 1");
        }
        response.taggedResponse("OK Completed");
    }

    // ToDo fix
    private void store(CommandParser commandParser, ImapResponse response) {
        response.untaggedResponse("FETCH FLAGS (\\Deleted)"); // \Recent \Seen
        response.taggedResponse("OK Completed");
    }

    private void getQuotaRoot(CommandParser commandParser, ImapResponse response) {
        if (user != null) {
            response.untaggedResponse("QUOTAROOT inbox user/" + user.getUid());
            response.untaggedResponse("QUOTA user/" + user.getUid() + "()");
        }
        response.taggedResponse("OK Completed");
    }

    private void capabilities(CommandParser commandParser, ImapResponse response) {
        response.untaggedResponse("CAPABILITY IMAP4 IMAP4rev1 ACL QUOTA LITERAL+ NAMESPACE UIDPLUS CHILDREN BINARY UNSELECT LANGUAGE XSENDER X-NETSCAPE XSERVERINFO AUTH=PLAIN");
        response.taggedResponse("OK Completed");
    }
}


