package com.mobeon.masp.rpcclient;

import com.mobeon.masp.operateandmaintainmanager.CallType;

import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class MasMibConnection  implements Serializable {
    public CallType connectionType ;
    public long currentConnections = 0;
    public long peakConnections = 0;
    public long peakTime = 0;
    public long totalConnections = 0;
    public long accumulatedConnections = 0;
}
