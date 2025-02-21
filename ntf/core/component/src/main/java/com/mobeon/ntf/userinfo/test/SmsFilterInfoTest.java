/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.util.CommaStringTokenizer;
import java.util.*;
import junit.framework.*;

/**
 * This class tests SmsFilterInfo.
 */
public class SmsFilterInfoTest extends NtfTestCase {

    public SmsFilterInfoTest(String name) {
	super(name);
    }


    private String[] smsNumbers;
    private String[] mwiNumbers;

    protected void setUp() {
        //	fSpec= new Properties();
        //	filterSpec= (String[])(fSpec.values().toArray(filterSpec));

        smsNumbers = new String[] { "123456" };
        mwiNumbers = new String[] { "123456" };
    }

    public void testNumbers() throws Exception {
        SmsFilterInfo f;
        l("testNumbers");


        f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI", "h,c"), new String[] { "111" }, new String[] {"222"});
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals(2, f.getNumbers().length);

        assertTrue(f.isSms("111"));
        assertFalse(f.isMwi("111"));
        assertFalse(f.isSms("222"));
        assertTrue(f.isMwi("222"));

    }

    public void testNeither() throws Exception {
        SmsFilterInfo f;
	l("testNeither");
        //Test not SMS,MWI
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",,,,", ",,,,"), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("", ""), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",MMS,,,", ",,,,"), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
    }

    public void testSmsOnly() throws Exception {
        SmsFilterInfo f;
	l("testSmsOnly");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS", "h"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(!f.isMwi());
        assertEquals("h", f.getNotifContent());
    }

    public void testMwiOnly() throws Exception {
        SmsFilterInfo f;
	l("testMwiOnly");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("MWI", "c"), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(f.isMwi());
        assertTrue(f.hasMwiCount());
    }

    public void testSmsAndMwi() throws Exception {
        SmsFilterInfo f;
	l("testSmsAndMwi");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI,MMS,WAP,ODL", "h,c,,,"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("h", f.getNotifContent());
        assertTrue(f.hasMwiCount());
    }

    public void testContent() throws Exception {
        SmsFilterInfo f;
	l("testContent");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI", "h,c"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("h", f.getNotifContent());
        assertTrue(f.hasMwiCount());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI", "c,s"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("c", f.getNotifContent());
        assertTrue(!f.hasMwiCount());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI", "s,c"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("s", f.getNotifContent());
        assertTrue(f.hasMwiCount());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("ODL", ""), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("SMS,MWI", "specialSmsTemplate,s"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("specialSmsTemplate", f.getNotifContent());
        assertTrue(!f.hasMwiCount());
      //1;y;a;evfm;SMS,FLS,WAP,MMS,MWI,CMW,ODL,PAG,EML;c,f,,,,,,,c;1;;;;;own;;

        f = new SmsFilterInfo(CommaStringTokenizer.getPropertiesFromLists("SMS,FLS,WAP,MMS,MWI,CMW,ODL,PAG,EML","c,f,,,,,,,c"),smsNumbers, mwiNumbers);
        l("FLASH TEST");
        assertTrue(f.isFlash());
    	assertEquals("f", f.getFlashContent());
    }

    public void testPlacement() throws Exception {
        SmsFilterInfo f;
	l("testPlacement");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",MWI,,,SMS", ",,,,s"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("s", f.getNotifContent());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",MWI,,,SMS", ",,,,s"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("s", f.getNotifContent());
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("MMS,,SMS,,", ",,c,,"), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(!f.isMwi());
        assertEquals("c", f.getNotifContent());
    }

    public void testDefaults() throws Exception {
        SmsFilterInfo f;
	l("testDefaults");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists("MWI,SMS", ""), smsNumbers, mwiNumbers);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        assertEquals("s", f.getNotifContent());
        assertTrue(!f.hasMwiCount());
    }

    public void testSet() throws Exception {
        SmsFilterInfo f;
	l("testSet");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",,,,", ",,,,"), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
        f.setSms(true);
        assertTrue(f.isSms());
        assertTrue(!f.isMwi());
        f.setMwi(true);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        f.setSms(false);
        assertTrue(!f.isSms());
        assertTrue(f.isMwi());
        f.setMwi(false);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
    }

    public void testMultilineNumbers() throws Exception {
        SmsFilterInfo f;
	l("testSet");
	f = new SmsFilterInfo(CommaStringTokenizer.
                              getPropertiesFromLists(",,,,", ",,,,"), smsNumbers, mwiNumbers);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
        f.setSms(true);
        assertTrue(f.isSms());
        assertTrue(!f.isMwi());
        f.setMwi(true);
        assertTrue(f.isSms());
        assertTrue(f.isMwi());
        f.setSms(false);
        assertTrue(!f.isSms());
        assertTrue(f.isMwi());
        f.setMwi(false);
        assertTrue(!f.isSms());
        assertTrue(!f.isMwi());
    }

}
