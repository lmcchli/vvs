/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.text;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.util.Logger;
import java.io.*;
import java.util.*;


/***************************************************************
 *Phrases maintains template strings for different languages
 ****************************************************************/
public class Phrases {
    private final static Logger log = Logger.getLogger(Phrases.class); 
    /**Contains phrase lists keyed by language (String). The phrases for one
     language are kept in one Properties in the Hashtable.*/
    private static  volatile Hashtable<String, Properties> phrases;
    /**Contains phrases lists keyed by language (String). Phrases are from the 
    new format with file extension .cphr*/
    private static volatile Hashtable<String, String> cPhrases;
    
    /** Hashtables of indices of all tags read in files. */
    private static volatile Hashtable<String, String> cTypes;
  
    /** Phrases represented by byte array, keyed by Strings. */
    private static  volatile Hashtable<String, byte[]> bPhrases;
    
    


  /*****************************************************************
   * Constructor
   *****************************************************************/
  public Phrases() {
    refresh();
  }

    
  /****************************************************************
   *Refresh rereads parameters and phrase files.
   */
  public static void refresh() {
    //The updated phrases are put in a tmp variable, so other threads can go
    //on using the existing prases until the refresh is done.
    File[] cphrFiles; /* Containing phrases for different language with the 
                         new format */
    Hashtable<String, Properties> tmpPhr= new Hashtable<String, Properties>();
    cPhrases = new Hashtable<String, String>();
    cTypes = new Hashtable<String, String>();
    bPhrases = new Hashtable<String, byte[]>();
    File phrDir = new File(Config.getPhraseDirectory());
    String name; //name of current file
    String lang; //current language
    LinkedList<String> byteLangs = new LinkedList<String>(); // A list of languages to look for when reading charsets
    LinkedList<String> byteFiles = new LinkedList<String>(); // A list of all charset files stored
    
    log.logMessage("Refreshing phrase files", Logger.L_VERBOSE);
    if (!phrDir.isDirectory()) {
      log.logMessage("Failed to find phrase directory " + phrDir.getPath(), Logger.L_ERROR);
    } else {
      log.logMessage("Reading phrase files in " + phrDir.getPath(), Logger.L_VERBOSE);
      //find all .cphr files in the phrase directory
      cphrFiles = phrDir.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(".cphr") && name.indexOf("-x-") == -1;
          }
        });
      
      //Read phrases (.cphr) from each of the language files
      for (int i= 0; i < cphrFiles.length; i++) {
        name = cphrFiles[i].getName();
        lang = getLanguage(name);
        log.logMessage("Reading \"" + lang + "\" counter specific phrase", Logger.L_VERBOSE);
        String charsetname = getCharset(name);
        if (charsetname != null && charsetname != "")
        {
            parseBytesFromFile(cphrFiles[i], lang, null, charsetname);
            byteLangs.add(lang);
            byteFiles.add(name);
            continue;
        }
        String str = readCphrFile(cphrFiles[i], lang, null);
        if (str != null && str.length() > 0) {
          cPhrases.put(lang, str);
          //log.logMessage("XXXX Putting (nocos)" + lang + "," + str, Logger.L_VERBOSE);
          // Now set the property-values from the .cphr (not cos) file.
          Properties p = getDefaultProperties(str);                  
          p.setProperty("urgent", p.getProperty("urgent", "\"urgent\""));
          p.setProperty("normal", p.getProperty("normal", "\"normal\""));
          p.setProperty("fax", p.getProperty("fax", "\"fax\""));
          p.setProperty("voice", p.getProperty("voice", "\"voice\""));
          p.setProperty("email", p.getProperty("email", "\"email\""));
          p.setProperty("video", p.getProperty("video", "\"video\""));
          p.setProperty("mwiontext", p.getProperty("mwiontext", ""));
          p.setProperty("mwiofftext", p.getProperty("mwiofftext", ""));
          p.setProperty("smstype0text", p.getProperty("smstype0text", ""));
          tmpPhr.put(lang, p);
        }
      }                 

      //Make sure there is always an english alternative, so we do not
      //have to check that everywhere
      if (tmpPhr.get("en") == null) {
        Properties props= new Properties();
        
        log.logMessage("Setting default english phrases", Logger.L_VERBOSE);
	    
        props.setProperty("urgent", props.getProperty("urgent", "\"urgent\""));
        props.setProperty("normal", props.getProperty("normal", "\"normal\""));
        props.setProperty("fax", props.getProperty("fax", "\"fax\""));
        props.setProperty("voice", props.getProperty("voice", "\"voice\""));
        props.setProperty("email", props.getProperty("email", "\"email\""));
        props.setProperty("video", props.getProperty("video", "\"video\""));
        props.setProperty("mwiontext", props.getProperty("mwiontext", ""));
        props.setProperty("mwiofftext", props.getProperty("mwiofftext", ""));
        props.setProperty("smstype0text", props.getProperty("smstype0text", ""));
        
        tmpPhr.put("en", props);
      }

      // load cos specific phrase files.
      Enumeration<String> languages = tmpPhr.keys();
      while( languages.hasMoreElements() ) {
        final String language = languages.nextElement();
        
        // list files with language and cos
        File[] cosFiles = phrDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
              return name.endsWith(".cphr") && name.indexOf("-x-") != -1;
            }
          });
        if( cosFiles != null ) {
          for( int i=0;i<cosFiles.length;i++ ) {
            try {
              String fileName = cosFiles[i].getName();
              String fileLang = getLanguage(fileName);
              String cosName = getCosname(fileName);
              String charsetname = getCharset(fileName);
              if (charsetname != null && charsetname != "") continue;
              if (!fileLang.equals(language)) continue; // File language is not same as currently-checked language
              if (!fileLang.equals(language)) continue; // File language is not same as currently-checked language
              Properties languageProperties = tmpPhr.get(language);  
              log.logMessage("Reading \"" + cosFiles[i].getName() + "\" phrases", Logger.L_VERBOSE);
              Properties props = new Properties(languageProperties);
              String str = readCphrFile(cosFiles[i], language, cosName);
              if (str != null && str.length() > 0) {
                //log.logMessage("XXXX Putting (cos)" + language + ":" + cosName + "," + str, Logger.L_VERBOSE);
                cPhrases.put(language + "-x-" + cosName, str); // en-x-cos
              }
              Properties cosProps = getDefaultProperties(str, props);
              //props.load(config_stream);
              tmpPhr.put(language + "-x-" + cosName, cosProps);
            } catch (Exception e) {
              log.logMessage("Failed to read cos file " + cosFiles[i].getName(), Logger.L_ERROR );
            }
          }
        }
      }
      
      // Loop for each file with charset (for some we may have missed)
      File[] charsetFiles = phrDir.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(".cphr") && name.indexOf("-c-") != -1;
          }
        });
      if( charsetFiles != null ) {
          for (File f : charsetFiles)
          {
              String fileName = f.getName();
              String fileLang = getLanguage(fileName);
              String cosName = getCosname(fileName);
              String charsetname = getCharset(fileName);
              if (charsetname != null && charsetname != "" && !byteFiles.contains(fileName) && byteLangs.contains(fileLang))
              {
                  parseBytesFromFile(f, fileLang, cosName, charsetname);
                  byteFiles.add(fileName);
              }
          }
      }

      phrases = tmpPhr;
      //When this method returns, we can be certain that langProps contains
      //phrases for at least english and maybe some more languages. Some
      //languages may be missing due to various failures, but we can always
      //find some version of the template strings we need
    }
  }


  /**
   * Returns the charsetname part of the filename.
   */
  private static String getCharset(String fileName) {
      String[] fileParts = fileName.split("-");
      for (int i = 0; i < fileParts.length; i++)
      {
          try
          {
              if (fileParts[i].equalsIgnoreCase("c")) return fileParts[i+1].replace(".cphr", "");
          }
          catch (Exception e) // Catch all array index errors (misformed file name)
          {
              log.logMessage("Warning: Filename has a -c- tag but no charset: " + fileName);
              return null;
          }
      }
    return null;
}
  
  public static String getCosname(String filename)
  {
      String[] fileParts = filename.split("-");
      for (int i = 0; i < fileParts.length; i++)
      {
          try
          {
              if (fileParts[i].equalsIgnoreCase("x")) return fileParts[i+1].replace(".cphr", "");
          }
          catch (Exception e) // Catch all array index errors (misformed file name)
          {
              log.logMessage("Warning: Filename has a -x- tag but no cosname: " + filename);
              return null;
          }
      }
    return null;
  }
  
  public static String getLanguage(String filename)
  {
      String[] fileParts = filename.split("-");
      String potentialName = fileParts[0];
      int pointIndex = potentialName.indexOf('.');
      return (pointIndex == -1 ? potentialName : potentialName.substring(0, pointIndex));
  }


