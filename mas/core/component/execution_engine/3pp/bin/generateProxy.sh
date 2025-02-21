#! /usr/bin/csh

if($#argv != 1) then
   echo "Usage: $0 <file.wsdl>"
   echo "Generates the javascript proxy methods for SOAP services"
   echo "The output file is <file.wsdl>.js"
else
   /opt/java/bin/java -cp ../src com.generator.Proxy $1 ../ajax_engine/wsdl.xslt > $1.js
endif


