/*
 * DelayInfoDAO.java
 *
 * Created on den 25 augusti 2004, 17:05
 */

package com.mobeon.common.storedelay;

import com.mobeon.ntf.util.NtfUtil;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class handles the storage aspects for DelayInfo objects.
 * All persistence handling is in this class so it should be enough
 * to change its implementation to get a new storeage handling.
 * The storage operations are geared towards JDBC/SQL but another
 * implementation may be able to use another background storage. <br />
 * The implementation in this class uses JDBC towards the HSQLDB database.
 * Objects of the class knows when there is a cleanup in the database
 * and DB operations during that time will throw a DelayCleaningException,
 * SQL exceptions will be reported nested in a DelaySQLException.
 * <br/>
 * TODO: Should this be divided into interface+implementation?
 * <p>
 * <b>Note: </b> Future versions of this class must handle upgrading
 * of database schemas.
 *
 */
public class DelayInfoDAO {

    /** Max wantedtime for DelayInfo to be seen as not timebased.
     * Slight hack to allow timebased and not time based notifications
     * to be kept in the same table. To avoid clustering at wanttime==0
     * we allow the non time based a range of times.
     * Infos with time less than or equal to this will not be removed
     * by removeOldInfos
     */
    /* package */ static final int MAX_MS_FOR_NOTIFICATIONS = 100000;

    // ---------------------------------------------
    // Private Data
    // ---------------------------------------------
    private Connection conn;
    /** Number of rows changed (inserted,removed,modified) since last checkpoint */
    private int changedRowCount = 0;
    /** Number of compactings done without defragmentation */
    private int compactCount     = 0;
    /** Do a checkpoints when more rows than this is affected */
    private int checkPointLimit = 5000;
    /** Defragment after this many checkpoints */
    private int defragmentLimit = 3;
    /** Thread that does checkpoint/defragmentation */
    private Thread compactThread = null;



    /**
     * Creates a new instance of DelayInfoDAO
     * Connects to the database using properties from props and initializes
     * the tables in the database if needed.
     * Only one DelayInfoDAO object should exists towards each used database,
     * several databases may be used if there is a need for load balancing
     * between disks.
     * @see DBDelayHandler for keys of the properties.
     * @param props Properties telling database location and name.
     */
    public DelayInfoDAO(String storageDir, String storageBase)
        throws DelayDBException
    {
        connectToDB(storageDir, storageBase);
        if (!existsDB()){
            makeTables();
        }
        setProperties();
        compactData(true);
        saveScriptFile(storageDir, storageBase);
    }


    /* package */ void compactData(boolean defrag)
    throws DelayDBException {
        PreparedStatement ps = null;
        try {
            String sql = "CHECKPOINT";
            if (defrag) sql += " DEFRAG";
            SDLogger.log(SDLogger.TRACE, sql);
            Calendar start = Calendar.getInstance();
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();

            if (SDLogger.willLog(SDLogger.INFO)) {
                Calendar end = Calendar.getInstance();
                long updateMS = end.getTime().getTime() - start.getTime().getTime();
                long updateSec = updateMS / 1000;
                updateMS = updateMS % 1000;
                String text = "Cleaned DB in " + updateSec + " seconds " +
                              updateMS + " millisec ";
                if (defrag) text+= " DEFRAG=TRUE";
                SDLogger.log(SDLogger.INFO, text);
            }


        } catch (SQLException sqle) {
            throw new DelayDBException("Could not compact db", sqle);
        } finally {
            cleanUp(ps, null);
        }
    }
    /**
     * Release handled resources, call this before quitting.
     * Waits if a cleaning is done then close db connection.
     */
    public void close() {
        try {
            // TODO: Wait for cleaning done
            if (conn != null) {
                SDLogger.log(SDLogger.DEBUG, "DAO:Gong to Close connection");
                conn.close();
                SDLogger.log(SDLogger.DEBUG, "DAO:Connection closed");
            }
        } catch (SQLException se) {
            SDLogger.log(SDLogger.WARNING,
            "Could not close db connection ",se);
        }
    }

