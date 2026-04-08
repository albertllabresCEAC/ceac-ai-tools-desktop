param()

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$logDir = Join-Path $projectRoot "logs"
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$launcherLogPath = Join-Path $logDir "launcher-$timestamp.log"
Start-Transcript -Path $launcherLogPath -Force | Out-Null

function Resolve-MavenCommand {
    if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
        return "mvn"
    }

    $version = "3.9.9"
    $installDir = Join-Path $env:TEMP "apache-maven-$version"
    $mvnPath = Join-Path $installDir "bin\mvn.cmd"
    if (Test-Path $mvnPath) {
        return $mvnPath
    }

    $zipPath = Join-Path $env:TEMP "apache-maven-$version-bin.zip"
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/$version/binaries/apache-maven-$version-bin.zip" -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $env:TEMP -Force
    return $mvnPath
}

try {
    if (-not (Get-Command "java" -ErrorAction SilentlyContinue)) {
        throw "No encuentro 'java'. Instala Java 21 o superior."
    }

    $mavenCommand = Resolve-MavenCommand
    Push-Location $projectRoot
    try {
        & $mavenCommand "spring-boot:run"
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host ""
    Write-Host "ERROR EN EL ARRANQUE DEL CLIENTE" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "Log: $launcherLogPath" -ForegroundColor Yellow
    exit 1
}
finally {
    Stop-Transcript | Out-Null
}
