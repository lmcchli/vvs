/*
  BUILD: 
  javac -classpath /vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf/bin/je.jar:/vobs/ipms/ntf/bin/hsqldb.jar DbPerf.java

  RUN:
  java -mx256m -classpath .:/vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf/bin/je.jar:/vobs/ipms/ntf/bin/hsqldb.jar DbPerf 1h 5
*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import com.sleepycat.je.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DbPerf tests database performance.
 * The current version only tests write of small objects.
 * 
 * Usage: java <i>runoptions</i> [-D<i>dbselection</i>] DbPerf [<i>time</i>[ <i>threads</i>[ <i>rate</i>]]]
 * where <i>runoptions</i> is -mx256m -classpath .:/vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf/bin/je.jar:/vobs/ipms/ntf/bin/hsqldb.jar
 * <i>dbselection</i> is h for hsqldb or b (default) for berkeley db
 * <i>time</i> is the duration of the test (default 10s) in seconds (e.g. 5 or 5s), minutes (e.g. 5m) or hours (e.g. 5h)
 * <i>threads</i> number of parallell test threads (default 1).
 * <i>rate</i> is the target rate in writes per second. This can be used to
 * measure the load on the machine for a certain database activity.
 */
public class DbPerf extends Thread {

    private static final int BATCH_SIZE = 100; //Number of writes before checking
    private static final String DBDIR = "dbperf";
    private static final String DBNAME = "dbperf";

    private static int targetRate = 10000;
    private static PrintWriter rep = null;
    private static long stopTime = 0; //When the test ends
    private static int nThreads = 1;
    private static int checkedThreads = 0; //How many threads have reported to check
    private static int totalAdd = 0;
    private static int lastCount = 0;
    private static long lastTime = 0;
    private static Object checkLock = new Object();
    private static Db db;
    private static int sleeptime = 1; //Used to control rate

    private short threadId;
    private int add = 0;

    public DbPerf(short id) throws IOException {
        super("Test-" + id);
        threadId = id;
    }

    public void run() {
        try {
            System.out.println("    Starting " + threadId);
            while (System.currentTimeMillis() < stopTime) {
                check();
                for (int i = 0; i < BATCH_SIZE; i++) {
                    String key = "" + threadId + "/" + add;
                    String data = "" + lastTime + "/" + "DELI " + key;
                    //                    System.out.println(key +  "#" + data);
                    db.addEntry(key.getBytes("UTF-8"),
                                data.getBytes("UTF-8"));
                    add++;
                    if (sleeptime > 1) { sleep(sleeptime); }
                }
            }
            System.out.println("    Stopping " + threadId);
        } catch (Exception e) {
            errExit("Error:" + e);
        }
    }

    /**
     * Threads call this to report result. The first threads hang and wait for
     * the last thread that writes the report and releases the other threads.
     */
    private static void check(int a) throws Exception {
        long now = System.currentTimeMillis();

        synchronized (checkLock) {
            ++checkedThreads;
            totalAdd += a;

            if (checkedThreads == nThreads) {
                int r = (int) ((1000 * totalAdd - 1000 * lastCount) / (now - lastTime));
                if ( r > 0) { sleeptime = r * sleeptime / targetRate; }
                if (sleeptime < 1) { sleeptime = 1;}
                if (sleeptime > 1000) { sleeptime = 1000; }
                lastCount = totalAdd;
                lastTime = now;
                report("CHECK OK:" + totalAdd + " " + r + " " + sleeptime);
                totalAdd = 0;
                checkedThreads = 0;
                db.compact();
                checkLock.notifyAll();
            } else {
                try { checkLock.wait(); } catch (InterruptedException e) { ; }
            }
        }
    }
  
    /**
     * Report results to the static check.
     */
    private void check() throws Exception {
        check(add);
    }

    /**
     * Generate time stamps for logs.
     */
    private static String timestamp() {
        String s = "" + System.currentTimeMillis();
        return s.substring(0, s.length() - 6) + "."
            + s.substring(s.length() - 6, s.length() - 3) + "."
            + s.substring(s.length() - 3);
    }        

    /**
     * Write a message in the report file.
     */
    private static void report(String s) {
        rep.println(Thread.currentThread().getName() + "/\t" + timestamp() + "/" + s);
    }

    /**
     * Write an error message and exit.
     */
    private static void errExit(String s) {
        report(s);
        System.err.println(s);
        System.exit(1);
    }

    /**
     * Create and initialize the report file.
     */
    private static void initReport() throws IOException {
        System.out.println("Initializing report");
        rep = new PrintWriter(new FileOutputStream("DbPerf.report"), true);
        report("DATABASE PERFORMANCE TEST");
        report("Test date:         "
               + new SimpleDateFormat("yyyy-MM-dd  HH:mm").format(new Date()));
        rep.println("======================================================================");
    }
    
