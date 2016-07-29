@echo off

rem === Local build settings
set LOCAL_BUILD_DIR=c:\d\xds-de-builds\src\local_build\trunk
set LOCAL_SOURCE_DIR=%~dp0..\..

rem === JDK settings
set JAVA_HOME=c:\Program Files\Java\jdk1.8.0_45
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% 

set INCLUDE=%JAVA_HOME%\include;%JDKDIR%\include\win32;%INCLUDE%
set LIB=%JAVA_HOME%\lib;%LIB%

rem === Ant settings
set ANT_HOME=c:\distribs\java\ant\apache-ant-1.9.2
set PATH=%ANT_HOME%\bin;%PATH%


if "%1" == "" goto :EOF
call %*
goto :EOF
