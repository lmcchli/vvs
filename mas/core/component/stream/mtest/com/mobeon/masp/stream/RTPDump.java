package com.mobeon.masp.stream;

import java.util.*;
import java.io.IOException;
import java.io.FileWriter;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-28
 * Time: 15:03:09
 * To change this template use File | Settings | File Templates.
 */
public class RTPDump {
    public enum DiffType {ONLY_IN_LEFT, ONLY_IN_RIGHT, EQUAL}
    public class DiffElement {
        private DiffType dt;
        private int leftIx;
        private int rightIx;

        public DiffElement(int leftIx, int rightIx, DiffType dt) {
            this.leftIx = leftIx;
            this.rightIx = rightIx;
            this.dt = dt;
        }

        public int getLeftIx() {
            return leftIx;
        }

        public int getRightIx() {
            return rightIx;
        }

        public DiffType getDiffType() {
            return dt;
        }

        public String toString() {
            switch (dt) {
                case ONLY_IN_LEFT :
                    return "<-";
                case ONLY_IN_RIGHT :
                    return "->";
                case EQUAL :
                    return "==";
                default: return "?";
            }
        }
    }

    private class RTPDumpPacketComparator implements Comparator<RTPDumpPacket> {
        public int compare(RTPDumpPacket o1, RTPDumpPacket o2) {
            if (o1.isRTPPacket() && o2.isRTPPacket()) {
                return (int) (((RTPPacket) o1).getExtendedSequenceNumber() - ((RTPPacket) o2).getExtendedSequenceNumber());
            } else {
                return (int) (o1.getOffset() - o2.getOffset());
            }
        }
    }


    private boolean isIFrame(RTPPacket p) {
        if (p.getPayloadType() == 34) {
            if ((p.getData()[12] & (byte) 0x80) != 0) {
                System.out.println("Mode A: " + Integer.toHexString(p.getData()[13]));

                return (p.getData()[13] & (byte) 0x10) != 0;
            } else {
                System.out.println("Mode B or C: " + Integer.toHexString(p.getData()[16]));
                return (p.getData()[16] & (byte) 0x80) != 0;
            }
        }

        return false;
    }

    public List<RTPDump> splitOnIFrame() {
        LinkedList<RTPDump> iFrames = new LinkedList<RTPDump>();

        int iframeCount = 1;
        RTPDump currentIframe = null;
        for (RTPDumpPacket p : getPackets()) {
            System.out.println(p);
            if (p.isRTPPacket() && ((RTPPacket) p).getPayloadType() == 34) {
                if (isIFrame((RTPPacket) p)) {
                    if (currentIframe != null)
                        iFrames.add(currentIframe);
                    currentIframe = new RTPDump(Integer.toString(iframeCount++));
                }
                if (currentIframe != null)
                    currentIframe.addPacket(p);
            }
        }

        return iFrames;
    }


    private static final int MAX_DIFF = 100;

    private int recordStartSeconds;
    private int recordStartUsecs;
    private long source;
    private int port;
    private String name;

    private ArrayList<RTPDumpPacket> packets;

    public RTPDump(String name) {
        this.name = name;
        packets = new ArrayList<RTPDumpPacket>();
    }

    public void load(String dumpfile) throws IOException {
        RTPDumpReader dump = new RTPDumpReader(dumpfile);

        RTPDumpHeader header = dump.readHeader();

        setRecordStartSeconds(header.getSeconds());
        setRecordStartUsecs(header.getUsecs());
        setSource(header.getSource());
        setPort(header.getPort());

        while (dump.available()) {
            addPacket(dump.readPacket());
        }
    }

    public void store(String dumpfile) throws IOException {
        RTPDumpWriter writer = new RTPDumpWriter(dumpfile);

        RTPDumpHeader header = new RTPDumpHeader(recordStartSeconds, recordStartUsecs, source, port);

        writer.writeHeader(header);

        for (RTPDumpPacket packet : packets) {
            writer.writePacket(packet);
        }
    }

    public void dumpRTP(String dumpfile) throws IOException {
        FileWriter writer = new FileWriter(dumpfile);

        for (RTPDumpPacket p : getPackets()) {
            if (p.isRTPPacket()) {
                byte[] data = p.getData();
                writer.write("#|");
                for (int i = 0; i < data.length; i++) {
                    writer.write(Integer.toBinaryString((int)(data[i] & 0xff)) + "|");
                }
                writer.write("\n");
            }
        }
    }

    public static long calculateOffset(RTPDump a, RTPDump b) {
        if (!a.getPackets().isEmpty() && !b.getPackets().isEmpty()) {
            return a.getPackets().get(0).getOffset() - b.getPackets().get(0).getOffset();
        } else
            return 0;
    }

