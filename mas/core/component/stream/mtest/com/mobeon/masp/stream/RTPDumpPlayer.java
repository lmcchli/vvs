package com.mobeon.masp.stream;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-28
 * Time: 15:22:23
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpPlayer implements Runnable {
    private RTPDump dump;
    private String remoteHost;
    private int rtpPort;
    private int rtcpPort;

    private AtomicBoolean running;
    private Thread playThread;
    private long startDelay;

    public RTPDumpPlayer(RTPDump dump, String remoteHost, int rtpPort, int rtcpPort, long startDelay) {
        running = new AtomicBoolean();
        playThread = null;
        this.dump = dump;
        this.remoteHost = remoteHost;
        this.rtpPort = rtpPort;
        this.rtcpPort = rtcpPort;
        this.startDelay = startDelay;
    }

    public void play() {
        playThread = new Thread(this);
        playThread.start();
    }

    public void stop() {
        if (playThread != null) {
            running.set(false);
            boolean joined = false;
            while (!joined) {
                try {
                    playThread.join();
                    joined = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void waitUntilFinished() {
        boolean joined = false;
        while (!joined) {
            try {
                playThread.join();
                joined = true;
            } catch (InterruptedException e) {
            }
        }
    }

    public void run() {
        if (startDelay > 0) {
            try {
                Thread.sleep(startDelay);
            } catch(InterruptedException e) {
            }
        }

        running.set(true);
        try {
            playDump();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running.set(false);
    }

    private void playDump() throws IOException {
        int clockRate = dump.getMediaClockRate();
        long firstSentPacketOffset = -1;
        long firstSentPacketTime = -1;
        long firstSentRTPTimestamp = -1;
        long firstSentRTPPacketTime = -1;

        DatagramSocket sock = null;

        try {
            InetAddress address = InetAddress.getByName(remoteHost);

            sock = new DatagramSocket();
            ArrayList<RTPDumpPacket> packets = dump.getPackets();

            int i = 0;

            while (i < packets.size() && running.get()) {
                RTPDumpPacket p = packets.get(i);
                if (i == 0) {
                    firstSentPacketTime = System.currentTimeMillis();
                    firstSentPacketOffset = p.getOffset();
                }
                //System.out.println("T:" + System.currentTimeMillis());
                if (p.isRTPPacket()) {
                    if (firstSentRTPTimestamp < 0) {
                        firstSentRTPTimestamp = ((RTPPacket) p).getTimestamp();
                        firstSentRTPPacketTime = (i == 0 ? firstSentPacketTime : System.currentTimeMillis());
                    }

                    //System.out.println("RTP: " + ((RTPPacket)p).getExtendedSequenceNumber());                    
                    //System.out.println("Sending RTP on port: " + rtpPort);
                    sock.send(new DatagramPacket(p.getData(),
                                                           p.getData().length,
                                                           address,
                                                           rtpPort));

                } else {
                    if (firstSentPacketOffset < 0) {
                        firstSentPacketOffset = p.getOffset();
                        firstSentPacketTime = System.currentTimeMillis();
                    }
                    sock.send(new DatagramPacket(p.getData(),
                                                           p.getData().length,
                                                           address,
                                                           rtcpPort));
                }

                if (i < (packets.size() - 1)) {
                    try {
                        RTPDumpPacket next = packets.get(i + 1);
                  /*      if (clockRate > 0 && firstSentRTPTimestamp > 0 && next.isRTPPacket()) {
                            long timeDiff = ((RTPPacket) next).getTimestamp() - firstSentRTPTimestamp;
                            if (timeDiff < 0)
                                timeDiff += 0xFFFFFFFF;

                            timeDiff = timeDiff * 1000 / clockRate;

                            long now = System.currentTimeMillis();
                            long sleepTime = timeDiff - (now - firstSentRTPPacketTime);
                            //System.out.println("TD: " + timeDiff + " ST:" + sleepTime + " Now: " + now + "FT: " + (now + sleepTime));

                            Thread.sleep(Math.max(0,sleepTime));
                        } else { */
                            long sleepTime = packets.get(i + 1).getOffset() - firstSentPacketOffset - (System.currentTimeMillis() - firstSentPacketTime);
                            //System.out.println("ST:" + sleepTime);
                            Thread.sleep(Math.max(0,sleepTime));
                        //}

                    } catch (InterruptedException e) {
                        running.set(false);
                    }
                }

                ++i;
            }
        } finally {
            if (sock != null) {                
                sock.close();
            }
        }
    }
}
