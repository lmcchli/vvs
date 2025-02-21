package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;

public class HttpAddEsmeAndAccountPage implements HtmlConstants{
    
    protected TrafficCentral trafficInfo = null;
    
    /** Creates a new instance of HttpAddEsmeAndAccountPage */
    public HttpAddEsmeAndAccountPage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }
    
    public String getAddEsmeAndAccountPage(){

        StringBuffer httpString = new StringBuffer(1000); 
        httpString.append(HttpHandler.HtmlVersionHeader);
        httpString.append(HTML_STYLE);
        httpString.append("<B><U>Create new ESME</U></B><BR><BR>\n");
        httpString.append("<FORM METHOD=POST ACTION=add-esme>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bgcolor=\"#C0C0C0\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<td>");
        httpString.append("<B>ESME name</B>*1\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Loglevel</B>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Number of<br>messages displayed</B>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Account</B>*2\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Password</B>*3\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Max bindings</B>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<B>Add</B>\n");
        httpString.append("</td>\n");
        httpString.append("</tr>\n");
        httpString.append("<tr>\n");
        httpString.append("<td>");
        httpString.append("<input type=text name=esme size=10 maxlength=10>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<select name=loglevel size=1>" +
        "<option selected>fine</option>" +
        "<option>all</option>" +
        "<option>info</option>" +
        "<option>warning</option>" +
        "<option>off</option>" +
        "<option>severe</option>" +
        "</select>");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<input type=text name=cache_size size=5 maxlength=10>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<input type=text name=account_name size=10 maxlength=10>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<input type=text name=account_pwd size=10 maxlength=10>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<input type=text name=account_bindings size=5 maxlength=10>\n");
        httpString.append("</td>\n");
        httpString.append("<td>");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"update\" VALUE=\"Add\">\n");
        httpString.append("<td>");
        httpString.append("</tr>\n");
        httpString.append("</table>\n");
        httpString.append("</FORM>\n");                     
        httpString.append("<p align=\"left\">");
        httpString.append("*1 Corresponds to NTFs configparameter SMESystemType<BR>\n");
        httpString.append("*2 Corresponds to NTFs configparameter SMESystemID<BR>\n");
        httpString.append("*3 Corresponds to NTFs configparameter SMEPassword<BR>\n");                
        httpString.append("<FORM METHOD=POST ACTION=home>\n");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"back\" VALUE=\"Back\">\n");
        httpString.append("</FORM>\n"); 
        httpString.append("</p>");
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
    
}
