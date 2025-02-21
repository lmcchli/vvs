/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.text.test;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestUser;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.text.Phrases;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;
import junit.framework.*;

/**
 * This class tests TextCreator.
 */
public class TextCreatorTest extends NtfTestCase {

    TextCreator tc;
    NotificationEmail email;
    TestUser1 user;
    TestUser1 user2;
    TestUser1 user3;
    TestUser1 user4;
    UserMailbox inbox;
    Date when;

    public TextCreatorTest(String name) {
        super(name);
    }


    protected void setUp() {
        long whenl = (2004 - 1970) * 365L * 24 * 3600 * 1000;
        when = new Date(whenl);
    }

  private void copyCphr(String from, String to) throws Exception{
      File cphrFile = null;
      File phrDir= new File(Config.getPhraseDirectory());
      log.logMessage("Refreshing phrase files", log.L_VERBOSE);
      if (!phrDir.isDirectory()) {
        l("Failed to find phrase directory " + phrDir.getPath());
      }
      cphrFile = new File(phrDir.getAbsolutePath()+"/" + to);
      remove(phrDir.getAbsolutePath() + "/" + to);
      while(cphrFile.exists())
        Thread.sleep(1000);
      copy(phrDir.getAbsolutePath() + "/" + from , phrDir.getAbsolutePath() + "/" + to);
      while(!cphrFile.exists())
        Thread.sleep(1000);
      TextCreator.get().reset();
      Phrases.refresh();
  }

   private void phrEn() throws Exception {
	l("phrEn");
        tc = TextCreator.get();
        inbox = new UserMailbox(2, 2, 3, 2, false);
        email = new NotificationEmail(9999,
                                      "Date: Mon, 16 Dec 2002 13:50:15 +0100\r\n"
                                      + "Content-type:text/plain\r\n"
                                      + "From:nisse@host.domain\r\n"
                                      + "X-priority:3\r\n"
                                      + "Subject:testemail\r\n\r\n"
                                      + "template test");

        user = new TestUser1();

        assertEquals("You have new messages", tc.get().generateText(inbox, email, user, "nosuchnotificationtype", true, null));
        String tmp = tc.get().generateText(inbox, email, user, "s", true, null);
        assertEquals("You have a new size 1 kilobytes message with 0 attachments, regarding \"testemail\".",
                     tmp);

        l("before h");
        assertEquals("You have a normal new email message from nisse@host.domain. "
                     + "Message received at 14:20, 2003-08-22. The message text is \"template test\".",
                     tc.get().generateText(inbox, email, user, "h", true, null));

        l("after h");

        /**
         * Tests were moved to unit tests in {@link com.mobeon.ntf.text.TemplateTest} class
         */
        
        assertEquals("nisse@host.domain text",
                     tc.get().generateText(inbox, email, user, "aa", true, null));
        assertEquals("nisse@host.domain text",
                     tc.get().generateText(inbox, email, user, "bb", true, null));
        assertEquals("nisse@host.domain text",
                     tc.get().generateText(inbox, email, user, "cc", true, null));

    }

   private void phrEnDefault() throws Exception {
       l("phrEnDefault");

       tc = TextCreator.get();
       inbox = new UserMailbox(2, 2, 3, 2, false);
       email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
       user = new TestUser1();
       assertEquals("New message",
                    tc.get().generateText(inbox, email, user, "c", true, null));

       inbox = new UserMailbox(10, 10, 10, 10, false);
       assertEquals("New message",
                    tc.get().generateText(inbox, email, user, "c", true, null));
   }


