package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;
import java.util.*;

public class HttpEsmePage implements HtmlConstants{

    private static final int[] statusCodes = {    
        -1,
        0x00,
        0x01,
        0x02,
        0x03,
        0x04,
        0x05,
        0x06,
        0x07,
        0x08,
        0x0A,
        0x0B,
        0x0C,
        0x0D,
        0x0E,
        0x0F,
        0x11,
        0x13,
        0x14,
        0x15,
        0x33,
        0x34,
        0x40,
        0x42,
        0x43,
        0x44,
        0x45,
        0x48,
        0x49,
        0x50,
        0x51,
        0x53,
        0x54,
        0x55,
        0x58,
        0x61,
        0x62,
        0x63,
        0x64,
        0x65,
        0x66,
        0x67,
        0xC0,
        0xC1,
        0xC2,
        0xC3,
        0xC4,
        0xFE,
        0xFF,
    };
    
    private static final String[] statusSelections = {
        "default",
        "0x00 OK",
        "0x01 INVMSGLEN",
        "0x02 INVCMDLEN",
        "0x03 INVCMDID",
        "0x04 INVBNDSTS",
        "0x05 ALYBND",
        "0x06 INVPRTFLG",
        "0x07 INVREGDLVFLG",
        "0x08 SYSERR",
        "0x0A INVSRCADR",
        "0x0B INVDSTADR",
        "0x0C INVMSGID",
        "0x0D BINDFAIL",
        "0x0E INVPASWD",
        "0x0F INVSYSID",
        "0x11 CANCELFAIL",
        "0x13 REPLACEFAIL",
        "0x14 MSGQFUL",
        "0x15 INVSERTYP",
        "0x33 INVNUMDESTS",
        "0x34 INVDLNAME",
        "0x40 INVDESTFLAG",
        "0x42 INVSUBREP",
        "0x43 INVESMCLASS",
        "0x44 CNTSUBDL",
        "0x45 SUBMITFAIL",
        "0x48 INVSRCTON",
        "0x49 INVSRCNPI",
        "0x50 INVDSTTON",
        "0x51 INVDSTNPI",
        "0x53 INVSYSTYP",
        "0x54 INVREPFLAG",
        "0x55 INVNUMMSGS",
        "0x58 THROTTLED",
        "0x61 INVSCHED",
        "0x62 INVEXPIRY",
        "0x63 INVDFTMSGID",
        "0x64 X_T_APPN",
        "0x65 X_P_APPN",
        "0x66 X_R_APPN",
        "0x67 QUERYFAIL",
        "0xC0 INVOPTPARSTREAM",
        "0xC1 OPTPARNOTALLWD",
        "0xC2 INVPARLEN",
        "0xC3 MISSINGOPTPARAM",
        "0xC4 INVOPTPARAMVAL",
        "0xFE DELIVERYFAILURE",
        "0xFF UNKNOWNERR",
    };

    private int bindStatus = 0;
    private int submitStatus = 0;
    private int enquireLinkStatus = 0;
    private int cancelStatus = 0;

    protected TrafficCentral trafficInfo = null;

