import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Date: 2008-feb-26
 *
 * @author emahagl
 */
public class ImapSession implements Runnable {
    private Socket socket;

    private PrintWriter out;

    private ImapSessionHandler sessionHandler = new ImapSessionHandler();

    /**
     * Constructor.
     *
     * @param socket for the communication
     */
    public ImapSession(Socket socket) {
        this.socket = socket;

        System.out.println("New Client " + socket);
        Thread servThread = new Thread(this);
        servThread.start();
    }

    public void run() {

        try {
            //out = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
            //out = new InternetPrintWriter(new PrintWriter(new BufferedOutputStream(socket.getOutputStream(), 1024)));
            out = new InternetPrintWriter(new PrintWriter(new DataOutputStream(socket.getOutputStream())));
            // Write welcome message
            String welcomeMsg = "* OK [CAPABILITY NOTHING] Welcome to the Ms-Sim Server";
            out.println(welcomeMsg);
            out.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ASCII"), 512);

            String line = "";
            while ((line = reader.readLine()) != null) {
                boolean result = sessionHandler.handleRequest(line, out);
                if (!result) break;
            }
        }
        catch (UnknownHostException e) {
            System.out.println(e);
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
        finally {
            resetSession();
        }
    }

    void resetSession() {
        try {
            if (socket != null) {
                System.out.println("Reseting socket " + socket);
                socket.close();
            }

        } catch (IOException ioe) {
            // ignore
        } finally {
            socket = null;
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            // Ignored
        } finally {
            out = null;
        }
    }
}
