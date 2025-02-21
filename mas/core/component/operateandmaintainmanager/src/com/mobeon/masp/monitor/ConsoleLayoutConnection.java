package com.mobeon.masp.monitor;

import com.mobeon.masp.rpcclient.RpcMonitor;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoRead;

import java.util.*;

/*
* Copyright (c) $today.year Mobeon AB. All Rights Reserved.
*/

public class ConsoleLayoutConnection implements PageLayout {

    ILogger log;

    private String curPage;
    private String layoutDesc;
    private String keyDesc;
    String columnDescription;
    RpcMonitor mop;
    HashMap<String, SessionInfoRead> data;
    HashMap<String, SessionInfoRead> printedData;
    Connection conn;

    ColumnBuilder connectionRow;

    Integer layout ;         // Defines witch layot that should be shown.

    public ConsoleLayoutConnection (Connection conn){

        log = ILoggerFactory.getILogger(ConsoleLayoutConnection.class);

        data = new HashMap<String, SessionInfoRead>();
        //printedData = new HashMap<String, SessionInfoRead>();

        //log.debug("new RpcMonitor()");
        //mop = new RpcMonitor();
        this.conn = conn ;

        // Define key text
        keyDesc = "F2=Page 1,  F3=Page 2";
        layoutDesc = "Connection statistic";

        // Define columns and size
        connectionRow = new ColumnBuilder();
        connectionRow.addColumnWidth(1);  // Change time
        connectionRow.addColumnWidth(11); // Sid
        connectionRow.addColumnWidth(15); // Service
        connectionRow.addColumnWidth(25); // Session init
        connectionRow.addColumnWidth(8);  // Connection type
        connectionRow.addColumnWidth(6);  // Connection state
        connectionRow.addColumnWidth(4);  // Direction
        connectionRow.addColumnWidth(2);  // Outboud activity
        connectionRow.addColumnWidth(2);  // Inbound activity
        connectionRow.addColumnWidth(21); // ANI
        connectionRow.addColumnWidth(21); // DNIS
        connectionRow.addColumnWidth(21); // RDNIS
        connectionRow.addColumnWidth(75); // FarECP


        setLayout(1);                     // Set the default page
    }


    public void updateData(){
        // get data from  connection and update datacollection.
        HashMap<String, SessionInfoRead> tmpData;

        tmpData = conn.getConnectionData();


        //Hashtable<Integer, String> printData = new Hashtable<Integer, String>();
        //String row;
        Integer pos;

        for(Map.Entry service:tmpData.entrySet()) {
            SessionInfoRead session = (SessionInfoRead)service.getValue(); // Hämtar ut sessionen
            //row=parseRowData(session);  // Hämtar raden
            pos = session.getPos();     // Hämtar pos
            data.put(pos.toString(),session);
        }


    }

    public void keypress(Integer key) {
        // Handle keypres not handeld by the console.
        if (key == 266 ) {      // F2
            setLayout(1);
        } else
        if (key == 267 ) {      // F3
            setLayout(2);
        }



    }

    public Hashtable<Integer, String> getData(Boolean all) {
        // returns all rows that are going to be printed
        //Hashtable<Integer, Vector> outData = new Hashtable<Integer, Vector>();
        Hashtable<Integer, String> outData; //= new Hashtable<Integer, String>();

        // Compare data and send rows that have been updated/removed/added
        outData = prepareData(data,all);
        // Store printed data to be able to compare data next time
        //printedData = data;
        return outData;
    }

    private void setLayout(Integer layout){
            this.layout = layout;

            switch(layout) {
                case 1 :
                    columnDescription = "Sid        Service        Tp      St    Di  O I ANI                  DNIS                 RDNIS";
                    curPage = "Page 1";
                    break;
                case 2 :
                    columnDescription = "Sid        SessInit                 FarECP";
                    curPage = "Page 2";
                    break;
                default :
                    columnDescription = "";
            }
    }


