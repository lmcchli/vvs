package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 12, 2006
 * Time: 4:23:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MediaQualifierMock implements IMediaQualifier {

    String name;
    Gender gender;
    
    public MediaQualifierMock(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QualiferType getType() {
        return QualiferType.Number;
    }

    public Object getValue() {
        return new Object();
    }

    public Class getValueType() {
        return this.getClass();
    }

    public Gender getGender() {
        return Gender.Female;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
