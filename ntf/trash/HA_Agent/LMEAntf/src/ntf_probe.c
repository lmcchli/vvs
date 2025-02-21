#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <rgm/libdsdev.h>
#include "ntf.h"

int main( int argc, char *argv[] )
{
	scds_handle_t scds_handle;
	scha_err_t rc;
	float ht1, ht2;
	float probe_result;
	ulong_t dt;

	if( scds_initialize(&scds_handle, argc, argv) != SCHA_ERR_NOERR )
		return 1;

	while( 1 )
	{
		scds_fm_sleep( scds_handle, scds_get_rs_thorough_probe_interval( scds_handle ));

		ht1 = gethrtime();
		usleep( 10000 );
		ht2 = gethrtime();

		dt = (ulong_t)((ht2 - ht1) / 1e6);

		scds_fm_action(scds_handle, probe_result, (long)dt);
	}
}
