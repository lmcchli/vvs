/**
 * Date: 2008-mar-25
 *
 * @author emahagl
 */
public class CAIException extends Exception {
    private int errorCode;

    /**
     * Constructor.
     *
     * @param msg
     */
    public CAIException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param msg
     * @param errorCode
     */
    public CAIException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * Retrieves the error code
     *
     * @return message
     */
    public int getErrorCode() {
        return errorCode;
    }
}
