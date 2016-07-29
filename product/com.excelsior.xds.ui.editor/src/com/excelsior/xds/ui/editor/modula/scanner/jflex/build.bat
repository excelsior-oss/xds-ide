@echo off
set JAVA_HOME=%~dp0..\..\..\..\..\..\..\..\..\..\com.excelsior.xds.jre.feature\rootfiles.win32\jre
set PATH=%JAVA_HOME%\bin;%PATH%

set JFLEX_HOME=%~dp0..\..\..\..\..\..\..\..\..\..\..\tools\jflex
set CLASSPATH=%CLASSPATH%;%JFLEX_HOME%\lib\JFlex.jar 
                       
set JFLEX_SKELETON=..\..\..\..\..\..\..\..\..\..\com.excelsior.xds.ui.editor.commons\src\com\excelsior\xds\ui\editor\commons\scanner\jflex\eclipse-flex.skeleton

call java JFlex.Main --charat --skel "%JFLEX_SKELETON%" %*