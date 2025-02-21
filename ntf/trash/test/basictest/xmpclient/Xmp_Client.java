
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.XMLSerializer;
import java.net.*;
import org.xml.sax.InputSource;

import com.mobeon.common.externalcomponentregister.IServiceName;


public class Xmp_Client {


	private class Reader extends Thread{
		  
	Reader(){
	} 

        public void run(){
        try{
             String inStr="";
             String temp;
             int index;
             int length;
             char[] buffer = null;
          
             try{
                 BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                 while ( (temp = in.readLine()) != null) {
                     if((index = temp.toLowerCase().indexOf("content-length")) != -1){
                         length = Integer.parseInt(temp.substring(temp.indexOf(":")+1).trim());
                         buffer = new char[length+2];
                         in.read(buffer, 0, (length+2));

                         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                         DocumentBuilder builder = factory.newDocumentBuilder();
                         StringReader sr = new StringReader(new String(buffer,2,buffer.length-2));
                         InputSource iSrc = new InputSource(sr);
                         Document doc = builder.parse(iSrc);
			 for(int i = 0; i < doc.getElementsByTagName("xmp-service-response").getLength(); i++){
				String id = "";
				String code = "";
				String statusText = "";
 				Element xsr = (Element)doc.getElementsByTagName("xmp-service-response").item(i);
		    		if(xsr.getAttributes().getNamedItem("transaction-id") != null){
					id = xsr.getAttributes().getNamedItem("transaction-id").getNodeValue();
		    		}
		    
		    		if(xsr.getElementsByTagName("status-code").getLength() != 0){
					code = xsr.getElementsByTagName("status-code").item(0).getFirstChild().getNodeValue();
		    		}
		    
		    		if(xsr.getElementsByTagName("status-text").getLength() != 0){
					statusText = xsr.getElementsByTagName("status-text").item(0).getFirstChild().getNodeValue();
		    		}
                        	System.out.println("Status-Code: " + code);
                                System.out.println("Status-Text: " + statusText);
                         	System.out.println("ID: " + id);

			}
                     }

                 }
             }
             catch(Exception e){
                e.printStackTrace();
             }
          
        }catch(Exception e){
            e.printStackTrace();
        } 
    	}//end of function	
	
	}




    private Document doc;
    private Element root;
    private Element service;
    private Socket socket; 
    OutputStream out;
    OutputStreamWriter wout;

    public static void main(String[] args) {
           Xmp_Client x = new Xmp_Client(args);           
    }
    
    public Xmp_Client(String[] a) {
        try{
            System.out.println("Start! Connecting to host " + a[0] + " on port " +a[1] );            
            socket = new Socket(a[0], Integer.parseInt(a[1]));
            out = socket.getOutputStream();
            wout = new OutputStreamWriter(out);
            new Reader().start();            
            System.out.println("Requested service: " + a[8]);
            if(a[8].equalsIgnoreCase("pagernotification")) buildPagerXmlFile(a);
            else if(a[8].equalsIgnoreCase("outdialnotification")) buildOdlXmlFile(a);            
        }catch(Exception e){e.printStackTrace();}
    }  
 
    private void buildOdlXmlFile(String a[]){    
     try {           
          for(int i =1; i<=Integer.parseInt(a[2]); i++){	
           doc= new DocumentImpl();
           root = doc.createElement("xmp-message");
           root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
           service = doc.createElement("xmp-service-request");
           service.setAttribute("service-id", IServiceName.OUT_DIAL_NOTIFICATION);
           service.setAttribute("client-id", "test_client");
           service.setAttribute("transaction-id", ""+i);                                 
           addElement(service, "validity", "200");    
           addElement(service, "message-report", "true");
           addParameterElement(service, "number", a[3]);
           addParameterElement(service, "mailbox-id", a[4]);
           root.appendChild( service );
           doc.appendChild( root );                       
           sendXmpRequest();
	   doc=null;
          }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }   
    }
    
    private void buildPagerXmlFile(String a[]){    
     try {           
          for(int i =1; i<=Integer.parseInt(a[2]); i++){	
           doc= new DocumentImpl();
           root = doc.createElement("xmp-message");
           root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
           service = doc.createElement("xmp-service-request");
           service.setAttribute("service-id", "PagingNotification");
           service.setAttribute("client-id", "test_client");
           service.setAttribute("transaction-id", ""+i);                                 
           addElement(service, "validity", "200");
           addParameterElement(service, "mailbox-id", a[4]);
           addParameterElement(service, "paging-system-number", a[3]);
           addParameterElement(service, "paging-digits", a[5]);
           addParameterElement(service, "hang-up", a[6]);
           addParameterElement(service, "pause-time", a[7]);
           root.appendChild( service );
           doc.appendChild( root );                       
           sendXmpRequest();
	   doc=null;
          }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }   
    }
    
    
    private void sendXmpRequest(){
        try{
           String post              = "POST /OutDialNotification HTTP/1.1\r\n";
           String host              = "Host: ntf1.moip.com\r\n";
	   String keep              = "Connection: keep-alive\r\n";
           String content_type      = "Content-Type: text/xml; charset=utf-8\r\n";
                      
           OutputFormat    format  = new OutputFormat( doc );
           StringWriter  stringOut = new StringWriter();
           XMLSerializer    serial = new XMLSerializer( stringOut, format );
           serial.asDOMSerializer();
           serial.serialize( doc.getDocumentElement() );            
           
           String content_length    = "Content-Length: " + stringOut.toString().length() + "\r\n\r\n";
           String xmp_message       = stringOut.toString();                      
           
           if(wout != null){
                          
               System.out.println(post+host+content_type+content_length+xmp_message);
               wout.write(post+keep+host+content_type+content_length+xmp_message);
               wout.flush();
           }
         } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
    
    private void addParameterElement(Element service, String attributeName, String textValue){
           Element parameter = doc.createElement("parameter");
           parameter.setAttribute("name",  attributeName);
           parameter.appendChild( doc.createTextNode(textValue) );       
           service.appendChild( parameter ); 
    }
    
    private void addElement(Element service, String attributeName, String textValue){
           Element parameter = doc.createElement(attributeName);
           parameter.appendChild( doc.createTextNode(textValue) );
           service.appendChild( parameter );
    }
}

