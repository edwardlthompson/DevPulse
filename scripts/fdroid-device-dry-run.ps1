# F-Droid device dry-run (Windows). Usage: pwsh scripts/fdroid-device-dry-run.ps1
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) { throw "adb not found at $adb" }

# Do not kill-server — preserves wireless adb pairings
$devices = & $adb devices | Select-String "\tdevice"
if (-not $devices) {
    Write-Host "No authorized device. On the phone: enable USB debugging, set USB mode to File transfer, accept RSA prompt."
    & $adb devices -l
    exit 1
}

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$env:SOURCE_DATE_EPOCH = "1700000000"

Push-Location $Root
try {
    bash scripts/verify-fdroid-metadata.sh
    $apkDir = Join-Path $Root "examples\android\app\build\outputs\apk\debug"
    $apk = Get-ChildItem $apkDir -Filter *.apk -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $apk) {
        Push-Location (Join-Path $Root "examples\android")
        .\gradlew.bat assembleDebug --no-daemon
        Pop-Location
        $apk = Get-ChildItem $apkDir -Filter *.apk | Select-Object -First 1
    }
    Write-Host "APK: $($apk.FullName)"
    & $adb logcat -c 2>$null
    & $adb install -r $apk.FullName
    & $adb shell am start -n "dev.foss.goldenpath/.MainActivity"
    Start-Sleep -Seconds 5
    $log = Join-Path $env:TEMP "fdroid-dry-run-logcat.txt"
    & $adb logcat -d | Out-File -Encoding utf8 $log
    $crash = Select-String -Path $log -Pattern "FATAL EXCEPTION" -Quiet
    if ($crash) { throw "Crash detected in logcat: $log" }
    Write-Host "F-Droid device dry-run passed. Logcat: $log"
} finally {
    Pop-Location
}
