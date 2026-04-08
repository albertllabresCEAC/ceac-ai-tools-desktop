@echo off
setlocal
cd /d "%~dp0"
if not defined DARTMAKER_DEV_MODE set "DARTMAKER_DEV_MODE=false"
if not defined CONTROL_PLANE_URL set "CONTROL_PLANE_URL=https://control.dartmaker.com"
if not defined CONTROL_PLANE_CLIENT_VERSION set "CONTROL_PLANE_CLIENT_VERSION=1.0.0"
if not defined CONTROL_PLANE_MACHINE_ID set "CONTROL_PLANE_MACHINE_ID=%COMPUTERNAME%"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\Start-Launcher.ps1" %*
if errorlevel 1 (
  echo.
  echo El arranque del cliente ha fallado. Revisa:
  echo   - logs\launcher-*.log
  echo.
  pause
)
endlocal
