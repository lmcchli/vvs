#!/bin/sh
JAVA=/usr/java/bin/java

$JAVA -cp lib/mobeon_media_content_manager.jar:lib/mobeon_logging.jar:lib/log4j-1.2.9.jar com.mobeon.masp.mediacontentmanager.grammar.grammarfiletool.GrammarFileTool $1 $2

