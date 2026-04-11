param(
    [string]$ControlPlaneRoot = $(if ($env:DEV_CONTROL_PLANE_ROOT) { $env:DEV_CONTROL_PLANE_ROOT } else { (Join-Path (Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))) "dartmakerTunelControl") }),
    [int]$ControlPlanePort = $(if ($env:DEV_CONTROL_PLANE_PORT) { [int]$env:DEV_CONTROL_PLANE_PORT } else { 8090 }),
    [int]$ControlPlaneKeycloakPort = $(if ($env:DEV_CONTROL_PLANE_KEYCLOAK_PORT) { [int]$env:DEV_CONTROL_PLANE_KEYCLOAK_PORT } else { 8181 }),
    [string]$DesktopUsername = $(if ($env:CONTROL_PLANE_LOGIN_USERNAME) { $env:CONTROL_PLANE_LOGIN_USERNAME } else { "desktop-user" }),
    [string]$DesktopPassword = $(if ($env:CONTROL_PLANE_LOGIN_PASSWORD) { $env:CONTROL_PLANE_LOGIN_PASSWORD } else { "change-me" }),
    [string]$MachineId = $(if ($env:CONTROL_PLANE_MACHINE_ID) { $env:CONTROL_PLANE_MACHINE_ID } else { $env:COMPUTERNAME }),
    [string]$ClientVersion = $(if ($env:CONTROL_PLANE_CLIENT_VERSION) { $env:CONTROL_PLANE_CLIENT_VERSION } else { "1.0.0-dev" })
)

$ErrorActionPreference = "Stop"
$clientRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$runDevLogPath = Join-Path $clientRoot "run-dev.log"
Start-Transcript -Path $runDevLogPath -Force | Out-Null

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Ensure-Command {
    param([string]$Name, [string]$InstallHint)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "No encuentro '$Name'. $InstallHint"
    }
}

function Wait-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-RestMethod -Method Get -Uri $Url -TimeoutSec 5 | Out-Null
            return
        }
        catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "No he podido obtener respuesta de $Url en $TimeoutSeconds segundos."
}

function Stop-JavaProcessesMatching {
    param([string[]]$Patterns)

    $javaProcesses = Get-CimInstance Win32_Process -Filter "name = 'java.exe'" -ErrorAction SilentlyContinue
    foreach ($process in $javaProcesses) {
        if (-not $process.CommandLine) {
            continue
        }

        foreach ($pattern in $Patterns) {
            if ($process.CommandLine -like "*$pattern*") {
                try {
                    Stop-Process -Id $process.ProcessId -Force -ErrorAction Stop
                }
                catch {
                }
                break
            }
        }
    }
}

function Start-ControlPlaneDependencies {
    param(
        [string]$ProjectRoot,
        [int]$KeycloakPort
    )

    Write-Step "Levantando PostgreSQL y Keycloak del control plane"
    Push-Location $ProjectRoot
    try {
        $env:KEYCLOAK_LOCAL_PORT = "$KeycloakPort"
        docker compose up -d | Out-Host
    }
    finally {
        Remove-Item Env:KEYCLOAK_LOCAL_PORT -ErrorAction SilentlyContinue
        Pop-Location
    }
}

function Start-ControlPlaneApplication {
    param(
        [string]$ProjectRoot,
        [int]$ServerPort,
        [int]$KeycloakPort
    )

    $stdoutPath = Join-Path $ProjectRoot "control-plane.stdout.log"
    $stderrPath = Join-Path $ProjectRoot "control-plane.stderr.log"
    foreach ($path in @($stdoutPath, $stderrPath)) {
        if (Test-Path $path) {
            Remove-Item $path -Force
        }
    }

    Stop-JavaProcessesMatching -Patterns @(
        "com.dartmaker.tunnelcontrolplane.DartmakerTunnelControlPlaneApplication",
        "dartmaker-tunnel-control-plane",
        $ProjectRoot
    )

    $command = @(
        "set ""SPRING_PROFILES_ACTIVE=local""",
        "set ""SERVER_PORT=$ServerPort""",
        "set ""APP_SECURITY_ISSUER_URI=http://localhost:$KeycloakPort/realms/ceac-ia-tools""",
        "set ""APP_SECURITY_JWK_SET_URI=http://localhost:$KeycloakPort/realms/ceac-ia-tools/protocol/openid-connect/certs""",
        ".\mvnw.cmd -q spring-boot:run"
    ) -join " && "

    Write-Step "Arrancando control plane en http://localhost:$ServerPort"
    $process = Start-Process -FilePath "cmd.exe" `
        -WorkingDirectory $ProjectRoot `
        -ArgumentList "/c", $command `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -PassThru

    Start-Sleep -Seconds 5
    if ($process.HasExited) {
        throw "El control plane no ha arrancado correctamente. Revisa $stdoutPath y $stderrPath."
    }
}

try {
    Ensure-Command -Name "docker" -InstallHint "Instala y arranca Docker Desktop."
    Ensure-Command -Name "java" -InstallHint "Instala Java 21 o superior."
    $env:DARTMAKER_DEV_MODE = "true"

    if (-not (Test-Path $ControlPlaneRoot)) {
        throw "No encuentro el repo del control plane en '$ControlPlaneRoot'. Usa DEV_CONTROL_PLANE_ROOT para ajustarlo."
    }

    Start-ControlPlaneDependencies -ProjectRoot $ControlPlaneRoot -KeycloakPort $ControlPlaneKeycloakPort
    Wait-HttpReady -Url "http://localhost:$ControlPlaneKeycloakPort/realms/ceac-ia-tools/.well-known/openid-configuration"

    Start-ControlPlaneApplication `
        -ProjectRoot $ControlPlaneRoot `
        -ServerPort $ControlPlanePort `
        -KeycloakPort $ControlPlaneKeycloakPort

    Wait-HttpReady -Url "http://localhost:$ControlPlanePort/actuator/health"

    $env:CONTROL_PLANE_URL = "http://localhost:$ControlPlanePort"
    $env:CONTROL_PLANE_LOGIN_USERNAME = $DesktopUsername
    $env:CONTROL_PLANE_LOGIN_PASSWORD = $DesktopPassword
    $env:CONTROL_PLANE_MACHINE_ID = $MachineId
    $env:CONTROL_PLANE_CLIENT_VERSION = $ClientVersion

    Write-Step "Resumen"
    Write-Host "Control plane:      $env:CONTROL_PLANE_URL" -ForegroundColor Green
    Write-Host "Keycloak control:   http://localhost:$ControlPlaneKeycloakPort" -ForegroundColor Green
    Write-Host "Usuario desktop:    $DesktopUsername" -ForegroundColor Green
    Write-Host "Launcher log:       $runDevLogPath" -ForegroundColor Green

    Write-Step "Arrancando cliente"
    Push-Location $clientRoot
    try {
        & (Join-Path $clientRoot "run.bat")
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host ""
    Write-Host "ERROR EN RUN-FULL-LOCAL" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
finally {
    Stop-Transcript | Out-Null
}
