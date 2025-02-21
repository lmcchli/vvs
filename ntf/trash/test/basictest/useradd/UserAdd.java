/*
 * UserAdd.java
 *
 * Created on den 8 november 2004, 15:32
 */

package useradd;

import java.io.*;
import java.util.*;
import java.text.*;
import netscape.ldap.*;

/**
 *
 * @author  MNIFY
 */
public class UserAdd {
    Properties props;
    String orgTelephoneNumber = "";
    String orgFaxNumber = "";
    String pncPostfix = "";
    int uniqueCount = 0;
    String uniqueName = "um";
    
    /** Creates a new instance of UserAdd */
    public UserAdd(File file) {
        LDAPConnection conn = null;
        
        LDAPEntry orgEntry = null;
        try {
            props = new Properties();
            props.load(new FileInputStream(file));
            conn = connect(true);
            if( conn == null ) {
                return;
            }
            orgEntry = loadEntry(conn);
        } catch(Exception e) {
            orgEntry = null;
        }
            
        if(orgEntry == null ) {
            System.out.println("Cant load original user");
            System.exit(1);
        }
        int startNumber = Integer.parseInt(props.getProperty("telephonenumber"));
        int startFaxNumber = Integer.parseInt(props.getProperty("faxnumber"));
        String baseName = props.getProperty("basename");
        int maxCount = Integer.parseInt(props.getProperty("count"));
        int tempCount = maxCount;
        String pattern = "";
        while(tempCount >= 1) {
            pattern += "0";
            tempCount /= 10;
        }
        DecimalFormat format = new DecimalFormat(pattern);
        
        String baseDn = orgEntry.getDN();
        int index = baseDn.indexOf(",");
        baseDn = baseDn.substring(index+1);
        getUniquiIdentifier(conn);
        for( int i=1;i<=maxCount;i++ ) {
            int newNumber = startNumber + (i-1);
            int newFaxNumber = startFaxNumber + (i-1);
            String uid = baseName + format.format(i);
            addUser(conn, orgEntry, uid, newNumber, newFaxNumber, baseDn);
        }
        saveUniqueIdentifier(conn);
        
    }
    
    private void addUser(LDAPConnection conn, LDAPEntry orgEntry, String uid, int number, int faxNumber, String baseDn) {
        System.out.println("Adding " + uid + "," + number);
        LDAPAttributeSet attrs = orgEntry.getAttributeSet();
        
        //remove uid,mail,telephone,notif
        String telephoneNumber = attrs.getAttribute("telephoneNumber").getStringValueArray()[0];
        if( orgTelephoneNumber.equals(""))
            orgTelephoneNumber = telephoneNumber;
        if( attrs.getAttribute("emPNC") != null && pncPostfix.equals("")) {
            String emPNC = attrs.getAttribute("emPNC").getStringValueArray()[0];
            int index = emPNC.indexOf("+");
            pncPostfix = emPNC.substring(index);
            
        }
        
        attrs.remove("uid");
        attrs.remove("telephoneNumber");
        attrs.remove("mailalternateaddress");
        attrs.remove("emNotifNumber");
        String mail = attrs.getAttribute("mail").getStringValueArray()[0];
        int index = mail.indexOf("@");
        mail = mail.substring(index);
        attrs.remove("mail");
        attrs.remove("uniqueIdentifier");
        attrs.remove("emPNC");
 	attrs.remove("emdeliveryProfile");
        
        attrs.add(new LDAPAttribute("uid",uid));
        attrs.add(new LDAPAttribute("telephoneNumber",""+number));
        attrs.add(new LDAPAttribute("mailalternateaddress","FAX="+faxNumber+"@"));
        attrs.add(new LDAPAttribute("emNotifNumber",""+number));
        attrs.add(new LDAPAttribute("mail",uid + mail));
	attrs.add(new LDAPAttribute("emDeliveryProfile", "" + number + ";MWI;I"));
        if( !pncPostfix.equals("")) {
            attrs.add(new LDAPAttribute("emPNC", number+pncPostfix ));
        }
        
        String uniqueId = getNextUnigueIdentifier(conn);
        
        if( uniqueId.equals("")) {
            System.out.println("Cant get unique identifier");
            return;
        }
        attrs.add(new LDAPAttribute("uniqueIdentifier",uniqueId));
        String dn = "uniqueIdentifier=" + uniqueId + "," + baseDn; 
        
        LDAPEntry newEntry = new LDAPEntry(dn, attrs);
        LDAPEntry billingEntry = null;
        try {
            conn.add(newEntry);
            billingEntry = getBillingEntry(conn, orgEntry.getDN(), orgTelephoneNumber);
            if( billingEntry != null ) {
                LDAPAttributeSet billingAttrs = billingEntry.getAttributeSet();
                billingAttrs.remove("billingNumber");
                billingAttrs.add(new LDAPAttribute("billingNumber",""+number ));
                String billingDN = "billingNumber=" + number + "," + dn;
                LDAPEntry newBillingEntry = new LDAPEntry(billingDN,billingAttrs);
                conn.add(newBillingEntry);
            }
        } catch (Exception e ) {
            System.out.println("Couldnt add " + uid + "," + e.toString());
            System.out.println("Entry: " + newEntry);
            System.out.println("Billingnumberentry: " + billingEntry);
        }
        
        
    }
    
