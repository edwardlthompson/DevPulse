# Poll GitHub Actions for required workflows on a commit.
# Usage: scripts/check-github-ci.ps1 [-Ref SHA] [-WaitSeconds 300] [-Jobs "Repo Hygiene,Feature Gate"] [-SkipWorkflows] [-DispatchIfMissing]
param(
    [string]$Ref = "",
    [int]$WaitSeconds = 0,
    [string]$Jobs = "",
    [switch]$SkipWorkflows,
    [switch]$DispatchIfMissing
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: gh CLI required (https://cli.github.com/)"
    exit 1
}

if (-not $Ref) { $Ref = "HEAD" }
$Ref = (git rev-parse $Ref).Trim()
if ($SkipWorkflows -and -not $Jobs) {
    Write-Host "ERROR: -SkipWorkflows requires -Jobs"
    exit 1
}

$Required = @("CI", "Security Scan", "CodeQL")
$WorkflowFile = @{
    "CI"            = "ci.yml"
    "Security Scan" = "security.yml"
    "CodeQL"        = "codeql.yml"
}

$RepoJson = gh repo view --json nameWithOwner 2>$null
if (-not $RepoJson) {
    Write-Host "ERROR: run from a git repo with gh auth"
    exit 1
}
$Repo = (ConvertFrom-Json $RepoJson).nameWithOwner
$ShortRef = $Ref.Substring(0, [Math]::Min(7, $Ref.Length))
Write-Host "GitHub Actions status for $Repo @ $ShortRef"

$DispatchRef = (git rev-parse --abbrev-ref HEAD 2>$null).Trim()
if (-not $DispatchRef -or $DispatchRef -eq "HEAD") { $DispatchRef = "main" }

if ($DispatchIfMissing -and -not $SkipWorkflows) {
    foreach ($wf in $Required) {
        $existing = gh run list --repo $Repo --commit $Ref --workflow $wf --limit 1 --json databaseId 2>$null | ConvertFrom-Json
        if ($existing -and $existing.Count -gt 0 -and $existing[0].databaseId) {
            continue
        }
        $file = $WorkflowFile[$wf]
        if (-not $file) {
            Write-Host "WARN cannot dispatch ${wf}: no workflow file mapping"
            continue
        }
        Write-Host "DISPATCH ${wf}: no run on $ShortRef; workflow_dispatch $file @ $DispatchRef"
        gh workflow run $file --repo $Repo --ref $DispatchRef
        if ($LASTEXITCODE -ne 0) {
            Write-Host "WARN dispatch failed for ${wf} (${file})"
        }
    }
}

$deadline = (Get-Date).AddSeconds($WaitSeconds)
while ($true) {
    $runs = gh run list --repo $Repo --commit $Ref --json workflowName,conclusion,status,url | ConvertFrom-Json
    $pending = 0
    $failed = 0

    if (-not $SkipWorkflows) {
        foreach ($wf in $Required) {
            $wfRuns = @($runs | Where-Object { $_.workflowName -eq $wf })
            if ($wfRuns.Count -eq 0) {
                Write-Host "WAIT ${wf}: no run yet"
                $pending++
                continue
            }
            $run = $wfRuns | Where-Object { $_.conclusion -eq "success" } | Select-Object -First 1
            if (-not $run) {
                $run = $wfRuns | Where-Object { $_.status -ne "completed" } | Select-Object -First 1
            }
            if (-not $run) {
                $run = $wfRuns | Select-Object -First 1
            }
            switch ($run.conclusion) {
                "success" { Write-Host "OK   ${wf}: $($run.url)" }
                { $_ -in @("failure", "cancelled", "timed_out", "action_required") } {
                    Write-Host "FAIL ${wf} ($($run.conclusion)): $($run.url)"
                    $failed++
                }
                default {
                    if ($run.status -eq "completed") {
                        Write-Host "FAIL ${wf} ($($run.conclusion)): $($run.url)"
                        $failed++
                    } else {
                        Write-Host "WAIT ${wf} ($($run.status)): $($run.url)"
                        $pending++
                    }
                }
            }
        }
    }

    if ($Jobs) {
        $ciRun = $runs | Where-Object { $_.workflowName -eq "CI" } | Select-Object -First 1
        if (-not $ciRun) {
            Write-Host "WAIT CI jobs: no CI run yet"
            $pending++
        } else {
            $runId = (gh run list --repo $Repo --commit $Ref --workflow CI --json databaseId | ConvertFrom-Json)[0].databaseId
            $jobData = gh run view $runId --repo $Repo --json jobs | ConvertFrom-Json
            foreach ($jobName in ($Jobs -split "," | ForEach-Object { $_.Trim() } | Where-Object { $_ })) {
                $job = $jobData.jobs | Where-Object { $_.name -eq $jobName } | Select-Object -First 1
                if (-not $job) {
                    Write-Host "WAIT CI job: $jobName (not found)"
                    $pending++
                } elseif ($job.conclusion -eq "success") {
                    Write-Host "OK   CI job: $jobName"
                } elseif (-not $job.conclusion -or $job.status -ne "completed") {
                    Write-Host "WAIT CI job: $jobName ($($job.status))"
                    $pending++
                } else {
                    Write-Host "FAIL CI job ${jobName} ($($job.conclusion))"
                    $failed++
                }
            }
        }
    }

    if ($failed -gt 0) {
        Write-Host "$failed required workflow(s) failed on GitHub"
        exit 1
    }
    if ($pending -eq 0) {
        Write-Host "All required GitHub checks passed"
        exit 0
    }
    if ($WaitSeconds -eq 0 -or (Get-Date) -ge $deadline) {
        Write-Host "INCOMPLETE: $pending workflow(s)/job(s) still pending (use -WaitSeconds 300)"
        exit 1
    }
    Start-Sleep -Seconds 15
}
