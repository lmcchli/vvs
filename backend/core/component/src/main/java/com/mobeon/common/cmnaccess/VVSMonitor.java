package com.mobeon.common.cmnaccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.oe.lib.internalmonitor.OamAwareMonitor;
import com.abcxyz.messaging.oe.lib.monitor.ExceptionListener;
import com.abcxyz.messaging.oe.lib.monitor.MenuItem;
import com.abcxyz.messaging.oe.lib.monitor.StatsLoggingMenuItem;

public class VVSMonitor extends OamAwareMonitor {

    private String name = "";

    public VVSMonitor(String name, int aPort, String aPassword, OAMManager anOam) throws IOException {
        super(aPort, aPassword, new DefaultExceptionListener(anOam.getLoggingManager().getLogAgent(DefaultExceptionListener.class)), anOam);
        this.name = name;
    }

    public void showMenus(BufferedReader in, PrintWriter out) throws IOException {
        StatsLoggingMenuItem enableStatsLoggingItem = new StatsLoggingMenuItem(in, out, true);
        StatsLoggingMenuItem disableStatsLoggingItem = new StatsLoggingMenuItem(in, out, false);
        //MonitorPasswordChangeMenuItem passwordChangeItem = new MonitorPasswordChangeMenuItem(in, out, oam);

        MenuItem[] rootSubMenus = { enableStatsLoggingItem, disableStatsLoggingItem };

        MenuItem rootMenu = new MenuItem(in, out, name + " Proxy Monitor", rootSubMenus);
        rootMenu.show();
        MenuItem.exitSet = false;
    }

    private static class DefaultExceptionListener implements ExceptionListener {
        LogAgent logger = null;
        
        public DefaultExceptionListener(LogAgent aLogger){
            logger= aLogger;
        }
        public void handleException(Throwable t){
            if (logger!=null) logger.error(t.toString());
        }

      public void handleException(Throwable t, String aMessage){
            if (logger!=null) logger.error(t.toString() + " message = " + aMessage);
      }
    }
    
}
