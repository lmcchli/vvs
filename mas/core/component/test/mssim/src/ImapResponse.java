import java.io.PrintWriter;

/**
 * Date: 2008-feb-27
 *
 * @author emahagl
 */
public class ImapResponse {
    private static String UNTAGGED = "*";
    private static String BAD = "BAD";
    private String tag = UNTAGGED;
    private PrintWriter out;

    public ImapResponse(PrintWriter out) {
        this.out = out;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Writes the message provided to the client, prepended with the request tag.
     *
     * @param message The message to write to the client.
     */
    public void taggedResponse(String message) {
        tag();
        message(message);
        end();
    }

    /**
     * Writes the message provided to the client, prepended with the
     * untagged marker "*".
     *
     * @param message The message to write to the client.
     */
    public void untaggedResponse(String message) {
        untagged();
        message(message);
        end();
    }

    /**
     * Writes a standard BAD response on command error, together with a descriptive message.
     * Example:
     * <pre>a01 BAD Unrecognized command</pre>
     *
     * @param message The descriptive error message.
     */
    public void commandError(String message) {
        tag();
        message(BAD);
        message(message);
        end();
    }

    /**
     * Writes a standard untagged BAD response, together with a descriptive message.
     * Example:
     * <pre>* BAD Invalid tag</pre>
     */
    public void badResponse(String message) {
        untagged();
        message(BAD);
        message(message);
        end();
    }

    private void message(String message) {
        if (message != null) {
            out.print(" ");
            out.print(message);
        }
    }

    private void tag() {
        out.print(tag);
    }

    private void untagged() {
        out.print(UNTAGGED);
    }

    private void end() {
        out.println();
        out.flush();
    }
}
