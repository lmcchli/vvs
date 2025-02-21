/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package gov.nist.sdp;

import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.Version;
import com.mobeon.sdp.Origin;
import com.mobeon.sdp.SessionName;
import com.mobeon.sdp.TimeDescription;
import com.mobeon.sdp.Time;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.BandWidth;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.Info;
import com.mobeon.sdp.Phone;
import com.mobeon.sdp.EMail;
import com.mobeon.sdp.URI;
import com.mobeon.sdp.Key;
import com.mobeon.sdp.Media;
import com.mobeon.sdp.MediaDescription;
import com.mobeon.sdp.Connection;
import com.mobeon.sdp.RepeatTime;
import com.mobeon.sdp.TimeZoneAdjustment;
import gov.nist.sdp.fields.ProtoVersionField;
import gov.nist.sdp.fields.SessionNameField;
import gov.nist.sdp.fields.TimeField;
import gov.nist.sdp.fields.BandwidthField;
import gov.nist.sdp.fields.AttributeField;
import gov.nist.sdp.fields.InformationField;
import gov.nist.sdp.fields.PhoneField;
import gov.nist.sdp.fields.EmailField;
import gov.nist.sdp.fields.URIField;
import gov.nist.sdp.fields.KeyField;
import gov.nist.sdp.fields.MediaField;
import gov.nist.sdp.fields.OriginField;
import gov.nist.sdp.fields.SDPKeywords;
import gov.nist.sdp.fields.ConnectionField;
import gov.nist.sdp.fields.RepeatField;
import gov.nist.sdp.fields.ZoneField;
import gov.nist.sdp.parser.SDPAnnounceParser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.text.ParseException;

/**
 * This is an implementation of the abstract {@link SdpFactory} class.
 * <p>
 * The SdpFactory enables applications to encode and decode SDP messages.
 * The SdpFactory can be used to construct a SessionDescription
 * object programmatically.
 * The SdpFactory can also be used to construct a SessionDescription based
 * on the contents of a String.
 * <p>
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 *@author Malin Flodin
 */
public class SdpFactoryImpl extends SdpFactory {

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
    public SessionDescription createSessionDescription() throws SdpException
    {
        // Create a new SessionDescription
        SessionDescription sessionDescription = new SessionDescriptionImpl();

        // Add a new Version Field.
        Version version = new ProtoVersionField();
        version.setVersion(0);
        sessionDescription.setVersion(version);

        // Add a new Origin Field.
        Origin origin = null;
        try{
            origin=this.createOrigin(
                    "user", InetAddress.getLocalHost().getHostAddress());
        } catch(UnknownHostException e) {
            // Do nothing
        }
        sessionDescription.setOrigin(origin);

        // Add a new Session Name Field.
        SessionName sessionName = new SessionNameField();
        sessionName.setValue("-");
        sessionDescription.setSessionName(sessionName);

        // Create a new instance of a TimeDescription.
        TimeDescription timeDescription = new TimeDescriptionImpl();

        // Add a new Time Field.
        Time time = new TimeField();
        time.setZero();
        timeDescription.setTime(time);

        Vector<TimeDescription> times = new Vector<TimeDescription>();
        times.addElement(timeDescription);
        sessionDescription.setTimeDescriptions(times);

        return sessionDescription;
    }

    /**
     * Creates a SessionDescription populated with the information contained
     * within the string parameter.
     *
     *     Note: unknown field types should not cause exceptions.
     * @param s s - the sdp message that is to be parsed.
     * @throws SdpParseException - if there is a problem parsing the String.
     * @return a populated SessionDescription object.
     */
    public SessionDescription createSessionDescription(String s)
            throws SdpParseException
    {
        try{
            SDPAnnounceParser sdpParser =new SDPAnnounceParser( s );
            return sdpParser.parse();
        } catch(ParseException e) {
            throw new SdpParseException(0, 0, "Could not parse message", e);
        }
    }

    /** Returns Bandwidth object with the specified values.
     * @param modifier modifier - the bandwidth type
     * @param value the bandwidth value measured in kilobits per second
     * @return bandwidth
     * @throws SdpException
     */
    public BandWidth createBandwidth(String modifier, int value)
            throws SdpException {
        BandWidth bandWidth = new BandwidthField();
        bandWidth.setType(modifier);
        bandWidth.setValue(value);
        return bandWidth;
    }

    /** Returns Attribute object with the specified values.
     * @param name the namee of the attribute
     * @param value the value of the attribute
     * @return Attribute
     * @throws SdpException
     */
    public Attribute createAttribute(String name, String value)
            throws SdpException {
        AttributeField attribute = new AttributeField();
        attribute.setName(name);
        attribute.setValueAllowNull(value);
        return attribute;
    }

    /** Returns Info object with the specified value.
     * @param value the string containing the description.
     * @return Info
     * @throws SdpException
     */
    public Info createInfo(String value) throws SdpException {
        Info info = new InformationField();
        info.setValue(value);
        return info;
    }

