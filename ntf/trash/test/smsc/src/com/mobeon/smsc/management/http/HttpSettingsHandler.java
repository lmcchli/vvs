package com.mobeon.smsc.management.http;
import com.mobeon.smsc.interfaces.TrafficCentral;
import java.util.*;

public class HttpSettingsHandler{
        
    protected TrafficCentral trafficInfo = null;
    
    public HttpSettingsHandler(TrafficCentral trafficInfo){
        this.trafficInfo = trafficInfo;
    }       
    
    public void addEsme(char[] b){
        String request = new String(b);
        String esme_name = "";
        String logLevel = "";
        String cacheSize = "";
        String account_name = "";
        String password = "";
        String bindings = "";
        
        int esme_start = request.toLowerCase().indexOf("esme=");        
        int loglevel_start = request.toLowerCase().indexOf("loglevel=");
        int cache_size_start = request.toLowerCase().indexOf("cache_size=");
        int account_start = request.toLowerCase().indexOf("account_name=");        
        int password_start = request.toLowerCase().indexOf("account_pwd=");
        int bindings_start = request.toLowerCase().indexOf("account_bindings=");
                         
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5,  request.toLowerCase().indexOf("&", (esme_start+5)));
        }
        if( loglevel_start >= 0 ){
            logLevel = request.substring(loglevel_start+9, request.toLowerCase().indexOf("&", (loglevel_start+9)));
        }
        if( cache_size_start >= 0 ){
            cacheSize = request.substring(cache_size_start+11, request.toLowerCase().indexOf("&", (cache_size_start+11)));
        }
        if( account_start >= 0 ){
            account_name = request.substring(account_start+13, request.toLowerCase().indexOf("&", (account_start+13)));
        }
        if( password_start >= 0 ){
            password = request.substring(password_start+12, request.toLowerCase().indexOf("&", (password_start+12)));
        }
        if( bindings_start >= 0 ){
            bindings = request.substring(bindings_start+17, request.toLowerCase().indexOf("&", (bindings_start+17)));
        }        
        trafficInfo.addEsmeAndAccount(esme_name, logLevel, cacheSize, account_name, password, bindings);         
    }
    
    public String addAccount(char[] b){
        String request = new String(b);
        String esme_name = "";
        String account_name = "";
        String password = "";
        String bindings = "";

        int esme_start = request.toLowerCase().indexOf("esme_");        
        int account_start = request.toLowerCase().indexOf("account_name=");        
        int password_start = request.toLowerCase().indexOf("account_pwd=");
        int bindings_start = request.toLowerCase().indexOf("account_bindings=");
                         
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5,  request.toLowerCase().indexOf("=", (esme_start+5)));
        }
        if( account_start >= 0 ){
            account_name = request.substring(account_start+13, request.toLowerCase().indexOf("&", (account_start+13)));
        }
        if( password_start >= 0 ){
            password = request.substring(password_start+12, request.toLowerCase().indexOf("&", (password_start+12)));
        }
        if( bindings_start >= 0 ){
            bindings = request.substring(bindings_start+17, request.toLowerCase().indexOf("&", (bindings_start+17)));
        }                
        trafficInfo.addAccount(esme_name, account_name, password, bindings);         
        return esme_name;
    }
    
    public String updateEsme(char[] b){
        String request = new String(b);
        String esme_name = null;
        String logLevel = null;
        int logSize = -1;
        
        int esme_start = request.toLowerCase().indexOf("update_");
        int loglevel_start = request.toLowerCase().indexOf("loglevel=");
        int esmeName_start = request.toLowerCase().indexOf("esmeName=");
        int logsize_start = request.toLowerCase().indexOf("logsize=");
        
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+7, request.toLowerCase().indexOf("=", (esme_start+7)));
        }
        
        if ( esmeName_start >= 0 ){
            esme_name = request.substring(esmeName_start+9, request.toLowerCase().indexOf("=", (esmeName_start+9)));
        }
        
        if( loglevel_start >= 0 ){
            logLevel = request.substring(loglevel_start+9, request.toLowerCase().indexOf("&", (loglevel_start+9)));
            trafficInfo.setLogLevel(esme_name, logLevel);
        }
        
        if( logsize_start >= 0 ){
            try{
                logSize = Integer.parseInt(request.substring(logsize_start+8, request.toLowerCase().indexOf("&", (logsize_start+8))));
            }catch(Exception e){
                logSize = trafficInfo.getLogSize(esme_name);
            }
            trafficInfo.setLogSize(esme_name, logSize);
        }
        
        return esme_name;
    }
    
    public String resetAccount(char[] b){
        Properties param = HttpHandler.parseParams(b);
        
        String request = new String(b);
        String esme_name = param.getProperty("esme");
        String account_name = param.getProperty("account");        

	trafficInfo.resetAccountSMSList(esme_name, account_name);

        return esme_name;
    }
         
    public String updateAccountUidAndPwd(char[] b){
        
        String request = new String(b);
        String esme_name = null;
        String account_name = null;
        String uid = null;
        String pwd = null;        
        
        int esme_start = request.toLowerCase().indexOf("esme_");
        int account_start = request.toLowerCase().indexOf("account_");
        int pwd_start = request.toLowerCase().indexOf("account-pwd=");
        int uid_start = request.toLowerCase().indexOf("account-uid=");
        
        if ( account_start >= 0 ){
            account_name = request.substring(account_start+8, request.toLowerCase().indexOf("_", (account_start+8)));
        }
        
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5, request.toLowerCase().indexOf("=", (esme_start+5)));
        }
        
        if ( pwd_start >= 0 ){
            pwd = request.substring(pwd_start+12, request.toLowerCase().indexOf("&", (pwd_start+12)));
            System.out.println("1");
            trafficInfo.changeAccountPwd(esme_name, account_name, pwd);
        }
        
        if ( uid_start >= 0 ){                        
            uid = request.substring(uid_start+12, request.toLowerCase().indexOf("&", (uid_start+12)));
            trafficInfo.changeAccountUid(esme_name, account_name, uid);
        }
        
        return esme_name;
    }
    
    public String updateAccountBindings(char[] b){
        String request = new String(b);
        String esme_name = null;
        String account_name = null;
        int bindings = -1;
                
        int esme_start = request.toLowerCase().indexOf("esme_");
        int account_start = request.toLowerCase().indexOf("account_");
        int bindings_start = request.toLowerCase().indexOf("max-bindings=");
        
        if ( account_start >= 0 ){
            account_name = request.substring(account_start+8, request.toLowerCase().indexOf("_", (account_start+8)));
        }
        
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5, request.toLowerCase().indexOf("=", (esme_start+5)));
        }
        
        if( bindings_start >= 0 ){
            try{
                bindings = Integer.parseInt(request.substring(bindings_start+13, request.toLowerCase().indexOf("&", (bindings_start+13))));
            }catch(NumberFormatException e){
                bindings = 10;
            }
            trafficInfo.setMaxConnections(esme_name, account_name, bindings);
        }
        return esme_name;
    }
        
    public void removeEsme(char[] b){
        String request = new String(b);     
        String esme_name = "";
        int esme_start = request.toLowerCase().indexOf("esme_");                                 
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5,  request.toLowerCase().indexOf("=", (esme_start+5)));
            trafficInfo.removeEsme(esme_name);            
        }           
    }
    
    public String removeAccount(char[] b){
        String request = new String(b);        
        String esme_name = "";
        String account_name = "";
        int esme_start = request.toLowerCase().indexOf("esme_");
        int account_start = request.toLowerCase().indexOf("account_");
         
        if ( account_start >= 0 ){
            account_name = request.substring(account_start+8, request.toLowerCase().indexOf("_", (account_start+8)));
        }
        
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5, request.toLowerCase().indexOf("=", (esme_start+5)));
        } 
        trafficInfo.removeAccount(esme_name, account_name);
        
        return esme_name;        
    }
    
    public String resetSMSList(char[] b){
        String request = new String(b);
        String esme_name = "";
        int esme_start = request.toLowerCase().indexOf("esme_");                                 
        if ( esme_start >= 0 ){
            esme_name = request.substring(esme_start+5,  request.toLowerCase().indexOf("=", (esme_start+5)));
        }        
        trafficInfo.resetSMSList(esme_name);         
        return esme_name;
    }
}
