# =============================================================================
# Create links and cache to the libstdc+ that we install with MoIP
# =============================================================================
#!/bin/sh
ldconfig -nv /usr/lib64
echo " ldconfig has been run on /usr/lib64" > /opt/moip/logs/libstdc_install.log 
