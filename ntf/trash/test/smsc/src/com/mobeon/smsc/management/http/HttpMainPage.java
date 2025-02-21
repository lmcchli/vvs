package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;

public class HttpMainPage implements HtmlConstants{

    protected TrafficCentral trafficInfo = null;

    /** Creates a new instance of HttpMainPage */
    public HttpMainPage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }

    public String getMainPage(){
        String[] esme = trafficInfo.getRegistredESMEs();
        StringBuffer httpString = new StringBuffer(1000);
        httpString.append(HttpHandler.getHeader(""));
        httpString.append("<table border=1 cellpadding=20 width=\"50%\"><tr><td>\n");
        httpString.append("<h2>Reqistered ESME:s</h2>\n");
        httpString.append("<table border=1 cellpadding=10 cellspacing=0 bgcolor=\"#C0C0C0\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<th>ESME name *1</th>\n");
        httpString.append("<th>Received SMS</th>\n");
        httpString.append("<th>Registered accounts</th>\n");
        httpString.append("<th>&nbsp;</th>\n");
        httpString.append("</tr>\n");
        for( int i = 0; i < esme.length; i++){
            httpString.append("<tr>\n");
            httpString.append("<th>" + esme[i] + "</th>\n");
            httpString.append("<td align=\"center\">" + trafficInfo.getReceivedSMSRequests(esme[i]) + "</td>");
            httpString.append("<td align=\"center\">" + trafficInfo.getNumberOfAccounts(esme[i]) + "</td>\n");
            httpString.append("<td align=\"center\">");
            httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
            httpString.append("<INPUT TYPE=HIDDEN NAME=esme VALUE=\"" + esme[i] + "\"/>\n");
            httpString.append("<INPUT TYPE=SUBMIT NAME=getesme VALUE=View>\n");
            httpString.append("</FORM>");
            httpString.append("</td>");
            httpString.append("</tr>\n");
        }
        httpString.append("</table>\n");
        httpString.append("<p align=left>*1 Corresponds to NTFs configparameter SMESystemType<BR></p>\n");
        httpString.append("</td></tr></table>\n");
        httpString.append("<h1>&nbsp;</h1>\n");
        httpString.append("<table border=1 cellpadding=20 width=\"50%\"><tr><td>\n");
        httpString.append("<H2>Add new ESME and Account</H2>\n");
        httpString.append("<FORM METHOD=POST ACTION=get-add-esme-and-account-page>\n");
        httpString.append("<INPUT TYPE=SUBMIT NAME=addEsme VALUE=Add>\n");
        httpString.append("</FORM>");
        httpString.append("</td></tr></table>\n");
        httpString.append("<h1>&nbsp;</h1>\n");
        httpString.append("<table border=1 cellpadding=20 width=\"50%\"><tr><td>\n");
        httpString.append("<p align=left>An ESME represents a lab, i.e. HURR6.</p>\n");
        httpString.append("<p align=left>Under each ESME one can register one or several accounts. Accounts represent NTF instances or other SMS-C clients, ");
        httpString.append("e.g. NTF1, NTF2, MWS and so on.</p>\n");
        httpString.append("</td></tr></table>\n");
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
}
