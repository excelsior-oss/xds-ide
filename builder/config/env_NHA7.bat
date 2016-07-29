@echo off

rem === Local build settings
set LOCAL_BUILD_DIR=E:\FSA\K26_IDE\K26-IDE\trunkBuild\local_trunk
set LOCAL_SOURCE_DIR=E:\FSA\K26_IDE\K26-IDE\trunk

rem === JDK settings
set JAVA_HOME=E:\jdk\jdk1.6.0_20
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% 

set INCLUDE=%JAVA_HOME%\include;%JDKDIR%\include\win32;%INCLUDE%
set LIB=%JAVA_HOME%\lib;%LIB%

rem === Ant settings
set ANT_HOME=%LOCAL_SOURCE_DIR%\tools\ant
set PATH=%ANT_HOME%\bin;%PATH%


if "%1" == "" goto :EOF
call %*
goto :EOF
