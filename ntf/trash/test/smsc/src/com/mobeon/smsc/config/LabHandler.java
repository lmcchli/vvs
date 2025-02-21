package com.mobeon.smsc.config;

import org.w3c.dom.Document;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.util.logging.*;
import java.util.Hashtable;
import com.mobeon.smsc.containers.ESMEHandler;
import com.mobeon.smsc.interfaces.ConfigurationManagement;


/**
 *Reads the .labs.xml and inits all registred ESME and accounts 
 *that can be found in the .labs.xml file. 
 *It also handles requests to add and remove ESMEs and accounts during 
 *run time. When a request arrives to add or remove a account or an ESME 
 *the LabHandler do the modifications to the Doucument object and write it back 
 *to disk. The .labs.xml is holds a XML structure over all registred ESME and accounts.
 *The xml root is <labs>.
 *Each ESME entity is represented by the tag <lab> and has one attribute
 *called name, wich is the name of the ESME.
 *Each <lab> has has a <log> node wich has two children, <level> and <size>
 *<level> holds the loglevel for the ESME
 *<size> is the size of the SMS PDU cache
 *The <account> node hold information about accounts registred for a ESME.
 *The <account> node has one attribute, uid, wich the name of the account.
 *Each <account> node has two children, <password> and <connections>.
 *<password> contains the password for the account
 *<connections> contains information on how many connections that could be 
 *initated to the same account.
 *This is an example of how the .labs.xml could look like with one regisred ESME with one registred accont.
 *
 *<?xml version="1.0" encoding="UTF-8"?>
 *<labs>
 *   <lab name="MOIP">
 *        <log>
 *           <level>fine</level>
 *           <size>1000</size>
 *       </log>
 *       <account uid="AMPCLIENT">
 *           <password>AMPCLIENT</password>
 *           <connections>10</connections>
 *       </account>
 *    </lab>
 *</labs>
 *
 */
public class LabHandler implements ConfigurationManagement{
  
    /** Path to labs.xml*/
    private static final String CONFIG_FILE_NAME = "../cfg/.labs.xml";
    /** DOM document of all registred labs*/
    private Document doc = null;
    /** General log target */
    private Logger logger = null;
    
    /** Constructor */
    public LabHandler() {
        logger = Logger.getLogger("SMSC");
        initLabs();
        /** Let ESMEHandler know that it is this class that
         is responsable to update the labs.xml file.*/
        ESMEHandler.get().setConfigurationManagement(this);
    }
    
    /**
     *reads the labs.xml file and create a DOM object containg all ESME and accounts found 
     *in the file.
     */
    public boolean initLabs(){
        try{
            File xmlfile = new File(CONFIG_FILE_NAME);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (!xmlfile.exists()) {
                doc = new DocumentImpl();
                doc.appendChild(doc.createElement("labs"));
            } else {
                doc = builder.parse(xmlfile);
            }
            createlabs( doc.getElementsByTagName("lab") );
            return true;
        }catch(FactoryConfigurationError e){
            logger.severe("Could not parse xml file correctly. " + e);
            return false;
        }catch(ParserConfigurationException e){
            logger.severe("Could not parse xml file correctly. " + e);
            return false;
        }catch(FileNotFoundException e){
            logger.severe("Could not find xml file. " + e);
            return false;
        }catch(Exception e){
            logger.severe("Unknown error. " + e);
            return false;
        }
    }
    
