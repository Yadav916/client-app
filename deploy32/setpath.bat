@echo off

SET CURRPATH=%~dp0
echo The current path: %CURRPATH%

setx JAVA_HOME %CURRPATH%jdk /m
set JAVA_HOME=%CURRPATH%jdk