public static void clearPhrases() {
    cPhrases = null;
    cTypes = null;
    phrases = null;
    bPhrases = null;
  }

  public static void addPhraseString(String str, String lang, String cos) {
      if( cPhrases == null ) {
        cPhrases = new Hashtable<String,String>();
        bPhrases = new Hashtable<String, byte[]>();
        cTypes = new Hashtable<String,String>();
        phrases = new Hashtable<String,Properties>();
      }
      if( cos != null ) {
        cPhrases.put(lang + "-x-" + cos, str);
        phrases.put(lang + "-x-" + cos, getDefaultProperties(str));
        findCTypes(str, lang, lang + "-x-" + cos);
      }
      else {
        cPhrases.put(lang, str);
        phrases.put(lang, getDefaultProperties(str));
        findCTypes(str, lang, null);

      }


  }




  /*****************************************************************/
  
  public static Properties getTemplateStrings(String lang) {
    return getTemplateStrings(lang, null);
  }
     
  /**
   * Returns the templatestring for a given language and cos.
   *@param lang the language to check.
   *@param cosName - the name of the cos.
   *@return a properties with templatestrings.
   */
  public static  Properties getTemplateStrings(String lang, String cosName) {
    log.logMessage("Getting \"" + lang + "\" phrases for cos \"" + cosName + "\"", Logger.L_VERBOSE);
    if (phrases == null) refresh(); //First call
	
    Object phr = null;
    if( cosName != null ) {
      phr = phrases.get(lang + "-x-" + cosName ); // cos
    }        
    if (phr == null) { 
      phr = phrases.get(lang); // lang
    }
    if (phr == null) {
    	if(lang.indexOf("_")!= -1)
    	{
    		String prefLang = lang.split("_")[1];
    		phr = phrases.get(prefLang); // lang
    	}
    }
    if (phr == null) { //No such language
      log.logMessage("Could not find any phrases for language " + lang, Logger.L_VERBOSE);
      phr = phrases.get(Config.getDefaultLanguage());    
      if (phr == null) { //No default language
        log.logMessage("Could not find any phrases for (fallback) language " + 
                       Config.getDefaultLanguage(), Logger.L_ERROR);
        phr = phrases.get("en");
      }
    }    
    if (phr != null)
      log.logMessage("Found cached phrases", Logger.L_VERBOSE);
	
    return (Properties)phr;
  }
    
  /*****************************************************************/
  public static String getCphrTemplateStrings(String lang, String cosname) {
    log.logMessage("Getting \"" + lang + "\" phrases for cos " + cosname, 
                   Logger.L_VERBOSE);
    if (cPhrases == null) refresh(); //First call   
    if (cosname != null & cPhrases != null) {
      Object cosPhr = cPhrases.get(lang + "-x-" + cosname);
      if (cosPhr != null) { // Cos specific avail       
      log.logMessage("Found cached cos-specific .cphr phrases for language " + 
                     lang + " and cos " + cosname, Logger.L_VERBOSE);
      return (String)cosPhr;
      }
    }
    //log.logMessage("XXXX failed getting (cos) " + lang + ":" + cosname, Logger.L_VERBOSE);
    return getCphrTemplateStrings(lang);
  }


  public static String getCphrTemplateStrings(String lang) {
    log.logMessage("Getting \"" + lang + "\" phrases", Logger.L_VERBOSE);
    if (cPhrases == null) refresh(); //First call   
    Object phr= cPhrases.get(lang);

    if (phr == null) { //No such phrase with that language and suffix
      log.logMessage("Could not find any .cphr phrases for language " + lang, Logger.L_VERBOSE);
    } else {
      log.logMessage("Found cached .cphr phrases for language " + lang, Logger.L_VERBOSE);
      return (String)phr;
    }
    if(lang.indexOf("_")!= -1)
	{
    	String prefLang = lang.split("_")[1];
		phr = cPhrases.get(prefLang); // lang
	    if (phr == null) {
	    	log.logMessage("Could not find any .cphr phrases for language " + prefLang, Logger.L_VERBOSE);
	    }
	    else
	    	return (String)phr;
	}
    phr= cPhrases.get(Config.getDefaultLanguage());   
    if (phr == null) { //No such phrase with that language and suffix
      log.logMessage("Could not find any .cphr phrases for (fallback) language " + lang, Logger.L_VERBOSE);
    } else {
      log.logMessage("Found cached .cphr phrases for (fallback) language " + lang, Logger.L_VERBOSE);
      return (String)phr;
    }
    phr= cPhrases.get("en");   
    if (phr == null) { //No such phrase with that language and suffix
      log.logMessage("Could not find any .cphr phrases for (second fallback) language " + lang, Logger.L_VERBOSE);
    } else {
      log.logMessage("Found cached .cphr phrases for (second fallback) language " + lang, Logger.L_VERBOSE);
    }

    return (String)phr;
  }
    
  /*****************************************************************/
  public static boolean isCphrPhraseFound(String language, String content, String cosName) {
      if (cPhrases == null) refresh(); //First call   
      if (cosName == null) {
      return (cPhrases != null && (cTypes != null) &&
              (cPhrases.get(language)) != null && 
              (cTypes.get(language+":"+content)) != null) ? true : false;
    } else {
      return (cPhrases != null && (cTypes != null) &&
              (cPhrases.get(language + "-x-" + cosName)) != null && 
              (cTypes.get(language + "-x-" + cosName +":"+content)) != null) ? true : false;
    }
  }
    

  /*****************************************************************/
  public String toString() {
    String s= "[\n";
    String lang;
    int i;
	
    if (phrases == null || cPhrases == null) refresh(); //First call
	
    for (Enumeration<String> e1 = phrases.keys() ; e1.hasMoreElements() ;) {
      lang= e1.nextElement();
      s+= "\tlanguage " + lang;
      Properties p= phrases.get(lang);
      for (Enumeration e2 = p.keys() ; e2.hasMoreElements() ;) {
        String name= (String)(e2.nextElement());
        s+= "\n\t\t" + name + "=" + (String)(p.get(name));
      }
      s+= "\n]\n";
    }
    
    return s;
  }

  /**
   * See below
   */
  private static Properties getDefaultProperties(String cphr) {
    return getDefaultProperties(cphr, null);
  }

  /**
   * Used to get the properties like general, mwiontext etc.
   * A lot of properties will be unused. This method treats all
   * xxx={ as propery setters (though some are actually tag strings 
   * that need further processing).
   *
   * @param cphr the cphr-formatted string
   * @param prop properties to use as default
   * @return new properties object with all of the cphr as properties
   */
  private static Properties getDefaultProperties(String cphr, Properties prop) {
       
    Properties p = null;
    if (prop == null) {
      p = new Properties();
    } else {
      p = new Properties(prop);
    }
    String line;

    StringTokenizer cphrTokenizer = new StringTokenizer(cphr, "\n");
    while(cphrTokenizer.hasMoreTokens()) {      
      StringBuffer strbuf = new StringBuffer();
      line = cphrTokenizer.nextToken();
      if (line.matches(".*=[. ]*\\{.*")) {
        String tmp = line.substring(0, line.indexOf("=")).trim();
        while(cphrTokenizer.hasMoreTokens() && (line.indexOf("}") == -1)) {
          line = cphrTokenizer.nextToken();
          strbuf.append(line);
        }        
        String content = strbuf.toString();
        content = content.replaceAll("}", "").replaceAll("\"", "").trim(); 
        // Ok, the properties can not contain \" or }, I can live with that.
        // The actual strings are handled elsewhere, this is only for properties
        // that cannot contain tags. Like general, mwiontext etc.
        // No use setting count specific properties, they are handled separately
        if (!content.matches(".*\\(.*,.*,.*,.*\\).*")) {
          p.setProperty(tmp.trim().toLowerCase(), content);
        } 
      }
    }
    return p;
  }
    
  /**
   * Reads the new format CPHR file and returns content as a string
   *
   * @param file a CPHR file
   * @param language a language string (like en)
   * @return content of cphr file
   */
  private static String readCphrFile(File file, String language, String cosName) {
        
    String line;
    String dummy="";
    int counterTemplate=0;
    StringBuffer strbuf = new StringBuffer();
    try {
      RandomAccessFile rafile = new RandomAccessFile(file,"r");
      while  ((line = rafile.readLine()) != null) {

        if(line.matches(".*=[. ]*\\{.*")) {
          String [] tmp = line.split("=");
          if (tmp != null && tmp.length>=1 && tmp[0] != null && 
              tmp[0].trim() != "") {
            if (cosName == null) {
              cTypes.put(language+":" +tmp[0].trim().toLowerCase(), dummy);
            } else {
              cTypes.put(language + "-x-" + cosName+":" +tmp[0].trim().toLowerCase(), dummy); //en-x-cosname
            }
          }
        }
        if(!line.startsWith("#"))
          strbuf.append(line).append("\n");
      }
    }
    catch(NullPointerException npe) {
      log.logMessage("Failed to read phrase file", Logger.L_ERROR);
    }
    catch(IOException ioe) {
      log.logMessage("Failed to read phrase file " + file.getPath() + ioe, Logger.L_ERROR);
    }
    return strbuf.toString();
  }

  private static void findCTypes(String data, String lang, String cosName) {
      BufferedReader bufReader = new BufferedReader(new StringReader(data));
      String line;
      String dummy="";

      try {
      while  ((line = bufReader.readLine()) != null) {
        if(line.matches(".*=[. ]*\\{.*")) {
          String [] tmp = line.split("=");
          if (tmp != null && tmp.length>=1 && tmp[0] != null &&
              tmp[0].trim() != "") {
            if (cosName == null) {
              cTypes.put(lang+":" +tmp[0].trim().toLowerCase(), dummy);
            } else {
              cTypes.put(lang + "-x-" + cosName+":" +tmp[0].trim().toLowerCase(), dummy); //en-x-cosname
            }
          }
        }

        }
      } catch(Exception e) {
        log.logMessage("Got exception " + e.toString() );
      }
  }
  
  /**
   * Returns the table of all byte arrays which were associated with the given language,
   * cosname and charsetname.
   */
  public static Hashtable<String, byte[]> getBPhrases(String language, String cosName, String charsetname)
  {
      if (bPhrases == null) refresh(); //First call   
      Hashtable<String, byte[]> table = new Hashtable<String, byte[]>();
      String keyPrefix = language + (cosName == null ? "" : "-x-" + cosName) + (charsetname == null ? "" : "-c-" + charsetname) + ":";
      for (String s : bPhrases.keySet())
      {
          if (s.startsWith(keyPrefix)) table.put(s, bPhrases.get(s));
      }
      // If none were found, try getting them for generic cosNames
      // TODO
      return table;
  }
  
  /**
   * Returns the prefix for the keys in the byte table.
   */
  public static String getBytePrefix(String language, String cosName, String charsetname)
  {
      return language + (cosName == null ? "" : "-x-" + cosName) + (charsetname == null ? "" : "-c-" + charsetname) + ":";
  }
  
