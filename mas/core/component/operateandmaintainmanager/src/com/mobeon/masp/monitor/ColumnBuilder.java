/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.monitor;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Map;

public class ColumnBuilder
{
    private Integer offset = Integer.valueOf(0);
    private String blankRow = "";
    private Vector <Integer>dataSize = new Vector<Integer>();
    private Vector <Integer>columnIndent = new Vector<Integer>();
    private Vector columns = new Vector();




    public void setColOffset(Integer offset) {
        this.offset = offset;
    }

    public void addColumnWidth(Integer width) {
        dataSize.add(width);
        columns.add("");
        for (int i = 1; i < (width); i++)
            blankRow = blankRow.concat(" ");
    }

    public void addColumnWidth(Integer width,Integer indent) {
        addColumnWidth(width);
        columnIndent.add(indent);
        for (int i = 1; i < (indent); i++)
            blankRow = blankRow.concat(" ");
    }



    // todo rewrite this section
    public void addColumnData(Integer column, String data) {
        String blanks = "";
        String colData;

        // add indent if it exsists
//        if (columnIndent.size() > 0 ) {
//            for (int i = 0; i < columnIndent.get(column); i++ ) {
//                blanks = blanks + " ";
//            }
//            data = blanks + data;
//        }
        // add empty spacec to maintain correct column width
        if (data.length() <  dataSize.get(column)) {
            Integer diff =  dataSize.get(column) - data.length();
            for (int i = 0; i < diff; i++) {
                blanks = new StringBuilder().append(blanks).append(" ").toString();
            }
            colData = new StringBuilder().append(data).append(blanks).toString();
        } else{
            colData = data.substring(0, dataSize.get(column) - 1);
        }
        columns.set(column, colData);
    }

    public String getBlankRow() {
        return blankRow;
    }

    /**
     * Returns a string containing the specified columns
     * @param index of colums to be returned.
     * @return  String containinf columsn.
     */
    public String getColumns(Integer[] index){
        String retStr = "";
        for (int i = 0; i < index.length; i++) {
            String s = (String) columns.elementAt(index[i]);
            retStr = retStr.concat(s);
        }
        return retStr;

    }

    /**
     * Returns all columns in a string
     * @return String containing all columns.
     */
    public String getColumns() {
        String retStr = "";
        for (int i = 0; i < columns.size(); i++) {
            String s = (String) columns.elementAt(i);
            retStr = retStr.concat(s);
        }
        return retStr;
    }
}