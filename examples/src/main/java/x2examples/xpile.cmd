@echo off
@setlocal

set X2JAVA_HOME=%~dp0\..\..\..\..\..

java -cp "%CLASSPATH%;%X2JAVA_HOME%\lib\xpiler-0.1.0.jar" xpiler.Main . -f

@endlocal