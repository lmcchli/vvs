This file contains a list of changes made to the downloaded NIST SIP/SDP stack.
-------------------------------------------------------------------------------

JAIN_SIP_1_2_BASELINE_20061128
------------------------------
The NIST SIP/SDP stack was downloaded from the jain-sip project found here:
https://www.dev.java.net/servlets/Login

Since the release does not have a timestamp, it is unknown when it was built, 
but it was downloaded 2006-11-28. 
This release was imported in the vob and labeled with the above label. It is the
initial baseline for the NIST SIP/SDP stack in this branch.



START_JAIN_SIP_R3A
------------------
Changes was made to the downloaded version to make it compile and to get the
tests running.
It is the start for this branch and is labeled with the above label.


JAIN_SIP_P3A_01
---------------
For this version, changes made on dev_r1a was merged to this branch.
These changes mainly consists of:
- minor error corrections
- removal of System.out.println and printstacktrace when debugging/tracing has
  not been turned on.


JAIN_SIP_P3A_02
---------------
For this version, changes made on dev_r2a was merged to this branch.
These changes mainly consists of:
- error corrections
- performance improval


JAIN_SIP_P3A_03
---------------
This version contains changes made to get MAS working with the new upgraded
SIP stack. The changes consists of various error corrections only:
- minor error corrections
- added usage of Java 5 features for example to get more type safety
- changed separator in Privacy header from "," to ";" which as specified in
  RFC 3323
- made sure the dialog is not terminated if a re-invite is rejected with a
  3xx-6xx response
- made changes regarding how the SIP stack listener is assigned when SIP
  providers are added or removed
- made some changes to test cases that had timing issues
- removed SDP class files from the SIP stack implementation jar file


JAIN_SIP_P3A_04
---------------
This version contains changes made to support the P-Asserted-Identity and  
History-Info header fields fully. 


JAIN_SIP_P3A_05
---------------
- Made sure that all calls to log.debug and log.info are preceeded with a log.isDebugEnabled or log.isInfoEnabled.
- Corrected errors in the SIPDialog with regards to creating and sending a reliable provisional response.
- Made a change in DialogFilter.java in order to allow unsolicited NOTIFY's.


JAIN_SIP_P3A_06
---------------
- Made corrections so that 200 OK for an INVITE or re-INVITE is resent if the ACK is not received.


JAIN_SIP_P3A_07
---------------
- Corrected errors regarding retransmissions of reliable provisional responses.
- Made sure that an out-of-sequence PRACK request is rejected with a 481 instead of being dropped.


JAIN_SIP_P3A_08
---------------
- Corrected errors regarding provisional responses timer task.


JAIN_SIP_P3A_09
---------------
- Corrected errors regarding detecting loopback.
- Made corrections regarding setting bandwidth information in SDP.


JAIN_SIP_P3A_10
---------------
- Made minor corrections in SIPDialog after follow-up of changes made for 100rel in 
  JAIN_SIP_P3A_05, JAIN_SIP_P3A_06, JAIN_SIP_P3A_07, and JAIN_SIP_P3A_08.
- Corrected error regarding cseq-value chosen when sending a SIP ACK request.


JAIN_SIP_P3A_11
---------------
- TCP support implemented. Major rewrite of some parts of the stack, mostly regarding
  transport/io, message channels, message processors, transactions and dialogs.
  The TCP support is NOT fully tested!
- Implemented P-Charging-Vector header.
- Corrected TR 29879.
- Corrected TR 29840.


JAIN_SIP_P3A_12
---------------
- Corrected error with leaking dialogs. 
- Added more debug logging regarding creating and deleting dialogs.


JAIN_SIP_P3A_13
---------------
- Corrected TR 30450, retransmitted responses was sent to wrong remote party.


JAIN_SIP_P3A_14
---------------
- Corrected TR 31843, dialog leak occured in special case. Fixed in SIPDialog.java.


JAIN_SIP_P3A_15
---------------
- Corrected an error introduced in JAIN_SIP_P3A_14 when correcting TR 31843. 
- Removed support for character sets in the SIP body part due to faulty implementation.



