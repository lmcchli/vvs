package com.mobeon.masp.monitor;

import com.mobeon.masp.rpcclient.RpcMonitor;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
//import com.mobeon.masp.operateandmaintainmanager.DataSet;
//import com.mobeon.masp.operateandmaintainmanager.StatisticMonitorInfo;
import com.mobeon.masp.operateandmaintainmanager.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/*
* Copyright (c) $today.year Mobeon AB. All Rights Reserved.
*/

public class ConsoleLayoutStatistic implements PageLayout {

    ILogger log;

     private String curPage;
    private String layoutDesc;
    private String keyDesc;
    String columnDescription;
    RpcMonitor mop;
    Hashtable<String, Vector> data;
    ConnectionStatistics connectionStatistics;
    Hashtable<String, Vector> printedData;
    Connection conn;

    //private static Hashtable<String, ConsumedServiceEntry> lstConsumedServices; // holds list of registered consumed services
    //private static Hashtable<String,ServiceEnabler> lstServiceEnablers;        // holds list of registered service enablers
    private static ConcurrentHashMap<String, ConsumedServiceEntry> lstConsumedServices; // holds list of registered consumed services
    private static ConcurrentHashMap<String,ServiceEnabler> lstServiceEnablers;        // holds list of registered service enablers


    ColumnBuilder connectionRow;

    Integer layout ;         // Defines witch layot that should be shown.

    public ConsoleLayoutStatistic (Connection conn){

        log = ILoggerFactory.getILogger(ConsoleLayoutStatistic.class);

        data = new Hashtable<String, Vector>();
        printedData = new Hashtable<String, Vector>();

        //log.debug("new RpcMonitor()");
        //mop = new RpcMonitor();
        this.conn = conn ;

        layoutDesc = "Counter statistic";
        keyDesc = "F2=Page 1";

        // Define columns and size
        connectionRow = new ColumnBuilder();
        connectionRow.addColumnWidth(1);  // Change time
        connectionRow.addColumnWidth(47);  // Service enabler name
        //connectionRow.addColumnWidth(15);  // counter type  (current or total)
        //connectionRow.addColumnWidth(10);  // connection type
        //connectionRow.addColumnWidth(15); // Counter Name 1
        //connectionRow.addColumnWidth(10); // Counter Name 2
        connectionRow.addColumnWidth(8);  // Counter Value

        setLayout(1);                     // Set the default page
    }


    public void updateData(){
         //get data from  connection and update datacollection.
         //data = conn.getStatisticData();

         // Refator
         connectionStatistics = conn.getStatisticData();
         lstServiceEnablers = connectionStatistics.getServiceEnablers();
         //conn.getStatisticData()
         if (lstServiceEnablers!=null)
            data = parseData();

    }


    public void keypress(Integer key) {
        // Handle keypres not handeld by the console.
        if (key == 266 ) {      // F2
            setLayout(1);
        }

    }