    /** Creates a new instance of HttpEsmePage */
    public HttpEsmePage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }

    public String getEsmePage(Properties param, String esme){

        StringBuffer httpString = new StringBuffer(1000);
        String esmeName = esme;
        if (param != null) {
            esmeName = param.getProperty("esme", esme);
        }
        try { bindStatus = Integer.parseInt(param.getProperty("bsta", "0")); } catch (NumberFormatException e) { ; }
        try { submitStatus = Integer.parseInt(param.getProperty("ssta", "0")); } catch (NumberFormatException e) { ; }
        try { enquireLinkStatus = Integer.parseInt(param.getProperty("esta", "0")); } catch (NumberFormatException e) { ; }
        try { cancelStatus = Integer.parseInt(param.getProperty("csta", "0")); } catch (NumberFormatException e) { ; }

        //Set resultcode, optionally for just one response
        boolean bindOnce = param.getProperty("bonce") != null;
        boolean submitOnce = param.getProperty("sonce") != null;
        boolean enquireLinkOnce = param.getProperty("eonce") != null;
        boolean cancelOnce = param.getProperty("conce") != null;
        trafficInfo.setResultCode(esmeName, statusCodes[bindStatus], bindOnce,
                                 statusCodes[submitStatus], submitOnce,
                                 statusCodes[enquireLinkStatus], enquireLinkOnce,
                                 statusCodes[cancelStatus], cancelOnce );
        if (bindOnce) { bindStatus = 0; }
        if (submitOnce) { submitStatus = 0; }
        if (enquireLinkOnce) { enquireLinkStatus = 0; }
        if (cancelOnce) { cancelStatus = 0; }
        
        if (param.getProperty("clear", null) != null) {
            trafficInfo.resetSMSList(esmeName);
        }
        String errorString = null;
        
        String[] accounts = trafficInfo.getRegistredAccounts(esmeName);
        Date d = new Date();
        float smsPerSec = -1;
        try{
            smsPerSec = (trafficInfo.getReceivedSMSRequests(esmeName))/((d.getTime() - trafficInfo.getStartTime(esmeName))/1000);
        }catch(ArithmeticException e){
            errorString = e.getMessage();
        }catch(Exception e){
            errorString = e.getMessage();
        }

        httpString.append(HttpHandler.getHeader(esmeName));
        httpString.append("<table border=1 cellpadding=20 width=\"100%\"><tr><td>\n");
        httpString.append("<H2>ESME " + esmeName + "</H2>\n");
        httpString.append("<table border=0><tr><td>\n");
        httpString.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" bgcolor=\"#C0C0C0\">\n");
        httpString.append("<tr>\n");
        httpString.append("<th>Loglevel</th>\n");
        httpString.append("<th>Number of received<br>SMS requests</th>\n");
        httpString.append("<th>Number of<br>messages displayed</th>\n");
        httpString.append("<th>Started</th>\n");
        httpString.append("</tr>\n");
        httpString.append("<tr>\n");
        httpString.append("<td align=\"center\">" + trafficInfo.getLoglevel(esmeName) + "</td>\n");
        httpString.append("<td align=\"center\">" + trafficInfo.getReceivedSMSRequests(esmeName) + "</td>\n");
        httpString.append("<td align=\"center\">" + trafficInfo.getLogSize(esmeName) + "</td>\n");
        httpString.append("<td align=\"center\">" + new Date(trafficInfo.getStartTime(esmeName)) + "</td>\n");
        httpString.append("</tr>\n");
        httpString.append("</table>\n");
        httpString.append("</td><td>\n");
        httpString.append("<FORM METHOD=POST ACTION=config-esme>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Configure ESME\">\n");
        httpString.append("</FORM>\n");
        httpString.append("<FORM METHOD=POST ACTION=remove-esme>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Remove ESME\">\n");
        httpString.append("</FORM>\n");
        httpString.append("</td></tr></table>");
        //        httpString.append("</td></tr></table>\n");
        //        httpString.append("<br>\n");
        //
        //        httpString.append("<table border=1 cellpadding=20 width=\"100%\">\n");
        httpString.append("<tr><td><H3>Accounts</H3>\n");
        httpString.append("<table border=\"1\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#C0C0C0\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<th>Account uid *2</th>\n");
        httpString.append("<th>Account pwd *3</th>\n");
        httpString.append("<th>Received SMS requests</th>\n");
        httpString.append("<th>Clients bound</th>\n");
        httpString.append("<th>Max bindings</th>\n");
        httpString.append("<th>&nbsp;</th>\n");
        httpString.append("<th>&nbsp;</th>\n");
        httpString.append("</tr>\n");
        for( int i = 0; i < accounts.length; i++) {
            httpString.append("<tr>\n");
            httpString.append("<td>" + accounts[i] + "</td>\n");
            httpString.append("<td>" + trafficInfo.getAccountPassword(esmeName, accounts[i]) + "</td>\n");
            httpString.append("<td>");
            httpString.append("<FORM METHOD=POST ACTION=reset-account-sms-list>\n");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"account\" VALUE=\"" + accounts[i] + "\">");
            httpString.append(trafficInfo.getReceivedSMSRequests(esmeName, accounts[i]) + "&nbsp;&nbsp;&nbsp;");
            httpString.append("<INPUT TYPE=SUBMIT NAME=\"Account_" + accounts[i] + "_ESME_" + esmeName + "\" VALUE=\"Reset counter\"");
            httpString.append("</FORM>\n");
            httpString.append("</td>\n");
            httpString.append("<td align=\"center\">" + trafficInfo.getUsedConnections(esmeName,accounts[i]) + "</td>\n");
            httpString.append("<td align=\"center\">" + trafficInfo.getMaxConnections(esmeName, accounts[i]) + "</td>\n");
            httpString.append("<td>\n");
            httpString.append("<FORM METHOD=POST ACTION=config-account>\n");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"account\" VALUE=\"" + accounts[i] + "\">");
            httpString.append("<INPUT TYPE=SUBMIT NAME=\"Account_" + accounts[i] + "_ESME_" + esmeName + "\" VALUE=\"Configure Account\"");
            httpString.append("</FORM>\n");
            httpString.append("<td>\n");
            httpString.append("<FORM METHOD=POST ACTION=remove-account>\n");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
            httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"account\" VALUE=\"" + accounts[i] + "\">");
            httpString.append("<INPUT TYPE=SUBMIT NAME=\"Account_" + accounts[i] + "_ESME_" + esmeName + "\" VALUE=\"Remove Account\"");
            httpString.append("</FORM>\n");
            httpString.append("</td>\n");
            httpString.append("</td>\n");
            httpString.append("</tr>\n");
        }
        httpString.append("</table>\n");
        httpString.append("<table border=\"0\"><tr><td width=\"50%\">");
        httpString.append("*2 Corresponds to NTFs configparameter SMESystemID<BR>\n");
        httpString.append("*3 Corresponds to NTFs configparameter SMEPassword<BR>\n");
        httpString.append("</td><td align=\"center\">");
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"" + esmeName + "\" VALUE=\"Add Account\" ");
        httpString.append("onClick=\"MM_openBrWindow('/get-add-account-page/esme=" + esmeName + "','Add New Account','scrollbars=no,resizable=no,width=195,height=130')\">\n");
        httpString.append("</FORM>\n");
        httpString.append("</td></tr></table>\n");
        httpString.append("</td></tr></table>\n");
        httpString.append("<br>\n");

        httpString.append("<table border=1 cellpadding=20 width=\"100%\"><tr>");
        httpString.append("<td valign=\"middle\">\n");
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Reload Page\">\n");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"clear\" VALUE=\"Clear Cache\">\n");
        httpString.append("<br><hr><table border=0>");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<TR><TH>Request type</TH><TH>Result Code</TH><TH>Once</TH></TR>\n");
        httpString.append("<TR><TD>Login (bind)</TD><TD><select name=\"bsta\">");
        for (int i= 0; i < statusCodes.length; i++) {
            if (i == bindStatus) {
                httpString.append("<option selected value=\"");
            } else {
                httpString.append("<option value=\"");
            }
            httpString.append(i);
            httpString.append("\">");
            httpString.append(statusSelections[i]);
        }
        httpString.append("</select></TD><TD><input type=\"checkbox\" name=\"bonce\"<BR>");
        httpString.append("<TR><TD>Send (submit)</TD><TD><select name=\"ssta\">");
        for (int i= 0; i < statusCodes.length; i++) {
            if (i == submitStatus) {
                httpString.append("<option selected value=\"");
            } else {
                httpString.append("<option value=\"");
            }
            httpString.append(i);
            httpString.append("\">");
            httpString.append(statusSelections[i]);
        }
        httpString.append("</select></TD><TD><input type=\"checkbox\" name=\"sonce\"<BR>");
        httpString.append("<TR><TD>Poll (enquire link)</TD><TD><select name=\"esta\">");
        for (int i= 0; i < statusCodes.length; i++) {
            if (i == enquireLinkStatus) {
                httpString.append("<option selected value=\"");
            } else {
                httpString.append("<option value=\"");
            }
            httpString.append(i);
            httpString.append("\">");
            httpString.append(statusSelections[i]);
        }
        httpString.append("</select></TD><TD><input type=\"checkbox\" name=\"eonce\"<p>");
        httpString.append("<TR><TD>Cancel</TD><TD><select name=\"csta\">");
        for (int i= 0; i < statusCodes.length; i++) {
            if (i == cancelStatus) {
                httpString.append("<option selected value=\"");
            } else {
                httpString.append("<option value=\"");
            }
            httpString.append(i);
            httpString.append("\">");
            httpString.append(statusSelections[i]);
        }
        httpString.append("</select></TD><TD><input type=\"checkbox\" name=\"conce\"<p>");
        httpString.append("</TD></TR></TABLE><INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Set result code\">\n");
        httpString.append("</FORM>\n");
        httpString.append("</td><td width=200>\n");
        httpString.append("<h3>Received SMS/s</h3>\n");
        httpString.append("<TABLE BORDER=0><TR><TD>Since start</TD><TD>");
        if( errorString != null){
            httpString.append("N.A error " + errorString);
        }
        else{
            httpString.append(smsPerSec);
        }
        httpString.append(" ( Time " + (d.getTime() - trafficInfo.getStartTime(esmeName))/(1000) + " )</TD></TR>\n");
        httpString.append("<TR><TD>Last 5 minutes</TD><TD>N.I</TD></TR>\n");
        httpString.append("<TR><TD>Last minute</TD><TD>" + trafficInfo.getRate(esmeName) + "</TD></TR>\n");
        httpString.append("</TABLE></td>\n");

        httpString.append("<td width=200><h3>Distribution per Type</h3>\n");

        float total = trafficInfo.getReceivedSMSRequests(esmeName);
        float mwioff = trafficInfo.getReceivedMwiOff(esmeName);
        float mwion = trafficInfo.getReceivedMwiOn(esmeName);
        float sms = trafficInfo.getReceivedSms(esmeName);
        float smsnull = trafficInfo.getReceivedSmsNull(esmeName);
        float cancel = trafficInfo.getReceivedCancel(esmeName);
        if(total !=0 ){
            try{
                mwioff = (mwioff/total)*(100);
            }catch(ArithmeticException e){ mwioff =0; }
            try{
                mwion = (mwion/total)*(100);
            }catch(ArithmeticException e){ mwion =0; }
            try{
                sms = (sms/total)*(100);
            }catch(ArithmeticException e){ sms =0; }
            try{
                smsnull = (smsnull/total)*(100);
            }catch(ArithmeticException e){ smsnull =0; }
            try{
                cancel = (cancel/total)*(100);
            }catch(ArithmeticException e){ cancel =0; }
        }
        httpString.append("<TABLE BORDER=0>");
        httpString.append("<TR><TD>SMS</TD><TD>" + sms + "%</TD></TR>\n");
        httpString.append("<TR><TD>MWI</TD><TD>" + mwion + "%</TD></TR>\n");
        httpString.append("<TR><TD>MWI OFF</TD><TD>" + mwioff + "%</TD></TR>\n");
        httpString.append("<TR><TD>SMS type 0</TD><TD>" + smsnull + "%</TD></TR>\n");
        httpString.append("<TR><TD>Cancel</TD><TD>" + cancel + "%</TD></TR>\n");
        httpString.append("</TABLE></td></tr></table>\n");
        httpString.append("<br>\n");

        httpString.append("<table border=1 cellpadding=20 width=\"100%\"><tr><td>\n");
        httpString.append("<h3>Last Few Messages</h3>");
        httpString.append("<table border=1 cellspacing=0 cellpadding=3>\n");
        httpString.append(trafficInfo.getCacheSummaryHTML(esmeName));
        httpString.append("</table>\n");
        httpString.append("<p align=left>");
        httpString.append("<FORM METHOD=POST ACTION=view-sms-list>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"View all\">\n");
        httpString.append("</FORM>\n");
        httpString.append("</p>\n");
        httpString.append("</td></tr></table>\n");
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
}
