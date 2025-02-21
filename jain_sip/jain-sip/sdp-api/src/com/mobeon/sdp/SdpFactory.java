package com.mobeon.sdp;

import java.util.*;
import java.net.*;

/**
 * The SdpFactory is a singleton class which applications can use a single
 * access point to obtain proprietary implementations of this specification.
 * As the SdpFactory is a singleton class there will only ever be one instance of
 * the SdpFactory. The single instance of the SdpFactory can be obtained using
 * the {@link SdpFactory#getInstance()} method. If an instance of the SdpFactory
 * already exists it will be returned to the application, otherwise a new
 * instance will be created.
 * <p>
 * <p>
 * <b>Naming Convention</b><br>
 * Note that the SdpFactory utilises a naming convention defined by this
 * specification to identify the location of proprietary objects that
 * implement this specification. The naming convention is defined
 * as follows:
 * <ul>
 * <li>The <b>upper-level package structure</b> referred to by the SdpFactory
 * with the attribute <var>pathname</var> can be used to differentiate between
 * proprietary implementations from different SDP stack vendors. The
 * <var>pathname</var> used by each SDP vendor <B>must be</B> the domain name
 * assigned to that vendor in reverse order. For example, the pathname used by
 * NIST SIP would be <code>gov.nist</code>.</li>
 * <li>The <b>lower-level package structure and classname</b> of a peer object
 * is also mandated by this specification. The lower-level
 * package must be identical to the package structure defined by this
 * specification and the classname is mandated to the interface name
 * appended with the <code>Impl</code> post-fix. Currently this only applies
 * for the implementation of this abstract class, i.e. the lower-level
 * package structure and classname of a proprietary implementation of the
 * <code>com.mobeon.sdp.SdpFactory</code> abstract class <B>must</B> be
 * <code>sdp.SdpFactoryImpl</code>.</li>
 * </ul>
 *
 * Using this naming convention the SdpFactory can locate a vendor's
 * implementation of this specification without requiring an application to
 * supply a user defined string to each create method in the SdpFactory. Instead
 * an application merely needs to identify the vendors SDP implementation it
 * would like to use by setting the pathname of that vendors implementation.
 * <p>
 * It follows that a proprietary implementation of a peer object of this
 * specification can be located at: <p>
 * <code>'pathname'.'lower-level package structure and classname'.</code><p>
 * For example an application can use the SdpFactory to instantiate a NIST SDP
 * peer SessionDescription object by setting the pathname to
 * <code>gov.nist</code> and calling the {@link #getInstance()} method.
 * The SdpFactory would return a new instance of the SdpFactory object
 * at the following location: <code>gov.nist.sdp.SdpFactoryImpl.java</code>
 * Because the space of domain names is managed, this scheme ensures that
 * collisions between two different vendor's implementations will not happen.
 * For example: a different vendor with a domain name 'foo.com' would have
 * their peer SdpFactory object located at
 * <code>com.foo.sdp.SdpFactoryImpl.java</code>.
 * <p>
 * <b>Default Namespace:</b><br>
 * This specification defines a default namespace for the SdpFactory, this
 * namespace is the location of the NIST SDP, i.e. <code>gov.nist</code>.
 * An application must set the <var>pathname</var> of the SdpFactory on
 * retrieval of a new instance of the factory in order to use a different
 * vendors SDP stack from that of the NIST SDP.
 * An application can not mix different vendor's peer implementation objects.
 * <p>
 * The SdpFactory enables applications to encode and decode SDP messages.
 * The SdpFactory can be used to construct a SessionDescription 
 * object programmatically.  
 * The SdpFactory can also be used to construct a 
 * SessionDescription based on the
 * contents of a String.
 * <p>
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 *@author Malin Flodin
 */
public abstract class SdpFactory {

    // static representation of this factory
    private static SdpFactory myFactory = null;

    // default domain to locate NIST SDP Implementation
    private static String pathName = "gov.nist";

    /**
     * Returns an instance of an SdpFactory. This is a singleton class so this
     * method is the global access point for the SdpFactory.
     * <p>
     * Once an application has obtained a reference to an SdpFactory it can
     * use the factory to configure and obtain parser instances and to create
     * SDP objects.
     * @return a factory instance
     * @throws SdpException if an SdpFactory could not be created.
     */
    public static SdpFactory getInstance() throws SdpException {
        if(myFactory == null) {
            // Create a new instance of SdpFactoryImpl Class.
            SdpFactory sdpFactory;
            try {
                Class factoryClass =
                        Class.forName(getPathName() + ".sdp.SdpFactoryImpl");
                sdpFactory = (SdpFactory)factoryClass.newInstance();

            } catch(Exception e){
                String errmsg = "The SdpFactory could not be created : " +
                        getPathName() + ".sdp.SdpFactoryImpl could not " +
                        "be instantiated. Ensure the Path Name has been set.";
                throw new SdpException(errmsg, e);
            }

            myFactory = sdpFactory;
        }

        return myFactory;
    }

