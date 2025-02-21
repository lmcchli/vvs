package com.mobeon.masp.callmanager.configuration;

/**
 * Enumeration indicating the usage of pEarlyMediaHeader in SIP response.
 * 
 * <ul>
 * <li> {@link EarlyMediaHeaderUsage.OFF}: No P-Eearly-Media header shall be added by Call Manager into SIP response (call flow could add P-Eearly-Media header)
 * <li> {@link EarlyMediaHeaderUsage.SUPPORTED}: If incoming SIP INVITE contains 'supported' in P-Eearly-Media header, include appropriate value in P-Eearly-Media header SIP response 
 * <li> {@link EarlyMediaHeaderUsage.FORCED}: Regardless of incoming SIP INVITE, include appropriate value in P-Eearly-Media header SIP response 
 * </ul>
 */

public enum EarlyMediaHeaderUsage {
    OFF,
    SUPPORTED,
    FORCED;

    /**
     * Parses the configuration for pEarlyMediaHeader in SIP response usage and returns a {@link EarlyMediaHeaderUsage}.
     * @param usage String representation of the EarlyMediaHeaderUsage (from configuration)
     * @return Returns a {@link ReliableResponseUsage} 
     */
    public static EarlyMediaHeaderUsage parseEarlyMediaHeaderUsage(String usage) {
        EarlyMediaHeaderUsage result = EarlyMediaHeaderUsage.OFF;
        if (usage != null) {
            if (usage.equalsIgnoreCase("supported")) result = EarlyMediaHeaderUsage.SUPPORTED;
            else if (usage.equals("forced")) result = EarlyMediaHeaderUsage.FORCED;
        }
        return result;
    }
}
