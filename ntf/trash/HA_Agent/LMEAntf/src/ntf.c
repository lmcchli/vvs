/*
 * ntf.c - Master agent source file
 *
 * This source provides routines for validating, starting and stopping
 * a data service in a Sun Cluster 3.0/3.1 environment.
 *
 * Currently no probing is done so there is just a dummy routine in place
 * for this purpose.
 *
 * Copyright (C) 2003 Mobeon AB.
 * All Rights Reserved
 */

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <strings.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <scha.h>
#include <rgm/libdsdev.h>
#include <errno.h>
#include <dlfcn.h>
#include "ntf.h"

#define	SVC_SMOOTH_PCT		80
#define	SVC_HARD_PCT		15

typedef struct tagPROGRAM {
	char *          pCmd;
	unsigned        iMonLevel;
} PROGRAM;

PROGRAM pStartCmds[] = {
	{ "/apps/LMEAntf/bin/hantf-ntf.sh ${Basedir}", 2 },
	{ "/apps/LMEAntf/bin/hantf-agt.sh ${Basedir}", 5 },
	{ NULL, 0 }
};

char * pStopCmds[] = {
        "",
        NULL
};

/* This is the same as a pointer to the scds_hasp_check api */
typedef scha_err_t (*scds_hasp_check_t)(scds_handle_t, scds_hasp_status_t *);

/*
 * svc_validate():
 *
 * Validate configuration before starting up
 *
 */

int svc_validate(scds_handle_t scds_handle)
{
	struct stat statbuf;
	int i, err;
	int do_cmd_checks = 1;
	char cmd[SCDS_CMD_SIZE];
	scds_hasp_status_t hasp_status;
	scds_hasp_check_t scds_hasp_check_p;
	char * pTxt;

	/* Are we running with a libdsdev that does not have scds_hasp_check? */
	if( !(scds_hasp_check_p = (scds_hasp_check_t)dlsym( RTLD_DEFAULT, "scds_hasp_check" )))
	{
		err = SCHA_ERR_NOERR;
		hasp_status = SCDS_HASP_NO_RESOURCE;
	} 
	else
		err = (*scds_hasp_check_p)(scds_handle, &hasp_status);

	if( err != SCHA_ERR_NOERR )
		return 1;

	switch( hasp_status )
	{
		case SCDS_HASP_NO_RESOURCE:
			scds_syslog(LOG_INFO,
				"This resource does not depend on any SUNW.HAStoragePlus "
				"resources. Proceeding with normal checks.");
			do_cmd_checks = 1;
			break;

		case SCDS_HASP_ERR_CONFIG:
			/*
			* Configuration error, SUNW.HAStoragePlus resource is
			* in a different RG. Fail the validation.
			*/
			scds_syslog(LOG_ERR,
				"One or more of the LMEA.HAStoragePlus resources that "
				"this resource depends on is in a different resource "
				"group. Failing validate method configuration checks.");
			return 1;

		case SCDS_HASP_NOT_ONLINE:
			/*
			* There is at least one SUNW.HAStoragePlus resource not
			* online anywhere.
			*/
			scds_syslog(LOG_ERR,
				"One or more of the LMEA.HAStoragePlus resources that "
				"this resource depends on is not online anywhere. "
				"Failing validate method.");
			return 1;

		case SCDS_HASP_ONLINE_NOT_LOCAL:
			/*
			* Not all SUNW.HAStoragePlus we need, are online locally.
			*/
			scds_syslog(LOG_INFO,
				"All the SUNW.HAStoragePlus resources that this resource "
				"depends on are not online on the local node. "
				"Skipping the checks for the existence and permissions "
				"of the start/stop/probe commands.");
			do_cmd_checks = 0;
			break;

		case SCDS_HASP_ONLINE_LOCAL:
			/*
			* All LMEA.HAStoragePlus resources we need are available on
			* this node.
			*/
			scds_syslog(LOG_INFO,
				"All the LMEA.HAStoragePlus resources that this resource "
				"depends on are online on the local node. "
				"Proceeding with the checks for the existence and "
				"permissions of the start/stop/probe commands.");
			do_cmd_checks = 1;
			break;

		default:
			scds_syslog(LOG_ERR, "Unknown status code %d.", hasp_status );
			return 1;
	}

	if( do_cmd_checks )
	{
		for( i=0; pStartCmds[i].pCmd; i++ )
		{
			/* Verify that the start command(s) are accessable and executable */
			/* Before we can do anything we need to expand the command to its full form */

			strcpy( cmd, pStartCmds[i].pCmd );
			ExpandCmdString( scds_handle, cmd );

			/* Wipe away extra parameters and stuff for now, we just want the base command (poor mans basename) */
			for( pTxt=cmd; *pTxt && *pTxt != ' ' && *pTxt != '\t'; pTxt++ )
				;
			*pTxt=0;

			if( stat( cmd, &statbuf ))
			{
				scds_syslog( LOG_ERR, "Cannot access the %s command <%s> : <%s>", "start", cmd, strerror( errno ));
				return 1;
			}

			if( !(statbuf.st_mode & S_IXUSR) )
			{
				scds_syslog(LOG_ERR, "The %s command does not have execute permissions: <%s>", "start", cmd);
				return 1;
			}
		}
	}

	return 0;
}

