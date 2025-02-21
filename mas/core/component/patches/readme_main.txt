#:------------------------------------------------------------------------------:
#:            README FOR #_CORRECTION_#      		  
#:                Abcxyz AB
#:------------------------------------------------------------------------------:
#:------------------------------------------------------------------------------:
									        :
ID.buildnr	: #_SEQNUM_#_#_COMP_NAME_#_#_VERSION_#.#_BUILD_NUM_# 

PRODUCT NUMBER	: #_PRODUCT_NUMBER_#	

TYPE		: #_PATCH_TYPE_#
		
OS VERSION	: Solaris 10

ISSUE DATE	: 
                 
:------------------------------------------------------------------------------:
									       :
IMPORTANT INFO	: #_IMPORTANT_INFO_#

DESCRIPTION	: #_PATCH_DESCRIPTION_#

		  #_OSOLETED_PATCH_DESCRIPTION_#
		
DEPLOYMENT	: <Describe which instances this patch applies to>.

REQUIRES	:  #_DEPENDENCIES_#
		
OBSOLETES      : #_OBSOLETES_PATCHES_#

:------------------------------------------------------------------------------:
									       :
DOWNTIME       : 

SYSTEM IMPACT  : #_SYSTEM_IMPACT_#

:------------------------------------------------------------------------------:
									       :
INSTALLATION   : 

       	     NOTE! Before this patch is installed all previous patches, 
	     Emergency Corrections and Feature Enhancements installed on 
	     the MAS must be uninstalled. Uninstallation shall be done in 
	     reversed installation order.


	     Manual installation on one MAS host:

	     1. Login to the host where MAS is installed.

	     2. Become superuser (root)
	         $ su -

	     3. Copy the patch to a temporary directory on the host.
	          # cp #_PATCH_FILENAME_# <TEMP_DIR>

	     4. Uncompress the file
	         # cd <TEMP_DIR>
	         # unzip #_PATCH_FILENAME_#

	     5. Shutdown MAS
	         # /etc/init.d/rc.mas shutdown
		 (Wait for all calls to finish.)

	     6. Install the patch
	         # patchadd #_SEQNUM_#_#_COMP_NAME_#_#_VERSION_#.#_BUILD_NUM_#

	     7. Wait for the patch installation to end. 
	         The following text should be among the last lines of the output: 
	         Patch #_SEQNUM_#_#_COMP_NAME_#_#_VERSION_#.#_BUILD_NUM_# has been successfully installed.

	     8. Start MAS:
	         # /etc/init.d/rc.mas start

	     9. Unlock MAS (If not unlocked already):
	         # /etc/init.d/rc.mas unlock

		

	     The patch installation scripts will automatically update 
	     configuration files and MCR registration.



	     Install patch on several hosts using JumpStart Manager:

	     1. See Operation and Maintenance JumpStart Manager
	       on how to install the patch on several hosts using job.

	       Note! With this method the MAS being patched will be stopped
	       and ongoing calls will be disconnected. After patch install,
	       the MAS will started again (if the MAS was started prior to
	       patch installation).



:------------------------------------------------------------------------------:
									       :
UNINSTALLATION : 

	     Manual uninstallation of the patch:

       	     1. Login on the host where the patch shall be removed.

	     2. Become superuser (root)
	         $ su -

	     3. Shutdown MAS
	         # /etc/init.d/rc.mas shutdown
		 (Wait for all calls to finish.)

	     4. Run patchrm:
	         # patchrm #_SEQNUM_#_#_COMP_NAME_#_#_VERSION_#.#_BUILD_NUM_#

	     5. Start MAS:
	         # /etc/init.d/rc.mas start

	       This uninstalls and unregisters the patch.


	     The patch removal scripts will automatically restore
	     configuration files and remove the patch from MCR.


	     Uninstall patch on several hosts using JumpStart Manager:

	     1. See Operation and Maintenance JumpStart Manager
	       on how to uninstall the patch on several hosts using job.

	       Note! With this method the MAS being unpatched will be stopped
	       and ongoing calls will be disconnected. After patch removal,
	       the MAS will started again (if the MAS was started prior to
	       patch uninstallation).


:------------------------------------------------------------------------------:
:									       :
CONFIGURATION
CHANGES		: 
		  #_CONFIGURATION_CHANGES_#

		  #_OBSOLETED_CONFIGURATION_CHANGES_#


:------------------------------------------------------------------------------:
									       :
FIXED TR's and    : 
CR's		    
		    #_FIXED_XR_#

		    #_OBSOLETED_XR_#

:------------------------------------------------------------------------------:
								               :
INCLUDED FILES : #_INCLUDED_FILES_#

:------------------------------------------------------------------------------:

