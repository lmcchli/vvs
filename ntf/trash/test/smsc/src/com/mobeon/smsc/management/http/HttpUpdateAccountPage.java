package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;

public class HttpUpdateAccountPage implements HtmlConstants{
    
    protected TrafficCentral trafficInfo = null;

    /** Creates a new instance of HttpAddAccountPage */
    public HttpUpdateAccountPage(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }
    
    public String getUpdateAccountPage(char[] request){
                        
        StringBuffer httpString = new StringBuffer(1000);
        String esmeName = getEsmeFromRequest(request);
        String accountName = getAccountFromRequest(request);
        
        httpString.append(HttpHandler.HtmlVersionHeader);                
        httpString.append("<B><U>Config Account: " + accountName + " for ESME: " + esmeName + "</U></B><BR><BR>\n");
        httpString.append("<table border=\"1\" cellpadding=\"0\" bgcolor=\"#C0C0C0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#111111\" width=\"100%\">\n");
        httpString.append("<tr>\n");
        httpString.append("<td>\n");
        httpString.append("Account uid *1\n");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("Account pwd *2\n");                
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("Update uid and pwd\n");
        httpString.append("</td>\n");
        httpString.append("<td>\n");
        httpString.append("Max bindings\n");
        httpString.append("</td>\n");                 
        httpString.append("</tr>\n");        
        httpString.append("<tr>\n");        
        httpString.append("<td>\n");                        
        httpString.append("<FORM METHOD=POST ACTION=update-account-uid-pwd>\n");            
        httpString.append("<input type=text name=account-uid size=10 maxlength=10 value=\"" + accountName + "\">\n");                           
        httpString.append("</td>\n");                    
        httpString.append("<td>\n");            
        httpString.append("<input type=text name=account-pwd size=10 maxlength=10 value=\"" + trafficInfo.getAccountPassword(esmeName, accountName) + "\">\n");
        httpString.append("</td>\n");                                
        httpString.append("<td>");            
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"Account_" + accountName + "_ESME_" + esmeName + "\" VALUE=\"Update\"");            
        httpString.append("</FORM>\n");
        httpString.append("</td>\n");                         
        httpString.append("<td>\n");        
        httpString.append("<FORM METHOD=POST ACTION=update-account-bindings>\n");        
        httpString.append("<input type=text name=max-bindings size=5 maxlength=10 value=\"" + trafficInfo.getMaxConnections(esmeName, accountName) + "\">\n");        
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"Account_" + accountName + "_ESME_" + esmeName + "\" VALUE=\"Update\"\n");        
        httpString.append("</FORM>\n");
        httpString.append("</td>\n");                                                                            
        httpString.append("</tr>\n");            
        httpString.append("<BR>\n");        
        httpString.append("</table>\n");                                   
        httpString.append("<FORM METHOD=POST ACTION=update-esme-page>\n");
        httpString.append("<p align=left>");
        httpString.append("<INPUT TYPE=SUBMIT NAME=\"esme_" + esmeName + "\" VALUE=\"Back\">\n");
        httpString.append("</p>");
        httpString.append("</FORM>\n");                 
        httpString.append("<p align=left>");
        httpString.append("<BR>\n");                
        httpString.append("*1 Corresponds to NTFs configparameter SMESystemID<BR>\n");
        httpString.append("*2 Corresponds to NTFs configparameter SMEPassword<BR>\n");
        httpString.append("</p>");        
        httpString.append("<BR>\n"); 
        httpString.append(HTML_FOOTER);
        return httpString.toString();
    }
    
    private String getEsmeFromRequest(char[] b){
        String request = new String(b);
        String esmeName = null;
        int esmeStart = request.toLowerCase().indexOf("esme_");                
        if ( esmeStart >= 0 ){
            esmeName = request.substring(esmeStart+5, request.toLowerCase().indexOf("=", (esmeStart+5)));
        }
        return esmeName;
    }
         
    private String getAccountFromRequest(char[] b){
        String request = new String(b);
        String accountName = null;
        int accountStart = request.toLowerCase().indexOf("account_");                
        if ( accountStart >= 0 ){
            accountName = request.substring(accountStart+8, request.toLowerCase().indexOf("_", (accountStart+8)));
        }
        return accountName;
    }
}