int svc_start( scds_handle_t scds_handle )
{
	int rc = 0;
	int i;
	char cmd[SCDS_CMD_SIZE];
	scha_extprop_value_t *child_mon_level_prop = NULL;
	scha_extprop_value_t *basedir_prop = NULL;
	int child_mon_level;

	rc = scds_get_ext_property( scds_handle, "Child_mon_level", SCHA_PTYPE_INT, &child_mon_level_prop );

	if (rc == SCHA_ERR_NOERR)
	{
		child_mon_level = child_mon_level_prop->val.val_int;
		scds_syslog(LOG_INFO, "Extension property <Child_mon_level> has a value of <%d>", child_mon_level);
	}
	else
	{
		scds_syslog(LOG_INFO,
		    "Either extension property <Child_mon_level> is not "
		    "defined, or an error occurred while retrieving this "
		    "property; using the default value of 2.");
		child_mon_level = 2;
	}

	for( i=0; pStartCmds[i].pCmd; i++ )
	{
		strcpy( cmd, pStartCmds[i].pCmd );

		if( (rc = ExpandCmdString( scds_handle, cmd )))
		{
			scds_syslog(LOG_ERR, "Failed to form the %s command.", "start");
			return rc;
		}

		rc = scds_pmf_start(scds_handle, SCDS_PMF_TYPE_SVC, i, cmd, child_mon_level);

		if( rc == SCHA_ERR_NOERR )
			scds_syslog(LOG_INFO, "Start of %s completed successfully.", cmd);
		else
		{
			scds_syslog(LOG_ERR, "Failed to start %s.", cmd);
			return rc;
		}
	}

	return( rc );
}

int svc_stop( scds_handle_t scds_handle )
{
	int i;
	int rc = 0, cmd_exit_code = 0;
	int stop_smooth_timeout = (scds_get_rs_stop_timeout(scds_handle) * SVC_SMOOTH_PCT) / 100;
	int stop_hard_timeout = (scds_get_rs_stop_timeout(scds_handle) * SVC_HARD_PCT) / 100;
	char cmd[SCDS_CMD_SIZE];
	scha_extprop_value_t *stop_signal_prop = NULL;
	int stop_signal = SIGTERM;

	rc = scds_get_ext_property(scds_handle, "Stop_signal", SCHA_PTYPE_INT, &stop_signal_prop);

	if( rc == SCHA_ERR_NOERR )
	{
		stop_signal = stop_signal_prop->val.val_int;
		scds_syslog(LOG_INFO, "Extension property <stop_signal> has a value of <%d>", stop_signal);
	}
	else
	{
		scds_syslog(LOG_INFO,
		    "Either extension property <stop_signal> is not defined, "
		    "or an error occurred while retrieving this property; "
		    "using the default value of SIGTERM.");
	}

	if( strcmp( pStopCmds[0], "" ))
	{
		for( i=0; pStartCmds[i].pCmd; i++ )
		{
			/*
			 * First take the command out of PMF monitoring,
			 * so that it doesn't keep restarting it.
			 */
			rc = scds_pmf_stop_monitoring(scds_handle, SCDS_PMF_TYPE_SVC, i);

			if (rc != SCHA_ERR_NOERR)
			{
				scds_syslog(LOG_ERR, "Failed to take the resource out of PMF control. Sending SIGKILL now." );
				goto send_kill;
			}
		}

		/*
		 * First try to stop the application using the stop command
		 * provided.
		 */
		strcpy( cmd, pStopCmds[0] );

		if( (rc = ExpandCmdString( scds_handle, cmd )))
		{
			scds_syslog(LOG_ERR, "Failed to form the %s command.", "stop");
			/*
			 * We failed to preprocess the stop command so can't
			 * use that. We still proceed to send KILL signal and
			 * try to stop the application anyway.
			 */
			goto send_kill;
		}

		rc = scds_timerun(scds_handle, cmd, stop_smooth_timeout, SIGKILL, &cmd_exit_code);

		if (rc != 0 || cmd_exit_code != 0)
		{
			scds_syslog(LOG_ERR,
			    "The stop command <%s> failed to stop the "
			    "application. Will now use SIGKILL to stop the "
			    "application.",  cmd);
		}
	}
	else
	{
		for( i=0; pStartCmds[i].pCmd; i++ )
		{
			/*
			 * If no stop command is specified, we use
			 * scds_pmf_stop to stop the application.
			 */

			if( (rc = scds_pmf_stop(scds_handle, SCDS_PMF_TYPE_SVC, i, stop_signal, scds_get_rs_stop_timeout(scds_handle))) != SCHA_ERR_NOERR )
			{
				scds_syslog( LOG_ERR, "Failed to stop the application" );
				/*
				 * Since the Data service did not stop with a
				 * scds_pmf_stop, we return non-zero.
				 */
				goto finished;
			}
		}
		goto finished;
	}

send_kill:
	for( i=0; pStartCmds[i].pCmd; i++) {
		if ((rc = scds_pmf_stop(scds_handle, SCDS_PMF_TYPE_SVC, i, SIGKILL, stop_hard_timeout)) != SCHA_ERR_NOERR)
		{
			scds_syslog(LOG_ERR,
			    "Failed to stop the application with SIGKILL. "
			    "Returning with failure from stop method.");
			break;
		}
	}

finished:

	scds_free_ext_property(stop_signal_prop);

	if (rc == SCHA_ERR_NOERR)
		scds_syslog(LOG_INFO, "Successfully stopped the application");

	return( rc );
}

