import java.io.PrintWriter;

/**
 * Date: 2008-mar-20
 *
 * @author emahagl
 */
public class CAIResponse {

    private PrintWriter out;

    public CAIResponse(PrintWriter out) {
        this.out = out;
    }

    public void success(String message) {
        StringBuffer buf = new StringBuffer();
        buf.append("RESP:0");
        buf.append(":Successful");
        if (message != null) {
            buf.append(":");
            buf.append(message);
        }
        buf.append(";");

        out.println(buf.toString());
        out.print("CAI>:");
        out.flush();
    }

    public void bad(int code, String message) {
        StringBuffer buf = new StringBuffer();
        buf.append("RESP:");
        buf.append(code);
        if (message != null) {
            buf.append(":");
            buf.append(message);
        }
        buf.append(";");

        out.println(buf.toString());
        out.print("CAI>:");
        out.flush();
    }
}