    /**
     * Ensure driver is loaded, then get a Connection
     */
    private void connectToDB(String storageDirStr, String storageBase)
    throws DelayDBException {
        // TODO: Configure driverclass ??
        String url = null;
        try {
            // take backup of files
            takeBackup(storageDirStr, storageBase);
            restoreNeededFiles(storageDirStr, storageBase);
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
            url = "jdbc:hsqldb:file:" + storageDirStr + "/" + storageBase;
            SDLogger.log(SDLogger.INFO, "Connecting to DB: " + url);
            conn = DriverManager.getConnection(url, "sa","");

            // TODO: Set logzize to 0, i.e no autocompress
        } catch (ClassNotFoundException cnf) {
            SDLogger.log(SDLogger.FATAL, "Could not load JDBC driver", cnf);
            throw new DelayDBException("Could not load JDBC driver", cnf);
        } catch (InstantiationException ie) {
            SDLogger.log(SDLogger.FATAL, "Could not create JDBC driver", ie);
            throw new DelayDBException("Could not create JDBC driver", ie);
        } catch (IllegalAccessException iae) {
            SDLogger.log(SDLogger.FATAL, "Not allowed to create JDBC driver", iae);
            throw new DelayDBException("Not allowed to create JDBC driver", iae);
        } catch (SQLException sqle) {
            SDLogger.log(SDLogger.FATAL, "Could not get connection", sqle);
            // Try to determine why
            File storageDir = new File(storageDirStr);
            if (!storageDir.exists()) {
                throw new DelayDBException("Directory for DB " + storageDirStr +
                                           "does not exist, try creating", sqle);
            }
            if (!storageDir.isDirectory()) {
                throw new DelayDBException("DB location " + storageDirStr +
                                           "must be a directory", sqle);
            }
            if (!storageDir.canWrite()) {
                throw new DelayDBException("DB location " + storageDirStr +
                                           "must be writable" , sqle);
            }
            // Did we have a lockfile?
            File lockFile = new File(storageDir, storageBase + ".lck");
            if (lockFile.exists()) {
                try {
                    SDLogger.log(SDLogger.INFO,
                                 "Old lockfile exists, attempts to remove it");
                    // Remove and retry
                    lockFile.delete();
                    conn = DriverManager.getConnection(url, "sa", "");
                    return;
                } catch (Exception e) {
                   SDLogger.log(SDLogger.INFO, "Failure with removed lockfile", e);
                   
                }
            }
            // replace script file and backupdata
            // try once more to get connection.
            try {
                SDLogger.log(SDLogger.INFO, "trying to restore backup and connect again");
                restoreBackup(storageDirStr, storageBase);
                lockFile.delete();
                //System.exit(1);
                conn = DriverManager.getConnection(url, "sa", "");
                return;
            } catch (Exception e) {
                SDLogger.log(SDLogger.ERROR, "Failure to start with restored database", e);
                
                
            }
            
            // Still here, no lockfile or removing did not help
            // Try move old db to a safe location
            Calendar now = Calendar.getInstance();
            String saveDirStr = "save." + now.getTime().getTime(); // Time in millis
            String[] suffixes = { ".data", ".data.new", ".data.old", ".backup", ".log", ".script", ".properties" };
            try {
                File saveDir = new File(storageDir, saveDirStr);
                File f;
                if (saveDir.mkdir()) {
                    SDLogger.log(SDLogger.ERROR, "Saving old DB in " + saveDir);
                    for (int i = 0; i < suffixes.length; i++) {
                        f = new File(storageDir, storageBase + suffixes[i]);
                        if (f.exists()) {
                            f.renameTo(new File(saveDir, storageBase + suffixes[i]));
                            
                        }
                    }
                } else {
                    // Just delete old DB
                    SDLogger.log(SDLogger.ERROR, "Deleting old DB to start, LOSING DATA");
                    for (int i = 0; i < suffixes.length; i++) {
                        f = new File(storageDir, storageBase + suffixes[i]);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                }

                conn = DriverManager.getConnection(url, "sa", "");
                return;
            } catch (Exception e) {
               SDLogger.log(SDLogger.ERROR,
                           "Failed to move/remove old DB and then get conn",e);
            }
            throw new DelayDBException("Could not get connection", sqle);
        }
    }

    /**
     * Safe close of Statement and Resultset
     */
    private void cleanUp(Statement s, ResultSet rs) {
        try {
            if (s!=null) s.close();
        } catch (SQLException statementExc) {
            SDLogger.log(SDLogger.WARNING,"Failed to close statement", statementExc);
        }
        try {
            if (rs != null) rs.close();
        } catch (SQLException rsExc) {
            SDLogger.log(SDLogger.WARNING,"Failed to close resultset", rsExc);
        }

    }

    /**
     *Copies files from datadirectory to a backupdirectory.
     */
    private void takeBackup(String fromDir, String storageBase) {
        File storageDir = new File(fromDir);
        File backupDir = new File(storageDir, ".backup");
        if( backupDir.exists() && !backupDir.isDirectory() ) {
            boolean result = backupDir.delete();
            if( !result ) {
                SDLogger.log(SDLogger.ERROR, "Failed to remove file " + backupDir.toString() +
                    " when taking backup");
            }
        }
        if( !backupDir.exists() ) {
            boolean result = backupDir.mkdir();
            if( !result ) {
                SDLogger.log(SDLogger.ERROR, "Failed to create directory " + backupDir.toString() +
                    " when taking backup");
            }
        }
        
        String[] suffixes = { ".data", ".data.new", ".data.old", ".backup", ".log", ".properties" };
        for( int i=0;i<suffixes.length;i++ ) {
            File file = new File(storageDir, storageBase + suffixes[i]);
            if( file.exists() ) {
                File targetFile = new File(backupDir, storageBase + suffixes[i]);
                copyFile(file, targetFile);
            }
        }
    }
    
    /**
     *creates a backup file of the script file to be used in case of failures. 
     */
    private void saveScriptFile(String sourceDir, String storageBase) {
        File scriptFile = new File(sourceDir + "/" + storageBase + ".script");
        if( !scriptFile.exists() ) {
            SDLogger.log(SDLogger.ERROR, scriptFile.toString() + " not found. Failed to save script file");
            return;
        }
        File targetDir = new File(sourceDir + "/.backup");
        if( targetDir.exists() && !targetDir.isDirectory() ) {
            boolean result = targetDir.delete();
            if( !result ) {
                SDLogger.log(SDLogger.ERROR, targetDir.toString() + " exists and is not a directory. Failed to remove file. Script file is not saved");
                return;
            }
        }
        if( !targetDir.exists() ) {
            boolean result = targetDir.mkdir();
            if( !result ) {
                SDLogger.log(SDLogger.ERROR, "Failed to create " + targetDir.toString() + ", failed to save script file");
                return;
            }
        }
        File targetFile = new File(targetDir, storageBase + ".script");
        copyFile(scriptFile, targetFile);
    }
    
    private void restoreNeededFiles(String storageDir, String storageBase) {
        File scriptFile = new File(storageDir + "/.backup/" + storageBase + ".script");
        if( scriptFile.exists() ) {
            File targetFile = new File(storageDir + "/" + storageBase + ".script");
            if( !targetFile.exists() ) {
                copyFile(scriptFile, targetFile);
            }
            else {
                boolean found = false;
                // look for create SA in file
                try {
                BufferedReader reader = new BufferedReader(new FileReader(targetFile));
                String line = null;
                while( (line = reader.readLine()) != null ) {
                    if( line.indexOf("CREATE USER SA PASSWORD" ) != -1 ) {
                        found = true;
                        break;
                    }
                }
                reader.close();
                } catch(FileNotFoundException fnfe) {
                    SDLogger.log(SDLogger.ERROR,"Failed to read file " + targetFile.toString(), fnfe);
                } catch(IOException ioe) {
                    SDLogger.log(SDLogger.ERROR,"Failed to read file " + targetFile.toString(), ioe);
                }
                if( !found ) {
                    copyFile(scriptFile, targetFile);
                }
            }
        }
        File propertiesFile = new File(storageDir + "/.backup/" + storageBase + ".properties");
        if( propertiesFile.exists() ) {
            File targetFile = new File(storageDir + "/" + storageBase + ".properties");
            if( !targetFile.exists() ) {
                copyFile(propertiesFile, targetFile);
            }
        }
    }
    
    /**
     *Copies data from backup and script directory to storageDir
     */
    private void restoreBackup(String storageDir, String storageBase) {
        File backupDir = new File(storageDir + "/.backup");
        File orgDir = new File(storageDir);
        if( backupDir.exists() && orgDir.exists() ) {
            String[] suffixes = { ".data", ".data.new", ".data.old", ".backup", ".log", ".properties", ".script" };
            for( int i=0;i<suffixes.length;i++ ) {
                File backupFile = new File(backupDir, storageBase + suffixes[i]);
                File targetFile = new File(orgDir, storageBase + suffixes[i]);
                if( backupFile.exists() ) {
                    copyFile(backupFile, targetFile);
                }
            }
        }
    }
    
    private void copyFile(File fromFile, File targetFile) {
        try {
            if( targetFile.exists() ) {
                boolean result = targetFile.delete();
            }
            FileInputStream fin = new FileInputStream(fromFile);
            FileOutputStream fout = new FileOutputStream(targetFile);
            byte [] buf = new byte[8096];
            int read;
            while ((read = fin.read(buf)) > 0) {
                fout.write(buf, 0, read);
            }
            fin.close();
            fout.close();
            SDLogger.log(SDLogger.DEBUG, "Copied file " + fromFile + " to " + targetFile);
        } catch(FileNotFoundException fnfe) {
            SDLogger.log(SDLogger.ERROR,"Failed to copy file " + fromFile.toString(), fnfe);
        } catch(IOException ioe) {
            SDLogger.log(SDLogger.ERROR,"Failed to copy file " + fromFile.toString(), ioe);
        }
    }
    
    private static final String VERSION_KEY = "version";
    private static final String VERSION_STRING = "1.0";

    /**
     * Check if database already exists.
     * We see it as existing if we can find the correct version.
     */
    private boolean existsDB() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT admininfo from ADMIN where adminkey=? ";
            ps = conn.prepareStatement(sql);
            ps.setString(1, VERSION_KEY);
            rs = ps.executeQuery();
            if (!rs.next()) {
                SDLogger.log(SDLogger.DEBUG,"version not found, assuming new DB");
                return false;
            } else {
                String foundVersion = rs.getString("admininfo");
                SDLogger.log(SDLogger.DEBUG, "Got DB version " + foundVersion);
                // For now, any version string is ok
                return true;
            }
        } catch (SQLException findVersionException) {
            SDLogger.log(SDLogger.DEBUG,
            "Could not read from ADMIN table, assuming new DB");
            return false;
        } finally {
            cleanUp(ps,rs);
        }
    }


    /**
     * Create needed tables if they are not already there
     */
    private void makeTables()
    throws DelayDBException {
        try {
            makeAdminTable();
            makeEventsTable();
        } catch (SQLException se) {
            SDLogger.log(SDLogger.FATAL, "Delay: Failed to make tables ",se);
            throw new DelayDBException("Could not make tables", se);
        }
    }

    /**
     * Create and fill in the admin table
     */
    private void makeAdminTable()
    throws SQLException {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            String sqlCreate =
            "CREATE CACHED TABLE admin ( adminkey VARCHAR, " +
            "admininfo VARCHAR, PRIMARY KEY (adminkey) ) ";
            SDLogger.log(SDLogger.TRACE, sqlCreate);
            ps1 = conn.prepareStatement(sqlCreate);
            ps1.executeUpdate();

            String sqlInsert =
            "INSERT INTO admin (adminkey,admininfo) values (?,?)";
            SDLogger.log(SDLogger.TRACE, sqlInsert);
            ps2 = conn.prepareStatement(sqlInsert);
            ps2.setString(1, VERSION_KEY);
            ps2.setString(2, VERSION_STRING);
            ps2.executeUpdate();
        } finally {
            cleanUp(ps1, null);
            cleanUp(ps2, null);
        }
    }

    /**
     * Create the events table with indexes.
     */
    private void makeEventsTable()
    throws SQLException {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            String sqlCreateTable =
            "CREATE CACHED TABLE event " +
            "(keydata VARCHAR, keytype SMALLINT, wanttime BIGINT, " +
            " strinfo LONGVARCHAR, byteinfo LONGVARBINARY, " +
            "PRIMARY KEY (keydata, keytype))";
            String sqlCreateTimeIndex =
            "CREATE INDEX timeindex ON event(wanttime)";

            SDLogger.log(SDLogger.TRACE, sqlCreateTable);
            SDLogger.log(SDLogger.TRACE, sqlCreateTimeIndex);
            ps1 = conn.prepareStatement(sqlCreateTable);
            ps1.executeUpdate();

            ps2 = conn.prepareStatement(sqlCreateTimeIndex);
            ps2.executeUpdate();
        } finally {
            cleanUp(ps1, null);
            cleanUp(ps2, null);
        }
    }

    /**
     * Set database properties.
     */
    private void setProperties() throws DelayDBException {
        try {
            // Limit the database to 24000 rows and 24-48 MB
            // (3*8192 rows, rows*512*(2 to 4) bytes) and
            // Limit the log file to 10 MB.
            String[] sql = {
                "SET PROPERTY \"hsqldb.cache_scale\" 13",
                "SET PROPERTY \"hsqldb.cache_size_scale\" 9",
                "SET LOGSIZE 10",
            };
            PreparedStatement ps = null;
            for (int i = 0; i < sql.length; i++) {
                try {
                    SDLogger.log(SDLogger.TRACE, sql[i]);
                    ps = conn.prepareStatement(sql[i]);
                    ps.executeUpdate();
                } finally {
                    cleanUp(ps, null);
                }
            }
        } catch (SQLException se) {
            SDLogger.log(SDLogger.FATAL, "Delay: Failed to set properties ",se);
            throw new DelayDBException("Could not set properties", se);
        }
    }
        
    /**
     * Store a DelayInfo persistently.
     * The info must not already be stored.
     * @param newInfo Data to be stored, key and type must be set.
     * @throws DelaySQLException if there are problem with DB handling.
     * @throws DelayCleaningException if database currently unavailable
     */
    public void create(DelayInfo newInfo)
    throws DelayException {
        if (isBusy()) {
            throw new DelayCleaningException("Cannot create when cleaning DB");
        }
        PreparedStatement ps = null;
        try {
            String createSQL =
            "INSERT INTO event " +
            "(keydata,keytype,wanttime,strinfo,byteinfo) " +
            "VALUES (?,?,?,?,?) ";
            SDLogger.logObject(SDLogger.DEBUG, "DB-Creating", newInfo);
            ps = conn.prepareStatement(createSQL);
            ps.setString(1, newInfo.getKey());
            ps.setShort(2,  newInfo.getType());
            ps.setLong(3,   newInfo.getWantTime());
            ps.setString(4, newInfo.getStrInfo());
            ps.setBytes(5,  newInfo.getByteInfo());
            ps.executeUpdate();
            changedRowCount++;
        } catch (SQLException sqle) {
            SDLogger.logObject(SDLogger.DEBUG,
                               "DelayInfoDAO-Could not create",newInfo);
            throw new DelayDBException("Could not create ", sqle);
        } finally {
            cleanUp(ps, null);
        }

    }


    /**
     * Find the DelayInfo for the given key and type.
     * @param key The key to search for.
     * @param type The type of info we are looking for.
     * @return The found DelayInfo or null if no matching info was found.
     * @throws DelayDBException if there are problem with DB handling
     * @throws DelayCleaningException if database is currently unavailable.
     */
    public DelayInfo find(String key, short type)
    throws DelayException {
        /*
        if (isBusy()) {
            throw new DelayCleaningException("Cannot create when cleaning DB");
        }
        */
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String findSQL =
            "SELECT keydata,keytype,wanttime,strinfo,byteinfo " +
            "FROM event WHERE keydata=? and keytype=? ";
            if (SDLogger.willLog(SDLogger.DEBUG)) {
                SDLogger.log(SDLogger.DEBUG, "DB-Finding " + key + "/" + type);
            }
            ps = conn.prepareStatement(findSQL);
            ps.setString(1, key);
            ps.setShort(2,  type);
            rs = ps.executeQuery();
            DelayInfo foundInfo = null;
            if (rs.next()) {
                foundInfo = readInfo(rs);
                SDLogger.logObject(SDLogger.TRACE, "Read Object:", foundInfo);
            } else {
                SDLogger.logObject(SDLogger.TRACE, " Did not find ", key);
            }
            return foundInfo;
        } catch (SQLException sqle) {
            throw new DelayDBException("Could not create ", sqle);
        } finally {
            cleanUp(ps, rs);
        }

    }


    /**
     * Find all DelayInfo that has a wanted time that is within the given interval.
     * The interval is startMS (inclusive) to endMS (exclusive). This
     * means that a call with 0 and 1000 will find all entries with
     * times
     *
     * @param startMS Earliest wanttime for returned data.
     * @param endMS Latest wanttime for returned data.
     * @param limitFound - Max number of items to return
     * @param result The found delayinfo are appended to this list. It is
     *        the callers responsibility to empty the list before calling
     *        if that is wanted.
     * @return Timetamp for where the client can start searching for next
     *         batch of DelayInfo. If less than limitFound elements was
     *         read then endMS is returned, otherwise the return value
     *         is the timewanted for the last found element + 1.
     *         This value can be used as startMS in the next call to
     *         findForTime to ensure that all data are eventually found.
     * @throws DelaySQLException if there is a problem with DB handling
     * @throws DelayCleaningException if database is currently unavailable.
     */
    public long findForTime(long startMS, long endMS,
                            int limitFound, List result)
    throws DelayException {
        if (isBusy()) {
            throw new DelayCleaningException("Cannot find when cleaning DB");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String findSQL =
            "SELECT TOP ? keydata,keytype,wanttime,strinfo,byteinfo " +
            "FROM event " +
            "WHERE wanttime BETWEEN ? AND ? ORDER BY wanttime";
            SDLogger.log(SDLogger.TRACE, findSQL);
            ps = conn.prepareStatement(findSQL);
            ps.setInt(1, limitFound);
            ps.setLong(2,startMS);
            ps.setLong(3, endMS-1);
            rs = ps.executeQuery();
            while (rs.next()) {
                DelayInfo foundInfo = readInfo(rs);
                SDLogger.logObject(SDLogger.TRACE, "Read Object:", foundInfo);
                result.add(foundInfo);
            }
            if (result.size() == limitFound) {
                // We got exactly the amount we was looking for
                // the last one (or last ones) may have the same
                // millisecond value as some that was not retreived
                // so we can not return them now.

                // If all are at the same millisecond we have a
                // problem,
                // Solution: Throw exception
                // Solution, retry and find in that millisecond without limit?
                //           or with a bigger limit
                // Solution: Return the found ones, losing data in
                //           the process, for now use this solution.

                int currSize = result.size();
                DelayInfo first = (DelayInfo)result.get(0);
                DelayInfo last  = (DelayInfo)result.get(currSize -1);
                if (first.getWantTime() == last.getWantTime()) {
                   SDLogger.log(SDLogger.ERROR,
                                "DBDelayHandler.FindForTime - " +
                                "All Data in same millisecond, loosing data");
                    return last.getWantTime()+1;
                } else {
                    ArrayList al;
                    // Remove last untill new last has time that differs
                    DelayInfo newLast;
                    int removeCount = 0;
                    do {
                        removeCount++;
                        result.remove(currSize -1);
                        currSize--;
                        newLast = (DelayInfo)result.get(currSize- 1);
                    } while (newLast.getWantTime() == last.getWantTime());
                    SDLogger.log(SDLogger.WARNING,
                                "DBDelayHandler.FindForTime - " +
                                " Read limit reached for one read");
                    return newLast.getWantTime() +1;
                }

            } else {
                // We have all in the interval up to endMS
                return endMS;
            }
        } catch (SQLException sqle) {
            throw new DelayDBException("Could not find for time ", sqle);
        } finally {
            cleanUp(ps, rs);
        }
    }




    /**
     * Read a complete DelayInfo from a ResultSet
     */
    private DelayInfo readInfo(ResultSet rs)
    throws SQLException {
        String foundData = rs.getString("keydata");
        short  foundType = rs.getShort("keytype");
        long   wantTime  = rs.getLong("wanttime");
        String  strInfo   = rs.getString("strinfo");
        byte[] byteInfo  = rs.getBytes("byteinfo");
        DelayInfo info = new DelayInfo(foundData,foundType,strInfo, byteInfo);
        info.setWantTime(wantTime);
        return info;
    }
    /**
     * Update the given DelayInfo in the store.
     * The info in database with the same key and type as info is updated
     * with the wanttime and data in info
     * @param info Data to put into the store.
     * @throws DelayDBException if there is a problem with DB handling
     * @throws DelayCleaningException if database is currently unavailable.
     * @returns True if the info was updated, false if it was not updated
     *          due to not existing in database.
     */
    public boolean update(DelayInfo info)
    throws DelayException {
        if (isBusy()) {
            throw new DelayCleaningException("Cannot create when cleaning DB");
        }
        PreparedStatement ps = null;
        try {
            String updateSQL =
            "UPDATE event SET " +
            "wanttime=?,strinfo=?,byteinfo=? " +
            "WHERE keydata=? AND keytype=? ";
            SDLogger.logObject(SDLogger.DEBUG, "Updating", info);
            ps = conn.prepareStatement(updateSQL);
            ps.setLong(1,   info.getWantTime());
            ps.setString(2, info.getStrInfo());
            ps.setBytes(3,  info.getByteInfo());
            ps.setString(4, info.getKey());
            ps.setShort(5,  info.getType());
            int count = ps.executeUpdate();
            changedRowCount += count;
            return (count == 1);
        } catch (SQLException sqle) {
            throw new DelayDBException("Could not update ", sqle);
        } finally {
            cleanUp(ps, null);
        }
    }

    /**
     * Remove information for the given key and type from storage.
     * @param key Key for info to remove
     * @param type Type for info to remove.
     * @return true if data was found and removed, false if data was not found
     * @throws DelayDBException if there is a problem with DB handling
     * @throws DelayCleaningException if database is currently unavailable.
     */
    public boolean remove(String key, short type)
    throws DelayException {
        if (isBusy()) {
            throw new DelayCleaningException("Cannot remove when cleaning DB");
        }
        PreparedStatement ps = null;
        try {
            String removeSQL =
                "DELETE FROM event WHERE keydata=? and keytype=?";
            if (SDLogger.willLog(SDLogger.DEBUG)) {
                SDLogger.log(SDLogger.DEBUG, "DB-Removing :" + key +"/" + type);
            }
            ps = conn.prepareStatement(removeSQL);
            ps.setString(1, key);
            ps.setShort(2, type);
            int count = ps.executeUpdate();
            changedRowCount += count;
            return (count >= 1); // Count = number of rows removed
        } catch (SQLException sqle) {
            throw new DelayDBException("Could not delete ", sqle);
        } finally {
            cleanUp(ps, null);
        }
    }

    /**
     * Remove old DelayInfo.
     * Removes all DelayInfo with wanttime earlier than keeptime from storage.
     * Note that DelayInfos with wanttime == 0 is <b>NOT</b> removed.
     * This is because those are seen as waiting for events and not a
     * certain time, so we do not know how old they are.
     * Since events that are delivered might be rescheduled a while later
     * this method should not be used to remove events that are less
     * than a few days older than current time. The time to use depends
     * on how long other parts of the system waits for e.g. SMS TYPE-0.
     * @param keepTime All DelayInfo with wanttime earlier than this are removed.
     * @return Number of removed rows.
     * @throws DelayDBException if there is a problem with DB handling
     * @throws DelayCleaningException if database is currently unavailable.
     */
    public int removeOlderThan(long keepTime)
    throws DelayException {
        if (isBusy()) {
            throw new DelayCleaningException("Cannot remove when cleaning DB");
        }
        if (keepTime < MAX_MS_FOR_NOTIFICATIONS+1) {
            return 0; // Nothing to delete
        }
        PreparedStatement ps = null;
        try {
            String removeSQL =
                 "DELETE FROM event WHERE wanttime BETWEEN ? AND ?";
            SDLogger.log(SDLogger.DEBUG, "DB - Removing older than " + keepTime);
            ps = conn.prepareStatement(removeSQL);
            ps.setLong(1, MAX_MS_FOR_NOTIFICATIONS+1);
            ps.setLong(2, keepTime-1);
            int count = ps.executeUpdate();
            changedRowCount += count;
            SDLogger.log(SDLogger.DEBUG,"DB - Number of removed items : " + count);
            return count;
        } catch (SQLException sqle) {
            throw new DelayDBException("Could not remove old rows ", sqle);
        } finally {
            cleanUp(ps, null);
        }
    }

    /**
     * Check if the database is cleaning or ready for use.
     * @return true if the database is cleaning, a call to one of the storage
     *   operations will propebly throw a DelayCleaningException, false if
     *   the store is ready for use. A call to a storage operation will not
     *   throw a DelayCleaningException before a call to {@link #allowCleaning} has
     *   been made.
     */
    public boolean isBusy() {
        if (compactThread == null) return false;
        if (compactThread.isAlive()) {
            return true;
        } else {
            // Exists but no longer alive, clear reference
            compactThread = null;
            return false;
        }
    }


    /**
     * Update limits for when to clean.
     * Cleaning is a checkpoint with an optional defragmentation.
     * @param checkPointLimit How many rows that can be updated
     *                        before a checkpoint should be done
     */
    public void setCleaningLimits(int checkPointLimit,
                                  int defragmentLimit) {
        this.checkPointLimit = checkPointLimit;
        this.defragmentLimit = defragmentLimit;
        SDLogger.log(SDLogger.DEBUG,
                    "Limits are now " + checkPointLimit + "/" +
                    defragmentLimit);
    }

    /**
     * Tell the handler that it is allowed to start a cleaning of the storage.
     * If a cleaning is started all storage operations will throws a
     * {@link DelayCleaningException} when they are called until the cleaning
     * is done. It is possible to check if it is done by calling {@link #isBusy}.
     * @return True if a cleaning is under way, false if it was not.
     */
    public boolean allowCleaning() {
        if (isBusy()) return true; // Already cleaning!
        if (SDLogger.willLog(SDLogger.TRACE)) {
            SDLogger.log(SDLogger.TRACE,
                        "DAO-Allow Cleaning Count = " + changedRowCount);
        }
        if (changedRowCount >= checkPointLimit) {
            // A compact, maybe a defrag
            compactCount++;
            changedRowCount = 0;
            boolean compactFlag = false;
            if (compactCount >= defragmentLimit) {
                compactFlag = true;
                compactCount = 0;
            }
            compactThread = new Thread(new Compacter(compactFlag));
            compactThread.setName("DelayInfoDAO-Compacter");
            compactThread.start();
            return true; // We are cleaning now.
        } else {
            return false;
        }
    }

    /**
     * For testing only.
     *@return the database connection.
     */
    public Connection getConnectionForTestingOnly() {
        return conn;
    }

    /**
     * This thread handles compacting the DB
     */
    private class Compacter implements Runnable
    {
        private boolean defragment;

        public Compacter(boolean defragment)
        {
            this.defragment = defragment;
        }

        public void run() {
            try {
                compactData(defragment);
            } catch (DelayException de) {
                SDLogger.log(SDLogger.SEVERE, "Could not compact DB", de);
            }
        }

    }





}