    /**
      * Sets the <var>pathname</var> that identifies the location of a particular
      * vendor's implementation of this specification. The <var>pathname</var>
      * must be the reverse domain name assigned to the vendor
      * providing the implementation.
      *
      * @param pathName - the reverse domain name of the vendor, e.g. NIST SIP's
      *          would be 'gov.nist'
      */
     public static void setPathName(String pathName) {
         SdpFactory.pathName = pathName;
     }

     /**
      * Returns the current <var>pathname</var> of the SdpFactory. The
      * <var>pathname</var> identifies the location of a particular vendor's
      * implementation of this specification as defined the naming convention.
      * The pathname must be the reverse domain name assigned to the vendor
      * providing this implementation. This value is defaulted to
      * <code>gov.nist</code> the location of the NIST SIP Implementation.
      *
      * @return the string identifying the current vendor implementation.
      */
     public static String getPathName() {
         return pathName;
     }

    /**
     * Creates a new, empty SessionDescription. The session is set as follows:
     *
     *     v=0
     *
     *     o=this.createOrigin ("user",
     *     InetAddress.getLocalHost().toString());
     *
     *     s=-
     *
     *     t=0 0
     *
     * @throws SdpException - if there is a problem constructing
     *          the SessionDescription.
     * @return a new, empty SessionDescription.
     */
    public abstract SessionDescription createSessionDescription()
            throws SdpException;

    /**
     * Creates a SessionDescription populated with the information contained
     * within the string parameter.
     *
     *     Note: unknown field types should not cause exceptions.
     * @param s s - the sdp message that is to be parsed.
     * @throws SdpParseException - if there is a problem parsing the String.
     * @return a populated SessionDescription object.
     */
    public abstract SessionDescription createSessionDescription(String s)
            throws SdpParseException;

    /** Returns Bandwidth object with the specified values.
     * @param modifier modifier - the bandwidth type
     * @param value the bandwidth value measured in kilobits per second
     * @return bandwidth
     * @throws SdpException
     */
    public abstract BandWidth createBandwidth(String modifier, int value)
            throws SdpException;

    /** Returns Attribute object with the specified values.
     * If <param>value</param> is null, a property attribute is created, i.e.
     * an attribute with no value.
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return Attribute
     * @throws SdpException
     */
    public abstract Attribute createAttribute(String name, String value)
            throws SdpException;

    /** Returns Info object with the specified value.
     * @param value the string containing the description.
     * @return Info
     * @throws SdpException
     */
    public abstract Info createInfo(String value) throws SdpException;

    /** Returns Phone object with the specified value.
     * @param value the string containing the description.
     * @return Phone
     * @throws SdpException
     */
    public abstract Phone createPhone(String value) throws SdpException;

    /** Returns EMail object with the specified value.
     * @param value the string containing the description.
     * @return EMail
     * @throws SdpException
     */
    public abstract EMail createEMail(String value) throws SdpException;

    /** Returns URI object with the specified value.
     * @param value the URL containing the description.
     * @throws SdpException
     * @return URI
     */
    public abstract URI createURI(URL value) throws SdpException;

    /** Returns SessionName object with the specified name.
     * @param name the string containing the name of the session.
     * @return SessionName
     * @throws SdpException
     */
    public abstract SessionName createSessionName(String name) throws SdpException;

    /** Returns Key object with the specified value.
     * @param method the string containing the method type.
     * @param key the key to set
     * @return Key
     * @throws SdpException
     */
    public abstract Key createKey(String method, String key) throws SdpException;

    /** Returns Version object with the specified values.
     * @param value the version number.
     * @return Version
     * @throws SdpException
     */
    public abstract Version createVersion(int value) throws SdpException;

    /** Returns Media object with the specified properties.
     * @param media the media type, eg "audio"
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes vector to set
     * @throws SdpException
     * @return Media
     */
    public abstract Media createMedia(
            String media,
            int port,
            int numPorts,
            String transport,
            Vector<String> staticRtpAvpTypes)
            throws SdpException;

    /**
     * Returns Origin object with the specified properties.
     * @param userName the user name.
     * @param address the IP4 encoded address.
     * @throws SdpException if the parameters are null or if the
     *           <var>pathname</var> was set incorrectly.
     * @return Origin
     */
    public abstract Origin createOrigin(
            String userName, String address) throws SdpException;

    /** Returns Origin object with the specified properties.
     * @param userName String containing the user that created the
     *          string.
     * @param sessionId long containing the session identifier.
     * @param sessionVersion long containing the session version.
     * @param networkType String network type for the origin (usually
     *          "IN").
     * @param addrType String address type (usually "IP4").
     * @param address String IP address usually the address of the
     *          host.
     * @throws SdpException if the parameters are null
     * @return Origin object with the specified properties.
     */
    public abstract Origin createOrigin(
            String userName,
            long sessionId,
            long sessionVersion,
            String networkType,
            String addrType,
            String address) throws SdpException;