    private Hashtable<String, Vector> parseData(){
        // Shuld return statistic data via RPC to monitor.
        // This function prepares counter values and strings to be
        // sent to monitor.

        Integer key;

        String protocol;
        DataSet currentCounter;
        DataSet totalCounter;
        StatisticMonitorInfo smi = new StatisticMonitorInfo();

        key = 0;
         // get service enablers

        for (ServiceEnabler se : lstServiceEnablers.values() ) {
            currentCounter = se.getCurrentCounter();
            totalCounter =  se.getTotalCounter();

            smi.put(key.toString() ,"","");
            key++;
            protocol = se.getName(); //.getProtocol(); // TODO..chould it be the name of the service enabler..? getName() ?

            smi.put(key.toString() ,protocol,"");
            //key = protocol + ":CurrentConnections";
            key++;
            smi.put(key.toString() ,"   CURRENT CONNECTIONS","");
            key++;
            smi.put(key.toString() ,"      Voice",":"+currentCounter.filterType(CallType.VOICE ).sumCounters().toString());
            key++;
//            smi.put(key.toString() ,"","","Current connections","",":"+currentCounter.filterType("voice").sumCounters().toString() );
//            key++;
            smi.put(key.toString() ,"         inbound",":"+currentCounter.filterType(CallType.VOICE).filterDirection(CallDirection.INBOUND ).sumCounters().toString() );
            key++;
            smi.put(key.toString() ,"         outbound",":"+currentCounter.filterType(CallType.VOICE).filterDirection(CallDirection.OUTBOUND ).sumCounters().toString() );
            key++;


            smi.put(key.toString() ,"      Video",":"+currentCounter.filterType(CallType.VIDEO ).sumCounters().toString() );
            key++;
//            smi.put(key.toString() ,"","","Current connections","",":"+currentCounter.filterType("video").sumCounters().toString() );
//            key++;
            smi.put(key.toString() ,"         inbound",":"+currentCounter.filterType(CallType.VIDEO).filterDirection(CallDirection.INBOUND ).sumCounters().toString() );
            key++;
            smi.put(key.toString() ,"         outbound",":"+currentCounter.filterType(CallType.VIDEO).filterDirection(CallDirection.OUTBOUND ).sumCounters().toString() );
            key++;


            smi.put(key.toString() ,"","");        // empty row
            key++;

            smi.put(key.toString() ,"   TOTAL CONNECTIONS","");
            key++;
            Vector<CallType> types = totalCounter.getTypes();
            // For all types (Voice / Video )
            for (int i = 0; i < types.size(); i++) {
                CallType type = types.elementAt(i);
                //smi.put(key.toString() ,"",type.getInfo() ,"","",":"+totalCounter.filterType(type).sumCounters().toString() );

                smi.put(key.toString() ,"      "+type.getInfo() ,""); // total connections
                key++;

                Long totalConnected = totalCounter.filterType(type).filterResult(CallResult.CONNECTED).sumCounters();
                Long totalFailed = totalCounter.filterType(type).filterResult(CallResult.FAILED).sumCounters();
                smi.put(key.toString() ,"         Total connections",":"+(totalConnected+totalFailed)); // total connections
                key++;

                // print total connections for incoming & outgoing
                Vector<CallDirection> directions = totalCounter.getDirections();
                for (int j = 0; j < directions.size(); j++) {
                    CallDirection direction =  directions.elementAt(j);
                    Long totalDirectionConnected = totalCounter.filterType(type).filterDirection(direction).filterResult(CallResult.CONNECTED).sumCounters();
                    Long totalDirectionFailed = totalCounter.filterType(type).filterDirection(direction).filterResult(CallResult.FAILED).sumCounters();
                    smi.put(key.toString() ,"         Total "+direction+" connections",":"+(totalDirectionConnected+totalDirectionFailed));
                    //smi.put(key.toString() ,"","",direction,"",":"+totalCounter.filterType(type).filterDirection(direction).sumCounters().toString() );
                    key++;
                }


                /*Vector<String> results = totalCounter.getResults();
                for (int j = 0; j < results.size(); j++) {
                    String result =  results.elementAt(j);
                    smi.put(key.toString() ,"","",result,"",":"+totalCounter.filterType(type).filterResult(result).sumCounters().toString() );
                    key++;

                    //Vector<String> directions = totalCounter.getDirections();
                    for (int k = 0; k < directions.size(); k++) {
                        String direction =  directions.elementAt(k);
                        smi.put(key.toString() ,"","","",direction,":"+totalCounter.filterType(type).filterResult(result).filterDirection(direction).sumCounters().toString() );
                        key++;
                    }
                }
                */

                totalConnected = totalCounter.filterType(type).filterResult(CallResult.CONNECTED).sumCounters();
                smi.put(key.toString() ,"         Total connected connections",":"+totalConnected); // total connections
                key++;

                // print total connected for incoming & outgoing
                for (int k = 0; k < directions.size(); k++) {
                    CallDirection direction =  directions.elementAt(k);
                    Long counter = totalCounter.filterType(type).filterResult(CallResult.CONNECTED).filterDirection(direction).sumCounters();
                    smi.put(key.toString() ,"         Total connected "+direction+" connections",":"+counter);
                    key++;
                }

                Long totalAbandoned = totalCounter.filterType(type).filterResult(CallResult.ABANDONED).sumCounters();
                smi.put(key.toString() ,"         Total abandoned connections",":"+totalAbandoned); // total connections
                key++;

                // print total abandoned for incoming & outgoing
                for (int k = 0; k < directions.size(); k++) {
                    CallDirection direction =  directions.elementAt(k);
                    Long counter = totalCounter.filterType(type).filterResult(CallResult.ABANDONED).filterDirection(direction).sumCounters();
                    smi.put(key.toString() ,"         Total abandoned "+direction+" connections",":"+counter);
                    key++;
                }

                totalFailed = totalCounter.filterType(type).filterResult(CallResult.FAILED).sumCounters();
                smi.put(key.toString() ,"         Total failed connections",":"+totalFailed); // total connections
                key++;

                // print total abandoned for incoming & outgoing
                for (int k = 0; k < directions.size(); k++) {
                    CallDirection direction =  directions.elementAt(k);
                    Long counter = totalCounter.filterType(type).filterResult(CallResult.FAILED).filterDirection(direction).sumCounters();
                    smi.put(key.toString() ,"         Total failed "+direction+" connections",":"+counter);
                    key++;
                }
            }
        }

        return smi.getInfo() ;
    }


