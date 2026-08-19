# Fail when UI code drifts from the design token system.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

$Errors = 0

function Fail([string]$Message) {
    Write-Host "DESIGN: $Message"
    $script:Errors++
}

if (-not (Test-Path "design-tokens/design-tokens.json")) {
    Fail "missing design-tokens/design-tokens.json"
}

$brandFiles = @(
    "branding/BRANDING.md",
    "branding/product.json",
    "branding/product.schema.json",
    "branding/voice.md",
    "branding/assets/logo-mark.svg",
    "branding/official-colors.css",
    "branding/generated/README.preview.md",
    "branding/templates/README.product.md"
)
foreach ($brandFile in $brandFiles) {
    if (-not (Test-Path $brandFile)) {
        Fail "missing $brandFile (run scripts/sync-design-tokens.py and scripts/generate-project-readme.py)"
    }
}

$hexPattern = '#[0-9A-Fa-f]{6}\b'
$contentPattern = 'content\s*:\s*[''"][^''"]{2,}'

Get-ChildItem -Path "examples/web/src" -Recurse -Include *.css,*.ts -File |
    Where-Object { $_.Name -ne "design-tokens.css" } |
    ForEach-Object {
        if (Select-String -Path $_.FullName -Pattern $hexPattern -Quiet) {
            Fail "hardcoded hex in $($_.FullName)"
        }
    }

Get-ChildItem -Path "examples/web/src" -Recurse -Filter *.css -File |
    Where-Object { $_.Name -ne "design-tokens.css" } |
    ForEach-Object {
        if (Select-String -Path $_.FullName -Pattern $contentPattern -Quiet) {
            Fail "user-facing content property in $($_.FullName) (use locales/*.json)"
        }
    }

$mainTs = "examples/web/src/main.ts"
if (Test-Path $mainTs) {
    if (Select-String -Path $mainTs -Pattern '<(h1|p|button|span)[^>]*>[^<$]{3,}' -Quiet) {
        Fail "main.ts contains hardcoded HTML copy"
    }
    $py = @'
import re
import sys

path = sys.argv[1]
text = open(path, encoding="utf-8").read()
match = re.search(r"innerHTML\s*=\s*`([^`]*)`", text, re.DOTALL)
if not match:
    sys.exit(0)

template = match.group(1)
if re.search(r">[A-Za-z][^<${}]{3,}<", template):
    sys.exit(1)

for interp in re.findall(r"\$\{([^}]+)\}", template):
    expr = interp.strip()
    if expr.startswith("t("):
        continue
    if re.fullmatch(r"[a-zA-Z_][a-zA-Z0-9_]*", expr):
        continue
    sys.exit(1)
'@
    python3 -c $py $mainTs
    if ($LASTEXITCODE -ne 0) {
        Fail "main.ts innerHTML should use t() or i18n variable keys for visible copy"
    }
}

if (Test-Path "examples/android/app/src/main/java") {
    Get-ChildItem -Path "examples/android/app/src/main/java" -Recurse -Filter *.kt -File |
        Where-Object { $_.FullName -match '\\ui\\' -and $_.Name -ne 'Color.kt' } |
        ForEach-Object {
            if (Select-String -Path $_.FullName -Pattern 'Color\(0x|#[0-9A-Fa-f]{6}\b' -Quiet) {
                Fail "hardcoded color in $($_.FullName)"
            }
            if (Select-String -Path $_.FullName -Pattern 'Text\("[^"]+"\)' -Quiet) {
                Fail "string literal in composable: $($_.FullName)"
            }
        }
}

$required = @("branding/official-colors.css")
if (Test-Path "examples/web") {
    $required += @(
        "examples/web/src/design-tokens.css",
        "examples/web/src/theme-meta.json",
        "examples/web/public/icon.svg",
        "examples/web/public/favicon.svg",
        "examples/web/public/logo.svg"
    )
}
if (Test-Path "examples/android") {
    $required += @(
        "examples/android/app/src/main/java/dev/foss/goldenpath/ui/theme/Color.kt",
        "examples/android/app/src/main/res/drawable/ic_brand_mark.xml"
    )
}
foreach ($path in $required) {
    if (-not (Test-Path $path)) {
        Fail "missing generated output $path (run scripts/sync-design-tokens.py)"
    }
}

if ($Errors -gt 0) {
    Write-Host "$Errors design cohesion check(s) failed"
    exit 1
}

Write-Host "Design cohesion check passed"
