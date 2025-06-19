@REM Maven wrapper script for Windows
@REM
@echo off

set MAVEN_PROJECTBASEDIR=%~dp0

if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    echo Error: Maven wrapper not found
    echo Please run: mvn wrapper:wrapper
    exit /b 1
)

set MAVEN_CMD_LINE_ARGS=%*

"%JAVA_HOME%\bin\java" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