    private LDAPEntry getBillingEntry(LDAPConnection conn, String dn, String billingNumber) {
        String billingDN = "billingNumber=" + billingNumber + "," + dn;
        try {
            LDAPEntry entry = conn.read(billingDN);
            return entry;
        } catch(Exception e ) {
            System.out.println("Exception while loading billing number entry");
        }
        return null;
        
    }
    
    private void getUniquiIdentifier(LDAPConnection conn) {
        String dn = "authorityname=um,ou=Directory Administrators," + props.getProperty("searchbase");
       
        try {
            LDAPEntry entry = conn.read(dn);
            LDAPAttributeSet attrs = entry.getAttributeSet();
            String count = attrs.getAttribute("uniqueidentifiercounter").getStringValueArray()[0];
            int countInt = Integer.parseInt(count);
            uniqueCount = countInt;
                    
        } catch(Exception e ) {
            System.out.println("Exception getting uniqueIdentifier, " + e.toString() );
        }
    }
    
    private void saveUniqueIdentifier(LDAPConnection conn) {
        String dn = "authorityname=um,ou=Directory Administrators," + props.getProperty("searchbase");
       
        try {
            LDAPEntry entry = conn.read(dn);
            LDAPAttributeSet attrs = entry.getAttributeSet();
       
            LDAPModificationSet newEntry = new LDAPModificationSet();
            newEntry.add(LDAPModification.REPLACE, new LDAPAttribute("uniqueidentifiercounter", "" + uniqueCount));
            conn.modify(dn,newEntry);
           
        } catch(Exception e ) {
            System.out.println("Exception saving uniqueIdentifier, " + e.toString() );
        }
    }
    
    private String getNextUnigueIdentifier(LDAPConnection conn) {
       String returnValue =  uniqueName + uniqueCount;
       uniqueCount++;
       return returnValue;
    }
    
    private LDAPEntry loadEntry(LDAPConnection conn) {
        String userName = props.getProperty("uid");
       
        
        LDAPEntry entry = readUser(userName,conn);
        return entry;
        
    }
    
    private LDAPEntry readUser( String uid, LDAPConnection conn ) {
        
        String dn = "uid=" + uid +
            props.getProperty("searchbase");
        try {
            String baseDn = props.getProperty("searchbase");
            String pattern = "(uid=" + uid + ")";
            
            LDAPSearchResults result = conn.search(baseDn,LDAPv2.SCOPE_SUB,pattern,null,false);
            LDAPEntry entry = result.next();
            return entry;
            
        } catch(LDAPException e) {
            switch (e.getLDAPResultCode()) {
                case LDAPException.NO_SUCH_OBJECT:
                    // do nothing 
                    
                    break;
                default:
                    System.out.println("Unexpected exception searching user \""
                                   + uid + "\" from MUR. " + e);
                    
                }
        }
        return null;    
    }
    
    private LDAPConnection connect(boolean authenticate)  {
        LDAPConnection conn;
        String host = props.getProperty("murhost");
        int port = Integer.parseInt(props.getProperty("murport"));
       
        conn = new LDAPConnection();
                
                
        
        try {
            
            conn.connect(host, port);
        } catch (LDAPException e) {
            System.out.println("Failed to connect to MUR (" + host + ":" + port
                           + ") " + e);
        }
        if(!conn.isConnected())
            conn = null;
            
        if( authenticate ) {
            if( conn != null ) {
                try {
                    String MGR_DN = "uid=" + props.getProperty("murusername") + ",ou=Directory Administrators,"+ props.getProperty("searchbase");
                    String MGR_PW = props.getProperty("murpassword");
                    conn.authenticate( MGR_DN, MGR_PW );
                } catch(LDAPException e) {
                    System.out.println("Cant connect to MUR " + e.toString() );
                    conn = null;
                }
            }
        }
        return conn;
    }
    
    
    public static void main(String[] args) {
        String usage = "USAGE java UserAdd cfg-file";
        String example = "EXAMPLE java UserAdd volvo.cfg";
        
      
        if( args.length != 1 ) {
            System.out.println(usage);
            System.out.println(example);
            return;
        }
        
        String fileName = args[0];
        
        File file = new File(fileName);
        
        if( !file.canRead() ) {
            System.out.println("Cant find " + fileName + ".\nExiting");
        }
          
        new UserAdd(file);
        
    }
}