    /** Returns Phone object with the specified value.
     * @param value the string containing the description.
     * @return Phone
     * @throws SdpException
     */
    public Phone createPhone(String value) throws SdpException {
        Phone phone = new PhoneField();
        phone.setValue(value);
        return phone;
    }

    /** Returns EMail object with the specified value.
     * @param value the string containing the description.
     * @return EMail
     * @throws SdpException
     */
    public EMail createEMail(String value) throws SdpException {
        EMail email = new EmailField();
        email.setValue(value);
        return email;
    }

    /** Returns URI object with the specified value.
     * @param value the URL containing the description.
     * @throws SdpException
     * @return URI
     */
    public URI createURI(URL value) throws SdpException {
        URI uri = new URIField();
        uri.set(value);
        return uri;
    }

    /** Returns SessionName object with the specified name.
     * @param name the string containing the name of the session.
     * @return SessionName
     * @throws SdpException
     */
    public SessionName createSessionName(String name) throws SdpException {
        SessionName sessionName = new SessionNameField();
        sessionName.setValue(name);
        return sessionName;
    }

    /** Returns Key object with the specified value.
     * @param method the string containing the method type.
     * @param key the key to set
     * @return Key
     * @throws SdpException
     */
    public Key createKey(String method, String key) throws SdpException {
        Key keyField = new KeyField();
        keyField.setMethod(method);
        keyField.setKey(key);
        return keyField;
    }

    /** Returns Version object with the specified values.
     * @param value the version number.
     * @return Version
     * @throws SdpException
     */
    public Version createVersion(int value) throws SdpException {
        Version protoVersion = new ProtoVersionField();
        protoVersion.setVersion(value);
        return protoVersion;
    }

    /** Returns Media object with the specified properties.
     * @param media the media type, eg "audio"
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes vector to set
     * @throws SdpException
     * @return Media
     */
    public Media createMedia(
            String media,
            int port,
            int numPorts,
            String transport,
            Vector<String> staticRtpAvpTypes)
            throws SdpException {
        Media mediaField = new MediaField();
        mediaField.setMediaType(media);
        mediaField.setMediaPort(port);
        mediaField.setPortCount(numPorts);
        mediaField.setProtocol(transport);
        mediaField.setMediaFormats(staticRtpAvpTypes);
        return mediaField;
    }

    /**
     * Returns Origin object with the specified properties.
     * @param userName the user name.
     * @param address the IP4 encoded address.
     * @throws SdpException if the parameters are null.
     * @return Origin
     */
    public Origin createOrigin(String userName, String address)
            throws SdpException
    {
        Origin origin = new OriginField();
        origin.setUsername(userName);
        origin.setAddress(address);
        origin.setNetworkType(SDPKeywords.IN);
        origin.setAddressType(SDPKeywords.IPV4);
        return origin;
    }

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
    public Origin createOrigin(
            String userName,
            long sessionId,
            long sessionVersion,
            String networkType,
            String addrType,
            String address)
            throws SdpException {
        Origin origin = new OriginField();
        origin.setUsername(userName);
        origin.setAddress(address);
        origin.setSessionId(sessionId);
        origin.setSessionVersion(sessionVersion);
        origin.setAddressType(addrType);
        origin.setNetworkType(networkType);
        return origin;
    }

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
    public MediaDescription createMediaDescription(
            String media,
            int port,
            int numPorts,
            String transport,
            int[] staticRtpAvpTypes)
            throws IllegalArgumentException, SdpException {
        MediaDescription mediaDescription = new MediaDescriptionImpl();
        Media mediaField = new MediaField();
        mediaField.setMediaType(media);
        mediaField.setMediaPort(port);
        mediaField.setPortCount(numPorts);
        mediaField.setProtocol(transport);
        mediaDescription.setMedia(mediaField);

        Vector<String> payload=new Vector<String>();
        for (int staticRtpAvpType : staticRtpAvpTypes)
            payload.add(Integer.toString(staticRtpAvpType));
        mediaField.setMediaFormats(payload);

        return mediaDescription;
    }

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
    public MediaDescription createMediaDescription(
            String media,
            int port,
            int numPorts,
            String transport,
            String[] formats) throws SdpException {

        MediaDescription mediaDescription = new MediaDescriptionImpl();

        Media mediaField = new MediaField();
        mediaField.setMediaType(media);
        mediaField.setMediaPort(port);
        mediaField.setPortCount(numPorts);
        mediaField.setProtocol(transport);

        Vector<String> formatVector = new Vector<String>(formats.length);
        for (String format : formats)
            formatVector.add(format);
        mediaField.setMediaFormats(formatVector);
        mediaDescription.setMedia(mediaField);

        return mediaDescription;
    }

