package com.mobeon.backend;

import com.mobeon.backend.exception.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * TerminalSubscription is used to get and to set information for a specific
 * subscriber.
 */
public class TerminalSubscription {

    BufferedReader subDataFile;
    BufferedWriter outFile;
    String subDataFileName;
    String subscriber;
    Hashtable folders;

    public TerminalSubscription(String sub) throws NoUserException {
        subscriber = sub;
        boolean userFound = false;
        subDataFileName = System.getProperty("StoragePath") + subscriber + "/Profile.txt";
        try {
            File f = new File(subDataFileName);
            if (f.isFile()) {
                userFound = true;
            }
        }
        catch(Exception e){
        }
        if (!userFound)
            throw new NoUserException("The user " + subscriber + " is not registered in the system.");
        folders = new Hashtable(2);	// Today Inbox and Trash
    }

    /**
     * Retrieves a number of specified attribute values from the subscribers profile.
     * Returns the value of the attribute as an object. May be for example String, Long, Boolean, Vector.
     * @param  attribute  the name of the requested attribute.
     */
    public Object getProfile(String attribute) throws SystemException, DataException, SyntaxException, TimeoutException {
        String theLine, lowLine;
        String value = "";
        String myAttribute;
        String stringValue;
        Object result = null;
        Vector arrayString = new Vector();
        Vector arrayLong = new Vector();
        int res;
        int pos;
        int index = 0;
        myAttribute = attribute.toLowerCase() + "=";

        try {
            subDataFile = new BufferedReader(new FileReader(subDataFileName));
            while ((theLine = subDataFile.readLine()) != null) {
                lowLine = theLine.toLowerCase();
                if (lowLine.indexOf(myAttribute) != -1) {
                    if (theLine.indexOf("=") != -1) {
                        pos = theLine.indexOf("=") + 1;
                        if (lowLine.startsWith("s")) { 		// String
                            result = theLine.substring(pos);
                        }
                        else if (lowLine.startsWith("l")) {	// Long
                            stringValue = theLine.substring(pos);
                            try {
                                result = new Long(stringValue);
                            } catch (NumberFormatException e) {
                                throw new SyntaxException("TerminalSubscription.getProfile.NumberFormatException:" + stringValue);
                            }
                        }
                        else if (lowLine.startsWith("b")) {	// Boolean
                            stringValue = theLine.substring(pos);
                            if (stringValue.equalsIgnoreCase("yes")) {
                                result = new Boolean(true);
                            }
                            else if (stringValue.equalsIgnoreCase("no")) {
                                result = new Boolean(false);
                            }
                            else {
                                throw new SyntaxException("Boolean attribute " + myAttribute + " has not value yes or no.");
                            }
                        }
                        else if (lowLine.startsWith("as")) {	// ArrayString
                            arrayString.insertElementAt(theLine.substring(pos), index++);
                        }
                        else if (lowLine.startsWith("al")) {	// ArrayLong
                            stringValue = theLine.substring(pos);
                            try {
                                arrayLong.insertElementAt(new Long(stringValue), index++);
                            } catch (NumberFormatException e) {
                                throw new SyntaxException("TerminalSubscription.getProfile.NumberFormatException:" + stringValue);
                            }
                        }
                    }
                }
            }
        } catch(FileNotFoundException e) {
            throw new DataException("TerminalSubscription.getProfile.FileNotFoundException:" + subDataFileName);
        } catch(IOException e) {
            throw new SystemException("TerminalSubscription.getProfile.IOException:" + subDataFileName);
        }

        if (arrayString.size() > 0)
            result = arrayString;
        else if (arrayLong.size() > 0)
            result = arrayLong;
        else if (result == null) {
            throw new DataException("TerminalSubscription.getProfile.DataException.NotFound:" + attribute);
        }
        return result;
    }

    /**
     * Sets the specified value for the specified attribute on the subscribers profile.
     * @param  name   a String containing the name of the attribute.
     * @param  value  an Object containing the value for the attribute. Only String, Long and Boolean supported.
     */
    public void setProfile(String name, Object value) throws SystemException, DataException, SyntaxException, TimeoutException {

        String theLine, origLine;
        String myAttribute;
        int pos;
        String type = "";
        boolean found = false;
        Vector updatedFile = new Vector();
        String updatedLine = "";

        try {
            myAttribute = name.toLowerCase() + "=";
            subDataFile = new BufferedReader(new FileReader(subDataFileName));
            while ((origLine = subDataFile.readLine()) != null) {
                theLine = origLine.toLowerCase();
                type = origLine.substring(0,1);
                if (theLine.indexOf(myAttribute) != -1) {
                    if (theLine.indexOf("=") != -1) {
                        if (value instanceof String) {
                            if (!type.startsWith("S")) {
                                throw new DataException("TerminalSubscription.getProfile.DataException.NotCorrectType:" + name);
                            }
                        }
                        else if (value instanceof Long) {
                            if (!type.startsWith("L")) {
                                throw new DataException("TerminalSubscription.getProfile.DataException.NotCorrectType:" + name);
                            }
                        }
                        else if (value instanceof Boolean) {
                            if (!type.startsWith("B")) {
                                throw new DataException("TerminalSubscription.getProfile.DataException.NotCorrectType:" + name);
                            }
                            Boolean bool = (Boolean) value;
                            if (bool.booleanValue() )
                                value = new String("yes");
                            else
                                value = new String("no");
                        }
                        updatedLine = type + "\t" + name + "=" + value;
                        updatedFile.addElement(updatedLine);
                        found = true;
                    }
                }
                else {
                    updatedFile.addElement(origLine);
                }
            }
            if (!found) { // Write new value at end of file
                if (value instanceof String)
                    type = "S\t";
                else if (value instanceof Long)
                    type = "L\t";
                else if (value instanceof Boolean) {
                    type = "B\t";
                    Boolean bool = (Boolean) value;
                    if (bool.booleanValue() )
                        value = new String("yes");
                    else
                        value = new String("no");
                }
                updatedLine = type + name + "=" + value;
                updatedFile.addElement(updatedLine);
            }
            outFile = new BufferedWriter(new FileWriter(subDataFileName));
            for (int i=0; i<updatedFile.size(); i++) {
                outFile.write(updatedFile.elementAt(i) + "\n");
            }
            outFile.flush();
            outFile.close();
        } catch(FileNotFoundException e) {
            throw new DataException("TerminalSubscription.setProfile.FileNotFoundException:" + subDataFileName);
        } catch(IOException e) {
            throw new SystemException("TerminalSubscription.setProfile.IOException:" + subDataFileName);
        }
    }

