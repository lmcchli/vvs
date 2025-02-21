package com.mobeon.masp.operateandmaintainmanager;

import java.io.Serializable;
import java.util.Date;
import java.util.Calendar;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

/**
 * This class stores counter values for a spescific type, result and direction.
 */
public class CounterData implements Serializable {
    private CallType type;
    private CallResult result;
    private CallDirection direction;
    //private String direction;
    private Long counter;
    private Long peakValue;
    private Date peakTime;


    /**
     * Constructor
     * Creates a counter entry for the counter defined by type,result and direction.
     * @param type
     * @param result
     * @param direction
     */
    public CounterData (CallType type ,CallResult result ,CallDirection direction){
        this.type = type;
        this.result = result;
        this.direction = direction;
        this.counter =  (long)0;
        this.peakValue = (long)0;
        this.peakTime = Calendar.getInstance().getTime();
    }

    /**
     * Constructor.
     * Creates a counter entry for the counter defined by type and direction.
     * @param type
     * @param direction
     */
    public CounterData(CallType type, CallDirection direction) {
        this.type = type;
        this.direction = direction;
        this.counter = (long)0;
        this.peakValue = (long)0;
        this.peakTime = Calendar.getInstance().getTime();
    }

    /**
     * Returns type for counter.
     * @return type
     */
    public CallType getType(){
        return type;
    }

    /**
     * Returns result for counter.
     * @return result
     */
    public CallResult getResult(){
        return result;
    }
    /**
     * Returns direction for counter.
     * @return direction
     */
    public CallDirection getDirection(){
        return direction;
    }
    /**
     * Returns counter value.
     * @return counter value
     */
    public Long getCounter(){
        return counter;
    }


    /**
     * Increments counter by 1
     */
    public void incrementCounter(){
        counter++;
        if (counter > peakValue) {
            peakValue = counter;
            peakTime = Calendar.getInstance().getTime();
        }
    }

    /**
     * Decrements counter by 1
     */
    public void decrementCounter(){
        if (counter > 0) {
            counter--;
        }
    }

    /**
     * Returning a string representation of object.
     * @return type, result, direction and counter concatinated
     */
    public String toString(){
        return type+" "+result+" "+direction+" = "+ counter.toString();
    }


    /**
     * Sets conter value.
     * @param counter
     */
    public void setCounter(Long counter) {
        this.counter = counter;
        if (counter > peakValue) {
            peakValue = counter;
            peakTime = Calendar.getInstance().getTime();
        }

    }

    public Long getPeakValue(){
        return this.peakValue;
    }

    public Date getPeakTime(){
        return this.peakTime;
    }
}
