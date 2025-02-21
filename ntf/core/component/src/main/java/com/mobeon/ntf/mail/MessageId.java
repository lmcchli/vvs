/*
 * MessageId.java
 *
 * Created on den 6 oktober 2006, 12:28
 */

package com.mobeon.ntf.mail;

import java.util.*;

/****************************************************************
 * The class MessageId serves as a mailstore-unique message identifier, by
 * adding a folder id to the folder-unique message-id. It also stores the header
 * parameter Message-Id.
 */
public class MessageId {
    private int uid;
    private int folderId;
    private int /* time in seconds since January 1, 1970, 00:00:00 GMT*/ _time;
    private String /* from mail header attribute Message-Id*/ _messageID;
    
    /**
     * Stores the message UID and folder ID
     */
    public MessageId(int uid, int folderId) {
        this.uid= uid;
        this.folderId= folderId;
        this._time = (int)System.currentTimeMillis()/1000;
        this._messageID = null;
    }
    
    /**
     * Stores the message header parameter Message-Id
     * The folder-unique message-id will be set to 0.
     */
    public MessageId(String messageId) {
        this.uid=0;
        this.folderId=0;
        this._messageID = messageId;
        this._time = (int)System.currentTimeMillis()/1000;
    }
    
    /**
     * Returns the hashcode of the folder-unique message-id.
     * @return (int) value of the hash code folder-unique message-id.
     *         Value is 0 if folder-unique message-id is not set.
     */
    public int hashCode(){
        return uid^folderId;
    }
    
    /**
     *  Returns the message header parameter Message-Id
     *  @return String value of the message header parameter Message-Id.
     *          Value is null if Message-Id is not set.
     */
    public String getId() { return _messageID; }
    
    /**
     * Returns the time in seconds when object of MessageId was allocated.
     * The time since January 1, 1970, 00:00:00 GMT.
     * @returns (int) value of the time when MessageId was allocated.
     */
    public int getTime() { return _time; }
    
    /**
     * Compares two objects of MessageID with value of the folder-unique
     * message-id.
     * @return true if the folder-unique message-id is equal.
     *         Return false if the folder-unique message-id is not the same
     *         and if the hash code is (0) in object.
     */
    public boolean equals(Object o){
        if(o == null) return false;
        if(hashCode() == 0) return false;
        if(!(o instanceof MessageId)) return false;
        MessageId temp = (MessageId)o;
        if(temp.uid != uid) return false;
        return temp.folderId == folderId;
    }
}
