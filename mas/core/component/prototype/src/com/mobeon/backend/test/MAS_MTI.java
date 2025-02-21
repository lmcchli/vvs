package com.mobeon.backend.test;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import com.mobeon.backend.*;
import com.mobeon.backend.exception.*;
import com.mobeon.util.ErrorCodes;

public class MAS_MTI {


    public static void main(String argv[]) {
        TerminalSubscription mySub = null;
        try {
            mySub = new TerminalSubscription("161066");
        } catch (NoUserException e) {
            System.out.println("The specified user is not found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(ErrorCodes.BACKEND_ERROR);
        }
        int result;
        int i,j;
        Message message;
        Vector to = new Vector();
        Vector bodyList = new Vector();
        Vector mediaList = new Vector();
        String from = null;
        String date = null;
        String media = null;
        String body;
        long autoplay = 0;
        long badLoginCount_old = 0;
        String res ="";
        Long badLoginCount = null;
        Boolean fastLoginEnabled = null;
        String activeGreeting = null;
        Vector emTuiBlockMenu = new Vector();
        Vector listOfIntegers = new Vector();
        MessageFlags flags = new MessageFlags();


        try {
            badLoginCount = (Long) mySub.getProfile("BadLoginCount");
            activeGreeting = (String) mySub.getProfile("ActiveGreetingId");
            fastLoginEnabled = (Boolean) mySub.getProfile("FastLoginEnabled");
            emTuiBlockMenu = (Vector) mySub.getProfile("emTuiBlockMenu");
            listOfIntegers = (Vector) mySub.getProfile("listOfIntegers");
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        System.out.println("BadLoginCount=" + badLoginCount);
        System.out.println("ActiveGreetingId=" + activeGreeting);
        System.out.println("FastLoginEnabled=" + fastLoginEnabled);
        for (i=0; i < emTuiBlockMenu.size(); i++) {
            System.out.println("emTuiBlockMenu(" + i + ")=" + emTuiBlockMenu.elementAt(i));
        }
        for (i=0; i < listOfIntegers.size(); i++) {
            System.out.println("listOfIntegers(" + i + ")=" + listOfIntegers.elementAt(i));
        }

        try {
            mySub.setProfile("ActiveGreetingId", "NoAnswer, OutOfHours, Busy");
            mySub.setProfile("BadLoginCount", new Long(3));
            mySub.setProfile("FastLoginEnabled", new Boolean(false));
            mySub.setProfile("BadLoginCountNew", new Long(5));
            mySub.setProfile("ActiveGreetingIdNew", "AllCalls, Temporary");
            mySub.setProfile("FastLoginEnabledTrue", new Boolean(true));
            mySub.setProfile("FastLoginEnabledFalse", new Boolean(false));

        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        Vector attrList = new Vector();

        Greeting greeting = null;
        try {
            greeting = mySub.getGreeting(Greeting.CALLER_DEPENDENT, Greeting.VIDEO, "161098");
            System.out.println("CdgVideo=" + greeting.getFilename());
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }
        try {
            greeting = mySub.getGreeting(Greeting.NO_ANSWER, Greeting.VOICE, null);
            System.out.println("NoAnswerVoice=" + greeting.getFilename());
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        greeting = new Greeting();
        greeting.setFilename("data/Storage/britney.wav");
        try {
            mySub.setGreeting(Greeting.NO_ANSWER, Greeting.VOICE, null, greeting);
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        greeting.setFilename("H:\\H263_LogoCartoon_15fps.mov");
        try {
            mySub.setGreeting(Greeting.CALLER_DEPENDENT, Greeting.VIDEO, "161098", greeting);
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        Folder inbox = mySub.getFolder("Inbox");
        Folder trash = mySub.getFolder("Trash");

        BitSet type = new BitSet();
        type.set(Message.VIDEO);
        MessageFlags flagsAll = new MessageFlags();
        flags.set(MessageFlags.SEEN, MessageFlags.SET);
        flags.set(MessageFlags.URGENT, MessageFlags.NOT_SET);

        try {
            int videoAll = inbox.getNumberOf(type, flagsAll);
            System.out.println("videoAll=" + videoAll);
            int videoSeenUrgent = inbox.getNumberOf(type, flags);
            System.out.println("videoSeenUrgent=" + videoSeenUrgent);
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        Vector msgList = new Vector();
        try {
            inbox.getMessageList(msgList, type, flags, Message.FIFO);
            inbox.getMessageList(msgList, type, flagsAll, Message.FIFO);
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

        for (i=0; i<msgList.size(); i++) {
            Message msg = (Message) msgList.elementAt(i);
            System.out.println("\nUniqueMessageId=" + msg.getUID());
            try {
                from = (String) msg.getHeader(Message.FROM);
                to = (Vector) msg.getHeader(Message.TO);
                date = (String) msg.getHeader(Message.DATE);
                bodyList = (Vector) msg.getHeader(Message.BODY);
            } catch(DataException e) {
                System.out.println("DataException: " + e.getMessage());
            } catch(SyntaxException e) {
                System.out.println("SyntaxException: " + e.getMessage());
            } catch(SystemException e) {
                System.out.println("SystemException: " + e.getMessage());
            } catch(TimeoutException e) {
                System.out.println("TimeoutException: " + e.getMessage());
            }
            System.out.println("From=" + from);
            for (j=0; j<to.size(); j++) {
                System.out.println("To=" + to.elementAt(j));
            }
            System.out.println("Date=" + date);
            for (j=0; j<bodyList.size(); j++) {
                body = (String) bodyList.elementAt(j);
                System.out.println("BodyList(" + j + ")=" + body);
                try {
                    media = (String) msg.getBody(body, Message.MEDIA);
                } catch(DataException e) {
                    System.out.println("DataException: " + e.getMessage());
                } catch(SyntaxException e) {
                    System.out.println("SyntaxException: " + e.getMessage());
                } catch(SystemException e) {
                    System.out.println("SystemException: " + e.getMessage());
                } catch(TimeoutException e) {
                    System.out.println("TimeoutException: " + e.getMessage());
                }
                System.out.println("Media=" + media);
            }
            MessageFlags f = new MessageFlags();
            f.set(MessageFlags.URGENT, MessageFlags.NOT_SET);
            try {
                msg.setFlags(f);
            } catch(DataException e) {
                System.out.println("DataException: " + e.getMessage());
            } catch(SyntaxException e) {
                System.out.println("SyntaxException: " + e.getMessage());
            } catch(SystemException e) {
                System.out.println("SystemException: " + e.getMessage());
            } catch(TimeoutException e) {
                System.out.println("TimeoutException: " + e.getMessage());
            }
        }

        // SEND MESSAGE
        Message depositMsg = new Message();
        try {
//            depositMsg.setValue("Gurka", "This is the from address");
            depositMsg.setValue(Message.TYPE, new Integer(Message.VIDEO));
            depositMsg.setValue(Message.FROM, "This is the from address");
            to.removeAllElements();
            to.addElement("This is the first to address");
            to.addElement("This is the second to address");
            depositMsg.setValue(Message.TO, to);
            depositMsg.setValue(Message.DATE, "This is the date");
            depositMsg.setValue(Message.PHONE, "161066");
            bodyList.removeAllElements();
            bodyList.addElement("voiceBody 1");
            bodyList.addElement("voiceBody 2");
            depositMsg.setValue(Message.BODY, bodyList);
            mediaList.removeAllElements();
            mediaList.addElement("H:\\message9.wav");
            mediaList.addElement("H:\\message9.mov");
            depositMsg.setValue(Message.MEDIA, mediaList);
            depositMsg.setValue(Message.SUBJECT, "Voice message from MAS");
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        }
        try {
            depositMsg.send();
            flags = new MessageFlags();
            flags.set(MessageFlags.URGENT, MessageFlags.SET);
            depositMsg.setFlags(flags);
            depositMsg.send();
        } catch(DataException e) {
            System.out.println("DataException: " + e.getMessage());
        } catch(SyntaxException e) {
            System.out.println("SyntaxException: " + e.getMessage());
        } catch(SystemException e) {
            System.out.println("SystemException: " + e.getMessage());
        } catch(TimeoutException e) {
            System.out.println("TimeoutException: " + e.getMessage());
        }

    }

    MAS_MTI() {

    }


}


