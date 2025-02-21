The intent of this README file is to describe the structure of the jain-sip 
vob.

The jain-sip vob contains the following directories:
- jain-sip
- nist-sip
- sipunit

The nist-sip directory contains the first version of the SIP stack which is 
used in MAS for releases 14.1.0 - 14.1.2. It is only valid on development 
branch dev_r1a and should no longer be used. 

The jain-sip directory contains the updated version of the SIP stack. 
In branch dev_r2a this directory is called jain-sip-1.2 and is used in 
SSP for releases 14.1.0 - 14.1.2 and perhaps further on as well.
The SIP stack in branch dev_r3a is used in MAS release 14.1.3.

The sipunit directory contains a tool based on jain-sip that is used for 
basic testing in MAS. SipUnit version 0.0.2a was used in releases 
14.1.0 - 14.1.2 of MAS and since no changes was required in that release of 
SipUnit it has not been version controlled in ClearCase. 
When a new version of the SIP stack was imported in branch dev_r3a, a new 
version of SipUnit (version 0.0.6b) was needed. Unfortunately, this version 
needed corrections, and therefore sipunit was added to this jain-sip vob.

The jain-sip vob currently has three branches:
dev_r1a, dev_r2a and dev_r3a. 

In dev_r1a, the above mentioned directory structure did not exist. 
The vob simply contained the SIP stack code. 

The branch dev_r2a was created when the SIP stack needed to be upgraded from 
JAIN-SIP 1.1 to JAIN-SIP 1.2. Since this upgrade was considered large, 
the old source code was moved to /vobs/ipms/jain-sip/nist-sip/ and the new 
source code placed in /vobs/ipms/jain-sip/jain-sip-1.2/. 
This upgrade was made for SSP in order to boost performance.

The branch dev_r3a was created when the SIP stack needed to be upgraded to 
boost performance in MAS an in order to retrieve new functionality. 
This upgrade was only a never version of JAIN-SIP 1.2. 
When doing this upgrade, the directory jain-sip-1.2 was renamed to jain-sip
in order to let possible new versions of JAIN-SIP to be covered as well. 

SipUnit only exists in branch dev_r3a.

