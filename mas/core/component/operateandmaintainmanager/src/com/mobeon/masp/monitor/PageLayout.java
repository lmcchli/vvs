package com.mobeon.masp.monitor;

import java.util.Vector;
import java.util.Hashtable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public interface PageLayout {
    public void keypress(Integer key);
    public Hashtable<Integer, String> getData(Boolean all);
    //public void getLayout();
    public String getColumnDescription();
    public String getKeyDescription();
    public String getPageDescription();
    public void updateData();
}