    /**
     * Parse command line arguments.
     */
    private static void handleArgs(String[] args) throws Exception {
        System.out.println("Processing args");
        long duration = 10L;
        if (args.length > 0) {
            String a = args[0];
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
            
            if (args.length > 1) {
                nThreads = Short.parseShort(args[1]);
            }
            if (args.length > 2) {
                targetRate = Integer.parseInt(args[2]);
            }
        }
        report("Running " + nThreads + " threads for " + duration + " seconds with " + targetRate + " writes per second.");
        stopTime = System.currentTimeMillis() + duration * 1000L;
    }

    /**
     * Check of the database to use is hsqldb or Berkeley db
     */
    private static boolean isHsqldb() {
        return (System.getProperty("h") != null);
    }
    
    /**
     * Initialize the database.
     */
    private void init() throws Exception {
        if (isHsqldb()) {
            db = new HDb();
        } else {
            db = new BDb();
        }
        System.out.println("Initializing " + (isHsqldb() ? "HSQL" : "Berkeley") + " database");
        db.init();
    }

    public static void main(String[] args) {
        try {
            initReport();
            handleArgs(args);
            System.out.println("Starting write threads");
            for (short i = 0; i < nThreads; i++) {
                DbPerf p = new DbPerf(i);
                if (i == 0) {
                    p.init();
                }
                p.start();
                try { Thread.sleep(200); } catch (InterruptedException e) { ; }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
    
    /**
     * Generic base class hiding the type of database.
     */
    private static class Db {
        /**
         * Initialize the database.
         */
        public void init() throws Exception {
            System.out.println("Running super init");
        }

        /**
         * Empty or create the database directory.
         */
        protected void initDir() {
            System.out.println("Emptying data directory");
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

        /**
         * Compact the database in some way, througha a checkpoint or so.
         */
        public void compact() throws Exception { ; }

        /**
         * Add an entry to the database.
         */
        public void addEntry(byte[] key, byte[] data) throws Exception { ; }
    }

    /**
     * Db extension for the HSQL database.
     */
    private static class HDb extends Db {
        static Connection conn;

        public void init() throws Exception {
            initDir();

            String url = null;
            
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
            url = "jdbc:hsqldb:file:" + DBDIR + "/" + DBNAME;
            System.out.println("Connecting to DB: " + url);
            conn = DriverManager.getConnection(url, "sa","");
            
            PreparedStatement ps1 = null;
            PreparedStatement ps2 = null;
            PreparedStatement ps3 = null;
            PreparedStatement ps4 = null;
            
            String sqlCreate =
                "CREATE CACHED TABLE admin ( adminkey VARCHAR, " +
                "admininfo VARCHAR, PRIMARY KEY (adminkey) ) ";
            ps1 = conn.prepareStatement(sqlCreate);
            ps1.executeUpdate();
            
            String sqlInsert =
                "INSERT INTO admin (adminkey,admininfo) values (?,?)";
            ps2 = conn.prepareStatement(sqlInsert);
            ps2.setString(1, "1");
            ps2.setString(2, "Ett");
            ps2.executeUpdate();
            
            String sqlCreateTable =
                "CREATE CACHED TABLE event " +
                "(key LONGVARBINARY, data LONGVARBINARY, " +
                "PRIMARY KEY (key))";
            
            ps3 = conn.prepareStatement(sqlCreateTable);
            ps3.executeUpdate();
            
            ps1.close();
            ps2.close();
            ps3.close();
        }
        
        public void compact() throws Exception {
            PreparedStatement ps = null;
            
            String sql = "CHECKPOINT DEFRAG";
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        }

        public void addEntry(byte[] key, byte[] data) throws Exception {
            PreparedStatement ps = null;
            
            String createSQL =
                "INSERT INTO event " +
                "(key, data) " +
                "VALUES (?,?) ";
            ps = conn.prepareStatement(createSQL);
            ps.setBytes(1,  key);
            ps.setBytes(1,  data);
            ps.executeUpdate();
            
            if (ps!=null) ps.close();
        }
    }

    /**
     * Db extension for the Berkeley database.
     */
    private class BDb extends Db {
        Environment env;
        Database conn;

        public void init() throws Exception {
            initDir();

            System.out.println("Opening environment");
            EnvironmentConfig cfg = new EnvironmentConfig();
            cfg.setAllowCreate(true);
            cfg.setTransactional(true);
            cfg.setCacheSize(50000000L);
            env = new Environment(new File(DBDIR), cfg);
            
            System.out.println("Opening database");
            DatabaseConfig dbcfg = new DatabaseConfig();
            dbcfg.setAllowCreate(true);
            conn = env.openDatabase(null, DBNAME, dbcfg);
        }

        public void compact() throws Exception {
            env.checkpoint(null);
        }

        public void addEntry(byte[] key, byte[] data) throws Exception {
            DatabaseEntry k = new DatabaseEntry(key);
            DatabaseEntry d = new DatabaseEntry(data);
            conn.put(null, k, d);
        }
    }
}