    /**
     *Find all lab nodes and create a new ESME.
     *@param labs contains all lab nodes.
     */
    private void createlabs(NodeList labs){
        try{
            for( int i = 0; i < labs.getLength(); i++ ){
                Node  lab = labs.item(i);
                String labname = lab.getAttributes().getNamedItem("name").getNodeValue();
                if( labname != null ){
                    NodeList  log = ((Element)lab).getElementsByTagName("log");
                    if( log != null && log.getLength() != 0){
                        Node node = log.item(0);
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Adding new ESME: " + "{ " + labname + "}");
                        }                        
                        ESMEHandler.get().addESME(labname, getLoglevel(node), getLogSize(node), lab);
                        
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    /** Gets the loglevel for a ESME from the DOM object*/
    private String getLoglevel(Node lab){
        String level = "fine";
        try{
            return getElementValue(lab, "level");
        }catch(Exception e){
            logger.severe("Could not parse loglevel. Using default value "  + level + " " +  e);
            return level;
        }
    }
    
    /** Gets the size of the SMS PDU cache for a ESME from the DOM object*/
    private int getLogSize(Node lab){
        int size = 1000;
        try{
            int t = Integer.parseInt(getElementValue(lab, "size"));
            if( t != -1 ) size = t;
            return size;
        }catch(Exception e){
            logger.severe("Could not parse logsize. Using default value "  + size + " " +  e);
            return size;
        }
    }
    
        
    /**
     *Adds a new account to an existing ESME.
     *The DOM object and labs.xml is updated with this new information.
     *@param esmeName the name of the ESME
     *@param accountName the account name to add.
     *@param password for the account
     *@param connections max number of account threads bound to an account
     */
    public void addAccount(String esmeName,
                           String accountName,
                           String password,
                           String connections){                              
       Element account = doc.createElement("account");
       account.setAttribute("uid", accountName);                            
       Element pwd = doc.createElement("password");       	    
       pwd.appendChild(doc.createTextNode(password));
       account.appendChild(pwd);              
       Element con = doc.createElement("connections");       	    
       con.appendChild(doc.createTextNode(connections));
       account.appendChild(con); 
                      
       NodeList labs = doc.getElementsByTagName("lab");
        for( int i = 0; i < labs.getLength(); i++ ){
            Node  lab = labs.item(i);
            String esme = lab.getAttributes().getNamedItem("name").getNodeValue();
            if( esme != null && esmeName.equalsIgnoreCase(esmeName) ){
                lab.appendChild(account);
                writeChangesToDisk();
            }
        }       
    }
       
    /**
     *Adds a new ESME and a new accout to the DOM object. labs.xml
     * is updated with this new information
     *If the ESME exist the ESME will not be added to cache.
     *@param esmeName the name of the ESME to be added
     *@param loglev loglevel for the ESME
     *@param cacheSize the size of the SMS PDU cache
     *@param accountName the name of the new account
     *@param password password for the account
     *@param connections number of account threads that could be bound to the account.
     */
    public void addEsmeAndAccount(String esmeName,
                                  String loglevel,
                                  String cacheSize,
                                  String accountName,
                                  String password,
                                  String connections){      
       Element root = doc.getDocumentElement();
       Element lab = doc.createElement("lab");        
       lab.setAttribute("name", esmeName);              
       Element log = doc.createElement("log");              
       Element level = doc.createElement("level");       	    
       level.appendChild(doc.createTextNode(loglevel));	    
       log.appendChild(level);              
       Element size = doc.createElement("size");              
       size.appendChild(doc.createTextNode(cacheSize));	    
       log.appendChild(size);              
       Element account = doc.createElement("account");
       account.setAttribute("uid", accountName);                            
       Element pwd = doc.createElement("password");       	    
       pwd.appendChild(doc.createTextNode(password));
       account.appendChild(pwd);              
       Element con = doc.createElement("connections");       	    
       con.appendChild(doc.createTextNode(connections));
       account.appendChild(con);       
       lab.appendChild(log);
       lab.appendChild(account);       
       root.appendChild(lab);
       writeChangesToDisk();
    }
 
    
    /**
     *Removes a registred ESME from the DOM object and writes the changes to disk.
     *@param esme the ESME to remove.
     */
    public void removeEsme(String esme){
        NodeList labs = doc.getElementsByTagName("lab");
        Node root = doc.getDocumentElement();
        Node  remove = null;
        for( int i = 0; i < labs.getLength(); i++ ){
            Node lab = labs.item(i);
            String esmeName = lab.getAttributes().getNamedItem("name").getNodeValue();
            if( esmeName != null && esmeName.equalsIgnoreCase(esme) ){    
                System.out.println(lab.getAttributes().getNamedItem("name").getNodeValue());
                root.removeChild(lab);
                writeChangesToDisk();
            }
        }
    }
        
    /**
     *
     *Consider the following example:
     *<lab name="MOIP">
     *  <log>
     *      <level>fine</level>
     *      <size>300</size>
     *  </log>
     *  .
     *  .
     *  . 
     *</lab>
     * <lab name="MOIP">
     *  <log>
     *      <level>fine</level>
     *      <size>300</size>
     *  </log>
     *  .
     *  .
     *  . 
     *</lab>
     *
     *if changeEsmeLogSettings(....) is called with 
     *the following parameters MOIP, level and all               
     *@param esme the name of ESME to be updated.
     *@param nodeName the node to be updated 
     *@param newValue the new value
     */
    public void changeEsmeLogSettings(String esme, String nodeName, String newValue){
        NodeList labs = doc.getElementsByTagName("lab");
        for( int i = 0; i < labs.getLength(); i++ ){
            Node  lab = labs.item(i);
            String esmeName = lab.getAttributes().getNamedItem("name").getNodeValue();
            if( esmeName != null && esmeName.equalsIgnoreCase(esme) ){
                NodeList  logNode = ((Element)lab).getElementsByTagName("log");
                if( logNode != null && logNode.getLength() != 0){           
                    NodeList logChild = ((Element)logNode.item(0)).getElementsByTagName(nodeName);            
                    if ( logChild != null && logChild.getLength() != 0  ){                
                        logNode.item(0).replaceChild(createNode(nodeName, newValue), logChild.item(0));                         
                        writeChangesToDisk();
                    }                   
                }
            }
        }
    }

    /**
     * Removes a account from the DOM object and writes the chanages to disk.
     *@param esmeName the name of the ESME.
     *@param accountName the account to remove
     */
    public void removeAccount(String esmeName, String accountName){
        NodeList labs = doc.getElementsByTagName("lab");
        for( int i = 0; i < labs.getLength(); i++ ){
            Node  lab = labs.item(i);
            String esme = lab.getAttributes().getNamedItem("name").getNodeValue();
            if( esme != null && esme.equalsIgnoreCase(esmeName) ){    
                NodeList accounts = ((Element)lab).getElementsByTagName("account");
                for( int q = 0; q < accounts.getLength(); q++ ){                                        
                    Node account = accounts.item(q);            
                    if( account.getAttributes().getNamedItem("uid").getNodeValue().equalsIgnoreCase(accountName) ){  
                        System.out.println(account.getAttributes().getNamedItem("uid").getNodeValue());
                        lab.removeChild(account);
                        writeChangesToDisk();
                    }        
                }
            }
        }
    }
    
    /**
     * Modifies the account part in the xml document. 
     *@param esmeName the ESME under wich the account is located under.
     *@param accountName the account to modify.
     *@param nodeName is the node to modify, either password or connection.
     *@param newValue is the value to replace the old one with.
     */
    public void changeAccountSettings(String esmeName, String accountName, String nodeName, String newValue){
        NodeList labs = doc.getElementsByTagName("lab");
        for( int i = 0; i < labs.getLength(); i++ ){
            Node  lab = labs.item(i);
            String esme = lab.getAttributes().getNamedItem("name").getNodeValue();
            if( esme != null && esme.equalsIgnoreCase(esmeName) ){                            
                if(nodeName.equalsIgnoreCase("uid")){
                    changeAccountUid(accountName, newValue, lab);
                }
                else{                
                    changeAccountSettings(accountName, nodeName, newValue, ((Element)lab).getElementsByTagName("account"));                                
                }
            }
        }
    }
    
    private void changeAccountSettings(String accountName, String nodeName, String newValue, NodeList accounts){        
        if( accounts == null) return;        
        for( int i = 0; i < accounts.getLength(); i++ ){
            Node account = accounts.item(i);
            if( account.getAttributes().getNamedItem("uid").getNodeValue().equalsIgnoreCase(accountName) ){                
                NodeList  nl = ((Element)account).getElementsByTagName(nodeName);
                if( nl != null && nl.getLength() != 0){
                    account.replaceChild(createNode(nodeName, newValue), nl.item(0));                        
                    writeChangesToDisk();                    
                }
            }                        
        }    
    }
         
    private void changeAccountUid(String accountName, String newValue, Node lab){        
        if( lab == null) return;                
        NodeList accounts = ((Element)lab).getElementsByTagName("account");
        if( accounts == null) return;
        
        for( int i = 0; i < accounts.getLength(); i++ ){
            Node account = accounts.item(i);
            if( account.getAttributes().getNamedItem("uid").getNodeValue().equalsIgnoreCase(accountName) ){
                Element a = (Element)account;
                a.setAttribute("uid", newValue);    
                writeChangesToDisk();
            }
        }                                            
    }
    
    /*
     * Transforms the DOM object to a string object.
     *@return a string containing the DOM document.
     **/
    private String getDocumentAsString(){
        StringWriter  stringOut;
        try{
            OutputFormat format     = new OutputFormat( doc );
            stringOut               = new StringWriter();
            XMLSerializer serial    = new XMLSerializer( stringOut, format );
            serial.asDOMSerializer();
            serial.serialize( doc.getDocumentElement() );
            return stringOut.toString();
        }catch(Exception e){
            return null;
        }
    }
        
    /**
     * creates a node with a text node as a child.
     *@param nodeName the name of the Node.
     *@param nodeValue the value of the text node.     
     */
    private Node createNode(String nodeName, String nodeValue){
        try{
            Element el = doc.createElement(nodeName);
            el.appendChild(doc.createTextNode(nodeValue));
            return el;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }
         
    /**
     *Gets a text value from a Node.
     *@param root the root node to search from.
     *@param element the Node to search for
     *@return the text value of the element node. 
     */
    private String getElementValue(Node root, String element){
        String value = null;
        try{
            NodeList nl = ((Element)root).getElementsByTagName(element);
            if ( nl != null && nl.getLength() != 0  ){
                value = nl.item(0).getFirstChild().getNodeValue();
            }
            return value;
        }catch(Exception e){
            logger.severe("Could not parse xml file. " +  e);
            return value;
        }
    }
    
    /**
     * Writes the DOM object to disk.
     */
    private void writeChangesToDisk(){
        try{
        FileWriter fw = new FileWriter(CONFIG_FILE_NAME);
        fw.write(getDocumentAsString());
        fw.flush();
        fw.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    public static void main(String[] args){
        LabHandler lab = new LabHandler();
        lab.addEsmeAndAccount("MoIP", "all", "30", "ntf1", "ampclient", "1235");
        lab.removeAccount("MoIP", "ntf1");
    }
}