    public boolean calculateExtendedSequenceNumbers() {
        int maxSequence = -1;
        int cycle = 0;
        final int maxDropout = 3000;
        final int maxMisorder = 100;

        for (int i = 0; i < packets.size(); ++i) {
            RTPDumpPacket p = (RTPDumpPacket) packets.get(i);

            if (p.isRTPPacket()) {
                int currentSequence = (int) ((RTPPacket) p).getExtendedSequenceNumber() & 0xffff;
                if (maxSequence >= 0) {
                    int delta = currentSequence - maxSequence;
                    if (delta < 0)
                        delta += 0xffff;

                    if (delta < maxDropout) {
                        if (currentSequence < maxSequence) {
                            ++cycle;
                        }
                    } else if (delta <= 0xffff - maxMisorder) {
                        return false;
                    }
                }
                maxSequence = currentSequence;
                ((RTPPacket) p).setExtendedSequenceNumber(currentSequence | (cycle << 16));
            }
        }

        return true;
    }

    public void addPacket(RTPDumpPacket p) {
        packets.add(p);
    }

    public ArrayList<RTPDumpPacket> getPackets() {
        return packets;
    }

    public void sortRTPPackets() {
        Collections.sort(packets, new RTPDumpPacketComparator());
    }

    public String getName() {
        return name;
    }

    public int getRecordStartSeconds() {
        return recordStartSeconds;
    }

    public int getRecordStartUsecs() {
        return recordStartUsecs;
    }

    public long getSource() {
        return source;
    }

    public int getPort() {
        return port;
    }

    public void setRecordStartSeconds(int seconds) {
        recordStartSeconds = seconds;
    }

