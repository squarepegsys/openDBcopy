#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  opendbcopy Bootstrap Script                                             ##
##                                                                          ##
##  This script has not yet been tested. So if you are having trouble       ##
##  to launch opendbcopy with this script please provide use with           ##
##  with necessary feedback                                                 ##
##                                                                          ##
### ====================================================================== ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

# Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$OPENDBCOPY_HOME" ] &&
        OPENDBCOPY_HOME=`cygpath --unix "$OPENDBCOPY_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup OPENDBCOPY_HOME
if [ "x$OPENDBCOPY_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    OPENDBCOPY_HOME=`cd $DIRNAME/.; pwd`
fi
export OPENDBCOPY_HOME

# Increase the maximum file descriptors if we can
if [ "$cygwin" = "false" ]; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ]; then
	if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
	    # use the system max
	    MAX_FD="$MAX_FD_LIMIT"
	fi

	ulimit -n $MAX_FD
	if [ $? -ne 0 ]; then
	    warn "Could not set maximum file descriptor limit: $MAX_FD"
	fi
    else
	warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Setup Profiler
useprofiler=false
if [ "x$PROFILER" != "x" ]; then
    if [ -r "$PROFILER" ]; then
        . $PROFILER
        useprofiler=true
    else
        die "Profiler file not found: $PROFILER"
    fi
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup the classpath
runjar="$OPENDBCOPY_HOME/lib/opendbcopy.jar"
if [ ! -f $runjar ]; then
    die "Missing required file: $runjar"
fi

# Include the JDK javac compiler for JSP pages. The default is for a Sun JDK
# compatible distribution which JAVA_HOME points to
if [ "x$JAVAC_JAR" = "x" ]; then
    JAVAC_JAR="$JAVA_HOME/lib/tools.jar"
fi
if [ ! -f "$JAVAC_JAR" ]; then
   # MacOSX does not have a seperate tools.jar
   if [ "$darwin" != "true" ]; then
      warn "Missing file: $JAVAC_JAR"
      warn "Unexpected results may occur.  Make sure JAVA_HOME points to a JDK and not a JRE."
   fi
fi

OPENDBCOPY_CLASSPATH="$CLASSPATH:$OPENDBCOPY_CLASSPATH:$DIRNAME/lib/xerces.jar:$DIRNAME/lib/log4j-1.2.8.jar:$DIRNAME/lib/jdom.jar:$JAVAC_JAR"

# If JAVA_OPTS is not set try check for Hotspot
if [ "x$JAVA_OPTS" = "x" ]; then

    # Check for SUN(tm) JVM w/ HotSpot support
    if [ "x$HAS_HOTSPOT" = "x" ]; then
	HAS_HOTSPOT=`$JAVA -version 2>&1 | $GREP -i HotSpot`
    fi

    # Enable -server if we have Hotspot, unless we can't
    if [ "x$HAS_HOTSPOT" != "x" ]; then
	# MacOS does not support -server flag
	if [ "$darwin" != "true" ]; then
	    JAVA_OPTS="-server"
	fi
    fi
fi

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS -Dprogram.name=$PROGNAME"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    OPENDBCOPY_HOME=`cygpath --path --windows "$OPENDBCOPY_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    OPENDBCOPY_CLASSPATH=`cygpath --path --windows "$OPENDBCOPY_CLASSPATH"`
fi

# Display our environment
echo "================================================================================"
echo ""
echo "  opendbcopy Bootstrap Environment"
echo ""
echo "  OPENDBCOPY_HOME: $OPENDBCOPY_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $OPENDBCOPY_CLASSPATH"
echo ""
echo "================================================================================"
echo ""

if $useprofiler; then
    # Hand over control to profiler
    runProfiler
else
    STATUS=10
    while [ $STATUS -eq 10 ]
    do
    # Execute the JVM
       $JAVA $JAVA_OPTS \
               -classpath "$OPENDBCOPY_CLASSPATH" \
               opendbcopy.controller.MainController "$@"        
       STATUS=$?
    done
fi
