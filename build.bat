@echo off

rem Modify the java path for compiling...
SET _JAVA_HOME=%JAVA_HOME%
SET JAVA_HOME=..\..\..\Java\jdk-11.0.18

echo %JAVA_HOME%

if "%1%" == "c" GOTO DOCOMPILE
if "%1%" == "cc" GOTO DOCOMPILEONLY

:AFTERCOMPILE

%JAVA_HOME%\bin\java -jar --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED^
   -Dlog.folder=C:\logsss\^
   -Dspring.config.location=c:\Infospica-Workspace\eclarity-client\deploy\^
   -Dspring.config.name=application^
   -Dloader.path=c:\Infospica-Workspace\eclarity-client\deploy\^
   -Dwork.dir=.\deploy\workdir\^
   .\deploy\eclarity-client.jar

GOTO ENDEXEC

:DOCOMPILE
call mvn clean package -DskipTests=true
copy .\target\eclarity-client.jar .\deploy /y
copy .\target\eclarity-client.jar c:\eclarity /y
GOTO AFTERCOMPILE

:DOCOMPILEONLY
call mvn clean package -DskipTests=true
copy .\target\eclarity-client.jar .\deploy /y
copy .\target\eclarity-client.jar c:\eclarity /y

%JAVA_HOME%\bin\javac util\AlreadyRunning.java -d .\deploy
%JAVA_HOME%\bin\javac util\AlreadyRunning.java -d c:\eclarity

:ENDEXEC
SET JAVA_HOME=%_JAVA_HOME%