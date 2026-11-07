@echo off
setlocal
cd /d "%~dp0"
echo O JAR pronto para jogar esta na pasta INSTALAR.
echo Este comando recompila o codigo-fonte usando Java 21 e Gradle.
if exist "%~dp0ATUALIZAR_RECURSOS.zip" (
 echo Aplicando arquivos novos da atualizacao...
 powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%~dp0ATUALIZAR_RECURSOS.zip' -DestinationPath '%~dp0.' -Force" && del "%~dp0ATUALIZAR_RECURSOS.zip"
)
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\build.ps1" -Task build
set "BUILD_RESULT=%ERRORLEVEL%"
echo.
if not "%BUILD_RESULT%"=="0" echo Nao foi possivel compilar. Envie COMPILACAO.log para revisao.
echo Aperte uma tecla para fechar esta janela.
pause >nul
exit /b %BUILD_RESULT%