int mon_start( scds_handle_t scds_handle )
{
	scha_err_t	err;

	scds_syslog_debug(DBG_LEVEL_HIGH, "Calling MONITOR_START method for resource <%s>.", scds_get_resource_name(scds_handle));

	/*
	 * The probe ntf_probe is assumed to be available in the same
	 * subdirectory where the other callback methods for the RT are
	 * installed. The last parameter to scds_pmf_start denotes the
	 * child monitor level. Since we are starting the probe under PMF
	 * we need to monitor the probe process only and hence we are using
	 * a value of 0.
	 */

	err = scds_pmf_start(scds_handle, SCDS_PMF_TYPE_MON, SCDS_PMF_SINGLE_INSTANCE, "ntf_probe", 0);

	if (err != SCHA_ERR_NOERR)
	{
		scds_syslog(LOG_ERR, "Failed to start fault monitor.");
		return err;
	}

	scds_syslog(LOG_INFO, "Started the fault monitor.");

	return( SCHA_ERR_NOERR ); /* Successfully started Monitor */
}

int mon_stop(scds_handle_t scds_handle)
{
	scha_err_t err;

	scds_syslog_debug(DBG_LEVEL_HIGH, "Calling scds_pmf_stop method");
	err = scds_pmf_stop(scds_handle, SCDS_PMF_TYPE_MON, SCDS_PMF_SINGLE_INSTANCE, SIGKILL, scds_get_rs_monitor_stop_timeout(scds_handle));

	if (err != SCHA_ERR_NOERR)
	{
		scds_syslog( LOG_ERR, "Failed to stop fault monitor." );
		return( err );
	}

	scds_syslog( LOG_INFO, "Stopped the fault monitor." );

	return( SCHA_ERR_NOERR );
}

int ExpandCmdString( scds_handle_t scds_handle, char * pString )
{
	char cOutput[SCDS_CMD_SIZE*2];
	char *pTxt;
	char *pVar;
	char cVar[1024];
	char *pStart = pString;
	char *pValue;
	scha_extprop_value_t * basedir_prop = NULL;

	for( pTxt=cOutput; *pString; pString++ )
	{
		switch( *pString )
		{
			case '$':
				switch( pString[1] )
				{
					case '{':
						pVar = cVar;

						for( pString += 2; *pString && *pString != '}'; pString++ )
							*pVar++ = *pString;
						*pVar=0;

						if( *pString == '{' )
							pString++;

						if( !scds_get_ext_property( scds_handle, cVar, SCHA_PTYPE_STRING, &basedir_prop ))
						{
							pValue = basedir_prop->val.val_str;

							for( ; *pValue; pValue++ )
								*pTxt++ = *pValue;

							scds_free_ext_property( basedir_prop );
						}

						break;

					case '$':
						*pTxt++ = '$';
						pString++;
						break;

					default:
						*pTxt++ = *pString;
						break;
				}
				break;

			default:
				*pTxt++ = *pString;
				break;
		}
	}
	*pTxt=0;

	strcpy( pStart, cOutput );

	return 0;
}
