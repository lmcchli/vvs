package com.mobeon.common.util.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;

import com.abcxyz.messaging.common.oam.LogAgent;



/**
 * 
 * Special stream to redirect output to log4j.
 * modified version of:
 * http://www.sysgears.com/articles/how-to-redirect-stdout-and-stderr-writing-to-a-log4j-appender/
 * Made multi-thread safe...
 * @author lmcmajo
 */
public class loggingOutputStream extends OutputStream {
    
    /**
     * Default number of bytes in the buffer.
     */
    private static final int DEFAULT_BUFFER_LENGTH = 10240; //10k
    
    private static int MAX_QUEUE_LENGTH = 500;
    
    private LinkedBlockingQueue<String> logQueue;
    
    private stdOutLoggerThread loggingThread;

    /**
     * Indicates stream state.
     */
    private boolean hasBeenClosed = false;

    /**
     * Internal buffer used for write(int)
     */
    private byte[] buf;

    /**
     * Number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Remembers the size of the buffer.
     */
    private int curBufLength;

    /**
     * The logger to write to.
     */
    private final LogAgent log;

    /**
     * The log level.
     */
    private final Level level;
    
    //reported queue full or not
    private boolean reported = false;

   //the orignal strean redirected from, used for logging of logging stream issues..
    private final OutputStream origStream;

    //turn on stack tracing, if called back from log4j..  
    private boolean debugTrace=false;
    
    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param stdOutLog         the Logger to write to
     * @param level       the log level
     * @throws IllegalArgumentException in case if one of arguments
     *                                  is  null.
     */
    public loggingOutputStream(final LogAgent stdOutLog, final Level level,final OutputStream origStream) throws IllegalArgumentException {
        if (stdOutLog == null || level == null || origStream == null) {
            throw new IllegalArgumentException(
                    "Logger, log level or origStream must be not null");
        }
        this.log = stdOutLog;
        this.level = level;
        this.origStream = origStream;
        curBufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[curBufLength];
        count = 0;        
        if((System.getProperty("loggingStream.debug") != null) && (System.getProperty("loggingStream.debug").equalsIgnoreCase("true"))) {
            debugTrace=true;
        }
        if(System.getProperty("loggingStream.QueueSize") != null) {
            int tmp =Integer.parseInt(System.getProperty("loggingStream.QueueSize"));
            if (tmp > 0)
            {
                MAX_QUEUE_LENGTH=tmp;                
            }
        }
        logQueue = new LinkedBlockingQueue<String>(MAX_QUEUE_LENGTH);
        loggingThread = new stdOutLoggerThread(logQueue);
        loggingThread.start();       
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    public void write(final int b) throws IOException {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }
        String string = null;
        // don't log nulls
        if (b == 0) {
            return;
        }               
        synchronized(this) {
            // would this be writing past the buffer?
            if (count == curBufLength) {
                // grow the buffer
                final int newBufLength = curBufLength +
                        DEFAULT_BUFFER_LENGTH;               
                final byte[] newBuf = new byte[newBufLength];
                System.arraycopy(buf, 0, newBuf, 0, curBufLength);
                buf = newBuf;
                curBufLength = newBufLength;
            }
            if ( b != '\n') {
                buf[count++] = (byte) b;
            } else
            {
                string = new String(buf).trim();
                count=0;        
            }
        }
        if (string != null && string.length() != 0) {
            writetoLog4j(string);
        }
        
    }
    
    public void write(byte b[], int off, int len) throws IOException {
        if (hasBeenClosed){ throw new IOException("ERROR: Stream is closed, when attempting to Write"); }
        String string = new String(b,off,len);
        writetoLog4j(string);
    }
    
    public void write(byte b[]) throws IOException {
        if (hasBeenClosed){ throw new IOException("ERROR:  Stream is closed, when attempting to Write"); }
        String string = new String(b);
        writetoLog4j(string);
    }
    


    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    public void flush() {        
        final String string;

        if (hasBeenClosed) {
            writeOldStream("ERROR: Unable to flush stderr/out for " + level +" logger due to stream closed.");
            return;
        }

        try {

            synchronized (this) { 
                if (count == 0) {
                    return;
                }          
                //reset for next write to start at beginning of buffer.
                string = new String(buf).trim();
                count=0;
            }

            if (string.length() == 0) return;
            writetoLog4j(string);             

        } catch (Throwable t) {
            writeOldStream("ERROR: Unable to flush stderr/out for " + level +" logger due to exception: ",t);
        }
    }

