#include <rgm/libdsdev.h>
#include "ntf.h"

int main(int argc, char *argv[] )
{
	scds_handle_t scds_handle;
	int rc;

	/* Process the arguments passed by RGM and initialize syslog */
	if( scds_initialize(&scds_handle, argc, argv) != SCHA_ERR_NOERR )
		return 1;

	rc =  svc_validate(scds_handle);
	scds_syslog_debug(DBG_LEVEL_HIGH, "monitor_check method was called and returned <%d>.", rc);

	/* Free up all the memory allocated by scds_initialize */
	scds_close( &scds_handle );

	/* Return the result of validate method run as part of monitor check */
	return rc;
}
