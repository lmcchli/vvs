/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.test;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import java.io.FileInputStream;
import java.util.*;
import junit.framework.*;

/**
 * Base for all NTF test cases.
 */
public class NtfTestCase extends TestCase {
    protected Logger log;
    private Properties setup = new Properties();
    protected String o;
    protected String ou;
    protected static String community =  " ";
    protected String cosNumber;
    protected String anotherCosNumber;
    protected String mailDomain;
    protected int firstUniqueIdentifier;
    protected static boolean firstRun = true;

    public NtfTestCase(String name) {
	super(name);
        String temp;
	log= Logger.getLogger();
        try { setup.load(new FileInputStream("testsetup")); } catch (Exception e) { }
        o = setup.getProperty("o", "abcxyz.se");
        ou = setup.getProperty("ou", "C2");
        cosNumber = setup.getProperty("cos", "cos1");
        anotherCosNumber = setup.getProperty("anotherCos", "cos2");
	mailDomain = setup.getProperty("maildomain", "lab.mobeon.com");
        temp = setup.getProperty("firstuniqueidentifier", "10000");
        try { firstUniqueIdentifier = Integer.parseInt(temp); } catch (NumberFormatException e) { firstUniqueIdentifier = 10000; }
        

        community = "ou=" + ou + ",o=" + o;
        if (firstRun) {
            firstRun = false;
            log.logMessage("Running test with setup\n"
                           + "community=" + community + "\n"
                           + "cos=" + cosNumber + ", " + anotherCosNumber + "\n"
                           + "installdir=" + Config.getInstallDir() + "\n"
                           + "ntfhome=" + Config.getNtfHome() + "\n"
                           + "configfile=" + Config.getConfigFileName()
                           + "\n");
        }
    }
    
    protected String noSpaces(String s) {
        int ix;
        String result = s;
        do {
            ix = result.indexOf(" ");
            if (ix > 0) {
                result = result.substring(0, ix) + result.substring(ix + 1);
            }
        } while (ix >= 0);
        return result;
    }

    protected void l(String s) {
	log.logMessage("******** Test case " +getClass().getName() + "." + s, log.L_DEBUG);
    }
}