    //write logs to old stream stdout/stderr
    //Synchronised as can be called from more than one thread.
    private synchronized void writeOldStream(String msg) {
        msg="com.mobeon.common.util.logging.loggingOutputStream-" + level + " " + msg  + "\n";
        try {
            origStream.write(msg.getBytes());
            origStream.flush();
        } catch (IOException e) {
            //ignore..
        }
    }
    
    //write logs to old stream stdout/stderr
    //Synchronised as can be called from more than one thread.
    private synchronized void writeOldStream(String msg,Throwable t) {
        msg="com.mobeon.common.util.logging.loggingOutputStream " + msg  + t.getMessage() +"\n";
        try {
            origStream.write(msg.getBytes());
            origStream.flush();
            PrintWriter writer = new PrintWriter(origStream);
            t.printStackTrace(writer);
            writer.flush();
            origStream.flush();
        } catch (IOException e) {
            //ignore..
        }
    }
    
    private void writetoLog4j(String string) {
        
        //only enable in lab for troubleshooting
        if (debugTrace) {
            //Log stack trace to stderr/out if called from log4j - very heavy for debug only... 
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (int x=0; x < stack.length; x++ ) {
                String clazz = stack[x].getClassName();
                if (clazz.contains("org.apache.log4j")) //org.apache.log4j
                {
                    Throwable t = new Throwable("stackTrace");
                    t.fillInStackTrace();
                    writeOldStream("INFO: CALLED from log4j with string ["+string+"]");
                    writeOldStream("INFO: CALLED from log4j stack: ",t);
                }
            }
        }
        
        if ((string == null) || (string.length() == 0)) {return;}
                     
        String strings[] = string.split("\n"); 

        for (int i=0; i< strings.length; i++) {
            if (strings[i].trim().length() == 0) {continue;} //discard blank lines.
            if (!logQueue.offer(strings[i]) && !reported ) {
                writeOldStream("Buffer full for log level, discarding logs for " + level);
                reported=true;
            } else
            {
                if (reported) {
                    if (logQueue.size() < MAX_QUEUE_LENGTH/2 ) {
                        writeOldStream("Buffer back to half size for level" + level);
                        reported = false;
                    }
                }
            }
        }
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    public void close() {
        if (hasBeenClosed) {return;}
        flush();
        hasBeenClosed = true;
    }
  
    
    /* Thread to do the actual logging so we can't block */
    private class stdOutLoggerThread extends Thread {      
        private LinkedBlockingQueue<String> queue;
        String logString;
        
        
        private stdOutLoggerThread() {}

        private stdOutLoggerThread(LinkedBlockingQueue<String> logQueue) {
            super("stdOutLoggerThread-"+level);
            this.queue = logQueue;
        }
        public void run() {
            
            while(!interrupted())
            {
                try {
                    logString=queue.poll(5,TimeUnit.SECONDS); 
                    if(logString != null) {
                        if (level == Level.INFO)
                            log.info(logString);
                        else if (level == Level.DEBUG)
                            log.debug(logString);
                        else if (level == Level.WARN)
                            log.warn(logString);
                        else if (level == Level.ERROR)
                            log.error(logString);
                        else if (level == Level.FATAL)
                            log.fatal(logString);
                        else 
                            log.info(logString); //default.. 
                    }
                    if (hasBeenClosed && logQueue.isEmpty())
                        break; //Exit..
                    
                } catch (InterruptedException e) {
                    //will exit when queue is empty..
                    writeOldStream("WARN: Logger Thread, exiting due to interupt.");
                } catch (RuntimeException re) {
                    writeOldStream("Unexpected run time Exception,  exiting thread: ",re);
                    break;
                } catch (Exception e) {
                    writeOldStream("Unexpected Exception: ",e);
                    continue;
                }
                catch (Throwable t) {
                    writeOldStream("Unexpected Throwable, exiting thread: ",t);
                    break;                   
                }
            }        
        }
    }
}


