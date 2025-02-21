package com.mobeon.masp.rpcclient;

import com.mobeon.masp.operateandmaintainmanager.Status;

import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class MasMibProvidedServices implements Serializable {
    public String name = "";
    public Status status = Status.DOWN;
    public String hostName = "";
    public String protocol = "";
    public long port = 0;
    public String zone = "";
    public String applicationName = "";
    public String applicationVersion = "";
}
