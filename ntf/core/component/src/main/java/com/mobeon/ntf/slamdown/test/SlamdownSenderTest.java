/*
 * SlamdownSenderTest.java
 * JUnit based test
 *
 * Created on den 7 februari 2006, 16:13
 */

package com.mobeon.ntf.slamdown.test;

import junit.framework.*;
import com.mobeon.ntf.slamdown.*;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.Config;
import java.util.*;

/**
 *
 * @author ermjnil
 */
public class SlamdownSenderTest extends NtfTestCase {
    private SlamdownStore _store = SlamdownStore.get();
    private SlamdownSender _sender = SlamdownSender.get(); 
    public SlamdownSenderTest(java.lang.String testName) {
        super(testName);
    }
    private SlamdownList _sdl = new SlamdownList("0706028306", "jocke@ermjnil.su.erm.abcxyz.se", "en", "smsc-jawa", 24, "exclusiv");
    Integer val = new Integer(4);
    private Iterator it;
    private boolean _stopRequested = false;
    
    public void testRetrierThread() throws Exception {
        l("testRetrierThread");
        
        Thread t1 = new Thread() {
            public void run() {
                while(!_stopRequested) {
                    try {
                        try {
                            _sdl.setState(val.byteValue());
                            _sender.send(_sdl);
                            _sender.setObjectInRetrier(_sdl);
                        }
                        catch(Exception e) {    System.out.println("Error: " + e);}
                        Thread.sleep(1000);
                    }
                    catch(Exception e) {    System.out.println("Error: " + e);}
                }
            }
        };
        
        Thread t2 = new Thread() {
            public void run() {
                while(!_stopRequested) {
                    try {
                        try {
                            it = _sender.iterator();
                            if(it.hasNext()) {
                                it.next();
                                it.remove();
                            }
                            
                        }
                        catch(Exception e) {    System.out.println("Error: " + e);}
                        Thread.sleep(100);
                    }
                    catch(Exception e) {    System.out.println("Error: " + e);}
                }
            }
        };
        
        t1.start();
        t2.start();
        Thread.sleep(60000);
        _stopRequested = true;
        assertTrue(true);
    }
}
