import jakarta.mail.Flags;

/**
 * Class with IMAP formatted Strings of MessageFlags
 *
 * @author emahagl
 */
public class MessageFlags {
    public static final Flags ALL_FLAGS = new Flags();

    static {
        ALL_FLAGS.add(Flags.Flag.ANSWERED);
        ALL_FLAGS.add(Flags.Flag.DELETED);
        ALL_FLAGS.add(Flags.Flag.DRAFT);
        ALL_FLAGS.add(Flags.Flag.FLAGGED);
        ALL_FLAGS.add(Flags.Flag.RECENT);
        ALL_FLAGS.add(Flags.Flag.SEEN);
    }

    public static final String ANSWERED = "\\ANSWERED";
    public static final String DELETED = "\\DELETED";
    public static final String DRAFT = "\\DRAFT";
    public static final String FLAGGED = "\\FLAGGED";
    public static final String SEEN = "\\SEEN";

    /**
     * Returns IMAP formatted String of MessageFlags
     */
    public static String format(Flags flags) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        if (flags.contains(Flags.Flag.ANSWERED)) {
            buf.append("\\Answered ");
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            buf.append("\\Deleted ");
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            buf.append("\\Draft ");
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            buf.append("\\Flagged ");
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            buf.append("\\Recent ");
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            buf.append("\\Seen ");
        }
        // Remove the trailing space, if necessary.
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }
        buf.append(")");
        return buf.toString();
    }
}
