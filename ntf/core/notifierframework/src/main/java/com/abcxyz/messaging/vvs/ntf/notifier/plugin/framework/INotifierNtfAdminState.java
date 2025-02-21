package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;


public interface INotifierNtfAdminState {
     
    /**
     * AdministarativeState of NTF:
     * UNLOCKED: NTF is up and running normally.
     * LOCKED: NTF is paused, waiting to be unlocked
     * SHUTDOWN: NTF is in the process of shutting down, or has been asked to shutdown.
     * EXIT, NTF is in the final stages of shutting down and will no longer wait for any running process.
     * 
     * A state change is usually initiated by SNMP, or via the NTF script, however can also exit due to severe error conditions or a SIGTERM, SIGHUP
     */
    public static enum AdministrativeState {
        UNLOCKED(0), LOCKED(1), SHUTDOWN(2), EXIT(3);
        private int value;
        private AdministrativeState(int value) {
             this.value = value;
        }
        public int getValue(){
         return value;
        }
    }
    
    /**
     * @return AdministrativeState - the administrative state of NTF.
     */
    public AdministrativeState get();

}
