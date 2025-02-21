package com.mobeon.masp.execution_engine.runtime;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 20, 2006
 * Time: 4:24:36 PM
 * To change this template use File | Settings | File Templates.
 *
 * The purpose of this class is to be used as a special "mark" when
 * prompts are played. Once the PlayableObjectPlayer reaches this object,
 * it means all prompts are played and it is time to start the "no input"
 * timeout.
 */

public class StartNoInputTimer extends PlayableObjectImpl {

    private String timeout;
    public StartNoInputTimer(String timeout){
        this.timeout = timeout;
    }

    public String getTimeout(){
        return timeout;
    }
}
