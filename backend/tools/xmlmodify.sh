#! /bin/sh

##
## xmlmodify.sh --
##
##    Utility script for modifying an XML file. The "xsltproc" program
##    is used and must exist in the path. The script reads from stdin
##
##    This script does not validate the modified document. It is the
##    responsibility of the calles to ensure that the modified
##    document is valid to it's XML-schema.
##
##   General syntax;
##
##    xmlmodify.sh <command> [command-options] < source.xml [ > target.xml ]
##
##   XPath;
##
##    XML nodes are identified with an XPath for most commands. See
##    <http://www.w3.org/TR/xpath> for more info.
##
##   Commands;
##
##    setattr <XPath> [attr=value ...]
##
##      Sets attributes on the matching node(s). Example;
##
##        xmlmodify.sh setattr //storage overridemcr=true target=disc
##
##
##    rmattr <XPath> attr
##
##      Removes one attribute from the matching node(s). Example;
##
##        xmlmodify.sh rmattr '//profilemanager//*' default
##
##
##    nodecount <XPath>
##
##      Count matching nodes. The number of matching occurences
##      is printed on stdout. The exit-status is 'true' if there are
##      any (more than zero) occurences. Example;
##
##        xmlmodify.sh nodecount '*'   # Counts the total number of nodes
##        xmlmodify.sh nodecount //default && echo "<default> nodes exist"
##
##
##    replacenode <XPath> <node>
##
##      Replaces existing node(s). It is the responsibility of the
##      caller to ensure that the replacing node is well-formed XML.
##
##      A node on any level can be replaced, so this command is mighty
##      powerful. Example;
##
##        xmlmodify.sh replacenode / '<empty>'  # Wipes out the document
##
##
##    deletenode <XPath>
##
##      Delete node(s). Example;
##
##        xmlmodify.sh deletenode //default  # Delete all <default> nodes
##
##
##    addchild <XPath> <node>
##
##      Add a child node. A new child node is inserted first in the
##      parent node(s) selected by the XPath. The caller must ensure
##      that the node is well-formed XML. If you want to insert a node
##      at some other place than first, use the "addsibling"
##      command. Example;
##
##        xmlmodify.sh addchild //attribute '<default type="i">4</default>'
##
##
##    addsibling <XPath> <node>
##
##      Add a sibling node *after* the node(s) selected by the
##      XPath. The caller must ensure that the node is well-formed
##      XML.
##
##        xmlmodify.sh addsibling '//attribute[@name="ownername"]' \
##              '<attribute name="ownersurname" />'
##
##

this=$0
test -n "$tmpfile" || tmpfile="/tmp/xmlmodify_${USER}_$$"
test -n "$xsltproc" || xsltproc=xsltproc
test -n "$encoding" || encoding=ISO-8859-1
die() {
    echo "ERROR: $*" >&2
    exit 1
}

help() {
    grep '^##' $0 | cut -c3-
    exit 0
}

cleanup() {
    echo $tmpfile | grep "^/tmp/" > /dev/null && rm -f $tmpfile
}

cmd_setattr() {
    test -n "$1" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    attr=''
    for n in "$@"; do
        name=`echo $n | cut -d= -f1`
        value="`echo $n | sed -e "s,$name=,," | tr -d '\"'`"
        attr="$attr<xsl:attribute name=\"$name\">$value</xsl:attribute>"
    done
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}">
    <xsl:copy>
        <xsl:copy-of select="@*"/>
        $attr
        <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

cmd_rmattr() {
    test -n "$1" -a -n "$2" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    attr=$1
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}">
    <xsl:copy>
        <xsl:copy-of select="@*[name()!='$attr']"/>
        <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

cmd_nodecount() {
    test -n "$1" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">  
  <xsl:template match="${path}">
    % <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>
EOF
    count=`tr -d '%' | $xsltproc $tmpfile - | tr -dc '%' | wc -c`
    echo $count
    test $count -gt 0
}

cmd_replacenode() {
    test -n "$1" -a -n "$2" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    node=$1;
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">  
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}">
    $node
  </xsl:template>
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

cmd_deletenode() {
    test -n "$1" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">  
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}" />
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

cmd_addchild() {
    test -n "$1" || die "Parameters missing"
    test -n "$1" -a -n "$2" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    node=$1;
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">  
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}">
    <xsl:copy><xsl:copy-of select="@*"/>
      $node
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

cmd_addsibling() {
    test -n "$1" || die "Parameters missing"
    test -n "$1" -a -n "$2" || die "Parameters missing"
    path=`echo "$1" | tr '"' "'"`; shift
    node=$1;
    cat > $tmpfile <<EOF
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">  
  <xsl:output method="xml" indent="yes" encoding="$encoding"/>
  <xsl:template match="*">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  <xsl:template match="${path}">
    <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
    $node
  </xsl:template>
</xsl:stylesheet>
EOF
    $xsltproc $tmpfile -
}

test -n "$1" || help
cmd=$1
shift
case $cmd in
    setattr|nodecount|replacenode|deletenode|addchild|addsibling|rmattr)
	cmd_$cmd "$@"
        status=$?
	;;
    *)
	die "Invalid command [$cmd]"
	;;
esac

cleanup
exit $status
