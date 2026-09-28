@ECHO OFF
SET /A ProcessId=0
FOR /F "tokens=5" %%T IN ('netstat -ano ^| findstr :%1 ^| findstr "LISTENING"') DO (
SET /A ProcessId=%%T) &GOTO SkipLine
:SkipLine
IF %ProcessId% GTR 0 GOTO _killprocess
IF %ProcessId% LEQ 0 GOTO _skipkill
:_killprocess
echo ProcessId = %ProcessId%
taskkill /pid %ProcessId% /F
:_skipkill