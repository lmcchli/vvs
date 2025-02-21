#!/bin/ksh

export package="mediatranslationmanager"
export classpath=com.mobeon.masp.${package}

export maslib=/vobs/ipms/mas/lib
export jarfiles=${maslib}/spring.jar
export jarfiles=${maslib}/imap.jar:${jarfiles}
export jarfiles=${maslib}/jmock-1.0.1.jar:${jarfiles}
export jarfiles=/usr/local/ant-1.6.5/lib/junit.jar:${jarfiles}
export jarfiles=${maslib}/nlsml.jar:${jarfiles}
export jarfiles=${maslib}/ssml.jar:${jarfiles}
export jarfiles=${maslib}/xbean.jar:${jarfiles}
export jarfiles=${maslib}/jsr173_1.0_api.jar:${jarfiles}
export jarfiles=${maslib}/xercesImpl.jar:${jarfiles}
export jarfiles=${maslib}/nist-sdp-1.0.jar:${jarfiles}
export jarfiles=${maslib}/sdp-api.jar:${jarfiles}
export jarfiles=${maslib}/nist-sip-1.2.jar:${jarfiles}
export jarfiles=${maslib}/activation.jar:${jarfiles}
export jarfiles=${maslib}/dom4j-1.6.1.jar:${jarfiles}
export jarfiles=${maslib}/log4j-1.2.9.jar:${jarfiles}
export jarfiles=${maslib}/foundationjava.jar:${jarfiles}
export jarfiles=${maslib}/commons-collections-3.1.jar:${jarfiles}
export jarfiles=${maslib}/commons-logging.jar:${jarfiles}
export jarfiles=${maslib}/mobeon_rtpstack.jar:${jarfiles}
export jarfiles=/vobs/ipms/mas/${package}/classes:${jarfiles}

echo package: ${package}
echo classpath: ${classpath}
echo jarfiles: ${jarfiles}

/usr/local/jdk1.5.0_05/bin/java -classpath ${jarfiles} ${classpath}.$1 $2 $3 $4 $5 $6 $7 $8 $9
