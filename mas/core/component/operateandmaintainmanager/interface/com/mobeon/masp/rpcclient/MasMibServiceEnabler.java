package com.mobeon.masp.rpcclient;

import com.mobeon.masp.rpcclient.MasMibConnection;

import java.util.Vector;
import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class MasMibServiceEnabler implements Serializable {
    public String protocol = "";
    public long maxConnections = 0;
    public Vector<MasMibConnection> connections = new Vector<MasMibConnection>();


}
