@echo off

rem set /A pid=0
rem for /F "tokens=5" %%T IN ('netstat -ano ^| findstr :5610 ^| findstr "LISTENING"') do (
rem set /A pid=%%T) &GOTO _eol
rem :_eol
rem IF %pid% GTR 0 GOTO _killprocess
rem IF %pid% LEQ 0 GOTO _skipkill
rem :_killprocess
rem start /B /wait taskkill /pid %pid% /F > nul
rem :_skipkill

SET EXEC_JAVA=%~dp0

set /A pid=0
for /F "tokens=1" %%T IN ('start /B /MIN %EXEC_JAVA%jdk\bin\jps ^| findstr "eclarity-client.jar"') do (
set /A pid=%%T) &GOTO _eol
:_eol
IF %pid% GTR 0 GOTO _showmessage
IF %pid% LEQ 0 GOTO _skipkill

rem :_killprocess
rem echo One process found with id: %pid%
rem start /B /wait taskkill /pid %pid% /F > nul

:_skipkill
echo Java home found: ["%JAVA_HOME%"]
IF ["%JAVA_HOME%"] == [""] GOTO _setpath
IF NOT ["%JAVA_HOME%"] == [""] GOTO _pathfound

:_setpath
echo Java path not found. Please execute "setpath.bat" as administrator.
rem runas /noprofile /user:%COMPUTERNAME%\%USERNAME% setpath.bat
GOTO _exit

:_pathfound
start /B /MIN %EXEC_JAVA%jdk\bin\java -Xms256m -Xmx400M -XX:MaxGCPauseMillis=190 -XX:+UseG1GC -verbose:gc -XX:NewRatio=3 -XX:CompileThreshold=5000 -jar ^
   -Dlog.folder=.\logs\^
   -Dspring.config.location=.\^
   -Dspring.config.name=application^
   -Dloader.path=.\^
   -Dwork.dir=.\workdir\^
   .\eclarity-client.jar
GOTO _done

:_exit
echo Press any key to exit . . .
pause>nul
GOTO _done

:_showmessage
start /B /MIN %EXEC_JAVA%jdk\bin\javaw AlreadyRunning

:_done