    public Hashtable<Integer, String> getData(Boolean all) {
        // returns all rows that are going to be printed
        //Hashtable<Integer, Vector> outData = new Hashtable<Integer, Vector>();
        Hashtable<Integer, String> outData = new Hashtable<Integer, String>();

        // Compare data and send rows that have been updated/removed/added
        outData = prepareData(data);
        // Store printed data to be able to compare data next time
        printedData = data;
        return outData;
    }

    private void setLayout(Integer layout){
            this.layout = layout;

            switch(layout) {
                case 1 :
                    columnDescription = "Statistics";
                    curPage = "Page 1";
                    break;
                default :
                    columnDescription = "";
            }
    }


    private String getCurrentColumnLayout(ColumnBuilder columns){
        //Integer[] columnLayout1 = new Integer[] {1,2,3,4,5};    // connection page 1
        Integer[] columnLayout1 = new Integer[] {1,2};    // connection page 1
        Integer[] colLayout;

        switch(layout) {
            case 1 :
                colLayout =  columnLayout1;
                break;
            default :
                colLayout =  columnLayout1;
        }
        return columns.getColumns(colLayout);
    }


    private String parseRowData(Vector columns){
        String row;

        if(columns.get(2).equals("EMPTY SLOT")) {
            row = connectionRow.getBlankRow();
        }
        else
        {
            connectionRow.addColumnData(0, "");
            connectionRow.addColumnData(1, columns.get(2).toString());
            connectionRow.addColumnData(2, columns.get(3).toString());
            //connectionRow.addColumnData(3, columns.get(4).toString());
            //connectionRow.addColumnData(4, columns.get(5).toString());
            //connectionRow.addColumnData(5, columns.get(6).toString());
            row = getCurrentColumnLayout(connectionRow);
        }
        return row;
    }




    public Hashtable<Integer, String> prepareData(Hashtable<String,Vector> data) {
         Hashtable<Integer, String> printData = new Hashtable<Integer, String>();
         Integer size = data.size();
         String row;
         Vector v;
         Integer oldSize = printedData.size();
         boolean print = false;
         Integer i = 0;

         while (i  < size ) {
             v = data.get(i.toString());                               // get row
             if (oldSize < i + 1 || oldSize == 0)           // if new row..then print
                 print = true;
             else {
                 Vector v2 = printedData.get(i.toString());
                 if (v.get(0) != v2.get(0)){                    // if row change ..then print
                     print = true;
                 }
             }
             if (print) {                                       // add row to print table
                     row=parseRowData(v);
                     printData.put(i,row);
             }
             i=i+1;
         }
         return printData;
     }

    public String getColumnDescription() {
        return columnDescription;
    }

    public String getKeyDescription() {
        return keyDesc;
       // return "test";
    }

    public String getPageDescription() {
        return layoutDesc + ", "+curPage;
    }


}