    /** Returns TimeDescription object with the specified properties.
     * @param t the Time that the time description applies to. Returns
     *          TimeDescription object with the specified properties.
     * @throws SdpException
     * @return TimeDescription
     */
    public TimeDescription createTimeDescription(Time t) throws SdpException {
        TimeDescription timeDescription = new TimeDescriptionImpl();
        timeDescription.setTime(t);
        return timeDescription;
    }

    /** Returns TimeDescription unbounded (i.e. "t=0 0");
     * @throws SdpException
     * @return TimeDescription unbounded (i.e. "t=0 0");
     */
    public TimeDescription createTimeDescription() throws SdpException {
        TimeDescription timeDescription = new TimeDescriptionImpl();
        TimeField time = new TimeField();
        time.setZero();
        timeDescription.setTime(time);
        return timeDescription;
    }

    /** Returns TimeDescription object with the specified properties.
     * @param start start time.
     * @param stop stop time.
     * @throws SdpException if the parameters are null
     * @return TimeDescription
     */
    public TimeDescription createTimeDescription(
            Date start, Date stop) throws SdpException {
        TimeDescription timeDescription = new TimeDescriptionImpl();
        Time time = new TimeField();
        time.setStart(start);
        time.setStop(stop);
        timeDescription.setTime(time);
        return timeDescription;
    }

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
    public String formatMulticastAddress(String addr,
                                         int ttl,
                                         int numAddrs) {
        return addr+"/"+ttl+"/"+numAddrs;
    }

    /** Returns a Connection object with the specified properties a
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */
    public Connection createConnection(String netType,
                                       String addrType,
                                       String addr,
                                       int ttl,
                                       int numAddrs) throws SdpException {
        Connection connection = new ConnectionField();
        connection.setNetworkType(netType);
        connection.setAddressType(addrType);
        connection.setAddress(addr);
        connection.setAddressTtl(ttl);
        connection.setAddressCount(numAddrs);
        return connection;
    }

    /** Returns a Connection object with the specified properties and no
     *     TTL and a default number of addresses (1).
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @throws SdpException if the parameters are null
     * @return Connection
     */
    public Connection createConnection(String netType,
                                       String addrType,
                                       String addr) throws SdpException {
        Connection connection = new ConnectionField();
        connection.setNetworkType(netType);
        connection.setAddressType(addrType);
        connection.setAddress(addr);
        return connection;
    }

    /** Returns a Connection object with the specified properties and a
     *     network and address type of "IN" and "IP4" respectively.
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */
    public Connection createConnection(String addr,
                                       int ttl,
                                       int numAddrs) throws SdpException{
        Connection connection = new ConnectionField();
        connection.setNetworkType(Connection.IN);
        connection.setAddressType(Connection.IP4);
        connection.setAddress(addr);
        connection.setAddressTtl(ttl);
        connection.setAddressCount(numAddrs);
        return connection;
    }

    /** Returns a Connection object with the specified address. This is
     *     equivalent to
     *
     *        createConnection("IN", "IP4", addr);
     *
     * @param addr connection address
     * @throws SdpException if the parameter is null
     * @return Connection
     */
    public Connection createConnection(String addr) throws SdpException {
        return createConnection(Connection.IN, Connection.IP4, addr);
    }

    /** Returns a Time specification with the specified start and stop
     *     times.
     * @param start start time
     * @param stop stop time
     * @throws SdpException if the parameters are null
     * @return a Time specification with the specified start and stop
     *          times.
     */
    public Time createTime(Date start,
                           Date stop)
                       throws SdpException {
        TimeField timeImpl=new TimeField();
        timeImpl.setStart(start);
        timeImpl.setStop(stop);
        return timeImpl;
    }

    /** Returns an unbounded Time specification (i.e., "t=0 0").
     * @throws SdpException
     * @return an unbounded Time specification (i.e., "t=0 0").
     */
    public Time createTime()
    throws SdpException {
        TimeField timeImpl=new TimeField();
        timeImpl.setZero();
        return timeImpl;
    }

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
    public RepeatTime createRepeatTime(
            int repeatInterval,
            int activeDuration,
            int[] offsets) throws SdpException {
        RepeatTime repeatTime = new RepeatField();
        repeatTime.setRepeatInterval(repeatInterval);
        repeatTime.setActiveDuration(activeDuration);
        repeatTime.setOffsetArray(offsets);
        return repeatTime;
    }

    /** Constructs a timezone adjustment record.
     * @param d the Date at which the adjustment is going to take
     *          place.
     * @param offset the adjustment in number of seconds relative to
     *          the start time of the SessionDescription with which this
     *          object is associated.
     * @throws SdpException
     * @return TimeZoneAdjustment
     */
    public TimeZoneAdjustment createTimeZoneAdjustment(
            Date d, int offset) throws SdpException {
        TimeZoneAdjustment timeZoneAdjustment = new ZoneField();
        Hashtable<Date, Integer> map = new Hashtable<Date, Integer>();
        map.put(d, offset);
        timeZoneAdjustment.setZoneAdjustments(map);
        return timeZoneAdjustment;
    }

}
