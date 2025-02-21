package com.mobeon.masp.rpcclient;

import com.mobeon.masp.operateandmaintainmanager.Status;

import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class MasMibConsumedServices  implements Serializable {
    public String name = "";
    public Status status = Status.DOWN;
    public long successOperations = 0;
    public long failedOperations = 0;
    public long statusChangedTime = 0;


}
