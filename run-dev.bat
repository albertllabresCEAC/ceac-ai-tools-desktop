@echo off
setlocal
cd /d "%~dp0"
echo [deprecated] run-dev.bat sigue funcionando, pero el punto de entrada recomendado para entorno completo local es run-full-local.bat
set "DARTMAKER_DEV_MODE=true"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\dev\Run-Full-Local.ps1" %*
if errorlevel 1 (
  echo.
  echo El arranque de desarrollo ha fallado. Revisa:
  echo   - run-dev.log
  echo   - logs\launcher-*.log
  echo   - logs\cloudflared-*.stdout.log
  echo   - logs\cloudflared-*.stderr.log
  echo   - C:\Users\alber\Documents\IdeaProjects\dartmakerTunelControl\control-plane.stdout.log
  echo   - C:\Users\alber\Documents\IdeaProjects\dartmakerTunelControl\control-plane.stderr.log
  echo.
  pause
)
endlocal
