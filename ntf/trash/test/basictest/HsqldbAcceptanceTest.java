/**
 * This program performs and automated test of hsqldb, as it is used in NTF.
 * The purpose is to facilitate thorough testing of new versions in a short
 * time, and with little manual intervention.
 *
 * The testing is done through the public interfaces of NTFs data access
 * objects. The tests are executed with high load, while verifiying that what
 * is inserted and retrieved is consistent, and also that the transaction log is
 * OK.
 */

/*
  BUILD: 
  javac -classpath /vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf/bin/hsqldb.jar HsqldbAcceptanceTest.java

  RUN:
  java -mx256m -classpath .:/vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf/bin/hsqldb.jar HsqldbAcceptanceTest
*/

import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.DelayInfoDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mobeon.common.storedelay.*;

/**
 *
 */
public class HsqldbAcceptanceTest extends Thread {

    private static final int BATCH_SIZE = 100;
    private static final int RATE_DELETE = 5;
    private static final int RATE_UPDATE = 2;
    private static final int AGE = 3600;
    private static final String DBDIR = "hsqldbat";
    private static final String DBNAME = "hsqldbat";

    private static DelayInfoDAO dao = null;
    private static PrintWriter rep = null;
    private static long stopTime = 0;
    private static int nThreads = 1;
    private static Random rand = new Random();
    private static int checkedThreads = 0;
    private static int totalAdd = 0;
    private static int totalDelete = 0;
    private static int totalUpdate = 0;
    private static int lastCount = 0;
    private static long lastTime = 0;
    private static Object checkLock = new Object();

    private short threadId;
    private int checkedDi = 0;
    private int liveCount = 0;
    private HashSet exist = new HashSet();
    private int add = 0;
    private int del = 0;
    private int upd = 0;
    private int waitDelete = 0;
    private int waitUpdate = 0;
    private PrintWriter trlog;

    public HsqldbAcceptanceTest(short id) throws IOException {
        super("Test-" + id);
        threadId = id;
        initTransLog();
    }

    public void run() {
        report("Starting " + threadId);
        while (System.currentTimeMillis() < stopTime) {
            check();
            testBatch(rand.nextInt(BATCH_SIZE));
        }
        report("Stopping " + threadId);
    }

    private void initTransLog() throws IOException {
        if (isContinue()) {
            int tadd = 0;
            int tdel = 0;
            int tupd = 0;
            
            String line;
            RandomAccessFile f = new RandomAccessFile(DBDIR + "/testlog-" + threadId, "r");
            f.seek(Math.max(0, f.length() - 1000));
            while ((line = f.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                try {
                    tadd = Integer.parseInt(st.nextToken());
                    tdel = Integer.parseInt(st.nextToken());
                    tupd = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException e) {
                    ;
                } catch (NumberFormatException e) {
                    ;
                }
            }
            add = tadd;
            del = tdel;
            upd = tupd;
            f.close();
        }
        trlog = new PrintWriter(new FileOutputStream(DBDIR + "/testlog-" + threadId), true);
        report("Starting with " + add + " " + del + " " + upd);
    }

    private void log() {
        trlog.println(add + " " + del + " " + upd);
    }

    private void testBatch(int n) {
        try {
            for (int i = 0; i < n; i++) {
                addDi();
                //                report("Count " + nextDi + " " + waitDelete);
                ++waitDelete;
                ++waitUpdate;
                if (waitDelete == RATE_DELETE) {
                    waitDelete = 0;
                    deleteDi();
                }
                if (waitUpdate == RATE_UPDATE) {
                    waitUpdate = 0;
                    updateDi();
                }

                try {Thread.sleep((long) (rand.nextInt(10))); } catch (InterruptedException e) { ; }
            }
            //            report("Created " + add + ", deleted " + del + ", updated " + upd);
        } catch (DelayException e) {
            report("ERROR: " + e);
        }
    }

    private static int getCount() {
        Connection conn = dao.getConnectionForTestingOnly();
        int count = -1;
        
        while (dao.isBusy()) {
            try { Thread.sleep(100); } catch (InterruptedException e) { ; }
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT count(*) FROM event";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                report("No count result");
            }
        } catch (SQLException sqle) {
            report("Count failed:" + sqle);
        } finally {
            try {
                if (ps!=null) ps.close();
            } catch (SQLException statementExc) {
                ;
            }
            try {
                if (rs != null) rs.close();
            } catch (SQLException rsExc) {
                ;
            }
        }
        return count;
    }

    private static void check(int a, int d, int u) {
        int count;
        long now = System.currentTimeMillis();

        synchronized (checkLock) {
            ++checkedThreads;
            totalAdd += a;
            totalDelete += d;
            totalUpdate += u;
            
            if (checkedThreads == nThreads) {
                //count = getCount();
                count = totalAdd - totalDelete;
                if (count < 0) {
                    report("CHECK UNKNOWN:could not count");
                } else {
                    int r = (int) ((1000 * count - 1000 * lastCount) / (now - lastTime));
                    lastCount = count;
                    lastTime = now;
                    if (totalAdd - totalDelete == count) {
                        report("CHECK OK:" + count + " " + r);
                    } else {
                        report("CHECK ERROR:expected " + (totalAdd - totalDelete) + ", was " + count + " " + r);
                    }
                }
                totalAdd = 0;
                totalDelete = 0;
                totalUpdate = 0;
                checkedThreads = 0;
                dao.allowCleaning();
                do {
                    try { sleep(1000); } catch (InterruptedException e) { ; }
                } while (dao.isBusy());
                checkLock.notifyAll();
            } else {
                try { checkLock.wait(); } catch (InterruptedException e) { ; }
            }
        }
    }
  
