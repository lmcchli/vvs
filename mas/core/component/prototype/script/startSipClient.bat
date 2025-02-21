# Usage: startSipClient <port> [admin]


java -classpath ../classes;../lib/JainSipApi1.1.jar;../lib/nist-sip-1.2.jar;../lib/nist-sdp-1.0.jar;../lib/log4j-1.2.9.jar -Dlog4j.configuration=file:/c:/develop/mobeon_demo/lib/mobeon_client.properties com.mobeon.client.SIP.SipClientUA %1 %2