    private String getCurrentColumnLayout(ColumnBuilder columns){
        Integer[] columnLayout1 = new Integer[] {1,2,4,5,6,7,8,9,10,11};    // connection page 1
        Integer[] columnLayout2 = new Integer[] {1,3,12};                   // connection page 2
        Integer[] colLayout;

        switch(layout) {
            case 1 :
                colLayout =  columnLayout1;
                break;
            case 2 :
                colLayout =  columnLayout2;
                break;
            default :
                colLayout =  columnLayout1;
        }
        return columns.getColumns(colLayout);
    }


    //private String parseRowData(Vector columns){
    private String parseRowData(SessionInfoRead session){
        String row;

        //if(columns.get(2).equals("EMPTY SLOT")) {
        if(session.isDisposed()) {
            row = connectionRow.getBlankRow();
        }
        else
        {
/*            connectionRow.addColumnData(0, "");
            connectionRow.addColumnData(1, columns.get(2).toString());
            connectionRow.addColumnData(2, columns.get(3).toString());
            connectionRow.addColumnData(3, columns.get(4).toString());
            connectionRow.addColumnData(4, columns.get(5).toString());
            connectionRow.addColumnData(5, columns.get(6).toString());
            connectionRow.addColumnData(6, columns.get(7).toString());
            connectionRow.addColumnData(7, columns.get(8).toString());
            connectionRow.addColumnData(8, columns.get(9).toString());
            connectionRow.addColumnData(9, columns.get(10).toString());
            connectionRow.addColumnData(10, columns.get(11).toString());
            connectionRow.addColumnData(11, columns.get(12).toString());
            connectionRow.addColumnData(12, columns.get(13).toString());
            */
            String data;
            connectionRow.addColumnData(0, "");
            connectionRow.addColumnData(1, session.getSessionId());
            connectionRow.addColumnData(2, session.getService());
            connectionRow.addColumnData(3, session.getSessionInitiator());
            //data = session.getConnetionType().getInfo();
            connectionRow.addColumnData(4, session.getConnetionType().getInfo());
            //data = session.getConnetionState().getShortInfo();
            connectionRow.addColumnData(5, session.getConnetionState().getShortInfo());
            //data = session.parseDirection().getShortInfo();
            connectionRow.addColumnData(6, session.getDirection().getShortInfo());
            //data = session.getOutboundActivity().getShortInfo();
            connectionRow.addColumnData(7, session.getOutboundActivity().getShortInfo() );
            //data = session.gettInboundActivity().getShortInfo();
            connectionRow.addColumnData(8, session.gettInboundActivity().getShortInfo() );
            connectionRow.addColumnData(9, session.getANI());
            connectionRow.addColumnData(10, session.getDNIS());
            connectionRow.addColumnData(11, session.getRDNIS());
            connectionRow.addColumnData(12, session.getFarEndConProp());


            row = getCurrentColumnLayout(connectionRow);
        }
        return row;
    }

    public Hashtable<Integer, String> prepareData(HashMap<String,SessionInfoRead> data,Boolean all) {
         Hashtable<Integer, String> printData = new Hashtable<Integer, String>();
//         Integer size = data.size();
         String row;
         Integer pos;
//         Vector v;
//         Integer oldSize = printedData.size();
//         boolean print = false;
//         Integer i = 0;

//         while (i + 1 < size ) {
//             v = data.get(i.toString());                    // get row
//             if (oldSize < i + 1 || oldSize == 0)           // if new row..then print
//                 print = true;
//             else {
//                 Vector v2 = printedData.get(i.toString());
//                 if (v.get(0) != v2.get(0)){                    // if row change ..then print
//                     print = true;
//                 }
//             }
//             if (print) {                                       // add row to print table
//                     row=parseRowData(v);
//                     printData.put(i,row);
//             }
//             i=i+1;
//         }
        for(Map.Entry service:data.entrySet()) {
            SessionInfoRead session = (SessionInfoRead)service.getValue();
            if (!session.isPrinted() || all) {
                row=parseRowData(session);
                pos = session.getPos();
                printData.put(pos,row);
                session.setPrinted();
            }
        }

        return printData;
     }

    public String getColumnDescription() {
        return columnDescription;
       // return "test";
    }

    public String getKeyDescription() {
        return keyDesc;
       // return "test";
    }

    public String getPageDescription() {
        return layoutDesc + ", "+curPage;
    }

}