/**
 * Reads a cphr file in bytes and stores it in the byte hashmap.
 */
  private static void parseBytesFromFile(File f, String language, String cosName, String charsetname)
  {
      // The prefix for each keys. The prefic will be the file name without ".cphr"
      String keyPrefix = getBytePrefix(language, cosName, charsetname);
      byte NL = (byte)'\n'; // Newline
      byte CR = (byte)'\r'; // Carriage return
      byte EQ = (byte)'='; // Equal
      byte SP = (byte)' '; // Space
      byte HASH = (byte)'#'; // Hash
      byte CBoR = (byte)'{'; // Curly-bracket open right
      byte CBoL = (byte)'}'; // Curly-bracket open right
      try {
          // Read file
          FileInputStream fis = new FileInputStream(f);
          byte[] bytes = new byte[(int)f.length()]; // Complete file array
          byte[] lineStart = new byte[(int)f.length()]; // Potentially one-line file
          int lineStartLen = 0;
          boolean activateMatchSearch = false;
          boolean equalFound = true;
          String keySuffix = "";
          int CBoRIndex = 0;
          int CBoLIndex = 0;
          int i = 0; // index in complete file array
          int data = fis.read();
          while (data != -1) // while !eof
          {
              byte newByte = (byte)data;
              // First and foremost: ignore lines starting with #
              if (lineStartLen == 0 && newByte == HASH)
              {
                  while (newByte != NL && newByte != CR)
                  {
                      bytes[i] = newByte;
                      lineStart[lineStartLen] = newByte;
                      data = fis.read();
                      i++;
                      newByte = (byte)data;
                  }
              }
              bytes[i] = newByte;
              lineStart[lineStartLen] = newByte;
              // Not encountered equal yet
              if (!equalFound && newByte == EQ)
              {
                  equalFound = true;
                  // Get tag suffix here
                  byte[] suffix = new byte[lineStartLen - (bytes[lineStartLen - 1] == SP ? 1 : 0)];
                  for (int index = 0; index < suffix.length; index++)
                  {
                      suffix[index] = bytes[i - lineStartLen + index];
                  }
                  keySuffix = new String(suffix);
                  if (keySuffix.charAt(keySuffix.length() - 1) == ' ') keySuffix = keySuffix.substring(0, keySuffix.length() - 1);
                  keySuffix.trim();
              }
              // Encountered equal, not {
              else if (equalFound && !activateMatchSearch && newByte == SP);
              else if (equalFound && !activateMatchSearch && newByte == CBoR)
              {
                  activateMatchSearch = true;
                  CBoRIndex = i;
              }
              else if (equalFound && !activateMatchSearch) equalFound = false;
              // Encountered = and {
              else if (equalFound && activateMatchSearch && newByte == CBoL)
              {
                  CBoLIndex = i;
                  byte[] line = new byte[CBoLIndex - CBoRIndex];
                  for (int index = CBoRIndex + 1; index < CBoLIndex + 1; index++)
                  {
                      line[index - (CBoRIndex + 1)] = bytes[index];
                  }
                  line = fixLine(line);
                  // PROCESS
                  bPhrases.put(keyPrefix + keySuffix, line);
                  // PROCESS END
                  equalFound = false;
                  activateMatchSearch = false;
              }
              if (newByte == NL || newByte == CR) lineStartLen = 0;
              else lineStartLen++;
              data = fis.read();
              i++;
          }
      } catch (FileNotFoundException e) {
          System.out.println("ERROR FNFE");
      } catch (IOException e) {
          System.out.println("ERROR IOE");
      }
  }
  
  /**
   * Fixes the line of bytes by first removing lines starting with a hash
   * (#) character, then by removing newline characters, carriage returns
   * and open/closing brackets. The exact characcters removed this way are
   * the following:
   * <ul>
   * <li>\n</li>
   * <li>\r</li>
   * <li>\t</li>
   * <li>{</li>
   * <li>}</li>
   * </ul>
   * Finally, once the given array represents only a single line, we remove
   * leading whitespace.
   * @return the fixed version of the given array.
   */
  private static byte[] fixLine(byte[] array)
  {
      byte NL = (byte)'\n'; // Newline
      byte CR = (byte)'\r'; // Carriage return
      byte TAB = (byte)'\t'; // Carriage return
      byte HASH = (byte)'#'; // Hash
      byte CBoR = (byte)'{'; // Curly-bracket open right
      byte CBoL = (byte)'}'; // Curly-bracket open right
      byte SP = (byte)' '; // Space
      byte[] copy = new byte[array.length];
      for (int i = 0; i < array.length; i++) copy[i] = array[i];
      // Remove hash lines
      for (int i = 0; i < copy.length;)
      {
          boolean newline = true;
          if (newline && copy[i] == HASH)
          {
              while (!(copy[i] == NL || copy[i] == CR))
              {
                  copy[i] = CBoL; // Will be removed in next step
                  i++;
              }
          }
          else if (copy[i] == NL || copy[i] == CR)
          {
              while (copy[i] == NL || copy[i] == CR)
              {
                  i++;
              }
              // Skip to end and set newline = true
              newline = true;
          }
          else
          {
              i++;
          }
      }
      // Remove CBoR, CBoL, CR, NL, TAB
      int toRemove = 0;
      for (byte b : copy) if (b == NL || b == CR || b == CBoR || b == CBoL || b == TAB) toRemove++;
      byte[] newArray = new byte[copy.length - toRemove];
      for (int i = 0, j = 0; i < copy.length; i++)
      {
          if (!(copy[i] == NL || copy[i] == CR || copy[i] == CBoR || copy[i] == CBoL || copy[i] == TAB))
          {
              newArray[j] = copy[i];
              j++;
          }
      }
      // Remove leading whitespace
      if (newArray[0] == SP)
      {
          byte[] temp;
          int i = 0;
          while (newArray[i] == SP)
          {
              i++;
          }
          temp = new byte[newArray.length - i];
          for (int index = 0; index < newArray.length; index++)
          {
              if (index >= i)
              {
                  temp[index - i] = newArray[index];
              }
          }
          newArray = temp;
      }
      return newArray;
  }
}