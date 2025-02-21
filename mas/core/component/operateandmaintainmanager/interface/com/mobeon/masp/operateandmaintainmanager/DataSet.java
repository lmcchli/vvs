package com.mobeon.masp.operateandmaintainmanager;

import java.util.*;

import java.io.*;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 *
 */

/**
 * This class stores counter values defined by type, result and direction.
 * Each uniqe combination of type,result and direction will have its own counter.
 * With this class you can filter on each of these types (type,result,direction)
 * to collect correct counter values.
 * This class is able to store the whole DataSet on disk and to restore DataSet on restart.
 */
public class DataSet implements Serializable {

    private Hashtable<String,CounterData> counterRows;
    private Vector<CallType> types;
    private Vector<CallResult> results;
    private Vector<CallDirection> directions;

    //private Long upTime;
    /**
     * Constructor
     */
    DataSet (){
       counterRows = new  Hashtable<String,CounterData>();
       types = new Vector<CallType>();
       results = new Vector<CallResult>();
       directions = new Vector<CallDirection>();
    }

    /**
     * Sets this DataSet to
     * @param dataSet
     */
    public DataSet(DataSet dataSet) {
        this.counterRows = dataSet.getCounterRows();
    }

    //public void setUptime(Long time){
    //    upTime = time;
    //}

    //public Long getUpTime(){
    //    return upTime;
    //}

    /**
     * Returns a copy of counter data.
     * @return Hashtable containing counter data.
     */
    public Hashtable<String, CounterData> getCounterRows() {
        return (Hashtable<String, CounterData>)counterRows.clone();
    }


    /**
     * Sets counter data from a Hashtable.
     * @param counterRows
     */
    public void setCounterRows(Hashtable<String, CounterData> counterRows) {
       this.counterRows = counterRows;
    }


    /**
     * Increment counter values defined by type, result and direction.
     * @param type
     * @param direction
     */
    public synchronized void incrementCounter(CallType type, CallDirection direction){
        CounterData counter;
        String key = type+":"+direction;
        // add counter data to dataset
        if (counterRows.containsKey(key) ) {
            counter = counterRows.get(key);
            counter.incrementCounter();
        } else {
            counter = new CounterData(type,direction);
            counter.incrementCounter();
            counterRows.put(key,counter);
        }

        // add to type list.
        if ( !types.contains(type) ){
            types.add(type);
        }

        // add to direction list
        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }

    /**
     * Increment counter values defined by type, result and direction.
     * @param type
     * @param direction
     */
    public synchronized void decrementCounter(CallType type, CallDirection direction){
        CounterData counter;
        String key = type+":"+direction;
        // add counter data to dataset
        if (counterRows.containsKey(key) ) {
            counter = counterRows.get(key);
            counter.decrementCounter();
        } else {
            counter = new CounterData(type,direction);
            counterRows.put(key,counter);
        }

        // add to type list.
        if ( !types.contains(type) ){
            types.add(type);
        }

        // add to direction list
        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }



    /**
     * Increment counter values defined by type, result and direction.
     * @param type
     * @param result
     * @param direction
     */
    public synchronized void incrementCounter(CallType type,CallResult result, CallDirection direction){
        CounterData counter;
        String key = type+":"+result+":"+direction;
        // add counter data to dataset
        if (counterRows.containsKey(key) ) {
            counter = counterRows.get(key);
            counter.incrementCounter();
        } else {
            counter = new CounterData(type,result,direction);
            counter.incrementCounter();
            counterRows.put(key,counter);
        }

        // add to type list.
        if ( !types.contains(type) ){
            types.add(type);
        }

        // add to result list.
        if ( !results.contains(result) ){
            results.add(result);
        }

        // add to direction list
        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }

    /**
     * Increment counter values defined by type, result and direction.
     * @param type
     * @param result
     * @param direction
     */
    public synchronized void decrementCounter(CallType type,CallResult result, CallDirection direction){
        CounterData counter;
        String key = type+":"+result+":"+direction;
        // add counter data to dataset
        if (counterRows.containsKey(key) ) {
            counter = counterRows.get(key);
            counter.decrementCounter();
        } else {
            counter = new CounterData(type,result,direction);
            counterRows.put(key,counter);
        }

        // add to type list.
        if ( !types.contains(type) ){
            types.add(type);
        }

        // add to result list.
        if ( !results.contains(result) ){
            results.add(result);
        }

        // add to direction list
        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }

    /**
     * Removes counters fo a specific type and returns the remaining counters.
     * @param type
     * @return Counter values after remove.
     */
    public synchronized DataSet parseType(CallType type){

        for  (Iterator<CounterData> iterator = counterRows.values().iterator(); iterator.hasNext();) {
            CounterData counterData =  iterator.next();
            if(!counterData.getType().equals(type)) {
                iterator.remove();
            }
        }
       return this;
    }

