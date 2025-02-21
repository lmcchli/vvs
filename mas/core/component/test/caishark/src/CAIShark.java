import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Date: 2008-mar-19
 *
 * @author emahagl
 */
public class CAIShark implements Runnable {

	public static boolean MOIP_SYSTEM = false;
	
    private Thread serverThread;
    private volatile boolean started = false;
    private int caiPort;
    private ServerSocket serverSocket;

    /**
     * Constructor.
     */
    public CAIShark(int caiPort) {
        this.caiPort = caiPort;
    }

    public void start() {
        synchronized (this) {
            System.out.println("CAIShark is being started...");
            if (serverThread == null) {
                started = true;
                serverThread = new Thread(this, "CAIShark");
                serverThread.start();
            }
        }
    }

    public void stop() {
        System.out.println("CAIShark is being stopped...");
        if (serverThread != null) {
            started = false;
            serverThread = null;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Exception in stop" + e);
                }
            }
        }
        System.out.println("...done");
    }

    /**
     * Current thread runs here until it is stopped.
     */
    public void run() {
        try {
            runService();
        } catch (IOException e) {
            // Suppress socket closed messages that comes when the serversocket is closed in the stop method.
            if (e.toString().indexOf("socket closed") == -1) {
                System.out.println("Exception in run: " + e);
            }
        }
    }

    private void runService() throws IOException {
        serverSocket = new ServerSocket(caiPort);
        System.out.println("CAIShark started on port: " + caiPort);

        try {
            while (Thread.currentThread() == serverThread && started) {
                // Blocks until a connection occurs:
                Socket inSocket = serverSocket.accept();
                new CAISession(inSocket);
            }
        }
        finally {
            serverSocket.close();
        }
        System.out.println("CAIShark is stopped");
    }

    public static void main(String[] argv) {
        int port = 7102;

        int optind = 0;
        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-p")) {
                port = Integer.parseInt(argv[++optind]);
            } else if (argv[optind].startsWith("-")) {
                System.out.println("Usage: CaiShark [-p port]");
                System.exit(1);
            } else {
                break;
            }
        }

        CAIShark server = new CAIShark(port);
        server.start();
    }
}
