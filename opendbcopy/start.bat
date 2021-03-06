@echo off
rem -------------------------------------------------------------------------
rem opendbcopy Bootstrap Script for Win32
rem -------------------------------------------------------------------------

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

rem Read all command line arguments

set ARGS=
:loop
if [%1] == [] goto endloop
        set ARGS=%ARGS% %1=%2
        shift
        shift
        goto loop
:endloop

if not "%JAVA_HOME%" == "" goto ADD_TOOLS

echo JAVA_HOME is not set.  Unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
goto SKIP_TOOLS

:ADD_TOOLS

set JAVA=%JAVA_HOME%\bin\java

if exist "%JAVA_HOME%\lib\tools.jar" goto SKIP_TOOLS
echo Could not locate %JAVA_HOME%\lib\tools.jar. Unexpected results may occur.
echo Make sure that JAVA_HOME points to a JDK and not a JRE.

:SKIP_TOOLS

set OPENDBCOPY_CLASSPATH=%CLASSPATH%;%DIRNAME%lib\opendbcopy.jar;%DIRNAME%lib\log4j-1.2.8.jar;%DIRNAME%lib\xerces.jar;%DIRNAME%lib\jdom.jar;%JAVAC_JAR%;%RUNJAR%

rem Sun JVM memory allocation pool parameters. Uncomment and modify as appropriate.
set JAVA_OPTS=-Xms128m -Xmx512m

echo ===============================================================================
echo .
echo   opendbcopy Bootstrap Environment
echo .
echo   JAVA: %JAVA%
echo .
echo   ARGS: %ARGS%
echo .
echo   JAVA_OPTS: %JAVA_OPTS%
echo .
echo   CLASSPATH: %OPENDBCOPY_CLASSPATH%
echo .
echo ===============================================================================
echo .
echo loading opendbcopy ...
echo .
echo java %JAVA_OPTS% -classpath "%OPENDBCOPY_CLASSPATH%" opendbcopy.controller.MainController %ARGS%
echo .

:RESTART
java %JAVA_OPTS% -classpath "%OPENDBCOPY_CLASSPATH%" opendbcopy.controller.MainController %ARGS%
IF ERRORLEVEL 10 GOTO RESTART

:END
echo bye