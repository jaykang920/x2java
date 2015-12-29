@echo off
@setlocal

set X2JAVA_HOME=%~dp0\..

set CP=%CLASSPATH%;%X2JAVA_HOME%\lib\xpiler-0.1.0.jar

java -cp "%CP%" xpiler.Main %*

@endlocal