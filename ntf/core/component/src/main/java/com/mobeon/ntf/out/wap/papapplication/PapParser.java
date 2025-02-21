/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapplication;

import com.mobeon.ntf.util.Logger;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.ParserFactory;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 */
public class PapParser
{
  private final static Logger logger = Logger.getLogger(PapParser.class);
  // This flag should only be set to true for debugging purposes and development
  private boolean debug = false;

  // return code for push response
  private int code;
  private Object codeLock;

  // pushID of the push message
  private String pushID;
  private Object pushIDLock;

  // IPMS LOGGER used for logging

   /**
    * main is only used as a test engine to test PapParser
    * @roseuid 3A9FC4810023
    */
  public static void main(String[] args)
  {
    String uri;
    if ( args.length == 1 )
      uri = args[0];
    else
      uri = "myPushCtrl.xml";

    PapParser myParser = new PapParser();

    try
    {
      myParser.parsePushResult(uri);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
    System.exit(0);
  }

  /**
  * @roseuid 3A9FC4810025
  */
  public PapParser()
  {
    codeLock = new Object();
    pushIDLock = new Object();
  }

  /**
   * sets the parse result code
   */
  private void setCode(int code) {
    synchronized(codeLock) {
      this.code = code;
    }
  }


  /**
   * gets the parse result code
   */
  public int getCode() {
    int tmp;
    synchronized(codeLock) {
      tmp = code;
    }
    return tmp;
  }


  /**
   * sets the pushID
   */
  private void setPushID(String pushID) {
    synchronized(pushIDLock) {
      this.pushID = pushID;
    }
  }


  /**
   * gets the pushID
   */
  public String getPushID() {
    String tmp;
    synchronized(pushIDLock) {
      tmp = pushID;
    }
    return tmp;
  }


   /**
    * Parses the notification-response message  from PPG and retrieves the result
    * code.
    * @roseuid 3A9FC4810026
    */
   public int parsePushResult(InputStream is)
   {
    setCode(-1);
    setPushID("");
    ContentHandler contentHandler = new PapContentHandler();
    ErrorHandler errorHandler = new PapErrorHandler();

    try
    {
	// XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
// 	parser.setContentHandler(contentHandler);
// 	parser.setErrorHandler(errorHandler);
// 	parser.setContinueAfterFatalError(true);
// 	// Only validate the document, during development
// 	parser.setFeature("http://xml.org/sax/features/validation", false);
// 	//Parse the document
// 	parser.parse(new InputSource(is));

	org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
  	parser.setFeature("http://xml.org/sax/features/validation", false);
  	parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
  	parser.setErrorHandler(errorHandler);
// 	Document doc = parser.getDocument();
// 	NodeList elements = doc.getElementsByTagName("response-result");
// 	Element element = (Element)elements.item(0);
// 	NamedNodeMap attributes = element.getAttributes();
// 	Attr attribute = (Attr)attributes.item(0);
// 	String a = attribute.getNodeValue();
// 	setCode(Integer.parseInt(a));

	//Added by ermahen 6/7-2001 in order to set responsecode from  wgp.
	BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
	String s2;
	String s3="";
	while((s2 = bufferedreader.readLine()) != null)
	    s3 += s2;
	bufferedreader.close();
	int start = s3.indexOf("code=");
	String resultcode = s3.substring(start+6,start+10);    
	setCode(Integer.parseInt(resultcode));
    }
    catch (SAXException saxe)
    {
    	logger.logMessage("ERROR in parsing: " + saxe.getMessage(), logger.L_ERROR);
	return getCode();
    }
    catch (Exception ioe)
    {
      logger.logMessage("ERROR reading URI: " + ioe.getMessage(), logger.L_ERROR);
      return getCode();
    }
    return getCode();
   }

   /**
    * @roseuid 3A9FC4810028
    */
   public int parsePushResult(String uri)
   {
    setCode(-1);
    setPushID("");
    ContentHandler contentHandler = new PapContentHandler();
    ErrorHandler errorHandler = new PapErrorHandler();

    try
    {
	XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	parser.setContentHandler(contentHandler);
	parser.setErrorHandler(errorHandler);

      // Only validate the document, during development
	parser.setFeature("http://xml.org/sax/features/validation", false);
 
      //Parse the document
      parser.parse(uri);
    }
    catch (SAXException saxe)
    {
      logger.logMessage("ERROR in parsing: " + saxe.getMessage(), logger.L_ERROR);
      return getCode();
    }
    catch (IOException ioe)
    {
      logger.logMessage("ERROR reading URI: " + ioe.getMessage(), logger.L_ERROR);
      return getCode();
    }
    return getCode();
   }

   /**
    */
   class PapErrorHandler implements ErrorHandler
   {

      /**
       * @roseuid 3A9FC4810073
       */
      public void warning(SAXParseException spe) throws SAXException
      {
        logger.logMessage("**Parsing Warning**\n" +
                           "  Line:    " +
                           spe.getLineNumber() + "\n" +
                           "  URI:     " +
                           spe.getSystemId() + "\n" +
                           "  Message: " +
                           spe.getMessage(), logger.L_DEBUG);

        throw new SAXException("Warning encountered");
      }

      /**
       * @roseuid 3A9FC4810075
       */
      public void error(SAXParseException spe) throws SAXException
      {
        logger.logMessage("**Parsing Error**\n" +
                           "  Line:    " + spe.getLineNumber() + "\n" +
                           "  column:  " + spe.getColumnNumber() + "\n" +
                           "  PUBLIC:  " + spe.getPublicId() + "\n" +
                           "  SYSTEM:  " + spe.getSystemId() + "\n" +
                           "  Message: " + spe.getMessage(), logger.L_ERROR);

        throw new SAXException("Error encountered");
      }

      /**
       * @roseuid 3A9FC481007E
       */
      public void fatalError(SAXParseException spe) throws SAXException
      {
        logger.logMessage("**Parsing Fatal Error**\n" +
                           "  Line:    " +
                           spe.getLineNumber() + "\n" +
                           "  URI:     " +
                           spe.getSystemId() + "\n" +
                           "  Message: " +
                           spe.getMessage(), logger.L_ERROR);

        throw new SAXException("Fatal Error encountered");
      }
   }

   /**
    */
   class PapContentHandler implements ContentHandler
   {
      private Locator locator;

      /**
       * @roseuid 3A9FC4810094
       */
      public void setDocumentLocator(Locator locator)
      {
        logger.logMessage("     * setDocumentLocator() called", logger.L_DEBUG);
        this.locator = locator;
      }

      /**
       * @roseuid 3A9FC4810096
       */
      public void startDocument() throws SAXException
      {
        logger.logMessage("parsing begings...", logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC481009B
       */
      public void endDocument() throws SAXException
      {
        logger.logMessage("parsing ends...", logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC481009C
       */
      public void processingInstruction(String target, String data) throws SAXException
      {
        logger.logMessage("PI: Traget:" + target + " and Data:" + data, logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC481009F
       */
      public void startPrefixMapping(String prefix, String uri)
      {
        logger.logMessage("Mapping starts for prefix " + prefix + " mapped to URI " + uri, logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100A6
       */
      public void endPrefixMapping(String prefix)
      {
        logger.logMessage("Mapping ends for prefix: " + prefix, logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100A8
       */
      public void endElement(String namespaceURI, String localName, String rawName) throws SAXException
      {
        logger.logMessage("endElement: " + localName + "\n", logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100AF
       */
      public void characters(char[] ch, int start, int length) throws SAXException
      {
        String s = new String(ch, start, length);
        logger.logMessage("characters: " + s, logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100B3
       */
      public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
      {
        String s = new String(ch, start, length);
        logger.logMessage("ignorableWhitespace: [" + s + "]", logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100BB
       */
      public void skippedEntity(String name) throws SAXException
      {
        logger.logMessage("Skipping entity " + name, logger.L_DEBUG);
      }

      /**
       * @roseuid 3A9FC48100BD
       */
      public void startElement(String namespaceURI, String localName, String rawName, Attributes atts) throws SAXException
      {
        logger.logMessage("start element: " + localName, logger.L_DEBUG);

        if (debug)
        {
          if (!namespaceURI.equals(""))
          {
            logger.logMessage(" in namespace " + namespaceURI + " (" + rawName + ")", logger.L_DEBUG);
          }
          else
          {
            logger.logMessage(" has no associated namespace", logger.L_DEBUG);
          }

          for (int i=0; i<atts.getLength(); i++)
          {
            logger.logMessage(" Attributes: " + atts.getLocalName(i) + "=" + atts.getValue(i), logger.L_DEBUG);
            if (atts.getLocalName(i).compareToIgnoreCase("code") == 0)
            {
              setCode(Integer.parseInt(atts.getValue(i)));
              break;
            }
          }
        } // if debug

        if ((localName.equals("response-result")) ||
            (localName.equals("badmessage-response")))
        {
          for (int i=0; i<atts.getLength(); i++)
          {
            if (atts.getLocalName(i).compareToIgnoreCase("code") == 0)
            {
              setCode(Integer.parseInt(atts.getValue(i)));
              break;
            }
          } //for
        }
      } // startElement
   } // PapContentHandler
} // PapParser
