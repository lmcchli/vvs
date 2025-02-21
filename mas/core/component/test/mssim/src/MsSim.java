import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Date: 2008-feb-26
 *
 * @author emahagl
 */
public class MsSim implements Runnable {

    private Thread serverThread;
    private volatile boolean started = false;
    private int imapPort;
    private ServerSocket serverSocket;

    /**
     * Constructor.
     */
    public MsSim(int imapPort) {
	    SimpleStore.load();
        this.imapPort = imapPort;
    }

    public void start() {
        synchronized (this) {
            System.out.println("MsSim is being started...");
            if (serverThread == null) {
                started = true;
                serverThread = new Thread(this, "MS-Sim");
                serverThread.start();
            }
        }
    }

    public void stop() {
        System.out.println("MsSim is being stopped...");
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
            runImapService();
        } catch (IOException e) {
            // Suppress socket closed messages that comes when the serversocket is closed in the stop method.
            if (e.toString().indexOf("socket closed") == -1) {
                System.out.println("Exception in run: " + e);
            }
        }
    }

    private void runImapService() throws IOException {
        serverSocket = new ServerSocket(imapPort);
        System.out.println("MsSim started on imapPort: " + imapPort);

        try {
            while (Thread.currentThread() == serverThread && started) {
                // Blocks until a connection occurs:
                Socket inSocket = serverSocket.accept();
                new ImapSession(inSocket);
            }
        }
        finally {
            serverSocket.close();
        }
        System.out.println("MsSim is stopped");
    }

    public static void main(String[] argv) {
        int imapPort = 1143;

        int optind = 0;
        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-p")) {
                imapPort = Integer.parseInt(argv[++optind]);
            } else if (argv[optind].startsWith("-")) {
                System.out.println("Usage: MsSim [-p imapPort]");
                System.exit(1);
            } else {
                break;
            }
        }

        MsSim server = new MsSim(imapPort);
        server.start();

        /*
        try {
            Thread.sleep(4 * 1000);
        } catch (InterruptedException e) {
            System.out.println("Exception in main " + e);
        }
        server.stop();

        try {
            Thread.sleep(4 * 1000);
        } catch (InterruptedException e) {
            System.out.println("Exception in main " + e);
        }
        server.start();
        */
    }


}
