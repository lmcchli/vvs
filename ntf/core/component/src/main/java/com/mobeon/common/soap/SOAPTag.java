/*
 * SOAPTag.java
 * Created on March 26, 2004, 3:41 PM
 */
package com.mobeon.common.soap;

import java.util.*;

/**
 * Represents a SOAP parameter which is enclosed by tags <>
 * Ex: <Priority>Normal</Priority>
 * The toString() method prints out the data on the tag format.
 *
 * The class can contain linked SOAPTags. The value will replaced with
 * the linked tags as value.
 *
 * @author  ermmaha
 */
public class SOAPTag {
    
    private String _name;
    private String _value;
    private List _attributes;
    private List _soapTags;
    
    /**
     * No-arg constructor
     */
    public SOAPTag() {
        
    }
    
    /**
     * @param String name on the tag
     */
    public SOAPTag(String name) {
        _name = name;
    }
    
    /**
     * @param String name on the tag
     * @param String value on the tag
     */
    public SOAPTag(String name, String value) {
        _name = name;
        _value = value;
    }
    
    public SOAPTag(String name, SOAPTag tag) {
        _name = name;
        _soapTags = new ArrayList();
        _soapTags.add(tag);
    }

    /**
     * Adds an attribute to the tag. The attribute string should have the format:
     * key="value"
     * @param String 
     */
    public void addAttribute(String attr) {
        if(_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }
    
    /**
     * Returns an attribute from the tag
     * @param int index
     * @return String
     */
    public String getAttribute(int index) {
        return (_attributes == null) ? null : (String) _attributes.get(index);
    }
    
    /**
     * @param SOAPTag Adds a linked SOAPTag to this tag, the value is set to null
     */
    public void addTag(SOAPTag tag) {
        if(_soapTags == null) {
            _soapTags = new ArrayList();
        }
        _value = null;
        _soapTags.add(tag);
    }
    
    /**
     * @param int index of tag in the list
     * @return SOAPTag null if no tags are added.
     */
    public SOAPTag getTag(int index) {
        if(_soapTags == null) {
            return null;
        }
        return (SOAPTag) _soapTags.get(index);
    }
    
    /**
     * @param String name of tag to be returned
     * @return SOAPTag, null if no tags are added or no tag specified with the 
     * name is found
     */
    public SOAPTag getTag(String name) {
        if(_soapTags == null) {
            return null;
        }
        Iterator it = _soapTags.iterator();
        while(it.hasNext()) {
            SOAPTag tag = (SOAPTag) it.next();
            String tagName = tag.getName();
            if(tagName != null && name.equals(tagName)) {
                return tag;
            }
        }
        return null;
    }
    
    /**
     * @param String Sets the name of the tag
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * @return String the name of the tag
     */
    public String getName() {
        return _name;
    }
    
    /**
     * @param String sets value to the tag all linked tags are set to null.
     */
    public void setValue(String value) {
        _value = value;
        _soapTags = null;
    }
    
    /**
     * @return value for the tag
     */
    public String getValue() {
        return (_value != null) ? _value : "";
    }
    
    public boolean hasTag() {
        return (_soapTags != null);
    }
    
    /**
     * @return String the tag is written out on the SOAP format
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<");
        buf.append(_name);
        if(_attributes != null) {
            buf.append(" ");
            for(int i = 0; i < _attributes.size(); i++) {
                buf.append((String)_attributes.get(i));
                if((i + 1) < _attributes.size()) buf.append(" ");
            }
        }
        
        //If empty value or no linked tags, add the closing tag and return
        //Ex. <name attr/>
        if(getValue().length() == 0 && _soapTags == null) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        if(_soapTags != null) {
            for(int i = 0; i < _soapTags.size(); i++) {
                SOAPTag tag = (SOAPTag) _soapTags.get(i);
                buf.append(tag.toString());
            }
        }
        else{
            buf.append(_value);
        }
        buf.append("</");
        buf.append(_name);
        buf.append(">");
        return buf.toString();
    }
    
    public boolean equals(Object obj) {
        if(!(obj instanceof SOAPTag))
            return false;
        
        SOAPTag toTest = (SOAPTag) obj;
        String str1 = toString();
        String str2 = toTest.toString();
        return str1.equals(str2);
    }
}
