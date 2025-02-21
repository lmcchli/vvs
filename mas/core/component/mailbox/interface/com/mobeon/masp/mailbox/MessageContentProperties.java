/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.io.Serializable;

/**
 * @author QHAST
 */
public class MessageContentProperties implements Serializable,Cloneable {

    private String filename;
    private String description;
    private String language;
    private String duration = null;

    public MessageContentProperties(String filename, String description, String language, String duration) {
        this.filename = filename;
        this.description = description;
        this.language = language;
        this.duration = duration;
    }

    public MessageContentProperties() {
        this(null,null,null,null);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o != null && o.getClass().equals(MessageContentProperties.class)) {
            MessageContentProperties other = (MessageContentProperties) o;
            return other.toString().equals(this.toString());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("filename=");
        sb.append(filename);
        sb.append(",");
        sb.append("description=");
        sb.append(description);
        sb.append(",");
        sb.append("language=");
        sb.append(language);
        sb.append(",");
        sb.append("duration=");
        sb.append(duration);
        sb.append("}");
        return sb.toString();
    }


}
