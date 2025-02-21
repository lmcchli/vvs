#include <rgm/libdsdev.h>
#include "ntf.h"

int main( int argc, char *argv[] )
{
	scds_handle_t scds_handle;

	if( scds_initialize( &scds_handle, argc, argv ) != SCHA_ERR_NOERR )
		return 1;

	if( svc_validate( scds_handle ))
	{
		scds_syslog(LOG_ERR, "Failed to validate configuration." );
		return 1;
	}

	svc_start( scds_handle );
	scds_close( &scds_handle );

	return 0;
}
