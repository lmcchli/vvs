package com.abcxyz.services.moip.ntf.logging;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;

import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.ntf.out.mms.MMSOut;
import com.mobeon.ntf.util.DelayLoggerProxy;

import static org.junit.Assert.*;

/**
 * 
 * This is a junit class to test if the Logger(com.mobeon.ntf.util), DelayedLoggerProxy, MMout are writing 
 * in three separate files using the LogAgentFactory from the backend. 
 *
 */
public class NtfLogAgentTest {

	   private static String curDir;
	   private static String logFileName;
	   
	   static String NotificationTrace = "NotificationTrace.log";
	   static String error = "error.log";
	   static String MM7Out = "MM7Out.log";
	   
	   
	   static File errorOutputFile = new File(resolveName(NotificationTrace)); 
	   static File mm7OutputFile = new File(resolveName(error));
	   static File ntfOutputFile = new File(resolveName(MM7Out));
	   

	   @BeforeClass
	   public static void setUp() throws Exception {
		   
		   //resolveNameFiles(NotificationTrace, error, MM7Out);
		   
		   curDir = System.getProperty("user.dir");
		   if (!curDir.endsWith("logging")) {
			   
			   curDir +=  File.separator + "test" + File.separator + "junit" + File.separator + "com" + 
			   File.separator + "abcxyz" + File.separator + "services" + File.separator + "moip" + File.separator + 
			   File.separator + "ntf" + File.separator + "logging";
		   }
		   if (isWindows())
			   logFileName = curDir + File.separator + "log4j.xml";
		   else
			   logFileName = File.separator + "tmp" + File.separator + "log4ju.xml";
		   System.setProperty("log4j.configuration",logFileName );
		   
		   ntfOutputFile.delete();		   
		   errorOutputFile.delete();		   
		   mm7OutputFile.delete();
	   }

	   @AfterClass
		public static void tearDown() throws Exception {
		}
	   
	   @Test
	   public void testNotificationTraceLog() throws Exception {		    

		   ntfOutputFile = new File(resolveName(NotificationTrace));

		   String message = "All the logging that is using com.mobeon.ntf.util.Logger";
		   com.mobeon.ntf.util.Logger ntfLogger = com.mobeon.ntf.util.Logger.getLogger();
		   ntfLogger.logMessage(message, ntfLogger.L_DEBUG);    	

		   String content = getContents(ntfOutputFile);	    	

		   assertTrue(content.indexOf(message)!= -1);	        	        
	   }
	   
	   private static String resolveName(String name){
		   
		   if (isWindows()) {
			   return "C:\\" + name;
		   } else {
			   return "/tmp/" + name;
		   }		   
		   
	   }
		@Test
		public void testNotificationTraceLogWithLevelInfo() throws Exception {
			
			ntfOutputFile = new File(resolveName(NotificationTrace));
			
	        String message = "This Info message should end up in NotificationTrace.log";
	        com.mobeon.ntf.util.Logger ntfLogger = com.mobeon.ntf.util.Logger.getLogger();
	    	ntfLogger.logMessage(message, ntfLogger.L_VERBOSE);    	
	    	
	    	
	    	String content = getContents(ntfOutputFile);
	    	
	    	
	        assertTrue(content.indexOf(message)!= -1);
	        
	        
	    }
		
		@Test
		public void testNotificationTraceLogWithLevelError() throws Exception {

			ntfOutputFile = new File(resolveName(NotificationTrace));
			//outputFile.delete();

	        String message = "This Error message should end up in NotificationTrace.log";
	        com.mobeon.ntf.util.Logger ntfLogger = com.mobeon.ntf.util.Logger.getLogger();
	    	ntfLogger.logMessage(message, ntfLogger.L_ERROR);    	
	    	
	    	String content = getContents(ntfOutputFile);
	    	//System.out.println(content);
	        
	        assertTrue(content.indexOf(message)!= -1);
	        
	    }
		
		@Test
		public void testNotificationTraceLogWithLevelFatal() throws Exception {

			ntfOutputFile = new File(resolveName(NotificationTrace));
			//outputFile.delete();

	        String message = "This Fatal message should end up in NotificationTrace.log";
	        com.mobeon.ntf.util.Logger ntfLogger = com.mobeon.ntf.util.Logger.getLogger();
	    	ntfLogger.logMessage(message, ntfLogger.L_ERROR);    	
	    	
	    	String content = getContents(ntfOutputFile);
	    	//System.out.println(content);
	        
	        assertTrue(content.indexOf(message)!= -1);
	        
	    }
		
		@Test
		public void testErrorLogLog() throws Exception {

			errorOutputFile = new File(resolveName(error));
			//outputFile.delete();
   
	    	DelayLoggerProxy sdLogProxy = new DelayLoggerProxy();
	    	String message =  "THis is a FATAL error and should be stored in the error.log file as well";
	    	sdLogProxy.doLog(sdLogProxy.FATAL,message);    	
	    	
	    	String content = getContents(errorOutputFile);
	    	//System.out.println(content);
	        
	        assertTrue(content.indexOf(message)!= -1);
	        
	    }
     
		@Test
     	public void testErrorLogLogWhenLevelNotFatal() throws Exception {

			errorOutputFile = new File(resolveName(error));
			ntfOutputFile = new File(resolveName(NotificationTrace));

	    	DelayLoggerProxy sdLogProxy = new DelayLoggerProxy();
	    	String message =  "THis is a INFO LEVEL message and should not be stored in the error.log file, only in NotificationTrace.log";
	    	sdLogProxy.doLog(sdLogProxy.INFO,message);    	
	    	
	    	String content = getContents(errorOutputFile);
	    	//System.out.println(content);	        
	        assertTrue(content.indexOf(message)== -1);
	        
	        content = getContents(ntfOutputFile);
	        //System.out.println(content);
	        assertTrue(content.indexOf(message)!= -1);
	    }

		
		/**
		  * Fetch the entire contents of a text file, and return it in a String.
		  * This style of implementation does not throw Exceptions to the caller.
		  *
		  * @param aFile is a file which already exists and can be read.
		  */
		  public static String getContents(File aFile) {
		    
		    StringBuilder contents = new StringBuilder();
		    
		    try {
		      //use buffering, reading one line at a time
		      //FileReader always assumes default encoding is OK!
		      FileReader file = new FileReader(aFile);	
		      BufferedReader input =  new BufferedReader(file);
		      try {
		        String line = null; //not declared within while loop
		        
		        while (( line = input.readLine()) != null){
		          contents.append(line);
		          contents.append(System.getProperty("line.separator"));
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return contents.toString();
		  }
		  
		  public static String getOsName()
		   {
			  String OS = null;
		      return System.getProperty("os.name");
		      
		   }


		  public static boolean isWindows()
		   {
		      return getOsName().startsWith("Windows");
		   }


}
