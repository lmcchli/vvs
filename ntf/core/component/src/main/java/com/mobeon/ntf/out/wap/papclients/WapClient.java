/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papclients;

import com.mobeon.ntf.out.wap.papapi.*;
import com.mobeon.ntf.out.wap.papapplication.PapMsg;
import com.mobeon.ntf.util.Logger;
import java.io.*;
import java.net.URLDecoder;

/**
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public class WapClient extends HttpClient
{
    private final static Logger logger = Logger.getLogger(WapClient.class);
   /**
    * The boundary string used to separate individual messages in a multipar message
    */
   private static final String MESSAGE_BOUNDARY = "asdlfkjiurwghasfwwwwxxxpuu";

   /**
    * Set up a connection to the PPG server and send the push message to it.
    * @roseuid 3A9FC4780162
    */
   public InputStream pushMessage(PapMsg message, com.mobeon.ntf.out.wap.papapi.WapPerson person) throws IOException
   {
       try
	   {
	       connectServer(person);
	   }catch(IOException e){ logger.logMessage("Error when connect to WGP!" , logger.L_ERROR);}

       setConnectionProperty("Authorization",getBasicCredentials(person.getUserName(),person.getPassWd()));
       setConnectionProperty("Accept", "*/*");
       setConnectionProperty("Connection", "Keep-Alive");
       setConnectionProperty("Content-Type", "text/html");
       setConnectionProperty("Content-Type", message.getMimeContentType());
       return postMessage( message.getMsgText() );
   }

   /**
    * @roseuid 3A9FC4780165
    */
   public WapClient()
   {
   }

   /**
    * main is only used as a test engine for WapClient basic test
    * @roseuid 3A9FC4780166
    */
   public static void main(String args[])
   {/*
    try
    {
      WapClient myClient = new WapClient();
      Person person = WapPersonFactory.getWapPerson("1722300001", 1);

      PapMsg msg = new PapMsg();
      msg.createPushMsg((WapPerson)person);

      InputStream is = myClient.pushMessage(msg, (WapPerson)person);

      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      String result = "";
      String line;
      String linefeed = System.getProperty("line.separator", "\r\n");
      //String linefeed = "\r\n";
      while (( line = in.readLine()) != null)
        result += line + linefeed;
      System.out.println("result = " + result);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      System.exit(-1);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
    return;
   */}
}




