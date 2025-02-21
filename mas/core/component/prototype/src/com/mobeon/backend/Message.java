package com.mobeon.backend;

import com.mobeon.backend.exception.DataException;
import com.mobeon.backend.exception.SyntaxException;
import com.mobeon.backend.exception.SystemException;
import com.mobeon.backend.exception.TimeoutException;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Message {

    public static final int NR_OF_TYPES=4;	// Number of types below
    public static final int VOICE=0;
    public static final int VIDEO=1;
    public static final int FAX=2;
    public static final int EMAIL=3;

    public static final int URGENT_FIFO=0;
    public static final int FIFO=1;
    public static final int URGENT_LIFO=2;
    public static final int LIFO=3;

    public static final String FROM="From: ";
    public static final String TO="To: ";
    public static final String DATE="Date: ";
    public static final String PHONE="Phone";
    public static final String TYPE="Type";
    public static final String SUBJECT="Subject: ";
    public static final String MEDIA="Media: ";
    public static final String BODY="BODY: ";

    String from;
    Vector toList;
    String date;
    int type;
    String state;
    Vector bodyList;
    String UniqueMessageId;	// Filename of the message in the stub
    Vector mediaList;
    String phone;
    String subject;
    MessageFlags flags;

    /**
     * Creates a new empty message.
     */
    public Message() {
        toList = new Vector();
        bodyList = new Vector();
        mediaList = new Vector();
        flags = new MessageFlags();
    }

    /**
     * Creates a reference to an existing message.
     * @param  messageId      filename of the message in the stub
     */
    Message(String messageId) {
        UniqueMessageId=messageId;
    }

    /**
     * Returns the UniqueMessageId.
     */
    public String getUID() {
        return UniqueMessageId;
    }

    /**
     * Used to set a value for a parameter in a message.
     * @param  name      the name of the parameter
     * @param  value     the value of the parameter
     */
    public void setValue(String name, Object value) throws DataException {
        if (name == FROM) {
            from = (String) value;
        } else if (name == TO) {
            Vector vecValue = (Vector) value;
            for (int i=0; i < vecValue.size(); i++)
                toList.insertElementAt(vecValue.elementAt(i), i);
        } else if (name == DATE) {
            date = (String) value;
        } else if (name == PHONE) {
            phone = (String) value;
        } else if (name == TYPE) {
            Integer typeInt = (Integer) value;
            type = typeInt.intValue();
        } else if (name == SUBJECT) {
            subject = (String) value;
        } else if (name == BODY) {
            Vector vecValue = (Vector) value;
            for (int i=0; i < vecValue.size(); i++)
                bodyList.insertElementAt(vecValue.elementAt(i), i);
        } else if (name == MEDIA) {
            Vector vecValue = (Vector) value;
            for (int i=0; i < vecValue.size(); i++)
                mediaList.insertElementAt(vecValue.elementAt(i), i);
        } else {
            throw new DataException("Message.getHeader.NotAllowed:" + name);
        }

    }

    /**
     * Returns the value of the specified parameter in the header of a message.
     * @param  name      the name of the parameter
     */
    public Object getHeader(String name) throws SystemException, DataException, SyntaxException, TimeoutException {
        Object result = null;
        String theLine;
        String value;
        int pos;
        int res;
        Vector vec = new Vector();
        boolean vectorSet = false;

        try {
            BufferedReader file = new BufferedReader(new FileReader(UniqueMessageId));
            while ((theLine = file.readLine()) != null) {
                if ((pos = theLine.indexOf(name)) != -1) {
                    value = theLine.substring(theLine.indexOf(":") + 1);
                    if ((name == TO) | (name == BODY)) {
                        vec.addElement(value);
                        vectorSet = true;
                    }
                    else {
                        result = value;
                    }
                }
            }
        } catch(FileNotFoundException e) {
            throw new DataException("Message.getHeader.FileNotFoundException:" + UniqueMessageId);
        } catch(IOException e) {
            throw new SystemException("Message.getHeader.IOException:" + UniqueMessageId);
        }
        if (vectorSet)
            result = vec;
        return result;
    }

    /**
     * Returns the value of the specified parameter in the specified body of a message.
     * @param  bodyIndex      the index of the body
     * @param  attribute      the name of the parameter
     */
    public Object getBody(String bodyIndex, String attribute) throws SystemException, DataException, SyntaxException, TimeoutException {
        String result = null;
        String theLine;
        String value;
        int pos;
        int res;
        boolean correctBody = false;

        try {
            BufferedReader file = new BufferedReader(new FileReader(UniqueMessageId));
            while ((theLine = file.readLine()) != null) {
                if ((pos = theLine.indexOf(bodyIndex)) != -1) {
                    correctBody = true;
                }
                else if ((pos = theLine.indexOf(BODY)) != -1) {
                    correctBody = false;
                }
                if (correctBody) {
                    if ((pos = theLine.indexOf(attribute)) != -1) {
                        value = theLine.substring(theLine.indexOf(":") + 1);
                        result = value;
                    }
                }
            }
        } catch(FileNotFoundException e) {
            throw new SystemException("Message.getBody.FileNotFoundException:" + bodyIndex + ", " + attribute);
        } catch(IOException e) {
            throw new SystemException("Message.getBody.IOException:" + bodyIndex + ", " + attribute);
        }

        if (result == null) {
            throw new DataException("TerminalSubscription.getProfile.DataException.NotFound:" + attribute);
        }
        result = result.trim();
        return result;
    }

    /**
     * Set flags to a message.
     * @param  flags      the flags that shall be set on the message
     */
    public void setFlags(MessageFlags flags) throws SystemException, DataException, SyntaxException, TimeoutException {
        if (UniqueMessageId != null) { // A message exist in the file system
            try {
                BufferedReader file = new BufferedReader(new FileReader(UniqueMessageId));
                Vector updatedFile = new Vector();
                String updatedLine = "";
                String theLine;
                int pos;
                boolean firstLine = true;
                while ((theLine = file.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
//                        System.out.println("theLine:" + theLine);
                        if (flags.get(MessageFlags.SEEN) == MessageFlags.SET)
                            updatedLine = theLine + " SEEN";
                        else if (flags.get(MessageFlags.SEEN) == MessageFlags.NOT_SET) {
                            if ((pos= theLine.indexOf("SEEN")) != -1) {
                                if (theLine.length() > pos+5)
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+5);
                                else
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+4);
                            } else {
                                updatedLine = theLine;
                            }
                        }
                        if (flags.get(MessageFlags.SAVED) == MessageFlags.SET)
                            updatedLine = theLine + " SAVED";
                        else if (flags.get(MessageFlags.SAVED) == MessageFlags.NOT_SET) {
                            if ((pos= theLine.indexOf("SAVED")) != -1) {
                                if (theLine.length() > pos+6)
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+6);
                                else
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+5);
                            } else {
                                updatedLine = theLine;
                            }
                        }
                        if (flags.get(MessageFlags.URGENT) == MessageFlags.SET)
                            updatedLine = theLine + " URGENT";
                        else if (flags.get(MessageFlags.URGENT) == MessageFlags.NOT_SET) {
                            if ((pos= theLine.indexOf("URGENT")) != -1) {
                                if (theLine.length() > pos+7)
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+7);
                                else
                                    updatedLine =  theLine.substring(0,pos) + theLine.substring(pos+6);
                            } else {
                                updatedLine = theLine;
                            }
                        }
//                        System.out.println("updatedLine:" + updatedLine);
                        updatedFile.addElement(updatedLine);
                    }
                    else {
                        updatedFile.addElement(theLine);
                    }
                }
                BufferedWriter outFile = new BufferedWriter(new FileWriter(UniqueMessageId));
                for (int i=0; i<updatedFile.size(); i++) {
                    outFile.write(updatedFile.elementAt(i) + "\n");
                }
                outFile.flush();
                outFile.close();
            } catch(FileNotFoundException e) {
                throw new SystemException("Message.setFlags.FileNotFoundException:");
            } catch(IOException e) {
                throw new SystemException("Message.setFlags.IOException:");
            }
        } else {     // This is the case if the message has not been sent
            this.flags = new MessageFlags(flags);
        }
    }
    /**
     * Sends a message with the parameters that have been set.
     */
    public void send() throws SystemException, DataException, SyntaxException, TimeoutException {
        int i;
        BufferedWriter outFile;
        long unique = System.currentTimeMillis();
//        System.out.println("System.currentTimeMillis:" + unique);
        String dir = System.getProperty("StoragePath") + phone + "/Inbox/";
        String msgName = dir + "msg" + String.valueOf(unique) + ".txt";
//        System.out.println("msgName:" + msgName);

        try {
            outFile = new BufferedWriter(new FileWriter(msgName));
            if (type == VOICE)
                outFile.write("VOICE ");
            else if (type == VIDEO)
                outFile.write("VIDEO ");
            else if (type == FAX)
                outFile.write("FAX ");
            else if (type == EMAIL)
                outFile.write("EMAIL ");
            if (flags.get(MessageFlags.SEEN) == MessageFlags.SET)
                outFile.write("SEEN ");
            if (flags.get(MessageFlags.SAVED) == MessageFlags.SET)
                outFile.write("SAVED ");
            if (flags.get(MessageFlags.URGENT) == MessageFlags.SET)
                outFile.write("URGENT ");
            outFile.write("\n");
            outFile.write(SUBJECT + subject + "\n");
            outFile.write(DATE + date + "\n");
            outFile.write(FROM + from + "\n");
            outFile.write(TO);
            for (i=0; i < toList.size(); i++) {
                outFile.write(toList.elementAt(i) + "; ");
            }
            outFile.write("\n");
            for (i=0; i < bodyList.size(); i++) {
                outFile.write("\n" + BODY + bodyList.elementAt(i) + "\n");
                if (i < mediaList.size()) {
                    unique = System.currentTimeMillis();
                    String mediaDir = System.getProperty("StoragePath") + phone + "/Media/";
                    String origFile = (String) mediaList.elementAt(i);
                    String extension = "";
                    if (origFile.endsWith(".wav"))
                        extension = ".wav";
                    else if (origFile.endsWith(".mov"))
                        extension = ".mov";
                    String mediaFile = "media" + String.valueOf(unique) + extension;
                    outFile.write("\t\t" + MEDIA + " " + mediaDir + mediaFile + "\n");
//                    System.out.println("origFile:" + origFile);
//                    System.out.println("mediaDir:" + mediaDir);
//                    System.out.println("mediaFile:" + mediaFile);

                    try {
                        BufferedInputStream input = new BufferedInputStream(new FileInputStream(origFile));
                        try {
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(mediaDir + mediaFile));
                            int bits;
                            while ((bits = input.read()) != -1) {
                                output.write(bits);
                            }
                            input.close();
                            output.close();
                        } catch(FileNotFoundException e) {
                            throw new DataException("Message.send.FileNotFoundException:" + mediaDir + mediaFile);
                        } catch(IOException e) {
                            throw new SystemException("Message.send.IOException:" + mediaDir + mediaFile);
                        }
                    } catch(FileNotFoundException e) {
                        throw new DataException("Message.send.FileNotFoundException:" + origFile);
                    } catch(IOException e) {
                        throw new SystemException("Message.send.IOException:" + origFile);
                    }
                }
            }
            outFile.flush();
            outFile.close();
        } catch(FileNotFoundException e) {
            throw new DataException("Message.send.FileNotFoundException:" + msgName);
        } catch(IOException e) {
            throw new SystemException("Message.send.IOException:" + msgName);
        }
    }
}


