How to generate nlsml.jar and ssml.jar:

First ensure that apache XML Beans are installed (you must have the script scomp).
You should also ensure that the jar files are checked out.

1: Enter the directory 'schema'.
2: scomp -out ../../lib/ssml.jar -compiler %JAVA_HOME%\bin\javac synthesis.xsd synthesis.xsdconfig
3: scomp -out ../../lib/nlsml.jar -compiler %JAVA_HOME%\bin\javac nlsml.xsd nlsml.xsdconfig

Finally execute the unit tests for SSML and NSLML and check in the lot.

