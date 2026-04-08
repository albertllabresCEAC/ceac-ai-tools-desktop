Write-Warning "scripts\\Run-Dev.ps1 es un alias legado. Usa scripts\\dev\\Run-Full-Local.ps1 o run-full-local.bat."
& (Join-Path $PSScriptRoot "dev\\Run-Full-Local.ps1") @args
exit $LASTEXITCODE