    public void setRecordStartUsecs(int usecs) {
        recordStartUsecs = usecs;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMediaClockRate() {
        for (RTPDumpPacket p : getPackets()) {
            if (p.isRTPPacket()) {
                int payload = ((RTPPacket) p).getPayloadType();
                switch (payload) {
                    case 0: return 8000;
                    case 34: return 90000;
                }
            }
        }

        return 0;
    }

    public boolean isValid() {
        int clockrate = getMediaClockRate();
        if (clockrate > 0) {
            RTPPacket firstRTPPacket = null;
            RTPPacket lastRTPPacket = null;

            for (int i = 0; i < getPackets().size(); ++i) {
                RTPDumpPacket p = getPackets().get(i);
                if (p.isRTPPacket()) {
                    if (firstRTPPacket == null)
                        firstRTPPacket = (RTPPacket) p;

                    lastRTPPacket = (RTPPacket) p;
                    if (firstRTPPacket != null) {
                        long diffOffset = lastRTPPacket.getOffset() - firstRTPPacket.getOffset();

                        long calculatedDiff = (lastRTPPacket.getTimestamp() - firstRTPPacket.getTimestamp()) * 1000 / clockrate;

                        //System.out.println("calculatedDiff: " + calculatedDiff);
                        //System.out.println("diffOffset: " + diffOffset);

                       /* if (Math.abs(diffOffset - calculatedDiff) > MAX_JITTER) {
                            System.out.println("MAX_JITTER exeeded: " + Math.abs(diffOffset - calculatedDiff));
                            return false;
                        } */
                    }
                }
            }
            if (firstRTPPacket != null && lastRTPPacket != null) {
                long calcDiff = (lastRTPPacket.getTimestamp() - firstRTPPacket.getTimestamp()) * 1000 / clockrate;
                System.out.println("Diff ts: " + lastRTPPacket.getTimestamp() + " : " + firstRTPPacket.getTimestamp() + " : " + calcDiff);
                long diff = lastRTPPacket.getOffset() - firstRTPPacket.getOffset();
                System.out.println("Diff Off: " + lastRTPPacket.getOffset() + " : " + firstRTPPacket.getOffset() + " : " + diff);
                System.out.println("Diff " + getName() + " : " + Math.abs(diff - calcDiff));
                return Math.abs(diff - calcDiff) < MAX_DIFF;
            }
        }

        return true;
    }

    public static double diffRTP(RTPDump d1, RTPDump d2, LinkedList<DiffElement> diff) {
        ArrayList l1 = d1.getPackets();
        ArrayList l2 = d2.getPackets();

        int leftIx = 0;
        int rightIx = 0;
        int errorcount = 0;

        while (leftIx < l1.size() && rightIx < l2.size()) {
            int leftMatch = -1;
            while (leftMatch < leftIx && rightIx < l2.size()) {
                leftMatch = l1.indexOf(l2.get(rightIx));
                if (leftMatch < leftIx) {
                    //System.out.println("Diff only in right: " + leftIx + " , " + leftMatch);
                    diff.add(new RTPDump("").new DiffElement(leftIx, rightIx++, DiffType.ONLY_IN_RIGHT));
                    ++errorcount;
                }
            }

            int rightMatch = -1;
            while (rightMatch < rightIx && leftIx < l1.size()) {
                rightMatch = l2.indexOf(l1.get(leftIx));
                if (rightMatch < rightIx) {
                    //System.out.println("Diff only in left: " + rightIx + " , " + rightMatch);
                    diff.add(new RTPDump("").new DiffElement(leftIx++, rightIx, DiffType.ONLY_IN_LEFT));
                    ++errorcount;
                }
            }

            /*System.out.println("LeftIx: " + leftIx);
            System.out.println("LeftMatch: " + leftMatch);
            System.out.println("RightIx: " + rightIx);
            System.out.println("RightMatch: " + rightMatch);*/

            if (leftMatch >= 0 && rightMatch >=0) {
                if (leftMatch - leftIx < rightMatch - rightIx) {
                    while (leftIx < leftMatch) {
                        //System.out.println("Diff only in right: " + leftIx + " , " + rightIx);
                        diff.add(new RTPDump("").new DiffElement(leftIx++, rightIx, DiffType.ONLY_IN_LEFT));
                        ++errorcount;
                    }
                } else {
                    while (rightIx < rightMatch) {
                        //System.out.println("Diff only in left: " + leftIx + " , " + rightIx);
                        diff.add(new RTPDump("").new DiffElement(leftIx, rightIx++, DiffType.ONLY_IN_LEFT));
                        ++errorcount;
                    }
                }

                //System.out.println("Diff equals: " + leftIx + " , " + rightIx);
                diff.add(new RTPDump("").new DiffElement(leftIx++, rightIx++, DiffType.EQUAL));
            }
        }

        for (int i = leftIx; i < l1.size(); ++i) {
            //System.out.println("Diff only in left: " + i + " , " + rightIx);
            diff.add(new RTPDump("").new DiffElement(i, rightIx, DiffType.ONLY_IN_LEFT));
            ++errorcount;
        }

        for (int i = rightIx; i < l2.size(); ++i) {
            //System.out.println("Diff only in right: " + leftIx + " , " + i);
            diff.add(new RTPDump("").new DiffElement(leftIx, i, DiffType.ONLY_IN_RIGHT));
            ++errorcount;
        }

        System.out.println("errorcount: " + errorcount);
        System.out.println("errorfactor: " + ((double) errorcount / (double) Math.max(l1.size(), l2.size())));
        return (double) errorcount / (double) Math.max(l1.size(), l2.size());
    }


    public boolean equals(Object o) {
        if (o != null && o instanceof RTPDump) {
            RTPDump dump = (RTPDump) o;


            ArrayList<RTPPacket> l1 = new ArrayList<RTPPacket>();
            ArrayList<RTPPacket> l2 = new ArrayList<RTPPacket>();

            for (RTPDumpPacket p : getPackets()) {
                if (p.isRTPPacket())
                    l1.add((RTPPacket) p);
            }

            for (RTPDumpPacket p : dump.getPackets()) {
                if (p.isRTPPacket())
                    l2.add((RTPPacket) p);
            }

            if (l1.size() == l2.size()) {
                if (l1.equals(l2))
                    return true;
                else {
                	l1.remove(l1.size() -1);
                	l2.remove(l2.size() -1);
                	if (l1.equals(l2))
                        return true;
                    for (int i = 0; i < l1.size(); ++i) {
                        if (!l1.get(i).equals(l2.get(i))) {
                            System.out.println("Mismatch on packet: " + i);
                            System.out.println(l1.get(i));
                            System.out.println(l2.get(i));
                        }
                    }
                    return false;
                }
            } else {
                System.out.println("l1:" + l1.size() + " l2:" + l2.size());
                LinkedList<DiffElement> diff = new LinkedList<DiffElement>();
                double diffResult = diffRTP(this, dump, diff);
                //System.out.println(diff);
                if (diffResult <= 0.01)
                    return true;
            }
        }
        return false;
    }

    public static String compareDumps(RTPDump audio1, RTPDump video1, RTPDump audio2, RTPDump video2, boolean verifySynchronization) {

        if (audio1 != null && !audio1.isValid()) {
            return "Dump: " + audio1.getName() + " is not valid!";
        }

        if (video1 != null && !video1.isValid()) {
            return "Dump: " + video1.getName() + " is not valid!";
        }

        if (audio2 != null && !audio2.isValid()) {
            return "Dump: " + audio2.getName() + " is not valid!";
        }

        if (video2 != null && !video2.isValid()) {
            return "Dump: " + video2.getName() + " is not valid!";
        }

        if (audio1 != null && !audio1.equals(audio2)) {
            return "Dumps: " + audio1.getName() + " and " + audio2.getName() + " are not equal!";
        } else if (audio1 != null && audio2 == null) {
            return "Dumps: " + audio1.getName() + " and " + audio2.getName() + " are not equal!";
        }

        if (video1 != null && !video1.equals(video2)) {
            return "Dumps: " + video1.getName() + " and " + video2.getName() + " are not equal!";
        } else if (video1 != null && video2 == null) {
            return "Dumps: " + video1.getName() + " and " + video2.getName() + " are not equal!";
        }

        if (verifySynchronization && audio1 != null && audio2 != null && video1 != null && video2 != null) {
            long offset1 = RTPDump.calculateOffset(audio1, video1);
            long offset2 = RTPDump.calculateOffset(audio2, video2);

            if (Math.abs(offset1 - offset2) > 50) {
                return "Audio/Video offset (" + Math.abs(offset1 - offset2) + ") to large between (" +
                        audio1.getName() + "," + video1.getName() + ") and (" +
                        audio2.getName() + "," + video2.getName() + ")!";
            }
        }

        return null;
    }

    public void printPayloadHeader() {
        for (int i = 0; i < packets.size(); ++i) {
            if (((RTPDumpPacket) packets.get(i)).isRTPPacket()) {
                RTPPacket p = (RTPPacket) packets.get(i);

                int cc = p.getContributingSources() * 4;
                int extensions = (p.getExtension() ? 4 : 0);

                int payloadheaderStart = 16 + cc + extensions;
                int payloadHeader = p.getData()[payloadheaderStart] << 24;
                payloadHeader |= p.getData()[payloadheaderStart + 1] << 16;
                payloadHeader |= p.getData()[payloadheaderStart + 2] << 8;
                payloadHeader |= p.getData()[payloadheaderStart + 3];
                System.out.println(Integer.toBinaryString(payloadHeader));
            }
        }
    }

    public void printDTMF() {
        for (RTPDumpPacket dp : packets) {
            if (dp.isRTPPacket()) {
                RTPPacket p = (RTPPacket) dp;
                if (p.isDTMF()) {
                    System.out.println("Token: " + p.getDTMFToken() + " : " + p.getDTMFDuration() + " : " + p.getDTMFVolume());
                }
            }
        }
    }



    public void removeDTMF() {
        ListIterator<RTPDumpPacket> i = getPackets().listIterator();

        while (i.hasNext()) {
            RTPDumpPacket dp = i.next();
            if (dp.isRTPPacket()) {
                if (((RTPPacket) dp).getPayloadType() == 101) {
                    i.remove();
                }
            }
        }
    }

    public int shuffle(int count) {
        ArrayList<Integer> rtpPackets = new ArrayList<Integer>();

        for (int i = 0; i < packets.size(); ++i) {
            if (((RTPDumpPacket) packets.get(i)).isRTPPacket()) {
                rtpPackets.add(i);
            }
        }

        int shuffleCount = 0;

        HashSet<Integer> shuffled = new HashSet<Integer>();
        Random r = new Random();
        while (shuffleCount < Math.min(rtpPackets.size(), count)) {
            int i = r.nextInt(rtpPackets.size() - 2);
            if (!shuffled.contains(i)) {
                ++shuffleCount;
                RTPDumpPacket p = packets.get(rtpPackets.get(i));
                long tmp = p.getOffset();
                RTPDumpPacket p2 = packets.get(rtpPackets.get(i + 1));
                p.setOffset(p2.getOffset());
                p2.setOffset(tmp);
                Collections.swap(packets, rtpPackets.get(i), rtpPackets.get(i + 1));
                shuffled.add(i);
            }
        }

        return shuffleCount;
    }

    public int remove(int count, int rangesize) {
        ArrayList<Integer> rtpPackets = new ArrayList<Integer>();

        for (int i = 0; i < packets.size(); ++i) {
            if (((RTPDumpPacket) packets.get(i)).isRTPPacket()) {
                rtpPackets.add(i);
            }
        }

        int removeCount = 0;

        Random r = new Random();
        HashSet<RTPDumpPacket> removeSet = new HashSet<RTPDumpPacket>();

        while (removeCount < Math.min(rtpPackets.size(), count)) {
            int i = r.nextInt(rtpPackets.size() - 1);
            if (rtpPackets.get(i) >= 0) {
                ++removeCount;
                for (int j = i; j < Math.min(i + rangesize, rtpPackets.size()); ++j) {
                    if (rtpPackets.get(j) >= 0) {
                        removeSet.add(packets.get(rtpPackets.get(j)));
                        rtpPackets.set(j, -1);
                    }
                }
            }
        }

        for (RTPDumpPacket p : removeSet) {
            packets.remove(p);
        }

        return removeSet.size();
    }

    public void print() {
        for (RTPDumpPacket p : packets) {
            System.out.println(p);
        }
    }
}
