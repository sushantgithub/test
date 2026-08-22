# laptop-speedup.ps1 - diagnose slowness and apply safe cleanups on Windows.
#
# This does not overclock hardware, disable security tools, or kill random
# processes. It reports resource hogs and, with confirmation, frees disk
# space from user temp/cache folders.
[CmdletBinding()]
param(
    [switch]$Clean,
    [switch]$Yes,
    [switch]$DryRun,
    [switch]$Help
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Continue"

function Show-Usage {
    @"
Usage: .\laptop-speedup.ps1 [options]

Options:
  -Clean      After diagnosing, offer to clear user temp/cache files
  -Yes        Skip confirmation prompts (use with -Clean)
  -DryRun     Show what -Clean would remove without deleting anything
  -Help       Show this help

Examples:
  powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1
  powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean -DryRun
  powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean
"@
}

if ($Help) {
    Show-Usage
    exit 0
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "----------------------------------------"
    Write-Host $Title
    Write-Host "----------------------------------------"
}

function Format-Bytes {
    param([long]$Bytes)
    if ($Bytes -lt 1KB) { return "$Bytes B" }
    if ($Bytes -lt 1MB) { return ('{0:N1} KB' -f ($Bytes / 1KB)) }
    if ($Bytes -lt 1GB) { return ('{0:N1} MB' -f ($Bytes / 1MB)) }
    return ('{0:N1} GB' -f ($Bytes / 1GB))
}

function Get-FolderSizeBytes {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) { return [long]0 }
    try {
        $sum = (Get-ChildItem -LiteralPath $Path -Force -Recurse -ErrorAction SilentlyContinue |
            Measure-Object -Property Length -Sum).Sum
        if ($null -eq $sum) { return [long]0 }
        return [long]$sum
    } catch {
        return [long]0
    }
}

function Get-CleanupTargets {
    $targets = @()
    if ($env:TEMP) { $targets += $env:TEMP }
    if ($env:LOCALAPPDATA) {
        $targets += Join-Path $env:LOCALAPPDATA "Temp"
        $targets += Join-Path $env:LOCALAPPDATA "Microsoft\Windows\INetCache"
    }
    if ($env:USERPROFILE) {
        $targets += Join-Path $env:USERPROFILE ".cache"
        $targets += Join-Path $env:USERPROFILE ".npm\_cacache"
    }
    $targets | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -Unique
}

Write-Section ('Laptop check - {0}' -f (Get-Date))
Write-Host ('User:     {0}' -f $env:USERNAME)
Write-Host ('Host:     {0}' -f $env:COMPUTERNAME)
Write-Host ('OS:       {0}' -f [System.Environment]::OSVersion.VersionString)
try {
    $uptime = (Get-Date) - (Get-CimInstance Win32_OperatingSystem).LastBootUpTime
    Write-Host ('Uptime:   {0} days {1} hours' -f [int]$uptime.TotalDays, $uptime.Hours)
} catch {
    Write-Host "Uptime:   (could not read)"
}

Write-Section "CPU load"
try {
    $cpu = Get-CimInstance Win32_Processor | Select-Object -First 1
    Write-Host ('Name:           {0}' -f $cpu.Name.Trim())
    Write-Host ('Cores/threads:  {0} / {1}' -f $cpu.NumberOfCores, $cpu.NumberOfLogicalProcessors)
    Write-Host ('Current load:   {0} percent' -f $cpu.LoadPercentage)
    if ($cpu.LoadPercentage -ge 85) {
        Write-Host "Hint: CPU is very busy; check top processes below."
    } elseif ($cpu.LoadPercentage -ge 50) {
        Write-Host "Hint: CPU is fairly busy."
    } else {
        Write-Host "Hint: CPU load looks OK."
    }
} catch {
    Write-Host "Could not read CPU stats."
}

