/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.string;

import java.io.Serializable;

/**
 * @author QHAST
 */
public class FileName implements Serializable,Cloneable {

    private String fullname;
    private String name;
    private String extension;

    private FileName() {
    }

    public static FileName createFileName(String fullname) {
        FileName fnObject = new FileName();
        fnObject.fullname = fullname;
        int dotPos = fullname.lastIndexOf('.');
        if(dotPos > 0) {
            fnObject.name = fullname.substring(0,dotPos);
            fnObject.extension = fullname.substring(dotPos + 1);
        } else {
            fnObject.name = fullname;
        }
        return fnObject;
    }

    public static FileName createFileName(String name, String extension) {
        FileName fnObject = new FileName();
        fnObject.name = name;
        fnObject.extension = extension;
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        if(extension != null && extension.length()>0) {
            sb.append(".");
            sb.append(extension);
        }
        fnObject.fullname = sb.toString();
        return fnObject;
    }

    public String getFullname() {
        return fullname;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public int hashCode() {
        return fullname.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof FileName) {
            FileName other = (FileName) o;
            return other.fullname.equals(this.fullname);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return fullname;
    }

}
