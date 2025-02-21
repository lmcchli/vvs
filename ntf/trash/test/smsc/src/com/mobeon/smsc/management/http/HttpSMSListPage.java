package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;
import java.util.*;

public class HttpSMSListPage implements HtmlConstants{

    protected TrafficCentral trafficInfo = null;
    private static final int SMS_COLUMNS = 2;

    /** Creates a new instance of HttpSMSListPage */
    public HttpSMSListPage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }

    public String getSMSListPage(Properties param){

        if (param.getProperty("seqno") != null) {
            return getSingleSMSPage(param);
        }

        StringBuffer httpString = new StringBuffer(1000);
        String esmeName = param.getProperty("esme", null);
        String[] httpList = trafficInfo.getCachedSMSAsHTML(esmeName);

        httpString.append(HttpHandler.getHeader(esmeName));
        httpString.append("<H1>" + httpList.length + " latest SMS PDU requests.</H1>\n");
        if(httpList == null){
            httpString.append("<B>N.A</B><BR>\n");
            return httpString.toString();
        }
        httpString.append("<FORM METHOD=POST ACTION=reset-sms-cache>\n");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Reset\">\n");
        httpString.append("</FORM>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr><td width=\"100%\">");
        httpString.append("<B>NOTE!</B><BR>");
        httpString.append("If service_type, source_addr and destination_addr is equal, replace in SMS-C.<BR>");
        httpString.append("If source_addr and protocol_id (not 0) is equal, replace in mobile phone.<BR><BR>");
        httpString.append("All received messages are logged ");
        httpString.append("in " + esmeName  + "_SMS_PDU_cache.log in the logs directory.<BR>");
        httpString.append("</td>");
        httpString.append("</tr>");
        httpString.append("</table>");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        int col = 0;
        for (int i = 0 ; i < httpList.length; i++) {
            if (col == SMS_COLUMNS) { col = 0; }
            if (col == 0) {
                httpString.append("<tr>\n");
            }
            if (httpList[i] != null) {
                httpString.append("<td width=\"50%\" class=\"sms\">" + httpList[i] + "</td>");
                col++;
                if (col == SMS_COLUMNS) {
                    httpString.append("</tr>\n");
                }
            }
        }
        if (col < SMS_COLUMNS) {
            for (; col < SMS_COLUMNS; col++) {
                httpString.append("<td width=\"50%\" class=\"sms\">&nbsp;</td>");
            }
            httpString.append("</tr>");
        }
        httpString.append("</table>");
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Back\">\n");
        httpString.append("</FORM>\n");
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }

    public String getSingleSMSPage(Properties param){
        String result;

        String seqno = param.getProperty("seqno");
        String esmeName = param.getProperty("esme", null);

        String[] httpList = trafficInfo.getCachedSMSAsHTML(param.getProperty("esme"));

        result = HttpHandler.HtmlVersionHeader
            + "<H1> SMS PDU " + seqno + "</H1>\n"
            + "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\"><tr>\n";

        for (int i = httpList.length - 1 ; i >= 0; i--) {
            if (httpList[i].indexOf(">Sequence number:</td><td width=\"25%\">" + seqno + "<") > 0) {
                result += "<td width=\"50%\" class=\"sms\">" + httpList[i] + "</td>";
                break;
            }
        }
        return result + "</tr></table>" + HTML_FOOTER;
    }

    public String getSingleSMSPage(Properties param, String seqNo) {

        if (param.getProperty("seqno") != null) {
            return getSingleSMSPage(param);
        }

        StringBuffer httpString = new StringBuffer(1000);
        String esmeName = param.getProperty("esme", null);
        String[] httpList = trafficInfo.getCachedSMSAsHTML(esmeName);

        httpString.append(HttpHandler.HtmlVersionHeader);
        httpString.append("<H1>" + httpList.length + " latest SMS PDU requests.</H1>\n");
        if(httpList == null){
            httpString.append("<B>N.A</B><BR>\n");
            return httpString.toString();
        }
        httpString.append("<FORM METHOD=POST ACTION=reset-sms-cache>\n");
        httpString.append("Reset SMS PDU cache. <INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Reset\">\n");
        httpString.append("</FORM>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<td width=\"50%\"><B>Cached SMS PDU requests</B></td>");
        httpString.append("</tr>");
        httpString.append("<td width=\"100%\">");
        httpString.append("<B>NOTE!</B><BR>");
        httpString.append("If service_type, source_addr and destination_addr is equal, replace in SMS-C.<BR>");
        httpString.append("If source_addr and protocol_id (not 0) is equal, replace in mobile phone.<BR><BR>");
        httpString.append("Only the 10 latest SMS PDU requests will be presented here. The whole cache can be found <BR>");
        httpString.append("in " + esmeName  + "_SMS_PDU_cache.log in the logs directory.<BR>");
        httpString.append("</td>");
        httpString.append("</tr>");
        httpString.append("</table>");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        int col = 0;
        for (int i = 0 ; i < httpList.length; i++) {
            if (col == SMS_COLUMNS) { col = 0; }
            if (col == 0) {
                httpString.append("<tr>\n");
            }
            if (httpList[i] != null) {
                httpString.append("<td width=\"50%\" class=\"sms\">" + httpList[i] + "</td>");
                col++;
                if (col == SMS_COLUMNS) {
                    httpString.append("</tr>\n");
                }
            }
        }
        if (col < SMS_COLUMNS) {
            for (; col < SMS_COLUMNS; col++) {
                httpString.append("<td width=\"50%\" class=\"sms\">&nbsp;</td>");
            }
            httpString.append("</tr>");
        }
        httpString.append("</table>");
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Back\">\n");
        httpString.append("</FORM>\n");
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
}
