package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;

public class HttpAddAccountPage implements HtmlConstants{

    protected TrafficCentral trafficInfo = null;

    /** Creates a new instance of HttpAddAccountPage */
    public HttpAddAccountPage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }
    
    public String getAddAccountPage(char[] request){
        return getPage(getEsmeFromRequest(request));
    }
    
    public String getAddAccountPage(String esmeName){
        return getPage(esmeName);
    }
    
    private String getPage(String esmeName){                
        StringBuffer httpString = new StringBuffer(1000);                        
        httpString.append(HttpHandler.HtmlVersionHeader);
        httpString.append("<B><U>Create new account</U></B><BR><BR>\n");
        httpString.append("<FORM METHOD=POST ACTION=add-account>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bgcolor=\"#C0C0C0\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<td>\n");
        httpString.append("<B>Account UID:</B>\n");
        httpString.append("     <input type=text name=account_name size=10 maxlength=10>\n");
        httpString.append("<BR>\n");
        httpString.append("<B>Password:</B>\n");
        httpString.append("     <input type=text name=account_pwd size=10 maxlength=10>\n");
        httpString.append("<BR>\n");
        httpString.append("<B>Max bindings:</B>\n");
        httpString.append("     <input type=text name=account_bindings size=5 maxlength=10>\n");
        httpString.append("<BR>\n");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Add\">\n");
        httpString.append("</td>\n");                
        httpString.append("</table>\n");
        httpString.append("</FORM><BR>\n");                         
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Back\">\n");
        httpString.append("</FORM><BR>\n"); 
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
    
    private String getEsmeFromRequest(char[] b){
        String request = new String(b);
        return request.substring(0, request.toLowerCase().indexOf("="));
    }
    
}
