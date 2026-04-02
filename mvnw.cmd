@REM Maven Wrapper startup for Windows
@echo off
setlocal

if not "%JAVA_HOME%" == "" goto OkJHome
echo Error: JAVA_HOME not found. >&2
exit /b 1

:OkJHome
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Error: JAVA_HOME is invalid. >&2
  exit /b 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" (
  echo Error: Maven wrapper JAR not found at %WRAPPER_JAR% >&2
  exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
