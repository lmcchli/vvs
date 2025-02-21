#Add the following entries to /opt/moip/common/componentservices.cfg MIO configuration file
#So that the application can locate the MOIP MS an MUR service
#I used localhost because I created an SSH tunnel to the MOIP5.0 MUR and MS
#if you have direct acces to the MOIP 5.0 MS and MUR enter the IP addresses



#for migration from MoIP
servicename=userregister
componentname=moip_mur
componenttype=mur
hostname=localhost
port=389

servicename=storage
componentname=moip_ms
componenttype=ms
hostname=ms01.gsdcmoip5.lmc.abcxyz.se
port=145



To support extraction of subscriber profile, messages and greetings from systems other than MoIP 6.0, you will need to modify:

*Profile fetching via LDAP
*Message Fetching via IMAP
*Parsing of the IMAP MIME message into a IMediaObject 



**Profile Fetching


The ProfileManager implementation has been designed to be highly configurable. By modifying the migration.xml and the componentServices.cfg files, you should be able to extract the required data from nay LDAP subscriber repository.

 
The idea is to have the ProfileManagerImpl perform a LDAP queries and store the results in a IProfile:
 
The perfromed LDAP search can be configured  in migration.xml 

 

<userregister readtimeout="10000" writetimeout="5000" admin="cn=directory manager" password="emmanager" defaultsearchbase="o=userdb" trylimit="3" trytimelimit="500"/>

 

Replace admin, password and defaultsearch base with the proper values. 

ldapsearch -h beceb01dir01 -b "o=userdb" "telephoneNumber=$1"
 

As required, modify the mapping between internal representation of an attribute and the LDAP schema in the < attributemap> section of the migration.xml config.

 

In the following mapping example:

 

 <mailhost userregistername="mailhostblabla" type="string" default="" provisioningname="MAILHOST"/>

 

mailhost is the internal representation.

mailhostblabla is the name of the attribute in the LDAP schema that will map to mailhost internal representation.

The type of the attribute is string.

The provisioning name is not used in our case so you can leave it empty.

If useful, you might even want to specify a default value if the attribute is not present in the LDAP fetch.

 

Make sure that the following attributes are properly mapped because they are required to perform subsequent IMAP fetch for messages.

 

·         mailhost represents the smptstorage host that will be configured in the componentServices.cfg

·         uid represents the users accounted or IMAP username

·         password represents the user’s IMAP password

·         mail represents the user’s e-mail address.

 

Below are the definitions we use for MoIP. You have to figure out what are the proper mapping for the legacy system.

 

<mailhost userregistername="mailhost" type="string" default="" provisioningname="MAILHOST"/>

<uid userregistername="uid" type="string" provisioningname="UID"/>

<password userregistername="password" type="xstring" writelevel="user" provisioningname="MAILBOX_PW">

<syntax><re expr="[a-zA-Z0-9-\.]{4,8}"/></syntax>

</password>

<mail userregistername="mail" type="string" provisioningname="MAILADDRESS"/>

             

    

You then need to modify your extractor application so that it uses the proper filtering criteria for the LDAP fetch:

 

 

                  ProfileStringCriteria filter = new ProfileStringCriteria(

                              " telephoneNumber", subscriberNumber);

                  IProfile[] profiles = profileManager.getProfile(filter);

 

 

                  ProfileStringCriteria filter = new ProfileStringCriteria(

                              " umbillingNumber", subscriberNumber);

                  IProfile[] profiles = profileManager.getProfile(filter);

 
To populate the IProfile the ProfileManagerImpl will perform 4 ldap queries,

*User query
*billing query
*COS query
*Community query

These query are implemented by the following methods:

com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl.getSuperLevelAttributes(ProfileAttributes, DirContext)
com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl.getBillingResult(ProfileAttributes, DirContext)
com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl.getCosResult(String, ProfileAttributes, DirContext)
com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl.getCommunityResult(ProfileAttributes, DirContext)

Theese methods can be overwritten to return empty results if they are not pertinent to the lgacy system being queried.
 

 

**Message Fetching

 

The Mailbox component, in particular the JavamailMailboxAccountManager are very flexible and configurable. You should be able to use it and adapt it to fetch the messages from any server via IMAP.

 

The following internal attributes from the IProfile are used to fetched the mailbox via IMAP, make sure they are mapped properly.

 

mailHost = getStringAttribute("mailhost");

accountId = getStringAttribute("uid");

accountPassword = getStringAttribute("password");

emailAddress = getStringAttribute("mail"); 

 

mailHost value will then be searched in the MCR for service smtpstorage(conponentServices.conf) which will retur a serviceInstance containing the host and the port:

 

IServiceInstance serviceInstance = getContext().getServiceLocator().locateService("storage", mailHost);

 

Edit the componentServices.cfg

 

Simply add  in componentServices.cfg

 

servicename=storage

componentname=moip_ms

componenttype=ms

hostname=<mailhost>

port=<port>

 

If everything is properly configured, a call to subscriberProfile.getMailbox() should return you a IMailbox object that you can query to obtain folder and messages.

 

Parsing of the message into a IMediaObject

 

This part will require complete understanding of the legacy message format in order to parse the headers and message body properly. Legacy format must have similarities with MoIP but they probably use different headers.

 

As required, create subclasses of com.mobeon.masp.mailbox.javamail.JavamailMessageAdapter and com.mobeon.masp.mailbox.javamail.JavamailPartAdapter

 

Overwrite if necessary the parse message method and com.mobeon.masp.mailbox.javamail.JavamailPartAdapter.parseProperties()

 