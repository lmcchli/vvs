
#ifndef	_ntf_COMMON_H
#define	_ntf_COMMON_H

#pragma ident	"@(#)ntf.h	1.7	01/06/25 SMI"

#ifdef __cplusplus
extern "C" {
#endif

/* Debug levels for error messages */
#define	DBG_LEVEL_HIGH		9
#define	DBG_LEVEL_MED		5
#define	DBG_LEVEL_LOW		1

#define	SCDS_CMD_SIZE		(8 * 1024)

#define	SCDS_ARRAY_SIZE		1024

int svc_validate(scds_handle_t scds_handle);

int svc_start(scds_handle_t scds_handle);

int svc_stop(scds_handle_t scds_handle);

int svc_wait(scds_handle_t scds_handle);

int mon_start(scds_handle_t scds_handle);

int mon_stop(scds_handle_t scds_handle);

int svc_probe(scds_handle_t scds_handle, char *hostname, int port, int timeout);

int preprocess_cmd(scds_handle_t scds_handle, char *orig_cmd);

/* User added code -- BEGIN vvvvvvvvvvvvvvv */
/* User added code -- END   ^^^^^^^^^^^^^^^ */

#ifdef __cplusplus
}
#endif

#endif /* _ntf_COMMON_H */
