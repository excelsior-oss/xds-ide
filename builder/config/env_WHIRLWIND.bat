@echo off

rem === Local build settings
set LOCAL_BUILD_DIR=d:\xds-de-builds\src\local_build\trunk
set LOCAL_SOURCE_DIR=%~dp0..\..

rem === JDK settings
set JAVA_HOME=%JDK_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% 

set INCLUDE=%JAVA_HOME%\include;%JDKDIR%\include\win32;%INCLUDE%
set LIB=%JAVA_HOME%\lib;%LIB%

rem === Ant settings
set ANT_HOME=d:\distribs\developer\java\ant\apache-ant-1.9.4
set PATH=%ANT_HOME%\bin;%PATH%


if "%1" == "" goto :EOF
call %*
goto :EOF
