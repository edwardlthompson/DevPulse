# Local AOSP emulator + instrumented tests. Wraps the bash script.
param(
    [switch]$KeepEmulator,
    [switch]$IfDevice,
    [switch]$Help
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

$Bash = "bash"
if (-not (Get-Command $Bash -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Git\bin\bash.exe") {
        $Bash = "C:\Program Files\Git\bin\bash.exe"
    } else {
        Write-Host "ERROR: bash or Git for Windows required"
        exit 1
    }
}

$extra = @()
if ($Help) { $extra += "--help" }
if ($KeepEmulator) { $extra += "--keep-emulator" }
if ($IfDevice) { $extra += "--if-device" }

& $Bash scripts/run-android-emulator-local.sh @extra
exit $LASTEXITCODE