    /**
     * Removes counters fo a specific result and returns the remaining counters.
     * @param result
     * @return Counter values after remove.
     */
    public DataSet parseResult(CallResult result){

        for  (Iterator<CounterData> iterator = counterRows.values().iterator(); iterator.hasNext();) {
            CounterData counterData =  iterator.next();
            if(!counterData.getResult().equals(result)) {
                iterator.remove();
            }
        }
       return this;
    }

    /**
     * Removes counters fo a specific direction and returns the remaining counters.
     * @param direction
     * @return Counter values after remove.
     */
    public DataSet parseDirection(CallDirection direction){

        for  (Iterator<CounterData> iterator = counterRows.values().iterator(); iterator.hasNext();) {
            CounterData counterData =  iterator.next();
            if(!counterData.getDirection().equals(direction)) {
                iterator.remove();
            }
        }
       return this;
    }


    /**
     * Returns a copy of this DataSet.
     * @return DataSet copy.
     */
    public DataSet clone (){
        DataSet set = new DataSet();
        set.setCounterRows((Hashtable<String,CounterData>)this.counterRows.clone() );
        //set.setDirectionList((Hashtable<String,Vector>)this.directionList.clone() );
        //set.setResultList((Hashtable<String,Vector>)this.resultList.clone() );
        //set.setTypeList((Hashtable<String,Vector>)this.typeList.clone() );
        return set;
    }

    /**
     * Filter out counter of a specific type and returns a copy of remaining counters
     * @param type
     * @return DataSet copy of remaining values.
     */
    public DataSet filterType(CallType type){
        DataSet set = this.clone();
        set.parseType(type);
        return set;
    }

    /**
     * Filter out counter of a specific result and returns a copy of remaining counters
     * @param result
     * @return DataSet copy of remaining values.
     */
    public DataSet filterResult(CallResult result){
        DataSet set = this.clone();
        set.parseResult(result);
        return set;
    }

    /**
     * Filter out counter of a specific direction and returns a copy of remaining counters
     * @param direction
     * @return DataSet copy of remaining values.
     */
    public DataSet filterDirection(CallDirection direction){
        DataSet set = this.clone();
        set.parseDirection(direction);
        return set;
    }


    /**
     * Set counter value for a specific type and direction
     * @param type
     * @param direction
     * @param counter
     */
    public synchronized void setCounter(CallType type, CallDirection direction,Long counter){
        CounterData cnt;
        String key = type+":"+direction;

        if (counterRows.containsKey(key) ) {
            cnt = counterRows.get(key);
            cnt.setCounter(counter);
        } else {
            cnt = new CounterData(type,direction);
            cnt.setCounter(counter );
            counterRows.put(key,cnt);
        }
        //System.out.println("     "+ key+" "+counter );
        if ( !types.contains(type) ){
            types.add(type);
        }

        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }


    /**
     * Set counter value for a specific type, result and  direction.
     * @param type
     * @param result
     * @param direction
     * @param counter
     */
    public synchronized void setCounter(CallType type,CallResult result, CallDirection direction,Long counter){
        CounterData cnt;
        String key = type+":"+result+":"+direction;
        // add counter data to dataset
        if (counterRows.containsKey(key) ) {
            cnt = counterRows.get(key);
            cnt.setCounter(counter);
        } else {
            cnt = new CounterData(type,result,direction);
            cnt.setCounter(counter);
            counterRows.put(key,cnt);
        }

        if ( !types.contains(type) ){
            types.add(type);
        }

        if ( !results.contains(result) ){
            results.add(result);
        }

        if ( !directions.contains(direction) ){
            directions.add(direction);
        }

    }


    /**
     * Sum all counter values for all types,result and directions.
     * @return counter value
     */
    public Long sumCounters(){
        long sum = (long) 0;
        for (CounterData counterData : counterRows.values()) {
            sum = sum + counterData.getCounter();
        }

       return sum;

    }

    public Long getPeakValue(){
        Long sum = (long)0;
        for (CounterData counterData : counterRows.values()) {
            sum = sum + counterData.getPeakValue();
        }
       return sum;
    }

    public Long getPeakTime(){
        Long time = (long) 0;

        for (CounterData counterData : counterRows.values()) {
            if (time < counterData.getPeakTime().getTime() ) {
                time = counterData.getPeakTime().getTime();
            }
        }
       return time;
    }


    //public Long getCounter(String type,String result, String direction){
    public Long getCounter(CallType type,CallResult result, CallDirection direction){
        CounterData counter;
        Long counterVal = (long)0;

        String key = type+":"+result+":"+direction;
        if (counterRows.containsKey(key) ) {
            counter = counterRows.get(key);
            counterVal = counter.getCounter();
        }

        return counterVal;
    }


    public Vector<CallType> getTypes() {
      return types;
    }
    public Vector<CallResult> getResults() {
      return results;
    }
    public Vector<CallDirection> getDirections() {
      return directions;
    }


}