    /** Returns MediaDescription object with the specified properties.
     *     The returned object will respond to
     *     Media.getMediaFormats(boolean) with a Vector of media formats.
     * @param media media -
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes list of static RTP/AVP media payload
     *          types which should be specified by the returned MediaDescription 
     *   throws IllegalArgumentException if passed
     *          an invalid RTP/AVP payload type
     * @throws IllegalArgumentException
     * @throws SdpException
     * @return MediaDescription
     */
    public abstract MediaDescription createMediaDescription(
            String media,
            int port,
            int numPorts,
            String transport,
            int[] staticRtpAvpTypes)
            throws IllegalArgumentException, SdpException;

    /** Returns MediaDescription object with the specified properties.
     *     The returned object will respond to
     *     Media.getMediaFormats(boolean) with a Vector of String objects
     *     specified by the 'formats argument.
     * @param media the media type, eg "audio"
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param formats list of formats which should be specified by the
     *          returned MediaDescription
     * @throws SdpException
     * @return MediaDescription
     */
    public abstract MediaDescription createMediaDescription(
            String media,
            int port,
            int numPorts,
            String transport,
            String[] formats) throws SdpException;

    /** Returns TimeDescription object with the specified properties.
     * @param t the Time that the time description applies to. Returns
     *          TimeDescription object with the specified properties.
     * @throws SdpException
     * @return TimeDescription
     */
    public abstract TimeDescription createTimeDescription(Time t)
            throws SdpException;

    /** Returns TimeDescription unbounded (i.e. "t=0 0");
     * @throws SdpException
     * @return TimeDescription unbounded (i.e. "t=0 0");
     */
    public abstract TimeDescription createTimeDescription() throws SdpException;

    /** Returns TimeDescription object with the specified properties.
     * @param start start time.
     * @param stop stop time.
     * @throws SdpException if the parameters are null
     * @return TimeDescription
     */
    public abstract TimeDescription createTimeDescription(
            Date start, Date stop) throws SdpException;

    /** Returns a String containing the computed form for a
     *   multi-connection address. 
     *  Parameters:
     *     addr - connection address
     *    ttl - time to live (TTL) for multicast
     *     addresses
     *     numAddrs - number of addresses used by the
     *    connection 
     * Returns:
     *     a String containing the computed form for a
     *     multi-connection address.
     */
    public abstract String formatMulticastAddress(
            String addr, int ttl, int numAddrs);

    /** Returns a Connection object with the specified properties a
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */
    public abstract Connection createConnection(
            String netType,
            String addrType,
            String addr,
            int ttl,
            int numAddrs) throws SdpException;

    /** Returns a Connection object with the specified properties and no
     *     TTL and a default number of addresses (1).
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @throws SdpException if the parameters are null
     * @return Connection
     */
    public abstract Connection createConnection(
            String netType,
            String addrType,
            String addr) throws SdpException;

    /** Returns a Connection object with the specified properties and a
     *     network and address type of "IN" and "IP4" respectively.
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */
    public abstract Connection createConnection(
            String addr,
            int ttl,
            int numAddrs) throws SdpException;

    /** Returns a Connection object with the specified address. This is
     *     equivalent to
     *
     *        createConnection("IN", "IP4", addr);
     *
     * @param addr connection address
     * @throws SdpException if the parameter is null
     * @return Connection
     */
    public abstract Connection createConnection(String addr) throws SdpException;

    /** Returns a Time specification with the specified start and stop
     *     times.
     * @param start start time
     * @param stop stop time
     * @throws SdpException if the parameters are null
     * @return a Time specification with the specified start and stop
     *          times.
     */
    public abstract Time createTime(Date start, Date stop) throws SdpException;

    /** Returns an unbounded Time specification (i.e., "t=0 0").
     * @throws SdpException
     * @return an unbounded Time specification (i.e., "t=0 0").
     */
    public abstract Time createTime() throws SdpException;

    /** Returns a RepeatTime object with the specified interval,
     *     duration, and time offsets.
     * @param repeatInterval the "repeat interval" in seconds
     * @param activeDuration the "active duration" in seconds
     * @param offsets  the list of offsets relative to the start time of
     *          the Time object with which the returned RepeatTime will be
     *          associated
     * @throws SdpException
     * @return RepeatTime
     */
    public abstract RepeatTime createRepeatTime(
            int repeatInterval,
            int activeDuration,
            int[] offsets) throws SdpException;

    /** Constructs a timezone adjustment record.
     * @param d the Date at which the adjustment is going to take
     *          place.
     * @param offset the adjustment in number of seconds relative to
     *          the start time of the SessionDescription with which this
     *          object is associated.
     * @throws SdpException
     * @return TimeZoneAdjustment
     */
    public abstract TimeZoneAdjustment createTimeZoneAdjustment(
            Date d, int offset) throws SdpException;

    /**
     * @param ntpTime long to set
     * @return Returns a Date object for a given NTP date value.
     */
    public static Date getDateFromNtp(long ntpTime) {
         return new Date((ntpTime - SdpConstants.NTP_CONST) * 1000);
    }

    /** Returns a long containing the NTP value for a given Java Date.
     * @param d Date to set
     * @return long
     */
    public static long getNtpTime(Date d) {
        if (d == null) return -1;
        return ((d.getTime()/1000) + SdpConstants.NTP_CONST);
    }

}
