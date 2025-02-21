import jakarta.mail.Session;
import java.util.*;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.*;
import java.io.BufferedReader;
import java.io.*;

public class MWIOffSender {
    
    private static final int HELP_CODE = 44757230;
    private static final int SUBSCRIBER_CODE = 405576696;
    private static final int SMTP_HOST = 1499;
    private static final int SMTP_PORT = 1507;
    private static final int SENDER_EMAIL = 376997026;
    private static final int PRINT_VALUES = 44757230;
    private static final int FUNCTION = 1649866501;
    private static final int FILE = 44701481;    
    private boolean mwioff = false;
    private boolean slamdown = false;
    private String file = null;
    private String from = null;        
    private String to = null;
    private String port = null;
    private String host = null;
    private String subscriberTelephoneNumber = null;
    
    public static void main(String args[]) {
        MWIOffSender m = new MWIOffSender(args);        
    }
    
    MWIOffSender(String[] args){
        try{
            String usage = "Usage: java MWIOffSender [-help] " + 
                           "\r\n [-h <hostname>] " + 
                           "\r\n [-p <port>] " + 
                           "\r\n [-sender <sender-email>] " +
                           "\r\n [-num <mwi subscriber telephonenumber> | " +
                           "\r\n  -file <file with phonenumbers or telnum and emailaddresses> \r\n " + 
                           " e.g. telnum <new line > telnum or emailaddress <new line> .....]" + 
                           "\r\n [-function mwioff|slamdown]";                           
            boolean printValues = false;
            
            if(args.length > 0) {
                for(int i = 0; i < args.length; i++) {  
                    switch (args[i].hashCode()) {
                        case HELP_CODE:
                            System.out.println(usage);
                            System.exit(1);
                            break;
                        case FUNCTION:
                            String s = args[++i].trim();
                            if( s.equalsIgnoreCase("mwioff") ) mwioff = true;                                
                            else slamdown = true;
                            break;
                        case FILE:
                            file = args[++i];
                            break;
                        case SUBSCRIBER_CODE:
                            subscriberTelephoneNumber = args[++i];
                            break;
                        case SMTP_HOST:
                            host = args[++i];
                            break;
                        case SMTP_PORT:
                            port = args[++i];
                            break;
                        case SENDER_EMAIL:
                            from = args[++i];
                            break;
                        default:
                            System.out.println("Not recognized command " + args[i]);
                            System.out.println(usage);
                            System.exit(1);
                    }
                }
            }
            
            if(!mwioff && !slamdown){                        
                System.out.println(usage);                                               
                System.exit(1); 
            }
            if(port == null ||
               host == null ||
               from == null ||
               (subscriberTelephoneNumber == null && file == null)){                                             
                   System.out.println(usage);                            
                   System.exit(1);                   
            }                
            
            if(mwioff)
                sendMwiOff();
            else if(slamdown)
                sendSlamdown();
        }catch(Exception e) {
            System.out.println(e);
        }
    }
    
    private void sendSlamdown(){
        try{                      
            Vector v = new Vector();
            String text = "";
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.timeout", "2000");
            props.put("mail.smtp.connectiontimeout", "2000");
            Session session =  Session.getInstance(props);
                         
            if(file != null){             
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String s;
                while( (s = br.readLine()) != null){ v.add(s); }
            }
            else {
                System.out.println("No file found.");
                System.exit(1);
            }
            for(int i = 0; i < v.size(); i++){
                text += (String)v.get(i) + " " + (String)v.get(i+1) + "\r\n";               
                i++;
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sink"));
            message.setHeader("To", "notification.off@" + host);
            message.setSubject("ipms/message");
            message.setHeader("Ipms-Notification-Version", "1.0");
            message.setHeader("Ipms-Component-From", "emComponent=" + host);
            message.setHeader("Ipms-Notification-Type", "mvas.subscriber.slamdown");
            message.setHeader("Ipms-Notification-Content", "mvas.subscriber.slamdown");
            message.setText(text);
            Transport transport = session.getTransport("smtp");
            transport.send(message);
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    
    private void sendMwiOff(){
        try{
            Vector v = new Vector();
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.timeout", "2000");
            props.put("mail.smtp.connectiontimeout", "2000");
            Session session =  Session.getInstance(props);
            
            if(file != null){             
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String s;
                while( (s = br.readLine()) != null){ v.add(s); }
            }
            else
                v.add(subscriberTelephoneNumber);
            
            for(int i = 0; i < v.size(); i++){               
                System.out.println("Sending mwi_off to: " + (String)v.get(i));
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("sink"));
                message.setHeader("To", "notification.off@" + host);
                message.setSubject("ipms/message");
                message.setHeader("Ipms-Notification-Version", "1.0");
                message.setHeader("Ipms-Component-From", "emComponent=" + host);
                message.setHeader("Ipms-Notification-Type", "mvas.subscriber.logout");
                message.setHeader("Ipms-Notification-Content", (String)v.get(i));
                message.setText("Junk");
                Transport transport = session.getTransport("smtp");
                transport.send(message);
            }
        }catch(Exception e){
            System.out.println(e);
        }                
    }
    
    
}
