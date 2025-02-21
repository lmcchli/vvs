/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.client;
// This example is from the book _Java in a Nutshell_ by David Flanagan.
// Written by David Flanagan.  Copyright (c) 1996 O'Reilly & Associates.
// You may study, use, modify, and distribute this example for any purpose.
// This example is provided WITHOUT WARRANTY either expressed or implied.

import com.mobeon.util.ErrorCodes;

import java.io.*;
import java.net.*;

public class DummySessionClient extends Thread {
    public static final int DEFAULT_PORT = 6789;
    Thread myThread;
    private Boolean _stopRunning;

    public DummySessionClient() {
        _stopRunning = new Boolean(false);
    }

    public static void usage() {
        System.out.println("Usage: java DummySessionClient <hostname> [<port>]");
        System.exit(ErrorCodes.SUCCESS);
    }

    public void stopRunning() {
        synchronized (_stopRunning) {
            _stopRunning = new Boolean(true);
            myThread.interrupt();
        }
    }

    public boolean checkStopRunning() {
       synchronized (_stopRunning) {
            return _stopRunning.booleanValue();
        }
    }

    public void run() {
        int port = DEFAULT_PORT;
        Socket s = null;
        port = DEFAULT_PORT;
        myThread = Thread.currentThread();

        try {
            // Create a socket to communicate to the specified host and port
            s = new Socket( InetAddress.getLocalHost( ), port );
            BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            StreamDisplay display = new StreamDisplay(sin, this);
            PrintStream sout = new PrintStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(System.in);

            // Tell the user that we've connected
            System.out.println("Connected to " + s.getInetAddress()
                       + ":"+ s.getPort());
            BufferedReader userinput = new BufferedReader(new InputStreamReader(System.in));

            String line;
            int count = 0;
            while(!checkStopRunning()) {
                try  {
                    count++;
                    line = userinput.readLine();
                    if (line == null)
                        return;
                    sout.println(line);
                } catch (Exception e) {
                    System.out.println("DummySessionClient: Caught exception");
                }
            }

        }
        catch (IOException e) { e.printStackTrace(); }
        // Always be sure to close the socket
        finally {
            try { if (s != null) s.close(); } catch (IOException e2) { e2.printStackTrace(); }
        }
    }

}
