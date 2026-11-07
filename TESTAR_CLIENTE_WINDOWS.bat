@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\build.ps1" -Task runClient
set "BUILD_RESULT=%ERRORLEVEL%"
echo.
echo Aperte uma tecla para fechar esta janela.
pause >nul
exit /b %BUILD_RESULT%
