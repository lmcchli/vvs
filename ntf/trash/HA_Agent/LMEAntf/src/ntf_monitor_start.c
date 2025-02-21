#include <rgm/libdsdev.h>
#include "ntf.h"

int main( int argc, char *argv[] )
{
	scds_handle_t scds_handle;
	int rc;

	/* Process arguments passed by RGM and initialize syslog */
	if( scds_initialize(&scds_handle, argc, argv) != SCHA_ERR_NOERR)
		return 1;

	rc = mon_start(scds_handle);
	scds_close(&scds_handle);

	return (rc);
}
