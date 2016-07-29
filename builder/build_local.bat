@echo off
set BUILD_K26_VERSION=OFF

set ACTION=%1
if "%1" == "" set ACTION=build

set CONFIG_DIR=%~dp0config

set GET_LOG=%~dp0get.log
set BUILD_LOG=%~dp0build.log

set ENV_BAT=config\env_%COMPUTERNAME%.bat
if exist "%ENV_BAT%"   call "%ENV_BAT%"

echo K26 IDE local %ACTION%

echo Cleaning ...
for %%i in (log,out) do  if exist *.%%i  del /F /Q *.%%i 
if exist "%LOCAL_BUILD_DIR%"   rmdir /S /Q "%LOCAL_BUILD_DIR%"
mkdir "%LOCAL_BUILD_DIR%"

if "%ACTION%" == "clean" goto :lbl_Finish

echo Getting source code ...
xcopy "%LOCAL_SOURCE_DIR%\product" "%LOCAL_BUILD_DIR%\product\" /exclude:skiplist.txt /s /e  1> "%GET_LOG%" 2>&1 
for %%i in (builder,target-platform,docs,localization,tools)  do xcopy "%LOCAL_SOURCE_DIR%\%%i" "%LOCAL_BUILD_DIR%\%%i\" /s /e  1>> "%GET_LOG%" 2>>&1  
for %%i in (.rep_url)  do copy "%LOCAL_SOURCE_DIR%\%%i" "%LOCAL_BUILD_DIR%\%%i"  1>> "%GET_LOG%" 2>>&1  

echo Building ...
cd "%LOCAL_BUILD_DIR%/builder"

ant.bat -f build-xds-ide.xml 1> "%BUILD_LOG%" 2>&1
if errorlevel 1 goto lbl_Error
 
:lbl_Finish
echo === K26 IDE local %ACTION% === OK!
goto :EOF


:lbl_Error
echo *** K26 IDE local %ACTION% failed *** Erorr(s)!
goto :EOF