    private void check() {
        Connection conn = dao.getConnectionForTestingOnly();
        int count = 0;

        while (dao.isBusy()) {
            try { Thread.sleep(100); } catch (InterruptedException e) { ; }
        }
/*
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT count(*) FROM event";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                report("No count result");
            }
        } catch (SQLException sqle) {
            report("Count failed:" + sqle);
        } finally {
            try {
                if (ps!=null) ps.close();
            } catch (SQLException statementExc) {
                ;
            }
            try {
                if (rs != null) rs.close();
            } catch (SQLException rsExc) {
                ;
            }
        }
*/
        check(add, del, upd);
    }

    private synchronized void addDi () throws DelayException {
        //DelayInfo di = new DelayInfo("" + add, threadId, "DELI " + add, null);
                DelayInfo di = new DelayInfo("" + add, threadId, "DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI DELI " + add, null);
        di.setWantTime(newWantTime());
        dao.create(di);
                exist.add(di);
        ++add;
        log();
    }

    private DelayInfo getSomeDi() {
        if (exist.size() == 0) {
            return null;
        }
        Object o = (exist.toArray())[rand.nextInt(exist.size() - 1)];
        return (DelayInfo) o;
    }

    private synchronized void deleteDi() throws DelayException {
        DelayInfo di = getSomeDi();
        if (di == null) { return; }

        dao.remove(di.getKey(), di.getType());
        exist.remove(di);
        ++del;
        log();
    }

    private synchronized void updateDi() throws DelayException {
        DelayInfo di = getSomeDi();
        if (di == null) { return; }

        di.setStrInfo("" + add);
        dao.update(di);
        ++upd;
        log();
    }

    private long newWantTime() {
        long t = System.currentTimeMillis() + 20000L + (long) (rand.nextInt(10000));
        return (t / 1000L) * 1000L;
    }

    private static String timestamp() {
        String s = "" + System.currentTimeMillis();
        return s.substring(0, s.length() - 6) + "."
            + s.substring(s.length() - 6, s.length() - 3) + "."
            + s.substring(s.length() - 3);
    }        

    private static void report(String s) {
        rep.println(Thread.currentThread().getName() + "/\t" + timestamp() + "/" + s);
    }

    private static void errExit(String s) {
        report(s);
        System.err.println(s);
        System.exit(1);
    }

    private static boolean isContinue() {
        return (System.getProperty("cont") != null);
    }
    
    private static void initReport() throws IOException {
        rep = new PrintWriter(new FileOutputStream("HsqldbAcceptanceTest.report", isContinue()), true);
        if (isContinue()) {
            report("CONTINUE after break");
        } else {
            report("ACCEPTANCE TEST OF HSQLDB");
            report("Test date:         "
                   + new SimpleDateFormat("yyyy-MM-dd  HH:mm").format(new Date()));
            report("Tested product:    "
                   + org.hsqldb.jdbc.jdbcUtil.PRODUCT + " " + org.hsqldb.jdbc.jdbcUtil.VERSION);
            StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), ":");
            String s;
            while (st.hasMoreTokens()) {
                s = st.nextToken();
                if (s.indexOf("hsqldb") >= 0) {
                    try {
                        report("Tested jar file:   "
                               + new File(s).getCanonicalPath());
                    } catch (IOException e) {
                        ;
                    }
                }
            }
        }
        rep.println("======================================================================");
    }

    private static void handleArgs(String[] args) {
        long duration = 10L;
        if (args.length > 0) {
            String a = args[0];
            try {
                if (a.endsWith("h")) {
                    duration = 3600L * Integer.parseInt(a.substring(0, a.length() -1));
                } else if (a.endsWith("m")) {
                    duration = 60L * Integer.parseInt(a.substring(0, a.length() -1));
                } else {
                    if (a.endsWith("s")) {
                        a = a.substring(0, a.length() -1);
                    }
                    duration = Integer.parseInt(a.substring(0, a.length()));
                }
            } catch (NumberFormatException e) {
                errExit("Bad test duration " + a + ": " + e);
            }
            if (args.length > 1) {
                try {
                    nThreads = Short.parseShort(args[1]);
                } catch (NumberFormatException e) {
                    errExit("Bad thread count " + args[1] + ": " + e);
                }
            }
        }
        stopTime = System.currentTimeMillis() + duration * 1000L;
    }

    private static void initDb() throws IOException, DelayException {
        if (!isContinue()) {
            File f = new File(DBDIR);
            if (f.exists()) {
                if (f.isDirectory()) {
                    File[] fa = f.listFiles();
                    for (int i = 0; i < fa.length; i++) {
                        fa[i].delete();
                    }
                } else {
                    errExit(DBDIR + " is not a directory, can not continue");
                }
            } else {
                f.mkdirs();
            }
        }
        dao = new DelayInfoDAO(DBDIR, DBNAME);
    }

    public static void main(String[] args) throws Exception {
        initReport();
        handleArgs(args);
        initDb();
        //        SDLogger.setLogger(new TestLogger());

        for (short i = 0; i < nThreads; i++) {
            new HsqldbAcceptanceTest(i).start();
            try { Thread.sleep(1000); } catch (InterruptedException e) { ; }
        }
    }

    private static class TestLogger extends SDLogger {
        
        protected void doLog(int level, String message) {
            //            System.out.println("[" + LEVELS[level] + "] " + message);
        }

        protected void doLog(int level, String message, Throwable t) {
            //            System.out.println("[" + LEVELS[level] + "] " + message + " : Exc:" +
            //                               t.getMessage());
            //            t.printStackTrace();
        }

        protected void doLogObject(int level, String message, Object obj) {
            //            System.out.println("[" + LEVELS[level] + "] " + message +
            //                               ":" + obj.toString());
        }
    }
}
