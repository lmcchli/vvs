/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papclients;

import com.mobeon.ntf.util.Logger;
import java.io.*;
import java.net.*;

import java.util.Base64;

/**
 * This interface class creates an HTTP connection to the PPG.
 */
public class HttpClient
{
   private final static Logger logger = Logger.getLogger(HttpClient.class); 
   /**
    * The initial size of the ByteArrayOutputStream to print out the push message.
    * This size will grow automatically if needed
    */
   private static final int INITIAL_OUTPUT_SIZE = 2048;

   // IPMS Logger is used for logging

   /**
    * The HTTP server to connect
    */
   private URL url = null;

   /**
    * The HTTP connection to the server
    */
   private URLConnection con = null;
   private InputStream is = null;

   /**
    * Creates an HTTP connection to the server.
    * @roseuid 3A9FC46E00C9
    */
   public URLConnection connectServer(com.mobeon.ntf.out.wap.papapi.WapPerson person) throws IOException
   {
    logger.logMessage("URL = http://" + person.getHostName() + ":" +
		      person.getPortNumber() + person.getUrlSuffix(), Logger.L_DEBUG);

    url = new URL("http",
                  person.getHostName(),
                  person.getPortNumber(),
                  person.getUrlSuffix());
    try
	{
	    con = url.openConnection();
	}catch(IOException ioe){ logger.logMessage("Fail to connect to WAPGateWay: ", Logger.L_ERROR);}
    con.setUseCaches(false);
    con.setDoOutput(true);
    con.setDoInput(true);
    //Comment out by ermahen 6/7-2001. Request should be text/xml, but it
    //doesent matter sinze wgp  makes a text/xml response
    // con.setRequestProperty("Content-Type", "text/html");

    return con;
   }

   /**
    * Setup an HTTP connection to the PPG server and send the push message.
    * Set up an OutputStream to the PPG server and receives the notification
    * response on the InputStream.
    * @roseuid 3A9FC46E00D0
    */
   public InputStream postMessage(java.lang.String message)
   {
    try
    {
	java.io.OutputStream boStream = con.getOutputStream();
	PrintWriter out = new PrintWriter(boStream);
	out.print("Content-Type: multipart/related; boundary=msgpart; =\"application/xml\"\r\n"+message);
	out.flush();
	out.close();
	//ByteArrayOutputStream boStream = new ByteArrayOutputStream(INITIAL_OUTPUT_SIZE);
	//out.print(message);
	//con.setRequestProperty("Content-length", String.valueOf(boStream.size()));
	//boStream.writeTo(con.getOutputStream());

	is = con.getInputStream();

    }
    catch(MalformedURLException muEx)
    {
      muEx.printStackTrace();
      logger.logMessage("Error MalformedURLException: " + muEx, Logger.L_ERROR);
      return null;
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      logger.logMessage("Error IOException: " + ioe, Logger.L_ERROR);
      return null;
    }
    return is;
   }

   /**
    * @roseuid 3A9FC46E00D2
    */
   public HttpClient()
   {
   }

   /**
    * @roseuid 3A9FC46E00D3
    */
   public void setConnectionProperty(String property, String value)
   {
    con.setRequestProperty(property, value);
    return;
   }

   /**
    * @roseuid 3A9FC46E00DA
    */
   public String getBasicCredentials(String user, String pswd)
   {
    String up = user + ":" + pswd;
    return "Basic " +Base64.getEncoder().encodeToString(up.getBytes());
   }

   /**
    * @roseuid 3A9FC46E00DD
    */
   public static void main(String args[])
   {/*
    try
    {
    HttpClient myClient = new HttpClient();
    Person person = WapPersonFactory.getWapPerson("5166778010", 1);
    ((WapPerson)person).setUrlSuffix("/servlet/echo.EchoServer");
    myClient.connectServer((WapPerson)person);
    BufferedReader in = new BufferedReader(
      new InputStreamReader( myClient.postMessage("Hello server. Nice talking to you!")));

    String result = "";
    String line;
    String linefeed = System.getProperty("line.separator", "\r\n");
    //String linefeed = "\r\n";
    while (( line = in.readLine()) != null)
    {
      result += line + linefeed;
    }

    System.out.println("result = " + result);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      System.exit(-1);
    }
    return;*/
   }
}//end of class
