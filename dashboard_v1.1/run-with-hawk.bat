@echo off
setlocal

set HAWK_LIBS=%~dp0src\main\java\com\integrationhub\dashboard\lib
set WAR_FILE=%~dp0target\dashboard-0.0.1-SNAPSHOT.war

echo Starting Dashboard with TIBCO Hawk libraries...
echo.

java -cp "%WAR_FILE%;%HAWK_LIBS%\ami.jar;%HAWK_LIBS%\talon.jar;%HAWK_LIBS%\agent.jar;%HAWK_LIBS%\tibrv.jar" ^
  -Dspring.boot.loader.path="%HAWK_LIBS%" ^
  org.springframework.boot.loader.launch.WarLauncher

endlocal