Write-Section "Memory"
try {
    $os = Get-CimInstance Win32_OperatingSystem
    $total = [double]$os.TotalVisibleMemorySize * 1KB
    $free = [double]$os.FreePhysicalMemory * 1KB
    $used = $total - $free
    $pct = if ($total -gt 0) { ($used / $total) * 100 } else { 0 }
    Write-Host ('Total:     {0}' -f (Format-Bytes ([long]$total)))
    Write-Host ('Available: {0}' -f (Format-Bytes ([long]$free)))
    Write-Host ('In use:    {0} ({1:N0} percent)' -f (Format-Bytes ([long]$used)), $pct)
    if (($free / $total) -lt 0.10) {
        Write-Host "Hint: little free RAM; close unused apps (especially browsers)."
    } elseif (($free / $total) -lt 0.20) {
        Write-Host "Hint: RAM is getting tight."
    } else {
        Write-Host "Hint: memory pressure looks OK."
    }
} catch {
    Write-Host "Could not read memory stats."
}

Write-Section "Disk space"
try {
    Get-CimInstance Win32_LogicalDisk -Filter "DriveType=3" | ForEach-Object {
        $usedPct = if ($_.Size -gt 0) { [int]((($_.Size - $_.FreeSpace) / $_.Size) * 100) } else { 0 }
        Write-Host ('{0}  {1} free of {2} ({3} percent used)' -f $_.DeviceID, (Format-Bytes $_.FreeSpace), (Format-Bytes $_.Size), $usedPct)
        if ($usedPct -ge 90) {
            Write-Host ('Hint: {0} is almost full; low disk space slows Windows.' -f $_.DeviceID)
        }
    }
} catch {
    Write-Host "Could not read disk stats."
}

Write-Section "Top CPU processes"
Get-Process | Sort-Object CPU -Descending | Select-Object -First 10 |
    Format-Table Id, @{N="CPU(s)";E={'{0:N1}' -f $_.CPU}}, @{N="MemMB";E={'{0:N0}' -f ($_.WorkingSet64/1MB)}}, ProcessName -AutoSize

Write-Section "Top memory processes"
Get-Process | Sort-Object WorkingSet64 -Descending | Select-Object -First 10 |
    Format-Table Id, @{N="CPU(s)";E={'{0:N1}' -f $_.CPU}}, @{N="MemMB";E={'{0:N0}' -f ($_.WorkingSet64/1MB)}}, ProcessName -AutoSize

Write-Section "What usually helps"
@"
- Close unused browser tabs and apps (Chrome, Edge, Teams, Slack).
- Reboot if the PC has been on for many days and RAM looks exhausted.
- Keep at least 10-15 percent of C: free.
- Plug in the charger; Windows often throttles CPU on battery.
- Update Windows and GPU drivers; dust/thermal issues help more than scripts.
"@ | Write-Host

if ($Clean) {
    Write-Section "Safe cleanup (user temp and caches)"
    if ($DryRun) { Write-Host "Dry run: nothing will be deleted." }

    $total = [long]0
    $targets = @(Get-CleanupTargets)
    foreach ($path in $targets) {
        $size = Get-FolderSizeBytes $path
        $total += $size
        Write-Host ('  {0}  ({1})' -f $path, (Format-Bytes $size))
    }
    Write-Host ""
    Write-Host ('Estimated reclaimable (user-owned): {0}' -f (Format-Bytes $total))

    if ($DryRun) { exit 0 }

    $ok = $Yes
    if (-not $ok) {
        $reply = Read-Host "Delete files inside the listed folders? [y/N]"
        $ok = $reply -match '^[Yy]$'
    }
    if (-not $ok) {
        Write-Host "Skipped cleanup."
        exit 0
    }

    foreach ($path in $targets) {
        Get-ChildItem -LiteralPath $path -Force -ErrorAction SilentlyContinue |
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host ('Cleared: {0}' -f $path)
    }
    Write-Host "Cleanup finished. A reboot can still help if RAM was exhausted."
} else {
    Write-Section "Next step"
    Write-Host "Re-run with -Clean to clear user temp/cache files after review:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean"
    Write-Host "Preview deletions with:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean -DryRun"
}
