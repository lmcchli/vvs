package com.mobeon.ntf.out.sip;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-mar-05
 * Time: 11:57:04
 * To change this template use File | Settings | File Templates.
 */
public class SIPCommandHandler {

    // maxtimehours=3
    // waittime = 2
    // waittime.10-20 = 5
    // waittime.20-100 = 10
    // waittime.100-  = 60
    // alwayscheckcount = no


    private int maxTime;
    private int waitTime;
    private LinkedList waitTimeMappings;
    private boolean alwaysCheckCount = false;


    public SIPCommandHandler(String path) throws IOException {
        Properties props = new Properties();
        File f = new File(path);
        props.load(new FileInputStream(f));
        load(props);
    }

    private void load(Properties props) {
        waitTimeMappings = new LinkedList();
        Set set = props.keySet();
        Iterator iter = set.iterator();
        while( iter.hasNext() ) {
            String key = (String) iter.next();
            String value = props.getProperty(key);
            if( key.equalsIgnoreCase("maxtimehours")) {
                maxTime = Integer.parseInt(value);
            } else if( key.equalsIgnoreCase("waittime")) {
                waitTime = Integer.parseInt(value);
            } else if( key.equalsIgnoreCase("alwayscheckcount")) {
                if( value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") ) {
                    alwaysCheckCount = true;
                }


            } else if( key.startsWith("waittime")) {
                int tempTime = Integer.parseInt(value);
                String intervalLine = key.substring( key.indexOf(".") + 1);
                int index = intervalLine.indexOf("-");
                String firstEntry = intervalLine.substring(0,index);
                String secondEntry = intervalLine.substring(index+1);
                int firstCount = Integer.parseInt(firstEntry);
                int secondCount = Integer.MAX_VALUE;
                if( secondEntry != null && secondEntry.length() > 0 ) {
                    secondCount = Integer.parseInt(secondEntry);
                }
                waitTimeMappings.add(new RetryItem(firstCount, secondCount, tempTime));
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SIPCommandHandler\n");
        buffer.append("MaxTimeHours=" + maxTime + "\n");
        buffer.append("Wait time=" + waitTime + "\n" );
        Iterator iter = waitTimeMappings.iterator();
        while( iter.hasNext() ) {
            RetryItem item = (RetryItem) iter.next();
            buffer.append(item.toString());
        }
        return buffer.toString();

    }

    public SIPCommandHandler( Properties props ) {
        load(props);
    }

    public int getWaitTime(int retryCount) {
        Iterator iter = waitTimeMappings.iterator();
        while( iter.hasNext() ) {
            RetryItem item = (RetryItem) iter.next();
            if( item.matches(retryCount)) {
                return item.getRetryTime();
            }
        }
        return waitTime;
    }

    public int getMaxTimeHours() {
        return maxTime;
    }

    public boolean isAlwaysCheckCount() {
        return alwaysCheckCount;
    }


    private class RetryItem {
        private int startCount;
        private int stopCount;
        private int retryTime;

        RetryItem( int start, int stop, int retryTime ) {
            this.startCount = start;
            this.stopCount = stop;
            this.retryTime = retryTime;
        }

        public boolean matches( int count ) {
            return count >= startCount && count <= stopCount;
        }

        public int getRetryTime() {
            return retryTime;
        }

        public String toString() {
            return "Wait time for retry attempt " + startCount + "-" + stopCount + " = " + retryTime + "\n";
        }

    }



}
