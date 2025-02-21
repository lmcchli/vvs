#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <rgm/libdsdev.h>
#include "ntf.h"

int main( int argc, char *argv[] )
{
	scds_handle_t scds_handle;
	scha_err_t result;

	if (scds_initialize(&scds_handle, argc, argv) != SCHA_ERR_NOERR )
		return 1;

	/*
	 * check if the Fault monitor is already running and if so stop and
	 * restart it. The second parameter to scds_pmf_restart_fm() uniquely
	 * identifies the instance of the fault monitor that needs to be
	 * restarted.
	 */

	result = scds_pmf_restart_fm(scds_handle, 0);

	if( result != SCHA_ERR_NOERR )
	{
		scds_syslog( LOG_ERR, "Failed to restart fault monitor." );
		scds_close( &scds_handle );
		return 1;
	}

	scds_syslog(LOG_INFO, "Completed successfully." );

	/* Free up all the memory allocated by scds_initialize */
	scds_close( &scds_handle );

	return 0;
}
