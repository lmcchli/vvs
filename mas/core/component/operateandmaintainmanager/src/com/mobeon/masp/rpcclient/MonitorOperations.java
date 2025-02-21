package com.mobeon.masp.rpcclient;

import java.util.HashMap;
import java.io.IOException;
import com.mobeon.masp.operateandmaintainmanager.ConnectionStatistics;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoRead;

/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public interface MonitorOperations {
    public boolean monitorStarted();
    public boolean connected();
    public String startMonitor() throws IOException;
    public void stopMonitor() throws IOException;
    public HashMap<String,SessionInfoRead> getMonitorConnectionData() throws IOException;
    public ConnectionStatistics getMonitorStatisticData() throws IOException;
}
