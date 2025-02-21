#include <rgm/libdsdev.h>
#include "ntf.h"

int main( int argc, char *argv[] )
{
	scds_handle_t scds_handle;
	int rc;

	if( scds_initialize( &scds_handle, argc, argv ) != SCHA_ERR_NOERR )
		return (1);

	rc = svc_validate( scds_handle );

	/* Free up all the memory allocated by scds_initialize */
	scds_close( &scds_handle );

	/* Return the result of validate method */
	return rc;

}
