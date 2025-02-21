package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * Approved: Per Berggren</p>
 * No: 3.IWD.MAS0001</p>
 * Author: Marcus Haglund</p>
 * Title: IWD PlatformAccess </p>
 * Version: A</p>
 * </p>
 * The class PlatformAccessUtil is a container for utility methods.
 */
public interface PlatformAccessUtil {

    /**
     * Retrieves the current time depending on the timezone parameter. The format is "yyyy-MM-dd hh:mm:ss"
     * (the vvaTimeFormat).
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> The configured current time</dd></dl>
     *
     * @param timezone Must follow the syntax for timezones in the M3 system.
     * @return String formatted time string.
     */
    String getCurrentTime(String timezone);

    /**
     * Converts a vvaTime String to another time-string depending on timezone parameters. These parameters must follow
     * the syntax for timezones in the M3 system.
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> Same as production enviroment</dd></dl>
     * @param vvaTime      Time to convert
     * @param fromTimezone Timezone for the time to convert from. If null, the default timezone is used.
     * @param toTimezone   Timezone for the time to convert to. If null, the default timezone is used.
     * @return Converted time-string
     */
    String convertTime(String vvaTime, String fromTimezone, String toTimezone);

    /**
     * Formats a vvaTime String. This function supports the format described in <code>
     * <a href ="http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html">
     * http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html</a>
     * </code>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> Same as production enviroment</dd></dl>
     * @param vvaTime String to format
     * @param pattern Pattern describing the date and time format.
     *                If null number of milliseconds since 1970 will be returned.
     * @return String formatted time string
     */
    String formatTime(String vvaTime, String pattern);
    
    /**converts date string to vva date string
     * 
     * @param dateStr
     * @param timeZone
     * @return
     */
    public String stringDateToVvaTime(String dateStr, String timeZone);
    /**
     * Factory method for creating IMediaQualifier objects depending on type.
     * The types and value-formats are defined here:<br/>
     * <p/>
     * <b>Number</b>        A positive integer e.g. 101 and possibly a gender (female,male and neutral)<br/>
     * <b>CompleteDate</b>  A complete date in the form of YYYY-MM-DD HH:MM:SS +- UTC e.g. 2005-08-12 23:13:23 +0200.<br/>
     * Conversion is not supported for this type.<br/>
     * <b>DateDM</b>        A date in the form of YYYY-MM-DD e.g. 2005-08-12<br/>
     * <b>Weekday</b>       A date in the form of YYYY-MM-DD e.g. 2005-08-12<br/>
     * <b>Time12</b>        A time in the form HH:MM:SS e.g. 23:13:23<br/>
     * <b>Time24</b>        A time in the form HH:MM:SS e.g. 23:13:23<br/>
     * <b>String</b>        String in UTF-8 e.g. John Doe<br/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> Same as production enviroment </dd></dl>
     * @param type The type, for example "Number". The types are defined in FD_MediaContentManager.
     * @param value Optional, may be null. Gives the value of the qualifier.
     * @return a new IMediaQualifier object
     */
    IMediaQualifier getMediaQualifier(String type, String value);

    /**
     * Factory method for creating IMediaQualifier from an IMediaObject.
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b>Same as production enviroment</dd></dl>
     * @param iMediaObject Optional, may be null. The mediaObject the qualifier is fetched from.
     *                      Represented as a String.
     * @return a new IMediaQualifier object
     */
    IMediaQualifier getMediaQualifier(IMediaObject iMediaObject);

    /**
     * Utility function to retrieve a MediaObject that contains a text string. Can be used before the call to
     * getMediaQualifier(IMediaObject) above.
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> Same as production enviroment</dd></dl>
     * @param value The value of the MediaObject to be created
     * @return MediaObject that contains the string
     */
    IMediaObject getMediaObject(String value);

    /**
     * Utility function to convert a MediaObject array to a string. The array elements are appended in the order
     * they appear.
     * An error.com.mobeon.platform.system will be sent if one of the IMediaObject's is not a text-mediaobject
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> Same as production enviroment</dd></dl>
     * @param iMediaObject The mediaObject array to convert
     * @return string value of the MediaObject
     */
    String convertMediaObjectsToString(IMediaObject[] iMediaObject);

    /**
     * Sets a SSML (Speech Synthesis Markup Language) property to a MediaObject. The text part in the MediaObject is
     * encapsulated into a SSML document with corresponding property.
     * If the text is already an SSML document, it will be changed.
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> return <code>mediaObject</code></dd></dl>
     *
     * @param mediaObject    MediaObject to set property on.
     * @param propertyNames  List of names, valid values are: "language", "voice", "speed" and "volume"
     * @param propertyValues List of values, the propertyName with the same index corresponds to this value.
     */
    IMediaObject setMediaObjectProperty(IMediaObject mediaObject, String[] propertyNames, String[] propertyValues);

    /**
     * Retrieves a SSML (Speech Synthesis Markup Language) property from a MediaObject. If the textpart is plain text or
     * the property is missing, an empty string is returned.
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> empty string</dd></dl>
     *
     * @param mediaObject  MediaObject to get property from
     * @param propertyName Name on the property
     * @return value of the property
     */
    String getMediaObjectProperty(IMediaObject mediaObject, String propertyName);

    /**
     * Retrieves a list of supported TTS languages. The format is same as the MUR attribute preferredLanguage
     * Ex: en, sv
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> a list of languages that the TTS engine supports.</dd></dl>
     * @return list of languages
     */
    String[] getSupportedTTSLanguages();

    /**
     * Removes all tags from a string. Suitable to use to for example
     * remove HTML markup from a string and get only the "raw text".
     * <p/>
     * <dl><b>Simulation details:</b> Not implemented
     * @param s The string to detag
     * @return a detagged string
     */
    String deTag(String s);
    
    
    /**
     * Append one media object to another and return the result as a 
     * new media object. The following prerequisites must hold true 
     * for a successful append:
     *   - Both media objects must contain media with the same content type.
     *   - Currently only content type "audio/wav" is supported.
     *   - Currently only the G.711 ulaw and alaw codec is supported.
     *
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>an error occured when appending.
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b>mediaObject</dd></dl>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *    
     * @param mo1 First media object
     * @param mo2 Second media object
     * @return a new media object containing the concatenation of mo1 and mo2 
     */
    public IMediaObject appendMediaObjects(IMediaObject mo1, IMediaObject mo2);
    	 
    /**
     * Append one media object to another and return the result as a new AMR media object.
     * Prerequisites are: 
     *   - Both media objects must contain media with the same content type.
     * @deprecated - please use appendMediaObjects instead. TR HY12623 
     * @param mo1 First media object
     * @param mo2 Second media object
     * @return a new media object containing the concatenation of mo1 and mo2 
     */
    public IMediaObject appendMediaObjectsAmr(IMediaObject mo1, IMediaObject mo2);

    /**
     * This converts a 3gpp object to amr or amr-wb #!AMR or #!AMR-WB and
     * back again to 3gpp samr(amr-nb) or sawb(amr-wb) codec
     * The effect is to clean it up, by passing it through ffmpeg
     * 
     * Contrary to name it does not covert from amr ro 3gpp..
     * but kept name for backward compatibility.
     * 
     * It was created to resolve/cleanup bad recording at a customer site
     * during the MIO 2 days..
     * Use with caution as it uses extra IO and CPU
     * @deprecated
     * @param mo1 3gpp media object
     * @return a new media object containing the mo1 within a 3GPP container 
     */
    public IMediaObject amrTo3gp(IMediaObject mo1);      
}
