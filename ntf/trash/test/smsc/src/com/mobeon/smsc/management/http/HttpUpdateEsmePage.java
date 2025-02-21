package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;

public class HttpUpdateEsmePage implements HtmlConstants{
    
    protected TrafficCentral trafficInfo = null;       
    
    /** Creates a new instance of HttpUpdateEsmePage */
    public HttpUpdateEsmePage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }
    
    public String getpdateEsmePage(char[] request){                
        StringBuffer httpString = new StringBuffer(1000);
        String esmeName = getEsmeFromRequest(request);
        
        httpString.append(HttpHandler.HtmlVersionHeader);
        httpString.append("<FORM METHOD=POST ACTION=update-emse>\n");
        httpString.append("<B><U>Config ESME: " + esmeName + "</U></B><BR><BR>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#C0C0C0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<td>\n");
        httpString.append("ESME name");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("Loglevel");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("Max cache size");
        httpString.append("</td>\n");
        httpString.append("</tr>");
        httpString.append("<tr>\n");
        httpString.append("<td>\n");
        httpString.append("<input type=text name=emseName size=10 maxlength=10 value=\"" + esmeName + "\">\n");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append( "<select name=loglevel size=1>" +
                         "<option selected>" + trafficInfo.getLoglevel(esmeName) + "</option>" +
                         "<option>all</option>" +
                         "<option>info</option>" +
                         "<option>warning</option>" +
                         "<option>off</option>" +
                         "<option>fine</option>" +
                         "<option>severe</option>" +
                         "</select>");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("<input type=text name=logsize size=5 maxlength=10 value=\"" + trafficInfo.getLogSize(esmeName) + "\">\n");
        httpString.append("/<td>\n");
        httpString.append("</tr>");
        httpString.append("</table>");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"update_" + esmeName +"\" VALUE=\"Update\">\n");
        httpString.append("</p>");
        httpString.append("</FORM>\n");                        
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=\"HIDDEN\" NAME=\"esme\" VALUE=\"" + esmeName + "\">");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Back\">\n");
        httpString.append("</FORM>\n"); 
        httpString.append("</p>");
        httpString.append(HTML_FOOTER);        
        return httpString.toString();
    }
         
    private String getEsmeFromRequest(char[] b){
        String request = new String(b);
        String esmeName = "";
        int startOne = request.toLowerCase().indexOf("esme=");
        int startTwo = request.toLowerCase().indexOf("esme_");
        if ( startOne != -1 ){
            esmeName = request.substring(startOne+5, request.toLowerCase().indexOf("&", (startOne+5)));
        }
        else if( startTwo != -1 ){
            esmeName = request.substring(startTwo+5, request.toLowerCase().indexOf("=", (startTwo+5)));
        }
        else{
            esmeName = request.substring(0, request.toLowerCase().indexOf("="));
        }
        return esmeName;
    }
}
