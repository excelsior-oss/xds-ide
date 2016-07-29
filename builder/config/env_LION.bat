@echo off

rem === Local build settings
set LOCAL_BUILD_DIR=E:\K26_IDE\build-oss\local_branch
set LOCAL_SOURCE_DIR=%~dp0..\..

rem === JDK settings
set JAVA_HOME=C:\bin\Java\jdk1.8.0_45_x86
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% 

set INCLUDE=%JAVA_HOME%\include;%JDKDIR%\include\win32;%INCLUDE%
set LIB=%JAVA_HOME%\lib;%LIB%

rem === Ant settings
set ANT_HOME=C:\bin\Java\ant
set PATH=%ANT_HOME%\bin;%PATH%


if "%1" == "" goto :EOF
call %*
goto :EOF
