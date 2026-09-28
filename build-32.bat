@echo off

rem Modify the java path for compiling...
SET _JAVA_HOME=%JAVA_HOME%
SET JAVA_HOME=..\..\..\Java\jdk-1.8-32-202

echo %JAVA_HOME%

if "%1%" == "c" GOTO DOCOMPILE
if "%1%" == "cc" GOTO DOCOMPILEONLY

:AFTERCOMPILE

%JAVA_HOME%\bin\java -jar -Dlog.folder=C:\logsss\^
   -Dspring.config.location=c:\Infospica-Workspace\eclarity-client\deploy\^
   -Dspring.config.name=application^
   -Dloader.path=c:\Infospica-Workspace\eclarity-client\deploy\^
   -Dwork.dir=.\deploy\workdir\^
   .\deploy\eclarity-client.jar

GOTO ENDEXEC

:DOCOMPILE
echo "Compile and run..."
call mvn clean package -P 32bit -DskipTests=true
copy .\target\eclarity-client.jar .\deploy32 /y
copy .\target\eclarity-client.jar c:\eclarity32 /y
GOTO AFTERCOMPILE

:DOCOMPILEONLY
call mvn clean package -P 32bit -DskipTests=true
copy .\target\eclarity-client.jar .\deploy32 /y
copy .\target\eclarity-client.jar c:\eclarity32 /y

%JAVA_HOME%\bin\javac util\AlreadyRunning.java -d .\deploy32
%JAVA_HOME%\bin\javac util\AlreadyRunning.java -d c:\eclarity32

:ENDEXEC
SET JAVA_HOME=%_JAVA_HOME%