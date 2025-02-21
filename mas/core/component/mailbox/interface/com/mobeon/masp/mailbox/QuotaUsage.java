/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * @author QHAST
 */
public class QuotaUsage {

    private QuotaName name;
    long messageUsage;
    long byteUsage;

    protected QuotaUsage(QuotaName name, long messageUsage, long byteUsage) {
        this.name = name;
        this.messageUsage = messageUsage;
        this.byteUsage = byteUsage;
    }

    protected QuotaUsage(QuotaName name) {
        this(name,-1,-1);
    }

    public QuotaName getName() {
        return name;
    }

    public long getMessageUsage() {
        return messageUsage;
    }

    public long getByteUsage() {
        return byteUsage;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * An other object is considered equal to this object if
     * it is an instance of QuotaUsage and has the same name.
     * @param o other object
     * @return true if other object is equal to this object.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof QuotaUsage) {
            QuotaUsage other = (QuotaUsage) o;
            return other.name.equals(this.name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("QuotaUsage{Name=");
        sb.append(name);
        sb.append(",byteUsage=");
        sb.append(byteUsage);
        sb.append(",messageUsage=");
        sb.append(messageUsage);
        sb.append("}");
        return sb.toString();

    }


}