    /**
     * Retrieves the specified greeting.
     * Returns the reference to the greeting, e.g. a filename.
     * @param  type      the type of the greeting, e.g. AllCalls
     * @param  format    the format of the greeting, voice or video
     * @param  cdgSubId  the phone number for a caller dependent greeting
     */
    public Greeting getGreeting(int type, int format, String cdgSubId) throws SystemException, DataException, SyntaxException, TimeoutException {
        Greeting greeting = new Greeting();
        String extension = "";
        String typ ="";
        String filename;

        if (format == Greeting.VIDEO) {
            extension = ".mov";
        } else if (format == Greeting.VOICE) {
            extension = ".wav";
        }
        switch (type) {
            case Greeting.ALL_CALLS:
                {
                    typ = "AllCalls";
                    break;
                }
            case Greeting.CALLER_DEPENDENT:
                {
                    typ = cdgSubId;
                    break;
                }
            case Greeting.TEMPORARY:
                {
                    typ = "Temporary";
                    break;
                }
            case Greeting.NO_ANSWER:
                {
                    typ = "NoAnswer";
                    break;
                }
            case Greeting.BUSY:
                {
                    typ = "Busy";
                    break;
                }
            case Greeting.OUT_OF_HOURS:
                {
                    typ = "OutOfHours";
                    break;
                }
            case Greeting.ABSENCE:
                {
                    typ = "Absence";
                    break;
                }
            case Greeting.SPOKEN_NAME:
                {
                    typ = "SpokenName";
                    break;
                }
        }
        filename = System.getProperty("StoragePath") + subscriber + "/Media/" + typ + extension;
        try {
            subDataFile = new BufferedReader(new FileReader(filename));
            greeting.setFilename(filename);
        } catch(FileNotFoundException e) {
            throw new DataException("TerminalSubscription.getGreeting.FileNotFoundException:" + filename);
        } catch(IOException e) {
            throw new SystemException("TerminalSubscription.getGreeting.IOException:" + filename);
        }
        return greeting;
    }

    /**
     * Stores the specified greeting.
     * @param  type      the type of the greeting, e.g. AllCalls
     * @param  format    the format of the greeting, voice or video
     * @param  cdgSubId  the phone number for a caller dependent greeting
     * @param  greeting  the reference to the greeting, e.g. a filename
     */
    public void setGreeting(int type, int format, String cdgSubId, Greeting greeting) throws SystemException, DataException, SyntaxException, TimeoutException {
        String theLine;
        String extension = "";
        String typ ="";
        int bits;

        if (format == Greeting.VIDEO) {
            extension = ".mov";
        } else if (format == Greeting.VOICE) {
            extension = ".wav";
        }
        switch (type) {
            case Greeting.ALL_CALLS:
                {
                    typ = "AllCalls";
                    break;
                }
            case Greeting.CALLER_DEPENDENT:
                {
                    typ = cdgSubId;
                    break;
                }
            case Greeting.TEMPORARY:
                {
                    typ = "Temporary";
                    break;
                }
            case Greeting.NO_ANSWER:
                {
                    typ = "NoAnswer";
                    break;
                }
            case Greeting.BUSY:
                {
                    typ = "Busy";
                    break;
                }
            case Greeting.OUT_OF_HOURS:
                {
                    typ = "OutOfHours";
                    break;
                }
            case Greeting.ABSENCE:
                {
                    typ = "Absence";
                    break;
                }
            case Greeting.SPOKEN_NAME:
                {
                    typ = "SpokenName";
                    break;
                }
        }
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(greeting.getFilename()));
            String grtFilename = System.getProperty("StoragePath") + subscriber + "/Media/" + typ + extension;
            try {
                BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(grtFilename));
                while ((bits = input.read()) != -1) {
                    output.write(bits);
                }
                input.close();
                output.close();
            } catch(FileNotFoundException e) {
                throw new DataException("TerminalSubscription.setGreeting.FileNotFoundException:" + grtFilename);
            } catch(IOException e) {
                throw new SystemException("TerminalSubscription.setGreeting.IOException:" + grtFilename);
            }
        } catch(FileNotFoundException e) {
            throw new SystemException("TerminalSubscription.setGreeting.FileNotFoundException:" + greeting.getFilename());
        } catch(IOException e) {
            throw new SystemException("TerminalSubscription.setGreeting.IOException:" + greeting.getFilename());
        }
    }

    /**
     * Returns  the specified folder object. Creates a new folder object if it does not already exist.
     * @param  folderName      the name of the folder.
     */
    public Folder getFolder(String folderName) {
        Folder fold;
        if ((fold=(Folder)folders.get(folderName)) == null) {
            fold = new Folder(subscriber, folderName);
            folders.put(folderName, fold);
        }
        return fold;
    }

}