   public void testEn() throws Exception {
       l("testEn");
       File phrDir= new File(Config.getPhraseDirectory());
       File cphrFile = null;
       log.logMessage("Refreshing phrase files", log.L_VERBOSE);

       if (!phrDir.isDirectory()) {
           l("Failed to find phrase directory " + phrDir.getPath());
       }
       cphrFile = new File(phrDir.getAbsolutePath()+"/en.cphr");
       remove(phrDir.getAbsolutePath() + "/en.cphr");
       while (cphrFile.exists())
         Thread.sleep(1000);
       Phrases.refresh();
       phrEnDefault();
       // No cphr.. really basic stuff
       // Then get the en.cphr.default
       copy(phrDir.getAbsolutePath() + "/en.cphr.default", phrDir.getAbsolutePath() + "/en.cphr");
       while(!cphrFile.exists())
        Thread.sleep(1000);
       Phrases.refresh();
       phrEn();
   }

  /*
   public void testEnWithCphr() throws Exception {
       l("testEnWithCphr");
       File phrDir= new File(Config.getPhraseDirectory());
       File cphrFile = new File(phrDir.getAbsolutePath()+"/en_xtest.cphr");
       log.logMessage("Refreshing phrase files", log.L_VERBOSE);

       if (!phrDir.isDirectory()) {
           l("Failed to find phrase directory " + phrDir.getPath());
       }

       // Move en.cphr.template to en.cphr
       copy(phrDir.getAbsolutePath()+"/en.cphr.template",
            phrDir.getAbsolutePath()+"/en_xtest.cphr");
       while(!cphrFile.exists())
        Thread.sleep(1000);
       Phrases.refresh();
       cphrEn();
       remove(phrDir.getAbsolutePath()+"/en_xtest.cphr");

   }

    //Testing an old .phr file
    public void testOldEn() throws Exception {
        l("testOldEn");

        tc = TextCreator.get();
        inbox = new UserMailbox(0, 0, 0, 1, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        //Set language so that the en_old.phr file is loaded
        user.setPreferredLanguage("en_old");

        assertEquals("You have NO new messages.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
     }

    private void phrSv() throws Exception {
        l("phrSv");

        tc = TextCreator.get();
        inbox = new UserMailbox(2, 2, 3, 3, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        user.setPreferredLanguage("sv");
        assertEquals("Du har nya meddelanden", TextCreator.get().generateText(inbox, email, user, "nosuchnotificationtype", true, null));
        assertEquals("Du har ett nytt 1 kilobyte meddelande med 0 bilagor,"
                     + " angående \"testemail\".",
                     TextCreator.get().generateText(inbox, email, user, "s", true, null));
        assertEquals("Du har ett nytt epostmeddelande från nisse@host.domain. "
                     + "Det kom 14:20, 2003-08-22. Meddelandetexten är \"template test\".",
                     TextCreator.get().generateText(inbox, email, user, "h", true, null));
        inbox = new UserMailbox(0, 0, 0, 0, false);
        assertEquals("Du har inga nya meddelanden.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
        inbox = new UserMailbox(0, 0, 1, 0, false);
        assertEquals("Du har ett nytt epostmeddelande.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
        inbox = new UserMailbox(1, 1, 1, 1, false);
        assertEquals("Du har ett nytt röstmeddelande, ett nytt fax, ett nytt epostmeddelande och ett nytt videomeddelande.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
        inbox = new UserMailbox(1, 1, 6, 0, false);
        assertEquals("Du har ett nytt röstmeddelande, ett nytt fax och 6 nya epostmeddelanden.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
        inbox = new UserMailbox(3, 0, 1, 0, false);
        assertEquals("Du har 3 nya röstmeddelanden och ett nytt epostmeddelande.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
	inbox = new UserMailbox(7, 2, 7, 0, false);
        assertEquals("Du har 7 nya röstmeddelanden, 2 nya fax och 7 nya epostmeddelanden.",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
    }

    private void phrSvDefault() throws Exception {
        l("phrSvDefault");
        tc = TextCreator.get();
        inbox = new UserMailbox(2, 2, 3, 3, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        user.setPreferredLanguage("sv");

        assertEquals("Du har 10 nya meddelanden (2 nya röstmeddelanden, 2 nya fax, 3 nya epostmeddelanden, 3 nya videomeddelanden).",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));
        inbox = new UserMailbox(10, 10, 10, 5, false);
        assertEquals("Du har 35 nya meddelanden (10 nya röstmeddelanden, 10 nya fax, 10 nya epostmeddelanden, 5 nya videomeddelanden).",
                     TextCreator.get().generateText(inbox, email, user, "c", true, null));

    }

     public void IGNOREestSv() throws Exception {
        l("testSv");
        File phrDir= new File(Config.getPhraseDirectory());
        File cphrFile = null;
        log.logMessage("Refreshing phrase files", log.L_VERBOSE);

        if (!phrDir.isDirectory()) {
            l("Failed to find phrase directory " + phrDir.getPath());
        }

        remove(phrDir.getAbsolutePath()+"/sv_xtest.cphr");
        phrSv();
        phrSvDefault();
    }

    public void testSvWithCphr() throws Exception {
        l("testSvWithCphr");
        File phrDir= new File(Config.getPhraseDirectory());
        File cphrFile = new File(phrDir.getAbsolutePath()+"/sv_xtest.cphr");
        log.logMessage("Refreshing phrase files", log.L_VERBOSE);

        if (!phrDir.isDirectory()) {
            l("Failed to find phrase directory " + phrDir.getPath());
        }


        // Move en.cphr.template to en.cphr
        copy(phrDir.getAbsolutePath()+"/sv.cphr.template",
        phrDir.getAbsolutePath()+"/sv_xtest.cphr");

        while(!cphrFile.exists())
            Thread.sleep(1000);
        Phrases.refresh();
        cphrSv();
        remove(phrDir.getAbsolutePath()+"/sv_xtest.cphr");
    }
  */
    public void testTemp() throws Exception {
      l("testTemp");
      TextCreator tc = TextCreator.get();
      copyCphr("en.cphr.temp", "en.cphr");

      inbox = new UserMailbox(2, 2, 3, false);
      email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
      user = new TestUser1();
      user.setPreferredLanguage("en");
      assertEquals("header nisse@host.domain",
                   tc.generateText(inbox, email, user, "h", true, null));
      assertEquals("subject testemail",
                   tc.generateText(inbox, email, user, "s", true, null));
      assertEquals("header testemail nisse@host.domain",
                   tc.generateText(inbox, email, user, "g", true, null));
    }
    // Uses en.cphr.temp
    public void testFrom() throws Exception {
        l("testFrom");
        copyCphr("en.cphr.temp", "en.cphr");
        user = new TestUser1();

        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        assertEquals("from nisse@host.domain",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:(123456) <nisse@host.domain>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        assertEquals("from nisse@host.domain",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom: nisse (123456) <nisse@host.domain>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        assertEquals("from 123456",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom: 123456@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        log.logMessage(TextCreator.get().generateText(inbox, email, user, "f", true, null));
        assertEquals("from unknown caller",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:multipart/fax-message\nFrom: nisse (123456) <FAX=444444@ha1.lab.mobeon.com>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        log.logMessage(TextCreator.get().generateText(inbox, email, user, "f", true, null));
        assertEquals("from 444444",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:multipart/fax-message\nFrom: nisse +444444 <FAX=444444@ha1.lab.mobeon.com>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        log.logMessage(TextCreator.get().generateText(inbox, email, user, "f", true, null));
        assertEquals("from 444444",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));

        email = new NotificationEmail(9999, "Content-type:multipart/fax-message\nFrom: nisse <FAX=hidden@ha1.lab.mobeon.com>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        log.logMessage(TextCreator.get().generateText(inbox, email, user, "f", true, null));
        assertEquals("from unknown caller",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));
    }

    public void testDefaults() throws Exception {
        l("testDefaults");
        copyCphr("en.cphr.from", "en.cphr");

        inbox = new UserMailbox(2, 2, 3, 0, false);
        user = new TestUser1();
        email = new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom: nisse (123456) <nisse@host.domain>\nX-priority:3\nSubject:testemail\n\ntemplate test");

        assertEquals("", TextCreator.get().generateText(inbox, email, user, "mwiontext", true, null));

        assertEquals("", TextCreator.get().generateText(inbox, email, user, "mwiofftext", true, null));

        assertEquals("", TextCreator.get().generateText(inbox, email, user, "smstype0text", true, null));
    }


    public void testPhone() throws Exception {
        l("testPhone");
        copyCphr("en.cphr.from", "en.cphr");

        inbox = new UserMailbox(2, 2, 3, false);
        user = new TestUser1();
        email = new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom: nisse (123456) <nisse@host.domain>\nX-priority:3\nSubject:testemail\n\ntemplate test");
        assertEquals("from number +123456",
                     TextCreator.get().generateText(inbox, email, user, "f", true, null));


        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        user.setTelephoneNumber("46705354098");
        //Properties phr = new Properties();
        //phr.put("i", "This is a phone number \"+PHONE\"");
        //tc = new TextCreator(phr);
        assertEquals("This is a phone number \"+46705354098\"",
                      TextCreator.get().generateText(inbox, email, user, "i", true, null));
        TextCreator.get().reset();

    }


    // using en.cphr.from
    public void testDate() throws Exception {
        l("testDate");
        copyCphr("en.cphr.from", "en.cphr");
        String result;

        email = new NotificationEmail(9999,
                                      "Date: Mon, 16 Dec 2002 13:50:15 +0100\r\n"
                                      + "Content-type:text/plain\r\n"
                                      + "From:nisse@host.domain\r\n"
                                      + "X-priority:3\r\n"
                                      + "Subject:testemail\r\n\r\n"
                                      + "template test");
        email.setReceivedDate(when);
        user = new TestUser1();

        result = TextCreator.get().generateText(inbox, email, user, "d", true, null);
            assertEquals("Date 14:20/2003-08-22", result);

            //phr.put("h", "Date DATE=ggg"); //Illegal date format
        result = TextCreator.get().generateText(inbox, email, user, "dd", true, null);

        assertEquals("Date 0100", result);

        //phr.put("h", "Date DATE=yyyy-MM-dd HH:mm");
        result = TextCreator.get().generateText(inbox, email, user, "ddd", true, null);
        //log.logMessage(result);
        assertEquals("Date 2003-12-24 01:00", result);

        //phr.put("h", "$TDATE=HH:mm $DDATE=dd:MM:yy $U1");
        result = TextCreator.get().generateText(inbox, email, user, "dddd", true, null);
        //log.logMessage(result);
        assertEquals("$T01:00 $D24:12:03 $U1", result);
    }

  // Note: not yet corrected the ones below

  /*
    public void ignoreConvertedPhone() throws Exception {
        l("testConvertedPhone");

        inbox = new UserMailbox(2, 2, 3, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        user.setTelephoneNumber("+46705354098");
        Properties phr = new Properties();
        //phr.put("h", "This is a phone number \"CONVERTED_PHONE\"");
        assertEquals("This is a phone number \"+46705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty conversion
        //phr.put("CONVERTED_PHONE", "");
        assertEquals("This is a phone number \"+46705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Full conversion
        //phr.put("CONVERTED_PHONE", "+46>0,8>åtta");
        assertEquals("This is a phone number \"070535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty to in prefix
        //phr.put("CONVERTED_PHONE", "+46>,8>åtta");
        assertEquals("This is a phone number \"70535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty to in suffix
        //phr.put("CONVERTED_PHONE", "+46>0,8>");
        assertEquals("This is a phone number \"070535409\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty from in prefix
        //phr.put("CONVERTED_PHONE", ">Phone:,8>åtta");
        assertEquals("This is a phone number \"Phone:+4670535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty from in suffix
        //phr.put("CONVERTED_PHONE", "+46>0,>åtta");
        assertEquals("This is a phone number \"0705354098åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty to and from in prefix
        //phr.put("CONVERTED_PHONE", ">,8>åtta");
        assertEquals("This is a phone number \"+4670535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty to and from in suffix
        //phr.put("CONVERTED_PHONE", "+46>0,>");
        assertEquals("This is a phone number \"0705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Prefix only
        //phr.put("CONVERTED_PHONE", "+46>0");
        assertEquals("This is a phone number \"0705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //phr.put("CONVERTED_PHONE", "+46>0,");
        assertEquals("This is a phone number \"0705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Null conversions
        //phr.put("CONVERTED_PHONE", ">,>");
        assertEquals("This is a phone number \"+46705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //phr.put("CONVERTED_PHONE", ",");
        assertEquals("This is a phone number \"+46705354098\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        TextCreator.get().reset();
    }

    public void ignoreConvertedPhoneMulti() throws Exception {
        l("testConvertedPhoneMulti");
        tc = TextCreator.get();

        inbox = new UserMailbox(2, 2, 3, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();
        user.setTelephoneNumber("+46705354098");
        user2 = new TestUser1();
        user2.setTelephoneNumber("358705354098");
        user3 = new TestUser1();
        user3.setTelephoneNumber("88888888");
        user4 = new TestUser1();
        user4.setTelephoneNumber("47705354098");
        Properties phr = new Properties();
        //phr.put("h", "This is a phone number \"CONVERTED_PHONE\"");
        //Empty first conversion
        //phr.put("CONVERTED_PHONE", "/+46>0,8>åtta");
        assertEquals("This is a phone number \"070535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty last conversion
        //phr.put("CONVERTED_PHONE", "+46>0,8>åtta/");
        assertEquals("This is a phone number \"070535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Empty first and last conversion
        //phr.put("CONVERTED_PHONE", "/+46>0,8>åtta/");
        assertEquals("This is a phone number \"070535409åtta\"",
                     new TextCreator(phr).generateText(inbox, email, user, "h", true, null));
        //Several conversions
        //phr.put("CONVERTED_PHONE", "+46>46252,8>åtta/358>358252/,>999");
        TextCreator tc = new TextCreator(phr);
        assertEquals("This is a phone number \"4625270535409åtta\"",
                     TextCreator.get().generateText(inbox, email, user, "h", true, null));
        assertEquals("This is a phone number \"358252705354098\"",
                     TextCreator.get().generateText(inbox, email, user2, "h", true, null));
        log.logMessage(TextCreator.get().toString(), log.L_DEBUG);
        assertEquals("This is a phone number \"88888888999\"",
                     TextCreator.get().generateText(inbox, email, user3, "h", true, null));
        //Several, hiding conversions
        //phr.put("CONVERTED_PHONE", "+46>46252/4>999");
        tc = new TextCreator(phr);
        assertEquals("This is a phone number \"46252705354098\"",
                     TextCreator.get().generateText(inbox, email, user, "h", true, null));
        assertEquals("This is a phone number \"9997705354098\"",
                     TextCreator.get().generateText(inbox, email, user4, "h", true, null));
       TextCreator.get().reset();
    }

    public void ignoreFormatedDateTag() throws Exception {
        l("testFormatedDateTag");
        Properties phr = new Properties();
        inbox = new UserMailbox(2, 2, 3, false);
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user = new TestUser1();

        //phr.put("h", "DATE=a");
        //phr.put("AM", "am");
        //phr.put("PM", "pm");
        TextCreator tc = new TextCreator(phr);
        SimpleDateFormat sdf = new SimpleDateFormat("a");
        String ampm = sdf.format(new Date());
        assertEquals(ampm.toLowerCase(),
                     TextCreator.get().generateText(inbox, email, user, "h", true, null));

        //phr.put("h", "Date is DATE=yyyy-MM-dd HH:mm a today");
        tc = new TextCreator(phr);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf1.format(new Date());
        ampm = sdf.format(new Date());
        assertEquals("Date is " + date + " " + ampm.toLowerCase() + " today",
                     TextCreator.get().generateText(inbox, email, user, "h", true, null));

        //phr.put("h", "DATE=a");
        //phr.put("AM", "AM");
        //phr.put("PM", "PM");
        TextCreator tc1 = new TextCreator(phr);
        assertEquals(ampm,
                     tc1.generateText(inbox, email, user, "h", true, null));

        //phr.remove("AM");
        //phr.remove("PM");
        //phr.put("h", "Date is DATE=yyyy-MM-dd HH:mm a today");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
        date = sdf.format(new Date());
        TextCreator tc2 = new TextCreator(phr);
        assertEquals("Date is " + date + " today",
                     tc2.generateText(inbox, email, user, "h", true, null));


        //phr.put("h", "TIME");
        //phr.put("AM", "am");
        //phr.put("PM", "pm");
        tc1 = new TextCreator(phr);
        sdf = new SimpleDateFormat("hh:mm");
        date = sdf.format(new Date());
        TestUser2 testusr2 = new TestUser2();
        testusr2.setPreferredTimeFormat("12");
        assertEquals(date + " " + ampm.toLowerCase(),
                     tc1.generateText(inbox, email, testusr2, "h", true, null));

        phr.remove("AM");
        phr.remove("PM");
        tc1 = new TextCreator(phr);
        assertEquals(date + " " + ampm,
                     tc1.generateText(inbox, email, testusr2, "h", true, null));


     }
*/

    private class TestUser1 extends TestUser {
        String ph = "+46705354098";
        public String getUsersDate(Date d) {return "2003-08-22";}
        public String getUsersTime(Date d) {return "14:20";}
    }

    private class TestUser2 extends TestUser {
    }

     private void cphrEn() throws Exception {
        l("cphrEn");
        tc = TextCreator.get();
        email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        user3 = new TestUser1();
        user3.setPreferredLanguage("en_xtest");

        inbox = new UserMailbox(0, 0, 0, 0, false);
        assertEquals("You have no new messages.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(1, 0, 0, 0, false);
        assertEquals("You have one new voice message.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(0, 0, 1, 0, false);
        assertEquals("You have one new email.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(2, 0, 0, 0, false);
        assertEquals("You have 2 new voice messages.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(1, 1, 0, 0, false);
        assertEquals("You have one new voice message and one new fax.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(2, 1, 0, 0, false);
        assertEquals("You have 2 new voice messages and one new fax.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(2, 2, 0, 0, false);
        assertEquals("You have 2 new voice messages and 2 new faxes.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(12, 2, 0, 0, false);
        assertEquals("You have 12 new voice messages and 2 new faxes.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(0, 4, 4, 4, false);
        assertEquals("You have 4 new faxes, 4 new emails and 4 new video messages.",
            TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(1000, 10000, 0, 0, false);
        assertEquals("You have 1000 new voice messages and 10000 new faxes.", TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(1000, 10000, 10, 0, false);
        assertEquals("You have 1000 new voice messages, 10000 new faxes and 10 new emails.",
        TextCreator.get().generateText(inbox, email, user3, "c", true, null));

        inbox = new UserMailbox(1000, 10000, 10, 1, false);
        assertEquals("You have 1000 new voice messages, 10000 new faxes, 10 new emails and one new video message.",
        TextCreator.get().generateText(inbox, email, user3, "c", true, null));






        // Kroatia
       /* TestUser1 user2 = new TestUser1();
        user1.setPreferredLanguage("hr");

        inbox = new UserMailbox(1, 0, 0, 0, false);
        assertEquals("Imate 1 novu glasovnu poruku.", TextCreator.get().generateText(inbox, email, user2, "c", true, null));

        inbox = new UserMailbox(2, 0, 0, 0, false);
        assertEquals("Imate 2 nove glasovne poruke.", TextCreator.get().generateText(inbox, email, user2, "c", true, null));

        inbox = new UserMailbox(4, 0, 0, 0, false);
        assertEquals("Imate 2 nove glasovne poruke.", TextCreator.get().generateText(inbox, email, user2, "c", true, null));

        inbox = new UserMailbox(5, 0, 0, 0, false);
        assertEquals("Imate 5 novih glasovnih poruka.", TextCreator.get().generateText(inbox, email, user2, "c", true, null));

        inbox = new UserMailbox(10, 0, 0, 0, false);
        assertEquals("Imate 10 novih glasovnih poruka.", TextCreator.get().generateText(inbox, email, user2, "c", true, null));

        inbox = new UserMailbox(10, 3, 0, 0, false);
        assertEquals("Imate 10 novih glasovnih poruka i 3 nove fax poruke.",
        TextCreator.get().generateText(inbox, email, user2, "c", true, null));*/
       }

       private void cphrSv() throws Exception {

            l("cphrSv");
            tc = TextCreator.get();
            email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
            user = new TestUser1();
           // Swedish
           TestUser1 user2 = new TestUser1();
           user2.setPreferredLanguage("sv_xtest");

           inbox = new UserMailbox(1, 2, 2, 1, false);
           assertEquals("Du har ett nytt röstmeddelande, 2 nya fax, 2 nya epostmeddelanden och ett nytt videomeddelande.",
           TextCreator.get().generateText(inbox, email, user2, "c", true, null));

           inbox = new UserMailbox(1, 2, 2, 8, false);
           assertEquals("Du har ett nytt röstmeddelande, 2 nya fax, 2 nya epostmeddelanden och 8 nya videomeddelandennnn.",
           TextCreator.get().generateText(inbox, email, user2, "c", true, null));

           inbox = new UserMailbox(1, 2, 2, 6, false);
           assertEquals("Du har ett nytt röstmeddelande, 2 nya fax, 2 nya epostmeddelanden och 6 nya videomeddelanden.",
           TextCreator.get().generateText(inbox, email, user2, "c", true, null));
       }

       /*
       public void testCphrEnStrange() throws Exception {
           tc = TextCreator.get();
           email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:nisse@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
           user4 = new TestUser1();
           user4.setPreferredLanguage("en_strange");

           inbox = new UserMailbox(1, 1, 1, 9, false);
           l("Strange: " + TextCreator.get().generateText(inbox, email, user4, "c", true, null));
           assertEquals("empty stringstarstringtags", TextCreator.get().generateText(inbox, email, user4, "c", true, null));

            inbox = new UserMailbox(1, 1, 1, 8, false);
           assertEquals("starstringtags", TextCreator.get().generateText(inbox, email, user4, "c", true, null));

           inbox = new UserMailbox(1, 0, 0, 0, false);
           assertEquals("test of header", TextCreator.get().generateText(inbox, email, user4, "h", true, null));

           inbox = new UserMailbox(2, 1, 1, 1, false);
           l("Strange: " + TextCreator.get().generateText(inbox, email, user4, "c", true, null));
           assertEquals(" test stringstarstringtags",
           TextCreator.get().generateText(inbox, email, user4, "c", true, null));
       }*/


  private void echo(String content, String toFile) throws Exception {
    String cmd = new String("echo " + content + " > " + toFile);
    Runtime rt = Runtime.getRuntime();
    Process prcs = rt.exec(cmd);

  }


       private void copy(String source, String dest) throws Exception {
           String cmd = new String("cp " + source + " " + dest);
           Runtime rt = Runtime.getRuntime();
           Process prcs = rt.exec(cmd);
       }

        private void remove(String source) throws Exception {
           String cmd = new String("rm -rf " + source);
           Runtime rt = Runtime.getRuntime();
           Process prcs = rt.exec(cmd);
       }
}
